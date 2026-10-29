package com.etrisad.zenith.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etrisad.zenith.R
import com.etrisad.zenith.data.manager.MowatibSyncManager
import com.etrisad.zenith.data.manager.MowatibTelegramConfig
import com.etrisad.zenith.data.manager.MowatibTelegramManager
import com.etrisad.zenith.data.manager.SyncStatus
import com.etrisad.zenith.ui.components.focus.PreferenceCategory
import com.etrisad.zenith.ui.components.focus.SettingsToggle
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MowatibTelegramSettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAr = remember { Locale.getDefault().language.startsWith("ar") }
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val telegramManager = remember { MowatibTelegramManager(context) }
    val userUid = remember { telegramManager.getUniqueId() }
    var copiedUid by remember { mutableStateOf(false) }

    val syncStatus by MowatibSyncManager.syncStatus.collectAsState()
    val lastSyncedAt by MowatibSyncManager.lastSyncedAt.collectAsState()

    LaunchedEffect(Unit) {
        MowatibSyncManager.init(context)
        telegramManager.syncUidWithServer()
        MowatibSyncManager.scheduleSync(context, 100L)
    }

    LaunchedEffect(copiedUid) {
        if (copiedUid) {
            kotlinx.coroutines.delay(2000)
            copiedUid = false
        }
    }

    var config by remember { mutableStateOf(telegramManager.getConfig()) }
    var chatIdInput by remember { mutableStateOf(config.chatId) }
    var tokenInput by remember { mutableStateOf(config.botToken) }
    var isAdvancedExpanded by remember { mutableStateOf(config.botToken.isNotBlank()) }

    var isTestingConnection by remember { mutableStateOf(false) }
    var testResultStatus by remember { mutableStateOf<String?>(null) }
    var isTestSuccess by remember { mutableStateOf(false) }

    fun updateAndSave(newConfig: MowatibTelegramConfig) {
        config = newConfig
        telegramManager.saveConfig(newConfig)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        // Hero Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_telegram),
                            contentDescription = "Telegram",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isAr) "بوت مواظب في تيليجرام" else "Mowatib Telegram Bot",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        val statusText = if (config.enabled && config.chatId.isNotBlank()) {
                            if (isAr) "متصل بالمعرّف: ${config.chatId}" else "Linked to: ${config.chatId}"
                        } else if (config.enabled) {
                            if (isAr) "جاهز للربط بالمعرّف: $userUid" else "Ready to link via UID: $userUid"
                        } else {
                            if (isAr) "غير مفعّل" else "Disabled"
                        }
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isAr)
                        "اربط حسابك لتلقي تقارير الإنجاز اليومية، وتنبيهات انضباط الشاشة، ومتابعة عاداتك وجلسات تركيزك لحظياً عبر تيليجرام."
                    else
                        "Connect your Telegram account to receive daily progress reports, screen time discipline alerts, and real-time focus updates.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = if (isAr) "حالة التكامل" else "Integration Status")

        // Enable Switch
        SettingsToggle(
            title = if (isAr) "تفعيل إشعارات تيليجرام" else "Enable Telegram Notifications",
            description = if (isAr) "إرسال تقارير المواظبة وتنبيهات الانضباط عبر البوت" else "Send focus reports and discipline alerts via Telegram",
            checked = config.enabled,
            onCheckedChange = { isEnabled ->
                val updated = config.copy(enabled = isEnabled, chatId = chatIdInput, botToken = tokenInput)
                updateAndSave(updated)
            },
            icon = Icons.Outlined.NotificationsActive,
            shape = RoundedCornerShape(24.dp)
        )

        AnimatedVisibility(
            visible = config.enabled,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(16.dp))
                PreferenceCategory(title = if (isAr) "الربط مع بوت مواظب الرسمي" else "Official Mowatib Bot Linking")

                // Official Bot Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SmartToy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = if (isAr) "بوت مواظب الرسمي" else "Official Mowatib Bot",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "@mowatibot",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isAr) "معرّف الجهاز والحساب:" else "Device & Account ID:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = userUid,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(userUid))
                                    copiedUid = true
                                    val toastMsg = if (isAr) "تم نسخ المعرّف!" else "UID copied!"
                                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = if (copiedUid) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (copiedUid) {
                                        if (isAr) "تم النسخ" else "Copied"
                                    } else {
                                        if (isAr) "نسخ المعرّف" else "Copy UID"
                                    },
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isAr)
                                "١. اضغط زر «ربط مع تيليجرام» لفتح بوت مواظب والربط المباشر تلقائياً.\n" +
                                "٢. أو انسخ المعرّف وأرسله للبوت في محادثة تيليجرام بالأمر: /link <معرفك>.\n" +
                                "٣. لتأكيد استقبال الإشعارات على هذا الجهاز، الصق معرّف المحادثة أدناه واضغط فحص الاتصال."
                            else
                                "1. Tap \"Link with Telegram\" to open Mowatib Bot with auto-linking.\n" +
                                "2. Or copy your UID and send it to the bot in Telegram: /link <UID>.\n" +
                                "3. Paste your Chat ID below and tap Verify & Test Link to confirm.",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    telegramManager.syncUidWithServer()
                                    telegramManager.openTelegramBot()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_telegram),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isAr) "ربط مع تيليجرام (@mowatibot)" else "Link with Telegram (@mowatibot)")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                PreferenceCategory(title = if (isAr) "معرّف حسابك الرقمي" else "Your Telegram Chat ID")

                // Chat ID Field
                OutlinedTextField(
                    value = chatIdInput,
                    onValueChange = {
                        chatIdInput = it
                        updateAndSave(config.copy(chatId = it))
                    },
                    label = { Text(if (isAr) "معرّف المحادثة الرقمي" else "Telegram Chat ID") },
                    placeholder = { Text(if (isAr) "مثال: 123456789" else "e.g. 123456789") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Tag, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            clipboardManager.getText()?.text?.let { text ->
                                chatIdInput = text
                                updateAndSave(config.copy(chatId = text))
                            }
                        }) {
                            Icon(Icons.Outlined.ContentPaste, contentDescription = if (isAr) "لصق" else "Paste")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Test Connection Button
                Button(
                    onClick = {
                        if (chatIdInput.isBlank()) {
                            val msg = if (isAr) "يرجى إدخال معرّف الحساب الرقمي أولاً" else "Please enter your Chat ID first"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        coroutineScope.launch {
                            isTestingConnection = true
                            testResultStatus = null
                            val result = telegramManager.testConnection(chatIdInput, tokenInput)
                            isTestingConnection = false
                            if (result.isSuccess) {
                                isTestSuccess = true
                                testResultStatus = result.getOrNull()
                                val okToast = if (isAr) "تم تأكيد الربط بنجاح!" else "Linked successfully!"
                                Toast.makeText(context, okToast, Toast.LENGTH_SHORT).show()
                            } else {
                                isTestSuccess = false
                                val prefix = if (isAr) "فشل التحقق: " else "Verification failed: "
                                testResultStatus = prefix + (result.exceptionOrNull()?.message ?: "")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isTestingConnection,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isTestingConnection) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isAr) "جارٍ فحص الاتصال..." else "Testing connection...")
                    } else {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isAr) "فحص وتأكيد الربط" else "Verify & Test Link")
                    }
                }

                testResultStatus?.let { status ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (isTestSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Advanced Options for Developers (Collapsible)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isAdvancedExpanded = !isAdvancedExpanded },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isAr) "خيارات متقدمة (مخصص للمطورين)" else "Advanced Options (Developers)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isAr) "استخدام توكن بوت خاص بدلاً من البوت الرسمي" else "Use custom bot token instead of official bot",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = if (isAdvancedExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(visible = isAdvancedExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                OutlinedTextField(
                                    value = tokenInput,
                                    onValueChange = {
                                        tokenInput = it
                                        updateAndSave(config.copy(botToken = it))
                                    },
                                    label = { Text(if (isAr) "رمز مفتاح البوت الخاص (اختياري)" else "Custom Bot Token (Optional)") },
                                    placeholder = { Text(if (isAr) "مثال: 123456:ABC-DEF..." else "e.g. 123456:ABC-DEF...") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Key, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            clipboardManager.getText()?.text?.let { text ->
                                                tokenInput = text
                                                updateAndSave(config.copy(botToken = text))
                                            }
                                        }) {
                                            Icon(Icons.Outlined.ContentPaste, contentDescription = if (isAr) "لصق" else "Paste")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                PreferenceCategory(title = if (isAr) "تخصيص التنبيهات والتقارير" else "Notification Preferences")

                // Notification Preferences
                SettingsToggle(
                    title = if (isAr) "تقرير الإنجاز اليومي" else "Daily Progress Report",
                    description = if (isAr) "إرسال ملخص استخدام الشاشة والعادات مساء كل يوم" else "Send daily screen time and habits recap every evening",
                    checked = config.notifyDailyRecap,
                    onCheckedChange = { checked ->
                        updateAndSave(config.copy(notifyDailyRecap = checked))
                    },
                    icon = Icons.Outlined.Summarize,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                SettingsToggle(
                    title = if (isAr) "تنبيهات انضباط الشاشة" else "Screen Time Alerts",
                    description = if (isAr) "إرسال تنبيه فوري عند بلوغ الحد الأقصى لأي تطبيق محمي" else "Instant notification when an app reaches its usage limit",
                    checked = config.notifyShieldBreach,
                    onCheckedChange = { checked ->
                        updateAndSave(config.copy(notifyShieldBreach = checked))
                    },
                    icon = Icons.Outlined.Shield,
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                SettingsToggle(
                    title = if (isAr) "إتمام جلسات التركيز" else "Focus Session Completions",
                    description = if (isAr) "تأكيد إنجاز فترات التركيز بنجاح عبر تيليجرام" else "Confirm completed focus and Pomodoro intervals",
                    checked = config.notifyPomodoro,
                    onCheckedChange = { checked ->
                        updateAndSave(config.copy(notifyPomodoro = checked))
                    },
                    icon = Icons.Outlined.Timer,
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                SettingsToggle(
                    title = if (isAr) "إنجاز العادات وسلاسل الالتزام" else "Habit & Streak Completions",
                    description = if (isAr) "تسجيل إتمام العادات اليومية وتحديث سلسلة الأيام" else "Record completed daily habits and streak updates",
                    checked = config.notifyHabits,
                    onCheckedChange = { checked ->
                        updateAndSave(config.copy(notifyHabits = checked))
                    },
                    icon = Icons.Outlined.CheckCircle,
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                SettingsToggle(
                    title = if (isAr) "تنبيهات إنجاز وإضافة المهام" else "Task Creation & Completion Alerts",
                    description = if (isAr) "إرسال إشعار فوري عبر البوت عند إضافة أو إنجاز مهام التركيز" else "Instant notification via bot when tasks are added or completed",
                    checked = config.notifyTasks,
                    onCheckedChange = { checked ->
                        updateAndSave(config.copy(notifyTasks = checked))
                    },
                    icon = Icons.Outlined.Checklist,
                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))
                PreferenceCategory(title = if (isAr) "المزامنة السحابية (مواظب كلاود)" else "Cloud Synchronization (Mowatib Cloud)")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CloudSync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAr) "المزامنة السحابية الفورية" else "Real-time Cloud Sync",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val syncStatusDesc = when (syncStatus) {
                                    SyncStatus.SYNCING -> if (isAr) "جارٍ المزامنة السحابية..." else "Syncing with cloud..."
                                    SyncStatus.SYNCED -> if (isAr) "تمت المزامنة بنجاح${if (lastSyncedAt != null) " (آخر مزامنة: $lastSyncedAt)" else ""}" else "Synced successfully${if (lastSyncedAt != null) " (at $lastSyncedAt)" else ""}"
                                    SyncStatus.OFFLINE -> if (isAr) "وضع عدم الاتصال (حفظ محلي آمن)" else "Offline mode (saved locally)"
                                    SyncStatus.ERROR -> if (isAr) "تعذر الاتصال بالخادم" else "Server connection error"
                                    SyncStatus.IDLE -> if (isAr) "جاهز للمزامنة" else "Ready to sync"
                                }
                                Text(
                                    text = syncStatusDesc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (syncStatus == SyncStatus.SYNCED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isAr)
                                "مزامنة سحابية هجينة غير محدودة لمهامك وعاداتك وتنبيهاتك عبر خوادم Cloudflare D1 و Supabase بدون أي تكلفة تشغيلية لخدمة كافة المستخدمين."
                            else
                                "Unlimited hybrid zero-cost cloud sync for your tasks and habits via Cloudflare D1 and Supabase edge architecture.",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    MowatibSyncManager.sync(context)
                                }
                            },
                            enabled = syncStatus != SyncStatus.SYNCING,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (syncStatus == SyncStatus.SYNCING) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isAr) "جارٍ المزامنة..." else "Syncing...")
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isAr) "مزامنة الآن" else "Sync Now")
                            }
                        }
                    }
                }
            }
        }
    }
}
