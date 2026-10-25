package com.etrisad.zenith.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.etrisad.zenith.MainActivity
import com.etrisad.zenith.data.local.MowatibTaskManager

class MowatibTasksWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        MowatibTaskManager.init(context)
        val allTasks = MowatibTaskManager.tasks.value
        val completedCount = allTasks.count { it.isCompleted }
        val totalCount = allTasks.size
        val topTasks = allTasks.take(4)

        provideContent {
            GlanceTheme {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", "pomodoro")
                }
                val launchAction = actionStartActivity(intent)

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(24.dp)
                        .background(GlanceTheme.colors.widgetBackground)
                        .clickable(launchAction)
                        .padding(14.dp)
                ) {
                    TasksWidgetContent(
                        totalCount = totalCount,
                        completedCount = completedCount,
                        tasks = topTasks
                    )
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    @androidx.compose.runtime.Composable
    private fun TasksWidgetContent(
        totalCount: Int,
        completedCount: Int,
        tasks: List<com.etrisad.zenith.data.local.MowatibTask>
    ) {
        val size = LocalSize.current
        val isWide = size.width >= 180.dp

        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            // Header Row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📝 مهام مواظب",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 15.sp,
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
                        text = "$completedCount/$totalCount",
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد مهام حالياً\nاضغط لإضافة مهمة جديدة ✨",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            } else {
                tasks.forEach { task ->
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (task.isCompleted) "✓ " else "○ ",
                            style = TextStyle(
                                color = if (task.isCompleted) GlanceTheme.colors.primary else GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Text(
                            text = task.title,
                            maxLines = 1,
                            style = TextStyle(
                                color = if (task.isCompleted) GlanceTheme.colors.onSurfaceVariant else GlanceTheme.colors.onSurface,
                                fontSize = 13.sp,
                                fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium
                            ),
                            modifier = GlanceModifier.defaultWeight()
                        )

                        if (isWide && task.category.isNotBlank()) {
                            Spacer(modifier = GlanceModifier.width(6.dp))
                            Box(
                                modifier = GlanceModifier
                                    .cornerRadius(8.dp)
                                    .background(GlanceTheme.colors.secondaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = task.category,
                                    style = TextStyle(
                                        color = GlanceTheme.colors.onSecondaryContainer,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class MowatibTasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MowatibTasksWidget()
}
