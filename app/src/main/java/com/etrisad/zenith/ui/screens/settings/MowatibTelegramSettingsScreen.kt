package com.etrisad.zenith.ui.screens.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.etrisad.zenith.R
import com.etrisad.zenith.data.manager.MowatibTelegramConfig
import com.etrisad.zenith.data.manager.MowatibTelegramManager
import kotlinx.coroutines.launch

@Composable
fun MowatibTelegramSettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val telegramManager = remember { MowatibTelegramManager(context) }

    var config by remember { mutableStateOf(telegramManager.getConfig()) }
    var chatIdInput by remember { mutableStateOf(config.chatId) }
    var tokenInput by remember { mutableStateOf(config.botToken) }

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
        // Header Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
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
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                val isAr = java.util.Locale.getDefault().language.startsWith("ar")

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isAr) "تكامل منصة تيليجرام" else "Telegram Bot Integration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isAr)
                            "اربط التطبيق بحسابك لتلقي تقارير الإنجاز اليومية، وتنبيهات انضباط الشاشة، ومتابعة عاداتك وجلسات تركيزك لحظياً."
                        else
                            "Connect your account to receive daily progress reports, screen time discipline alerts, and real-time focus updates.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        val isAr = java.util.Locale.getDefault().language.startsWith("ar")

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = if (isAr) "حالة التكامل" else "Integration Status")

        // Enable Switch
        SettingsToggle(
            title = if (isAr) "تفعيل إشعارات تيليجرام" else "Enable Telegram Notifications",
            description = if (isAr) "إرسال تقارير المواظبة وتنبيهات الانضباط عبر البوت" else "Send focus reports and discipline alerts via Telegram bot",
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
                PreferenceCategory(title = if (isAr) "خطوات الربط السريع" else "Quick Setup Instructions")

                // Instructions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isAr) "كيف تربط حسابك؟" else "How to connect?",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isAr)
                                "١. اضغط زر «فتح البوت» أدناه واضغط زر البدء في تيليجرام.\n" +
                                "٢. أرسل أي رسالة للبوت لمعرفة معرّف حسابك الرقمي.\n" +
                                "٣. الصق معرّف الحساب ورمز مفتاح البوت ثم اضغط فحص الاتصال."
                            else
                                "1. Tap \"Open Bot\" below and start the bot in Telegram.\n" +
                                "2. Send any message to get your Chat ID, or use @userinfobot.\n" +
                                "3. Paste your Chat ID and Bot Token, then tap Test Connection.",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { telegramManager.openTelegramBot() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_telegram),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isAr) "فتح البوت" else "Open Bot")
                            }

                            OutlinedButton(
                                onClick = { telegramManager.openUserInfoBot() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isAr) "معرفة المعرّف" else "Get Chat ID")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                PreferenceCategory(title = if (isAr) "بيانات الحساب ومفتاح البوت" else "Account & Bot Credentials")

                // Chat ID Field
                OutlinedTextField(
                    value = chatIdInput,
                    onValueChange = {
                        chatIdInput = it
                        updateAndSave(config.copy(chatId = it))
                    },
                    label = { Text(if (isAr) "معرّف الحساب الرقمي" else "Chat ID") },
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

                Spacer(modifier = Modifier.height(8.dp))

                // Bot Token Field
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = {
                        tokenInput = it
                        updateAndSave(config.copy(botToken = it))
                    },
                    label = { Text(if (isAr) "رمز مفتاح البوت" else "Bot Token") },
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
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Test Connection Button
                Button(
                    onClick = {
                        if (chatIdInput.isBlank() || tokenInput.isBlank()) {
                            val msg = if (isAr) "يرجى ملء حقل معرّف الحساب ورمز مفتاح البوت أولاً" else "Please enter both Chat ID and Bot Token first"
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
                                testResultStatus = if (isAr) "تم إرسال رسالة تجريبية بنجاح إلى حسابك في تيليجرام!" else "Test message sent successfully to your Telegram account!"
                                val okToast = if (isAr) "الاتصال ناجح!" else "Connection successful!"
                                Toast.makeText(context, okToast, Toast.LENGTH_SHORT).show()
                            } else {
                                isTestSuccess = false
                                val prefix = if (isAr) "فشل الاتصال: " else "Connection failed: "
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
                        Icon(Icons.Outlined.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isAr) "فحص الاتصال وإرسال رسالة تجريبية" else "Test Connection & Send Message")
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
                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                )
            }
        }
    }
}
