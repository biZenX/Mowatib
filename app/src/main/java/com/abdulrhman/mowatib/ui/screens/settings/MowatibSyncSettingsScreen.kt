package com.abdulrhman.mowatib.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abdulrhman.mowatib.data.manager.MowatibSyncManager
import com.abdulrhman.mowatib.data.manager.SyncStatus
import com.abdulrhman.mowatib.ui.components.focus.PreferenceCategory
import com.abdulrhman.mowatib.ui.components.focus.SettingsToggle
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MowatibSyncSettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAr = com.abdulrhman.mowatib.ui.theme.LocalIsArabic.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    var mowatibId by remember { mutableStateOf(MowatibSyncManager.getMowatibId(context)) }
    var copiedId by remember { mutableStateOf(false) }
    var showEditIdDialog by remember { mutableStateOf(false) }
    var tempIdInput by remember { mutableStateOf(mowatibId) }

    var serverUrl by remember { mutableStateOf(MowatibSyncManager.getServerUrl(context)) }
    var showServerDialog by remember { mutableStateOf(false) }
    var tempServerInput by remember { mutableStateOf(serverUrl) }

    var autoSync by remember { mutableStateOf(MowatibSyncManager.isAutoSyncEnabled(context)) }

    val syncStatus by MowatibSyncManager.syncStatus.collectAsState()
    val lastSyncedAt by MowatibSyncManager.lastSyncedAt.collectAsState()

    LaunchedEffect(Unit) {
        MowatibSyncManager.init(context)
        MowatibSyncManager.scheduleSync(context, 100L)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Hero Card: Mowatib ID & Connection Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CloudSync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isAr) "سحابة مواظب و MCP" else "Mowatib Cloud & MCP",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isAr) "مزامنة المهام والعادات مباشرة" else "Direct task & habit synchronization",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Sync Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (syncStatus) {
                            SyncStatus.SYNCED -> MaterialTheme.colorScheme.primaryContainer
                            SyncStatus.SYNCING -> MaterialTheme.colorScheme.tertiaryContainer
                            SyncStatus.OFFLINE -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (syncStatus == SyncStatus.SYNCING) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Text(
                                text = when (syncStatus) {
                                    SyncStatus.SYNCED -> if (isAr) "متزامن" else "Synced"
                                    SyncStatus.SYNCING -> if (isAr) "جارِ المزامنة..." else "Syncing..."
                                    SyncStatus.OFFLINE -> if (isAr) "غير متصل" else "Offline"
                                    SyncStatus.ERROR -> if (isAr) "خطأ" else "Error"
                                    SyncStatus.IDLE -> if (isAr) "جاهز" else "Idle"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Mowatib ID Display Box
                Text(
                    text = if (isAr) "معرف مواظب الخاص بك (Mowatib ID)" else "Your Mowatib ID",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            clipboardManager.setText(AnnotatedString(mowatibId))
                            copiedId = true
                            Toast.makeText(
                                context,
                                if (isAr) "تم نسخ معرف مواظب" else "Mowatib ID copied",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = mowatibId,
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = {
                                    tempIdInput = mowatibId
                                    showEditIdDialog = true
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Edit,
                                    contentDescription = if (isAr) "تعديل المعرف" else "Edit ID",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(mowatibId))
                                    copiedId = true
                                    Toast.makeText(
                                        context,
                                        if (isAr) "تم نسخ معرف مواظب" else "Mowatib ID copied",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    if (copiedId) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                                    contentDescription = if (isAr) "نسخ" else "Copy",
                                    tint = if (copiedId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                if (lastSyncedAt != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isAr) "آخر مزامنة ناجحة: $lastSyncedAt" else "Last synced: $lastSyncedAt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sync Now Action Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val result = MowatibSyncManager.sync(context)
                            val msg = if (result.isSuccess) {
                                if (isAr) "تمت المزامنة بنجاح" else "Sync completed successfully"
                            } else {
                                if (isAr) "تعذرت المزامنة، تحقق من الاتصال" else "Sync failed, check connection"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isAr) "مزامنة الآن" else "Sync Now",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Settings Section
        PreferenceCategory(title = if (isAr) "خيارات المزامنة" else "Sync Settings")

        SettingsToggle(
            title = if (isAr) "المزامنة التلقائية" else "Automatic Sync",
            description = if (isAr) "مزامنة التغييرات تلقائياً عند إضافة أو تعديل المهام والعادات" else "Automatically sync changes when tasks or habits are updated",
            checked = autoSync,
            onCheckedChange = {
                autoSync = it
                MowatibSyncManager.setAutoSyncEnabled(context, it)
            },
            icon = Icons.Outlined.Sync,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    tempServerInput = serverUrl
                    showServerDialog = true
                },
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Dns,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isAr) "خادم MCP والمزامنة" else "MCP / Sync Server",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = serverUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Edit ID Dialog
    if (showEditIdDialog) {
        AlertDialog(
            onDismissRequest = { showEditIdDialog = false },
            title = { Text(if (isAr) "تعديل معرف مواظب" else "Edit Mowatib ID") },
            text = {
                Column {
                    Text(
                        text = if (isAr) "أدخل معرف حساب مواظب الخاص بك للربط مع باقي أجهزتك:" else "Enter your Mowatib ID to link with your other devices:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempIdInput,
                        onValueChange = { tempIdInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("MW-XXXXXX") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempIdInput.isNotBlank()) {
                            mowatibId = tempIdInput.trim().uppercase(Locale.US)
                            MowatibSyncManager.setMowatibId(context, mowatibId)
                            showEditIdDialog = false
                            coroutineScope.launch {
                                MowatibSyncManager.sync(context)
                            }
                        }
                    }
                ) {
                    Text(if (isAr) "حفظ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditIdDialog = false }) {
                    Text(if (isAr) "إلغاء" else "Cancel")
                }
            }
        )
    }

    // Edit Server URL Dialog
    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = { Text(if (isAr) "عنوان خادم المزامنة و MCP" else "Sync & MCP Server URL") },
            text = {
                Column {
                    Text(
                        text = if (isAr) "يمكنك استخدام خادم مواظب السحابي الافتراضي أو خادم MCP خاص بك:" else "Use the official Mowatib Cloud or your own MCP endpoint:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempServerInput,
                        onValueChange = { tempServerInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempServerInput.isNotBlank()) {
                            serverUrl = tempServerInput.trim()
                            MowatibSyncManager.setServerUrl(context, serverUrl)
                            showServerDialog = false
                        }
                    }
                ) {
                    Text(if (isAr) "حفظ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
                    Text(if (isAr) "إلغاء" else "Cancel")
                }
            }
        )
    }
}
