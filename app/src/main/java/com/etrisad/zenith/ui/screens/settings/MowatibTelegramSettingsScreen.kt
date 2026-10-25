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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "ربط تيليجرام | Mowatib Bot",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اربط تطبيق مواظب بحسابك لتلقي تقارير الإنجاز اليومية، تنبيهات انضباط الشاشة، ومتابعة عاداتك وجلسات تركيزك لحظياً.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = "حالة التكامل")

        // Enable Switch
        SettingsToggle(
            title = "تفعيل إشعارات تيليجرام",
            description = "إرسال تقارير المواظبة وتنبيهات الانضباط عبر البوت",
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
                PreferenceCategory(title = "خطوات الربط السريع")

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
                            text = "كيف تربط حسابك؟",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "١. اضغط زر «فتح البوت» أدناه واضغط Start في تيليجرام.\n" +
                                    "٢. أرسل أي رسالة للبوت لمعرفة الـ Chat ID أو استخدم بوت @userinfobot.\n" +
                                    "٣. الصق الـ Chat ID ورمز البوت (Bot Token) من @BotFather ثم اضغط فحص الاتصال.",
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
                                Text("فتح البوت")
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
                                Text("معرفة الـ ID")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                PreferenceCategory(title = "بيانات الحساب والبوت")

                // Chat ID Field
                OutlinedTextField(
                    value = chatIdInput,
                    onValueChange = {
                        chatIdInput = it
                        updateAndSave(config.copy(chatId = it))
                    },
                    label = { Text("معرّف الحساب (Telegram Chat ID)") },
                    placeholder = { Text("مثال: 123456789") },
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
                            Icon(Icons.Outlined.ContentPaste, contentDescription = "لصق")
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
                    label = { Text("رمز البوت (Telegram Bot Token)") },
                    placeholder = { Text("من @BotFather (مثال: 123456:ABC-DEF...)") },
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
                            Icon(Icons.Outlined.ContentPaste, contentDescription = "لصق")
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
                            Toast.makeText(context, "يرجى ملء حقل الـ Chat ID ورمز البوت أولاً", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        coroutineScope.launch {
                            isTestingConnection = true
                            testResultStatus = null
                            val result = telegramManager.testConnection(chatIdInput, tokenInput)
                            isTestingConnection = false
                            if (result.isSuccess) {
                                isTestSuccess = true
                                testResultStatus = "تم إرسال رسالة تجريبية بنجاح إلى حسابك في تيليجرام!"
                                Toast.makeText(context, "الاتصال ناجح!", Toast.LENGTH_SHORT).show()
                            } else {
                                isTestSuccess = false
                                testResultStatus = "فشل الاتصال: ${result.exceptionOrNull()?.message}"
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
                        Text("جارٍ فحص الاتصال...")
                    } else {
                        Icon(Icons.Outlined.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("فحص الاتصال وإرسال رسالة تجريبية")
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
                PreferenceCategory(title = "تخصيص التنبيهات والتقارير")

                // Notification Preferences
                SettingsToggle(
                    title = "تقرير الإنجاز اليومي",
                    description = "إرسال ملخص استخدام الشاشة والعادات مساء كل يوم",
                    checked = config.notifyDailyRecap,
                    onCheckedChange = { checked ->
                        updateAndSave(config.copy(notifyDailyRecap = checked))
                    },
                    icon = Icons.Outlined.Summarize,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                SettingsToggle(
                    title = "تنبيهات انضباط الشاشة",
                    description = "إرسال تنبيه فوري عند بلوغ الحد الأقصى لأي تطبيق محمي",
                    checked = config.notifyShieldBreach,
                    onCheckedChange = { checked ->
                        updateAndSave(config.copy(notifyShieldBreach = checked))
                    },
                    icon = Icons.Outlined.Shield,
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                SettingsToggle(
                    title = "إتمام جلسات البومودورو",
                    description = "تأكيد إنجاز فترات التركيز بنجاح عبر تيليجرام",
                    checked = config.notifyPomodoro,
                    onCheckedChange = { checked ->
                        updateAndSave(config.copy(notifyPomodoro = checked))
                    },
                    icon = Icons.Outlined.Timer,
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                SettingsToggle(
                    title = "إنجاز العادات وسلسلة الالتزام",
                    description = "تسجيل إتمام العادات اليومية وتحديث الـ Streak",
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
