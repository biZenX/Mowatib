package com.etrisad.zenith.data.local

import android.content.Context
import android.content.SharedPreferences
import com.etrisad.zenith.data.manager.MowatibSyncManager
import com.etrisad.zenith.data.manager.MowatibTelegramManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class MowatibTask(
    val id: String = "task_${System.currentTimeMillis()}_${(1000..9999).random()}",
    val listId: String? = null,
    val title: String,
    val description: String = "",
    val priority: String = "none",
    val status: String = "todo",
    val isCompleted: Boolean = false,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val category: String = "عام",
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: String = currentIsoTime(),
    val deletedAt: String? = null
)

private fun currentIsoTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(Date())
}

object MowatibTaskManager {
    private const val PREFS_NAME = "mowatib_tasks_prefs"
    private const val KEY_TASKS = "tasks_json"

    private var prefs: SharedPreferences? = null
    private var appContext: Context? = null
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // Internal list including soft-deleted tasks for accurate sync tombstones
    private val _internalTasks = mutableListOf<MowatibTask>()

    // Public StateFlow showing only active non-deleted tasks to UI
    private val _tasks = MutableStateFlow<List<MowatibTask>>(emptyList())
    val tasks: StateFlow<List<MowatibTask>> = _tasks.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            appContext = context.applicationContext
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadTasks()
        }
    }

    private fun loadTasks() {
        val jsonStr = prefs?.getString(KEY_TASKS, null)
        if (jsonStr.isNullOrEmpty()) {
            val initial = listOf(
                MowatibTask(
                    id = "1",
                    title = "جلسة تركيز بومودورو أولى",
                    category = "تركيز",
                    isCompleted = false,
                    status = "todo"
                ),
                MowatibTask(
                    id = "2",
                    title = "مراجعة العادات اليومية والإنتاجية",
                    category = "إنتاجية",
                    isCompleted = false,
                    status = "todo"
                )
            )
            _internalTasks.clear()
            _internalTasks.addAll(initial)
            _tasks.value = initial
            saveTasksInternal(_internalTasks)
            return
        }

        val list = mutableListOf<MowatibTask>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val statusStr = obj.optString("status", "todo")
                val completedBool = obj.optBoolean("isCompleted", false) || obj.optBoolean("completed", false) || statusStr == "done"
                list.add(
                    MowatibTask(
                        id = obj.optString("id"),
                        listId = if (obj.has("listId") && !obj.isNull("listId")) obj.optString("listId") else null,
                        title = obj.optString("title"),
                        description = obj.optString("description", ""),
                        priority = obj.optString("priority", "none"),
                        status = if (completedBool) "done" else statusStr,
                        isCompleted = completedBool,
                        dueDate = if (obj.has("dueDate") && !obj.isNull("dueDate")) obj.optString("dueDate") else null,
                        dueTime = if (obj.has("dueTime") && !obj.isNull("dueTime")) obj.optString("dueTime") else null,
                        category = obj.optString("category", "عام"),
                        position = obj.optInt("position", 0),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        updatedAt = obj.optString("updatedAt", currentIsoTime()),
                        deletedAt = if (obj.has("deletedAt") && !obj.isNull("deletedAt")) obj.optString("deletedAt") else null
                    )
                )
            }
        } catch (_: Exception) {}

        _internalTasks.clear()
        _internalTasks.addAll(list)
        _tasks.value = _internalTasks.filter { it.deletedAt == null }
    }

    fun addTask(title: String, category: String = "عام", dueDate: String? = null, dueTime: String? = null): MowatibTask? {
        if (title.isBlank()) return null
        val now = currentIsoTime()
        val newTask = MowatibTask(
            title = title.trim(),
            category = category,
            dueDate = dueDate,
            dueTime = dueTime,
            updatedAt = now
        )
        _internalTasks.add(newTask)
        _tasks.value = _internalTasks.filter { it.deletedAt == null }
        saveTasksInternal(_internalTasks)

        // Trigger Telegram alert & Cloud Sync
        appContext?.let { ctx ->
            MowatibSyncManager.scheduleSync(ctx)
            scope.launch {
                try {
                    MowatibTelegramManager(ctx).sendTaskCreated(newTask.title, newTask.category)
                } catch (_: Exception) {}
            }
        }
        return newTask
    }

    fun toggleTask(id: String) {
        var completedTask: MowatibTask? = null
        val now = currentIsoTime()
        val updated = _internalTasks.map { task ->
            if (task.id == id) {
                val nextCompleted = !task.isCompleted
                val modified = task.copy(
                    isCompleted = nextCompleted,
                    status = if (nextCompleted) "done" else "todo",
                    updatedAt = now
                )
                if (nextCompleted) completedTask = modified
                modified
            } else {
                task
            }
        }
        _internalTasks.clear()
        _internalTasks.addAll(updated)
        _tasks.value = _internalTasks.filter { it.deletedAt == null }
        saveTasksInternal(_internalTasks)

        appContext?.let { ctx ->
            MowatibSyncManager.scheduleSync(ctx)
            completedTask?.let { doneTask ->
                scope.launch {
                    try {
                        MowatibTelegramManager(ctx).sendTaskCompleted(doneTask.title, doneTask.category)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    fun deleteTask(id: String) {
        val now = currentIsoTime()
        val updated = _internalTasks.map { task ->
            if (task.id == id) {
                task.copy(deletedAt = now, updatedAt = now)
            } else {
                task
            }
        }
        _internalTasks.clear()
        _internalTasks.addAll(updated)
        _tasks.value = _internalTasks.filter { it.deletedAt == null }
        saveTasksInternal(_internalTasks)

        appContext?.let { ctx ->
            MowatibSyncManager.scheduleSync(ctx)
        }
    }

    fun getAllTasksForSync(): List<MowatibTask> {
        return _internalTasks.toList()
    }

    fun mergeCloudTasks(cloudTasks: List<MowatibTask>) {
        if (cloudTasks.isEmpty()) return
        val taskMap = mutableMapOf<String, MowatibTask>()

        // 1. Put cloud tasks in map
        for (ct in cloudTasks) {
            taskMap[ct.id] = ct
        }

        // 2. Merge local tasks with LWW (Last-Write-Wins)
        for (local in _internalTasks) {
            val cloud = taskMap[local.id]
            if (cloud == null) {
                taskMap[local.id] = local
            } else {
                val localTime = parseIsoToMillis(local.updatedAt)
                val cloudTime = parseIsoToMillis(cloud.updatedAt)

                val localDeleted = local.deletedAt != null
                val cloudDeleted = cloud.deletedAt != null

                if (localDeleted && !cloudDeleted) {
                    val delTime = parseIsoToMillis(local.deletedAt)
                    if (cloudTime > delTime) {
                        taskMap[local.id] = cloud
                    } else {
                        taskMap[local.id] = local
                    }
                } else if (cloudDeleted && !localDeleted) {
                    val delTime = parseIsoToMillis(cloud.deletedAt)
                    if (localTime > delTime) {
                        taskMap[local.id] = local
                    } else {
                        taskMap[local.id] = cloud
                    }
                } else if (localTime >= cloudTime) {
                    taskMap[local.id] = local
                } else {
                    taskMap[local.id] = cloud
                }
            }
        }

        _internalTasks.clear()
        _internalTasks.addAll(taskMap.values)
        _tasks.value = _internalTasks.filter { it.deletedAt == null }
        saveTasksInternal(_internalTasks)
    }

    private fun parseIsoToMillis(iso: String?): Long {
        if (iso.isNullOrBlank()) return 0L
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(iso.take(19))?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    private fun saveTasksInternal(list: List<MowatibTask>) {
        val arr = JSONArray()
        list.forEach { task ->
            val obj = JSONObject().apply {
                put("id", task.id)
                if (task.listId != null) put("listId", task.listId)
                put("title", task.title)
                put("description", task.description)
                put("priority", task.priority)
                put("status", task.status)
                put("isCompleted", task.isCompleted)
                if (task.dueDate != null) put("dueDate", task.dueDate)
                if (task.dueTime != null) put("dueTime", task.dueTime)
                put("category", task.category)
                put("position", task.position)
                put("createdAt", task.createdAt)
                put("updatedAt", task.updatedAt)
                if (task.deletedAt != null) put("deletedAt", task.deletedAt)
            }
            arr.put(obj)
        }
        prefs?.edit()?.putString(KEY_TASKS, arr.toString())?.apply()
    }
}
