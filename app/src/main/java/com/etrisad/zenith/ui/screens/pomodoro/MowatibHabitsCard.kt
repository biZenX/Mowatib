package com.etrisad.zenith.ui.screens.pomodoro

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            _habits.value = list
        } else {
            // Default Mowatib starter habits
            val defaults = listOf(
                MowatibHabit("1", "ورد القرآن اليومي وتدبره", 3, getTodayDateString(), "#3D5622"),
                MowatibHabit("2", "جلسة تركيز عميق (بومودورو)", 5, getTodayDateString(), "#5A6348"),
                MowatibHabit("3", "قراءة وتطوير الذات 20 دقيقة", 2, "", "#6B7724")
            )
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
                    habit.copy(
                        streak = habit.streak + 1,
                        lastCompletedDate = today
                    )
                }
            } else habit
        }
        save(context, current)
    }

    fun addHabit(context: Context, title: String) {
        val newHabit = MowatibHabit(
            id = System.currentTimeMillis().toString(),
            title = title,
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

@Composable
fun MowatibHabitsCard(
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        MowatibHabitsManager.init(context)
    }

    val habits by MowatibHabitsManager.habits.collectAsState()
    var isExpanded by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newHabitTitle by remember { mutableStateOf("") }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.TrackChanges,
                            contentDescription = "عادات مواظب",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "عادات وسلاسل مواظب",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "الالتزام اليومي وسلاسل النجاح (Streaks)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة عادة",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = Icons.Outlined.ExpandMore,
                            contentDescription = "توسيع",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (habits.isEmpty()) {
                        Text(
                            text = "لا توجد عادات مسجلة حالياً، اضغط + للبدء.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        habits.forEach { habit ->
                            val isDoneToday = habit.lastCompletedDate == today
                            val cardBg by animateColorAsState(
                                targetValue = if (isDoneToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                label = "HabitCardBg"
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(cardBg)
                                    .clickable { MowatibHabitsManager.toggleHabit(context, habit.id) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDoneToday) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDoneToday) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "منجز",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = habit.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isDoneToday) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = "سلسلة الالتزام",
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "${habit.streak} يوم",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFFE65100)
                                    )
                                    IconButton(
                                        onClick = { MowatibHabitsManager.deleteHabit(context, habit.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "حذف",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(text = "إضافة عادة جديدة لمواظب", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newHabitTitle,
                    onValueChange = { newHabitTitle = it },
                    placeholder = { Text("مثال: قراءة 15 صفحة، رياضة الصباح") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newHabitTitle.isNotBlank()) {
                            MowatibHabitsManager.addHabit(context, newHabitTitle.trim())
                            newHabitTitle = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
