package com.etrisad.zenith.ui.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.etrisad.zenith.MainActivity
import com.etrisad.zenith.ZenithApplication
import com.etrisad.zenith.data.local.MowatibTaskManager
import com.etrisad.zenith.ui.screens.pomodoro.MowatibHabitsManager
import kotlinx.coroutines.flow.first

class MowatibQuickDashboardWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as ZenithApplication
        val repo = app.shieldRepository
        val prefsRepo = app.userPreferencesRepository
        val prefs = try { prefsRepo.userPreferencesFlow.first() } catch (_: Exception) { null }

        val todayStr = com.etrisad.zenith.util.DateTimeUtils.getDayStartDateString(
            System.currentTimeMillis(), prefs?.dayStartHour ?: 0, prefs?.dayStartMinute ?: 0
        )
        val totalMillis = try {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val daily = repo.getDailyUsagesForDateSync(todayStr)
                val total = daily.find { it.packageName == "TOTAL" }?.usageTimeMillis
                if (total != null && total > 0) total else daily.filter { it.packageName !in setOf("SHIELD_TOTAL","GOAL_TOTAL","OTHER_TOTAL") }.sumOf { it.usageTimeMillis }
            }
        } catch (_: Exception) { 0L }

        val totalScreenTimeMinutes = (totalMillis / (1000 * 60)).toInt()
        val streak = prefs?.globalCurrentStreak ?: 0

        MowatibTaskManager.init(context)
        val tasks = MowatibTaskManager.tasks.value
        val completedTasks = tasks.count { it.isCompleted }
        val totalTasks = tasks.size

        MowatibHabitsManager.init(context)
        val habits = MowatibHabitsManager.habits.value
        val completedHabits = habits.count { it.lastCompletedDate.isNotEmpty() }
        val totalHabits = habits.size

        val hours = totalScreenTimeMinutes / 60
        val mins = totalScreenTimeMinutes % 60
        val screenTimeText = if (hours > 0) "${hours}س ${mins}د" else "${mins} دقيقة"

        provideContent {
            GlanceTheme {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val clickAction = actionStartActivity(intent)

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(24.dp)
                        .background(GlanceTheme.colors.widgetBackground)
                        .padding(14.dp)
                        .clickable(clickAction)
                ) {
                    DashboardContent(
                        screenTimeText = screenTimeText,
                        streak = streak,
                        completedTasks = completedTasks,
                        totalTasks = totalTasks,
                        completedHabits = completedHabits,
                        totalHabits = totalHabits
                    )
                }
            }
        }
    }

    @Composable
    private fun DashboardContent(
        screenTimeText: String,
        streak: Int,
        completedTasks: Int,
        totalTasks: Int,
        completedHabits: Int,
        totalHabits: Int
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🌱 مواظب — لوحة اليوم",
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = GlanceModifier.defaultWeight()
                )

                Box(
                    modifier = GlanceModifier
                        .cornerRadius(12.dp)
                        .background(GlanceTheme.colors.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (streak > 0) "🔥 $streak يوم" else "⚡ انطلق",
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(10.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metric 1: Screen time
                MetricCard(
                    title = "وقت الشاشة",
                    value = screenTimeText,
                    icon = "📱",
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                // Metric 2: Tasks
                MetricCard(
                    title = "المهام",
                    value = "$completedTasks/$totalTasks",
                    icon = "✅",
                    modifier = GlanceModifier.defaultWeight()
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                // Metric 3: Habits
                MetricCard(
                    title = "العادات",
                    value = "$completedHabits/$totalHabits",
                    icon = "⭐",
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }
    }

    @Composable
    private fun MetricCard(
        title: String,
        value: String,
        icon: String,
        modifier: GlanceModifier
    ) {
        Box(
            modifier = modifier
                .cornerRadius(16.dp)
                .background(GlanceTheme.colors.surfaceVariant)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = icon,
                    style = TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center)
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = value,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
                Text(
                    text = title,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

class MowatibQuickDashboardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MowatibQuickDashboardWidget()
}
