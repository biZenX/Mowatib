package com.etrisad.zenith.data.local

import android.content.Context
import com.etrisad.zenith.data.manager.MowatibTelegramManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MowatibHabit(
    val id: String,
    val title: String,
    val streak: Int,
    val lastCompletedDate: String,
    val colorHex: String = "#3D5622"
)

object MowatibHabitsManager {
    private const val PREFS_NAME = "mowatib_habits_prefs"
    private const val KEY_HABITS = "saved_habits"

    private val _habits = MutableStateFlow<List<MowatibHabit>>(emptyList())
    val habits: StateFlow<List<MowatibHabit>> = _habits.asStateFlow()

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HABITS, null)
        if (json != null) {
            val list = mutableListOf<MowatibHabit>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        MowatibHabit(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            streak = obj.optInt("streak", 0),
                            lastCompletedDate = obj.optString("lastCompletedDate", ""),
                            colorHex = obj.optString("colorHex", "#3D5622")
                        )
                    )
                }
            } catch (_: Exception) {}
            _habits.value = list
        } else {
            val isAr = Locale.getDefault().language.startsWith("ar")
            val defaults = if (isAr) {
                listOf(
                    MowatibHabit("1", "ورد القرآن اليومي وتدبره", 3, getTodayDateString(), "#3D5622"),
                    MowatibHabit("2", "جلسة تركيز عميق ومواظبة", 5, getTodayDateString(), "#5A6348"),
                    MowatibHabit("3", "قراءة وتطوير الذات 20 دقيقة", 2, "", "#6B7724")
                )
            } else {
                listOf(
                    MowatibHabit("1", "Daily Focus & Mindfulness", 3, getTodayDateString(), "#3D5622"),
                    MowatibHabit("2", "Deep Work Session", 5, getTodayDateString(), "#5A6348"),
                    MowatibHabit("3", "Reading & Learning 20m", 2, "", "#6B7724")
                )
            }
            save(context, defaults)
        }
    }

    fun toggleHabit(context: Context, id: String) {
        val today = getTodayDateString()
        val current = _habits.value.map { habit ->
            if (habit.id == id) {
                val isCompletedToday = habit.lastCompletedDate == today
                if (isCompletedToday) {
                    habit.copy(
                        streak = (habit.streak - 1).coerceAtLeast(0),
                        lastCompletedDate = ""
                    )
                } else {
                    val updated = habit.copy(
                        streak = habit.streak + 1,
                        lastCompletedDate = today
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            MowatibTelegramManager(context)
                                .sendHabitCompleted(habit.title, updated.streak)
                        } catch (_: Exception) {}
                    }
                    updated
                }
            } else habit
        }
        save(context, current)
    }

    fun addHabit(context: Context, title: String) {
        if (title.isBlank()) return
        val newHabit = MowatibHabit(
            id = System.currentTimeMillis().toString(),
            title = title.trim(),
            streak = 1,
            lastCompletedDate = getTodayDateString()
        )
        val current = _habits.value + newHabit
        save(context, current)
    }

    fun deleteHabit(context: Context, id: String) {
        val current = _habits.value.filterNot { it.id == id }
        save(context, current)
    }

    private fun save(context: Context, list: List<MowatibHabit>) {
        _habits.value = list
        val array = JSONArray()
        for (h in list) {
            val obj = JSONObject()
            obj.put("id", h.id)
            obj.put("title", h.title)
            obj.put("streak", h.streak)
            obj.put("lastCompletedDate", h.lastCompletedDate)
            obj.put("colorHex", h.colorHex)
            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HABITS, array.toString())
            .apply()
    }
}
