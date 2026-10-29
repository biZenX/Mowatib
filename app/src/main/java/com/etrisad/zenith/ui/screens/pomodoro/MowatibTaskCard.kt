package com.etrisad.zenith.ui.screens.pomodoro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.etrisad.zenith.data.local.MowatibTask
import com.etrisad.zenith.data.local.MowatibTaskManager
import com.etrisad.zenith.data.manager.MowatibSyncManager
import com.etrisad.zenith.data.manager.SyncStatus
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MowatibTaskCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isAr = remember { Locale.getDefault().language.startsWith("ar") }
    val tasks by MowatibTaskManager.tasks.collectAsState()
    val syncStatus by MowatibSyncManager.syncStatus.collectAsState()
    var newTaskText by remember { mutableStateOf("") }
    val selectedCategory = if (isAr) "تركيز" else "Focus"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isAr) "مهام جلسة التركيز" else "Focus Session Tasks",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val completedCount = tasks.count { it.isCompleted }
                    Text(
                        text = if (tasks.isEmpty()) {
                            if (isAr) "لا توجد مهام حالياً" else "No tasks currently"
                        } else {
                            if (isAr) "تم إنجاز $completedCount من ${tasks.size} مهام" else "Completed $completedCount of ${tasks.size} tasks"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            MowatibSyncManager.sync(context)
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    if (syncStatus == SyncStatus.SYNCING) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Sync,
                            contentDescription = if (isAr) "مزامنة سحابية" else "Cloud Sync",
                            tint = if (syncStatus == SyncStatus.SYNCED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    placeholder = { Text(if (isAr) "أضف مهمة جديدة للتركيز..." else "Add a new focus task...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newTaskText.isNotBlank()) {
                            MowatibTaskManager.addTask(newTaskText, selectedCategory)
                            newTaskText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = if (isAr) "إضافة مهمة" else "Add Task",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = if (isAr) "لا توجد مهام حالياً. أضف مهمة لتبدأ التركيز عليها!" else "No tasks yet. Add a task to start focusing!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { task ->
                        TaskItemRow(task = task, isAr = isAr)
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskItemRow(task: MowatibTask, isAr: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { MowatibTaskManager.toggleTask(task.id) }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { MowatibTaskManager.toggleTask(task.id) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = task.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = { MowatibTaskManager.deleteTask(task.id) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = if (isAr) "حذف" else "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
