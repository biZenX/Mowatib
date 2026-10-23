package com.etrisad.zenith.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class MowatibTask(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val category: String = "عام",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

object MowatibTaskManager {
    private const val PREFS_NAME = "mowatib_tasks_prefs"
    private const val KEY_TASKS = "tasks_json"

    private var prefs: SharedPreferences? = null
    private val _tasks = MutableStateFlow<List<MowatibTask>>(emptyList())
    val tasks: StateFlow<List<MowatibTask>> = _tasks.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadTasks()
        }
    }

    private fun loadTasks() {
        val jsonStr = prefs?.getString(KEY_TASKS, null)
        if (jsonStr.isNullOrEmpty()) {
            val initial = listOf(
                MowatibTask(id = "1", title = "جلسة تركيز بومودورو أولى", category = "تركيز"),
                MowatibTask(id = "2", title = "مراجعة العادات اليومية والإنتاجية", category = "إنتاجية")
            )
            _tasks.value = initial
            saveTasks(initial)
            return
        }
        val list = mutableListOf<MowatibTask>()
        try {
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    MowatibTask(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        category = obj.optString("category", "عام"),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        _tasks.value = list
    }

    fun addTask(title: String, category: String = "عام") {
        if (title.isBlank()) return
        val updated = _tasks.value + MowatibTask(title = title.trim(), category = category)
        _tasks.value = updated
        saveTasks(updated)
    }

    fun toggleTask(id: String) {
        val updated = _tasks.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
        _tasks.value = updated
        saveTasks(updated)
    }

    fun deleteTask(id: String) {
        val updated = _tasks.value.filter { it.id != id }
        _tasks.value = updated
        saveTasks(updated)
    }

    private fun saveTasks(list: List<MowatibTask>) {
        val arr = JSONArray()
        list.forEach { task ->
            val obj = JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("category", task.category)
                put("isCompleted", task.isCompleted)
                put("createdAt", task.createdAt)
            }
            arr.put(obj)
        }
        prefs?.edit()?.putString(KEY_TASKS, arr.toString())?.apply()
    }
}
