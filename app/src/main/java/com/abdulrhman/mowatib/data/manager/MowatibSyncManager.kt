package com.abdulrhman.mowatib.data.manager

import android.content.Context
import com.abdulrhman.mowatib.data.local.MowatibHabitsManager
import com.abdulrhman.mowatib.data.local.MowatibTask
import com.abdulrhman.mowatib.data.local.MowatibTaskManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class SyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE,
    ERROR
}

object MowatibSyncManager {
    private const val DEFAULT_SYNC_URL = "https://mowatib.com/api/sync"
    private const val PREFS_NAME = "mowatib_sync_prefs"
    private const val KEY_LAST_SYNCED = "last_synced_at"
    private const val KEY_MOWATIB_ID = "mowatib_user_id"
    private const val KEY_DEVICE_SECRET = "device_secret_token"
    private const val KEY_SERVER_URL = "mowatib_server_url"
    private const val KEY_AUTO_SYNC = "mowatib_auto_sync"

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncedAt = MutableStateFlow<String?>(null)
    val lastSyncedAt: StateFlow<String?> = _lastSyncedAt.asStateFlow()

    private var debounceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun init(context: Context) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _lastSyncedAt.value = prefs.getString(KEY_LAST_SYNCED, null)
        MowatibTaskManager.init(context)
        MowatibHabitsManager.init(context)
    }

    fun getMowatibId(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var id = prefs.getString(KEY_MOWATIB_ID, "") ?: ""
        if (id.isBlank()) {
            val randomSuffix = UUID.randomUUID().toString().replace("-", "").take(6).uppercase(Locale.US)
            id = "MW-$randomSuffix"
            prefs.edit().putString(KEY_MOWATIB_ID, id).apply()
        }
        return id
    }

    fun setMowatibId(context: Context, newId: String) {
        val clean = newId.trim().uppercase(Locale.US)
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_MOWATIB_ID, clean).apply()
    }

    fun getDeviceSecret(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var secret = prefs.getString(KEY_DEVICE_SECRET, "") ?: ""
        if (secret.isBlank()) {
            secret = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_DEVICE_SECRET, secret).apply()
        }
        return secret
    }

    fun getServerUrl(context: Context): String {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SERVER_URL, DEFAULT_SYNC_URL) ?: DEFAULT_SYNC_URL
    }

    fun setServerUrl(context: Context, url: String) {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_SERVER_URL, url.trim()).apply()
    }

    fun isAutoSyncEnabled(context: Context): Boolean {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_SYNC, true)
    }

    fun setAutoSyncEnabled(context: Context, enabled: Boolean) {
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_AUTO_SYNC, enabled).apply()
    }

    fun scheduleSync(context: Context, delayMs: Long = 1500L) {
        if (!isAutoSyncEnabled(context)) return
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(delayMs)
            sync(context.applicationContext)
        }
    }

    suspend fun sync(context: Context): Result<Boolean> = withContext(Dispatchers.IO) {
        _syncStatus.value = SyncStatus.SYNCING
        val uid = getMowatibId(context)

        // 1. Pull latest cloud snapshot and merge
        try {
            pullAndMerge(context, uid)
        } catch (_: Exception) {}

        // 2. Push unified snapshot to backend / MCP server
        val pushResult = pushSnapshot(context, uid)
        if (pushResult.isSuccess) {
            _syncStatus.value = SyncStatus.SYNCED
            val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            _lastSyncedAt.value = nowTime
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_LAST_SYNCED, nowTime).apply()
        } else {
            _syncStatus.value = SyncStatus.OFFLINE
        }
        pushResult
    }

    private suspend fun pullAndMerge(context: Context, uid: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val deviceSecret = getDeviceSecret(context)
            val serverUrl = getServerUrl(context)
            val url = "$serverUrl?userId=${uid}&_t=${System.currentTimeMillis()}"
            val request = Request.Builder()
                .url(url)
                .addHeader("x-mowatib-device-uid", uid)
                .addHeader("x-mowatib-device-secret", deviceSecret)
                .addHeader("x-mowatib-sync-client", "android-mowatib")
                .get()
                .build()

            val (bodyStr, isOk, code) = httpClient.newCall(request).execute().use { response ->
                Triple(response.body?.string().orEmpty(), response.isSuccessful, response.code)
            }
            if (!isOk) {
                return@withContext Result.failure(Exception("HTTP $code"))
            }

            val json = JSONObject(bodyStr)
            val dataObj = json.optJSONObject("data") ?: return@withContext Result.success(true)
            val payload = dataObj.optJSONObject("payload") ?: return@withContext Result.success(true)

            // Merge tasks
            val cloudTasksArr = payload.optJSONArray("tasks")
            if (cloudTasksArr != null) {
                val cloudTasks = mutableListOf<MowatibTask>()
                for (i in 0 until cloudTasksArr.length()) {
                    val obj = cloudTasksArr.getJSONObject(i)
                    cloudTasks.add(parseTaskJson(obj))
                }
                MowatibTaskManager.mergeCloudTasks(cloudTasks)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun pushSnapshot(context: Context, uid: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val deviceSecret = getDeviceSecret(context)
            val serverUrl = getServerUrl(context)
            val allTasks = MowatibTaskManager.getAllTasksForSync()
            val tasksArr = JSONArray()
            for (task in allTasks) {
                tasksArr.put(taskToJson(task))
            }

            val habitsArr = JSONArray()
            val allHabits = MowatibHabitsManager.habits.value
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            for (habit in allHabits) {
                val isDoneToday = habit.lastCompletedDate == todayStr
                val hObj = JSONObject().apply {
                    put("id", habit.id)
                    put("title", habit.title)
                    put("streak", habit.streak)
                    put("color_hex", habit.colorHex)
                    put("completed", isDoneToday)
                    put("is_completed", isDoneToday)
                    put("last_completed_date", habit.lastCompletedDate)
                    put("updated_at", nowIso())
                }
                habitsArr.put(hObj)
            }

            val snapshotPayload = JSONObject().apply {
                put("version", 2)
                put("timestamp", nowIso())
                put("tasks", tasksArr)
                put("habits", habitsArr)
            }

            val envelope = JSONObject().apply {
                put("userId", uid)
                put("payload", snapshotPayload)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = envelope.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(serverUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-mowatib-device-uid", uid)
                .addHeader("x-mowatib-device-secret", deviceSecret)
                .addHeader("x-mowatib-sync-client", "android-mowatib")
                .post(requestBody)
                .build()

            val (isOk, code) = httpClient.newCall(request).execute().use { response ->
                Pair(response.isSuccessful, response.code)
            }
            if (isOk) {
                Result.success(true)
            } else {
                Result.failure(Exception("HTTP $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseTaskJson(obj: JSONObject): MowatibTask {
        val statusStr = obj.optString("status", "todo")
        val completedBool = obj.optBoolean("completed", false) || obj.optBoolean("isCompleted", false) || statusStr == "done"
        return MowatibTask(
            id = obj.optString("id", "task_${System.currentTimeMillis()}"),
            listId = if (obj.has("list_id") && !obj.isNull("list_id")) obj.optString("list_id") else null,
            title = obj.optString("title", "مهمة"),
            description = obj.optString("description", ""),
            priority = obj.optString("priority", "none"),
            status = if (completedBool) "done" else statusStr,
            isCompleted = completedBool,
            dueDate = if (obj.has("due_date") && !obj.isNull("due_date")) obj.optString("due_date") else null,
            dueTime = if (obj.has("due_time") && !obj.isNull("due_time")) obj.optString("due_time") else null,
            category = obj.optString("category", "عام"),
            position = obj.optInt("position", 0),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = obj.optString("updated_at", nowIso()),
            deletedAt = if (obj.has("deleted_at") && !obj.isNull("deleted_at")) obj.optString("deleted_at") else null
        )
    }

    fun taskToJson(task: MowatibTask): JSONObject {
        return JSONObject().apply {
            put("id", task.id)
            if (task.listId != null) put("list_id", task.listId) else put("list_id", JSONObject.NULL)
            put("title", task.title)
            put("description", task.description)
            put("priority", task.priority)
            put("status", if (task.isCompleted) "done" else task.status)
            put("completed", task.isCompleted)
            put("is_completed", task.isCompleted)
            if (task.dueDate != null) put("due_date", task.dueDate) else put("due_date", JSONObject.NULL)
            if (task.dueTime != null) put("due_time", task.dueTime) else put("due_time", JSONObject.NULL)
            put("category", task.category)
            put("position", task.position)
            put("createdAt", task.createdAt)
            put("created_at", nowIso(task.createdAt))
            put("updated_at", task.updatedAt)
            if (task.deletedAt != null) put("deleted_at", task.deletedAt) else put("deleted_at", JSONObject.NULL)
        }
    }

    fun nowIso(timestampMs: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestampMs))
    }
}
