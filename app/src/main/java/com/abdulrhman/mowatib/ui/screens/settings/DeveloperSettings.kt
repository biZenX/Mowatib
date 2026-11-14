package com.abdulrhman.mowatib.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import com.abdulrhman.mowatib.BuildConfig
import com.abdulrhman.mowatib.crash.CrashActivity
import com.abdulrhman.mowatib.crash.CrashStore
import com.abdulrhman.mowatib.crash.SafeMode
import com.abdulrhman.mowatib.data.preferences.UserPreferences
import com.abdulrhman.mowatib.ui.components.MowatibButton
import com.abdulrhman.mowatib.ui.components.MowatibButtonSize
import com.abdulrhman.mowatib.ui.components.MowatibButtonType
import com.abdulrhman.mowatib.ui.components.focus.MowatibDropdown
import com.abdulrhman.mowatib.ui.components.overlay.InterceptBottomSheet
import com.abdulrhman.mowatib.ui.theme.LocalIsArabic
import com.abdulrhman.mowatib.ui.viewmodel.FocusUiState
import java.util.Locale

@Composable
fun DeveloperSettings(
    preferences: UserPreferences,
    focusUiState: FocusUiState,
    onSearchQueryChange: (String) -> Unit,
    onShowDatabaseIndicatorChange: (Boolean) -> Unit,
    onSmartRepairOnRefreshChange: (Boolean) -> Unit,
    onNavigateToDatabaseDebug: () -> Unit,
    onNavigateToDataRepairment: () -> Unit,
    onTestGoalOverlay: () -> Unit,
    onTestUpdateSheet: () -> Unit,
    onNavigateToFontTest: () -> Unit,
    onNavigateToSystemUsageDebug: () -> Unit,
    onTriggerOnboardingPermissions: () -> Unit,
    onTriggerOnboardingStats: () -> Unit,
    onTriggerOnboardingUpdate: () -> Unit,
    onTestUsageGlimpse: () -> Unit,
    onTestGoalCallerDelayed: () -> Unit,
    onTestAlarmOverlay: () -> Unit,
    onTestAchievementBanner: () -> Unit = {},
    onShowDbLogViewer: () -> Unit = {},
    onShowOverlayLogViewer: () -> Unit = {}
) {
    var showBottomSheetTest by remember { mutableStateOf(false) }
    var bottomSheetExpandValue by remember { mutableFloatStateOf(0f) }
    var showTtsTestSheet by remember { mutableStateOf(false) }

    if (preferences.developerModeEnabled) {
        val isAr = LocalIsArabic.current
        Column {
            PreferenceCategory(title = if (isAr) "معلومات وبيانات البناء" else "Build & Environment Info")

            SettingsActionItem(
                title = if (isAr) "حزمة وتطبيق مواظب" else "Application Package",
                summary = "com.abdulrhman.mowatib (v${BuildConfig.VERSION_NAME})",
                onClick = {},
                icon = Icons.Outlined.Info,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "مستوى الأمان والتحكم" else "Security & Isolation Mode",
                summary = if (isAr) "وضع المطور مقيّد للأغراض التشخيصية فقط للحفاظ على سلامة البيانات" else "Developer mode is restricted to diagnostics only to protect user data",
                onClick = {},
                icon = Icons.Outlined.Security,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            PreferenceCategory(title = if (isAr) "قواعد وسجلات البيانات" else "Database & Data")

            SettingsToggle(
                title = if (isAr) "مؤشر مصدر البيانات" else "Database Source Indicator",
                description = if (isAr) "إظهار مؤشر لسجلات البيانات في رسوم الاستخدام البيانية" else "Show indicator for database records in usage graphs",
                checked = preferences.showDatabaseIndicator,
                onCheckedChange = onShowDatabaseIndicatorChange,
                icon = Icons.Outlined.Storage,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsToggle(
                title = if (isAr) "الإصلاح الذكي عند التحديث" else "Smart Repair on Refresh",
                description = if (isAr) "تفعيل إعادة ضبط الترحيل عند السحب للتحديث في الواجهة" else "Enable resetCarryover() when pulling to refresh on dashboard",
                checked = preferences.smartRepairOnRefresh,
                onCheckedChange = onSmartRepairOnRefreshChange,
                icon = Icons.Outlined.AutoFixHigh,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "سجلات قاعدة البيانات" else "Database Records",
                summary = if (isAr) "عرض وفحص سجلات استخدام التطبيقات المسجلة" else "View and manage all recorded usage data",
                onClick = onNavigateToDatabaseDebug,
                icon = Icons.Outlined.SdStorage,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "فحص استخدام النظام" else "View System Usage Fetch",
                summary = if (isAr) "فحص سجلات الاستخدام المستخرجة مباشرة من نظام أندرويد" else "View 1:1 usage history duplication from system only",
                onClick = onNavigateToSystemUsageDebug,
                icon = Icons.Outlined.Analytics,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "فحص وإصلاح البيانات" else "Data Repairment",
                summary = if (isAr) "فحص سلامة سجلات الاستخدام وتصحيح الأخطاء" else "Fix missing or incorrect usage history",
                onClick = onNavigateToDataRepairment,
                icon = Icons.Outlined.Build,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            PreferenceCategory(title = if (isAr) "سجلات التشخيص الحية" else "Logs")

            SettingsActionItem(
                title = if (isAr) "سجل قاعدة البيانات المباشر" else "DB Log Viewer",
                summary = if (isAr) "عرض سجل عمليات البيانات والأهداف في الوقت الفعلي" else "View database and goal/shield operation logs in real-time",
                onClick = onShowDbLogViewer,
                icon = Icons.Outlined.BugReport,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "سجل الواجهات المنبثقة" else "Overlay Log Viewer",
                summary = if (isAr) "عرض سجل ظهور وتفاعل الواجهات المنبثقة في الوقت الفعلي" else "View overlay show/hide lifecycle and detection logs in real-time",
                onClick = onShowOverlayLogViewer,
                icon = Icons.Outlined.Visibility,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            PreferenceCategory(title = if (isAr) "محاكاة البداية والأذونات" else "Onboarding Triggers")

            SettingsActionItem(
                title = if (isAr) "إعادة فتح شاشة الأذونات" else "Trigger Permission Sheet",
                summary = if (isAr) "فتح شاشة أذونات وصلاحيات التطبيق الأولية" else "Open the initial permission onboarding sheet",
                onClick = onTriggerOnboardingPermissions,
                icon = Icons.Outlined.AdminPanelSettings,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "إعادة فتح شاشة الإحصائيات" else "Reset & Trigger Stats Onboarding",
                summary = if (isAr) "إعادة تعيين خيار نمط عرض الإحصائيات الأولي" else "Reset flag and show statistic experience choice",
                onClick = onTriggerOnboardingStats,
                icon = Icons.Outlined.BarChart,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = if (BuildConfig.SHOW_UPDATES) 8.dp else 24.dp, bottomEnd = if (BuildConfig.SHOW_UPDATES) 8.dp else 24.dp)
            )

            if (BuildConfig.SHOW_UPDATES) {
                Spacer(modifier = Modifier.height(4.dp))
                SettingsActionItem(
                    title = if (isAr) "إعادة فتح شاشة التحديثات" else "Reset & Trigger Update Onboarding",
                    summary = if (isAr) "إعادة تعيين خيار البحث التلقائي عن التحديثات" else "Reset flag and show update preference choice",
                    onClick = onTriggerOnboardingUpdate,
                    icon = Icons.Outlined.Update,
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            PreferenceCategory(title = if (isAr) "اختبارات وتجارب الواجهات" else "UI & Functional Testing")

            SettingsActionItem(
                title = if (isAr) "اختبار واجهة الحماية الكاملة" else "Test Goal Overlay",
                summary = if (isAr) "عرض واجهة الحماية المنبثقة بكامل الشاشة فورياً" else "Immediately trigger the full screen caller overlay",
                onClick = onTestGoalOverlay,
                icon = Icons.Outlined.BugReport,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "اختبار منبه الهدف (بعد 10 دقائق)" else "Test Goal Caller (10 min)",
                summary = if (isAr) "جدولة إنذار تجريبي لاختبار سلوك الشاشة المغلقة" else "Schedule a test goal caller alarm to fire after 10 minutes (tests screen-off behavior)",
                onClick = onTestGoalCallerDelayed,
                icon = Icons.Outlined.Alarm,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "اختبار مؤشر الاستخدام العائم" else "Test Usage Glimpse",
                summary = if (isAr) "عرض المؤشر العائم فورياً على الشاشة" else "Show the 5-minute usage glimpse HUD immediately",
                onClick = onTestUsageGlimpse,
                icon = Icons.Outlined.Visibility,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "اختبار شارة الإنجازات" else "Test Achievement Banner",
                summary = if (isAr) "محاكاة فتح إنجاز جديد لعرض التنبيه داخل التطبيق" else "Queue a fake unlock and open Profile to see the in-app notification",
                onClick = onTestAchievementBanner,
                icon = Icons.Outlined.EmojiEvents,
                shape = RoundedCornerShape(8.dp)
            )

            if (BuildConfig.SHOW_UPDATES) {
                Spacer(modifier = Modifier.height(4.dp))
                SettingsActionItem(
                    title = if (isAr) "اختبار نافذة التحديث الجديد" else "Test Update Sheet",
                    summary = if (isAr) "عرض نافذة التحديث الجديد التجريبية فورياً" else "Immediately trigger the new update bottom sheet",
                    onClick = onTestUpdateSheet,
                    icon = Icons.Outlined.NewReleases,
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "اختبار الخطوط المتغيرة" else "Test Variable Font",
                summary = if (isAr) "تجربة محاور الخط المتغير وتخصيص الأوزان" else "Demo variable axes with Google Sans Flex",
                onClick = onNavigateToFontTest,
                icon = Icons.Outlined.FontDownload,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "اختبار حركة القائمة السفلية" else "Test Bottom Sheet Expand",
                summary = if (isAr) "اختبار تفاعلي لحركة توسيع وطي القوائم السفلية" else "Interactive test for InterceptBottomSheet expand behavior",
                onClick = { showBottomSheetTest = true },
                icon = Icons.Outlined.OpenInFull,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "اختبار القارئ الصوتي" else "Test TTS",
                summary = if (isAr) "اختبار محرك تحويل النص إلى صوت مع نصوص مخصصة" else "Test Text-to-Speech engine with custom text",
                onClick = { showTtsTestSheet = true },
                icon = Icons.AutoMirrored.Outlined.VolumeUp,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            var showTestAlarmDialog by remember { mutableStateOf(false) }

            SettingsActionItem(
                title = if (isAr) "اختبار واجهة المنبه" else "Test Alarm Overlay",
                summary = if (isAr) "عرض واجهة المنبه الكاملة فورياً" else "Immediately trigger the alarm full screen overlay",
                onClick = { showTestAlarmDialog = true },
                icon = Icons.Outlined.NotificationsActive,
                shape = RoundedCornerShape(8.dp)
            )

            if (showTestAlarmDialog) {
                AlertDialog(
                    onDismissRequest = { showTestAlarmDialog = false },
                    title = { Text(if (isAr) "اختبار واجهة المنبه" else "Test Alarm Overlay") },
                    text = { Text(if (isAr) "سيتم عرض شاشة المنبه المنبثقة بكامل الشاشة." else "This will trigger the alarm full screen overlay.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showTestAlarmDialog = false
                            onTestAlarmOverlay()
                        }) { Text(if (isAr) "تشغيل" else "Trigger") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTestAlarmDialog = false }) { Text(if (isAr) "إلغاء" else "Cancel") }
                    }
                )
            }

            val crashContext = LocalContext.current

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "فتح آخر تقرير انهيار" else "Open Last Crash Report",
                summary = if (CrashStore.hasAnyCrash(crashContext)) {
                    if (isAr) "عرض تفاصيل آخر انهيار مسجل للتطبيق" else "View the most recent crash caught by Mowatib"
                } else {
                    if (isAr) "لم يتم تسجيل أي انهيار حتى الآن" else "No crash has been caught yet"
                },
                onClick = {
                    crashContext.startActivity(
                        Intent(crashContext, CrashActivity::class.java)
                    )
                },
                icon = Icons.Outlined.ReportProblem,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))
            SettingsActionItem(
                title = if (isAr) "إعادة التشغيل في الوضع الآمن" else "Restart in Safe Mode",
                summary = if (isAr) "تشغيل التطبيق دون تحميل بيانات مسبقة لمنع الانهيار" else "Bootstrap without loading user data; useful when the app keeps crashing",
                onClick = {
                    SafeMode.setEnabled(crashContext, true)
                    SafeMode.restartApp(crashContext)
                },
                icon = Icons.Outlined.HealthAndSafety,
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showBottomSheetTest) {
        var showContentB by remember { mutableStateOf(false) }
        val density = androidx.compose.ui.platform.LocalDensity.current
        val screenHeightDp = with(density) {
            androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
        }
        val contentHeight = screenHeightDp * (0.3f + bottomSheetExpandValue * 0.6f)

        Dialog(
            onDismissRequest = { showBottomSheetTest = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Box(Modifier.fillMaxSize()) {
                InterceptBottomSheet(
                    visible = true,
                    backgroundAlpha = 0.6f,
                    isLandscape = false,
                    showBedtimePill = false,
                    contentKey = showContentB
                ) { key ->
                    Column(
                        modifier = Modifier
                            .height(contentHeight)
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (key == true) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Content B, Shield",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "This demonstrates content transition.\nTap the button below to toggle.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.PauseCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )

                            Text(
                                text = "Content A, Pause Point",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Slide to adjust height.\nBottom stays fixed, top expands.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "Height: ${(30 + bottomSheetExpandValue * 60).toInt()}% of screen",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Slider(
                            value = bottomSheetExpandValue,
                            onValueChange = { bottomSheetExpandValue = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showContentB = !showContentB },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = if (key == true) "Show Content A" else "Show Content B",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { showBottomSheetTest = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Close", fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    if (showTtsTestSheet) {
        val ttsContext = LocalContext.current
        TtsTestSheet(
            context = ttsContext,
            onDismiss = { showTtsTestSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TtsTestSheet(
    context: Context,
    onDismiss: () -> Unit
) {
    val isAr = LocalIsArabic.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsStatus by remember { mutableStateOf("Initializing...") }
    var testText by remember { mutableStateOf(if (isAr) "تنبيه مواظب: حان وقت جلسة التركيز." else "Mowatib Reminder: Focus session has begun.") }
    var isSpeaking by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    DisposableEffect(Unit) {
        var localTts: TextToSpeech? = null
        localTts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts = localTts
                val defaultLocale = if (isAr) Locale("ar") else Locale.getDefault()
                val result = localTts?.setLanguage(defaultLocale)
                ttsStatus = if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    "Language not supported"
                } else {
                    "Ready (${defaultLocale.displayName})"
                }
            } else {
                ttsStatus = "TTS Init Failed ($status)"
            }
        }

        onDispose {
            localTts?.stop()
            localTts?.shutdown()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp)
        ) {
            Text(
                text = if (isAr) "اختبار قارئ النصوص" else "Test Text-to-Speech",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (tts != null) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = ttsStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = testText,
                onValueChange = { testText = it },
                label = { Text(if (isAr) "النص المراد نطقه" else "Text to speak") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            MowatibButton(
                onClick = {
                    if (tts != null && testText.isNotBlank()) {
                        isSpeaking = true
                        tts?.speak(testText, TextToSpeech.QUEUE_FLUSH, null, "test_utterance")
                        isSpeaking = false
                    }
                },
                enabled = tts != null && testText.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                text = if (isAr) "نطق النص الآن" else "Speak Now",
                icon = Icons.AutoMirrored.Outlined.VolumeUp,
                size = MowatibButtonSize.Large,
                fillMaxWidth = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            MowatibButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                text = if (isAr) "إغلاق" else "Close",
                type = MowatibButtonType.Outlined,
                size = MowatibButtonSize.Large,
                fillMaxWidth = true
            )
        }
    }
}
