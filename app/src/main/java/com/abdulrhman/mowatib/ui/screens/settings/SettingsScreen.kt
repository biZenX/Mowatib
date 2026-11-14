package com.abdulrhman.mowatib.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.abdulrhman.mowatib.data.manager.GitHubUpdateManager
import com.abdulrhman.mowatib.data.preferences.PerformanceConfig
import com.abdulrhman.mowatib.data.preferences.ThemeConfig
import com.abdulrhman.mowatib.data.preferences.UserPreferences
import com.abdulrhman.mowatib.data.preferences.UserPreferencesRepository
import com.abdulrhman.mowatib.data.preferences.detectPreset
import com.abdulrhman.mowatib.data.remote.model.GitHubRelease
import com.abdulrhman.mowatib.ui.components.LanguageSwitchBar
import com.abdulrhman.mowatib.ui.navigation.Screen
import com.abdulrhman.mowatib.util.DiagnosticReportUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    preferencesRepository: UserPreferencesRepository,
    innerPadding: PaddingValues,
    navController: NavController,
    onOpenPermissions: () -> Unit,
    permissionsMissing: Boolean = false
) {
    val preferences by preferencesRepository.userPreferencesFlow.collectAsState(initial = UserPreferences())
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val updateManager = remember { GitHubUpdateManager(context) }
    var checkingForUpdate by remember { mutableStateOf(false) }
    var showUpdateSheet by remember { mutableStateOf(false) }
    var showChangelogSheet by remember { mutableStateOf(false) }
    var latestRelease by remember { mutableStateOf<GitHubRelease?>(null) }
    var allReleases by remember { mutableStateOf<List<GitHubRelease>>(emptyList()) }
    var showWhitelistSheet by remember { mutableStateOf(false) }
    var showExcludeFromTrackingSheet by remember { mutableStateOf(false) }
    var showDiagnosticSheet by remember { mutableStateOf(false) }
    var diagnosticTargetUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var diagnosticDisplayName by remember { mutableStateOf<String?>(null) }
    var diagnosticExporting by remember { mutableStateOf(false) }
    var diagnosticError by remember { mutableStateOf<String?>(null) }
    var diagnosticSuccess by remember { mutableStateOf(false) }
    var diagnosticUnderstood by remember { mutableStateOf(false) }

    val diagnosticFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri ->
            uri?.let {
                diagnosticTargetUri = it
                diagnosticDisplayName = DiagnosticReportUtils.getDisplayName(context, it) ?: "diagnostic.zip"
                diagnosticError = null
                diagnosticSuccess = false
            }
        }
    )

    LaunchedEffect(Unit) {
        if (preferences.checkUpdateOnStart) {
            coroutineScope.launch {
                when (val result = updateManager.checkForUpdates()) {
                    is GitHubUpdateManager.UpdateResult.NewUpdate -> {
                        latestRelease = result.release
                    }
                    else -> {}
                }
            }
        }
    }

    val onCheckForUpdate: () -> Unit = {
        if (!checkingForUpdate) {
            coroutineScope.launch {
                checkingForUpdate = true
                when (val result = updateManager.checkForUpdates()) {
                    is GitHubUpdateManager.UpdateResult.NewUpdate -> {
                        latestRelease = result.release
                        showUpdateSheet = true
                    }
                    is GitHubUpdateManager.UpdateResult.NoUpdate -> {
                        Toast.makeText(context, "Mowatib is up to date!", Toast.LENGTH_SHORT).show()
                    }
                    is GitHubUpdateManager.UpdateResult.Error -> {
                        Toast.makeText(context, "Update check failed: ${result.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                checkingForUpdate = false
            }
        }
    }

    val onViewChangelog: () -> Unit = {
        coroutineScope.launch {
            val releases = updateManager.fetchAllReleases()
            if (releases != null) {
                allReleases = releases
                showChangelogSheet = true
            } else {
                Toast.makeText(context, "Failed to fetch changelog", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 100.dp
            )
        ) {
            item {
                FeaturedCarousel(
                    updateAvailable = latestRelease,
                    onUpdateClick = { showUpdateSheet = true },
                    onNavigate = { route ->
                        if (route == Screen.Focus.route || route == Screen.Home.route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            navController.navigate(route)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                val isAr = preferences.appLanguage == "ar"
                LanguageSwitchBar(
                    currentLanguage = preferences.appLanguage,
                    onLanguageSelected = { lang ->
                        coroutineScope.launch {
                            preferencesRepository.setAppLanguage(lang)
                        }
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            item {
                GeneralSettings(
                    preferences = preferences,
                    onSetTarget = { mins -> coroutineScope.launch { preferencesRepository.setScreenTimeTarget(mins) } },
                    onSetEmergencyRecharge = { mins -> coroutineScope.launch { preferencesRepository.setEmergencyRechargeDuration(mins) } },
                    onSetDelayAppDuration = { secs -> coroutineScope.launch { preferencesRepository.setDelayAppDuration(secs) } },
                    onSetDayStartTime = { hour, minute -> coroutineScope.launch { preferencesRepository.setDayStartTime(hour, minute) } },
                    onShowWhitelistSheetChange = { showWhitelistSheet = it },
                    onShowExcludeFromTrackingSheetChange = { showExcludeFromTrackingSheet = it },
                    onOpenPermissions = onOpenPermissions,
                    permissionsMissing = permissionsMissing
                )
            }

            item {
                val isAr = preferences.appLanguage == "ar"
                Spacer(modifier = Modifier.height(16.dp))
                PreferenceCategory(title = if (isAr) "الأقسام" else "Categories")
                
                SettingsActionItem(
                    title = if (isAr) "المميزات" else "Features",
                    summary = if (isAr) "بوابة الوعي، واجهة الجلسات، والحماية والمزيد" else "Mindful Gateway, Session Overlay, Early Kick, and more",
                    onClick = { navController.navigate(Screen.SettingsCategory.createRoute("Features")) },
                    icon = Icons.Outlined.AutoFixHigh,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                SettingsActionItem(
                    title = if (isAr) "المظهر واللغة" else "Appearance",
                    summary = if (isAr) "السمات والخطوط، الألوان المتغيرة، والواجهة" else "Theme, Fonts, Dynamic Colors, and Layout",
                    onClick = { navController.navigate(Screen.SettingsCategory.createRoute("Appearance")) },
                    icon = Icons.Outlined.Palette,
                    shape = RoundedCornerShape(8.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                val perfPreset = preferences.buildPerformanceConfig().detectPreset()
                SettingsActionItem(
                    title = if (isAr) "الأداء والبطارية" else "Performance",
                    summary = if (isAr) "سرعة الاستجابة وتوفير طاقة البطارية" else "Responsiveness & battery balance",
                    onClick = { navController.navigate(Screen.SettingsCategory.createRoute("Performance")) },
                    icon = Icons.Outlined.Timer,
                    shape = RoundedCornerShape(8.dp),
                    badge = {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = CircleShape
                        ) {
                            Text(
                                text = perfPreset.localizedLabel(isAr),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))
                SettingsActionItem(
                    title = if (isAr) "إدارة البيانات" else "Data Management",
                    summary = if (isAr) "النسخ الاحتياطي، الاستعادة، ومزامنة الاستخدام" else "Backup, Restore, and Usage Sync",
                    onClick = { navController.navigate(Screen.SettingsCategory.createRoute("Data Management")) },
                    icon = Icons.Outlined.Storage,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
                SettingsActionItem(
                    title = if (isAr) "مزامنة سحابة مواظب و MCP" else "Mowatib Cloud & MCP Sync",
                    summary = if (isAr) "ربط التطبيق مع خادم المزامنة و MCP عبر معرف مواظب" else "Sync tasks and habits with Mowatib Cloud & MCP via your ID",
                    onClick = { navController.navigate(Screen.SettingsCategory.createRoute("sync")) },
                    icon = Icons.Outlined.CloudSync,
                    shape = RoundedCornerShape(
                        topStart = 8.dp, topEnd = 8.dp,
                        bottomStart = if (preferences.developerModeEnabled) 8.dp else 24.dp,
                        bottomEnd = if (preferences.developerModeEnabled) 8.dp else 24.dp
                    )
                )

                if (preferences.developerModeEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    SettingsActionItem(
                        title = if (isAr) "خيارات المطور" else "Developer",
                        summary = if (isAr) "سجلات التشخيص واختبارات النظام" else "System diagnostics, logs, and testing",
                        onClick = { navController.navigate(Screen.SettingsCategory.createRoute("developer")) },
                        icon = Icons.Outlined.Build,
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                AboutSettings(
                    developerModeEnabled = preferences.developerModeEnabled,
                    onDeveloperModeChange = { enabled -> coroutineScope.launch { preferencesRepository.setDeveloperModeEnabled(enabled) } },
                    checkUpdateOnStart = preferences.checkUpdateOnStart,
                    onCheckUpdateOnStartChange = { enabled -> coroutineScope.launch { preferencesRepository.setCheckUpdateOnStart(enabled) } },
                    isCheckingForUpdate = checkingForUpdate,
                    onCheckForUpdate = onCheckForUpdate,
                    onViewChangelog = onViewChangelog,
                    onExportDiagnostics = {
                        diagnosticError = null
                        diagnosticSuccess = false
                        diagnosticExporting = false
                        showDiagnosticSheet = true
                    }
                )
            }
        }
    }

    if (showUpdateSheet && latestRelease != null) {
        val isDark = when (preferences.themeConfig) {
            ThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
            ThemeConfig.LIGHT -> false
            ThemeConfig.DARK -> true
        }
        com.abdulrhman.mowatib.ui.components.UpdateBottomSheet(
            release = latestRelease!!,
            useExpressiveColors = preferences.expressiveColors,
            isDark = isDark,
            onDismiss = { showUpdateSheet = false },
            onUpdate = {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(latestRelease!!.htmlUrl))
                context.startActivity(intent)
                showUpdateSheet = false
            }
        )
    }

    if (showChangelogSheet && allReleases.isNotEmpty()) {
        val isDark = when (preferences.themeConfig) {
            ThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
            ThemeConfig.LIGHT -> false
            ThemeConfig.DARK -> true
        }
        com.abdulrhman.mowatib.ui.components.ChangelogBottomSheet(
            releases = allReleases,
            useExpressiveColors = preferences.expressiveColors,
            isDark = isDark,
            onDismiss = { showChangelogSheet = false }
        )
    }

    if (showWhitelistSheet) {
        WhitelistBottomSheet(
            initialWhitelisted = preferences.whitelistedPackages,
            onDismiss = { showWhitelistSheet = false },
            onSave = { packages ->
                coroutineScope.launch {
                    preferencesRepository.setWhitelistedPackages(packages)
                }
                showWhitelistSheet = false
            }
        )
    }

    if (showExcludeFromTrackingSheet) {
        ExcludeFromTrackingBottomSheet(
            initialExcluded = preferences.excludedFromTrackingPackages,
            onDismiss = { showExcludeFromTrackingSheet = false },
            onSave = { packages ->
                coroutineScope.launch {
                    preferencesRepository.setExcludedFromTrackingPackages(packages)
                }
                showExcludeFromTrackingSheet = false
            }
        )
    }

    if (showDiagnosticSheet) {
        DiagnosticExportBottomSheet(
            targetDisplayName = diagnosticDisplayName,
            isExporting = diagnosticExporting,
            exportError = diagnosticError,
            exportSuccess = diagnosticSuccess,
            understandChecked = diagnosticUnderstood,
            onUnderstandChange = { diagnosticUnderstood = it },
            onPickFile = { diagnosticFileLauncher.launch(DiagnosticReportUtils.defaultFileName()) },
            onExport = {
                val target = diagnosticTargetUri ?: return@DiagnosticExportBottomSheet
                if (diagnosticExporting || !diagnosticUnderstood) return@DiagnosticExportBottomSheet
                coroutineScope.launch {
                    diagnosticExporting = true
                    diagnosticError = null
                    try {
                        val prefs = preferencesRepository.userPreferencesFlow.first()
                        DiagnosticReportUtils.export(context, target, prefs, userConfirmed = diagnosticUnderstood)
                            .onSuccess {
                                diagnosticExporting = false
                                diagnosticSuccess = true
                            }
                            .onFailure { e ->
                                diagnosticExporting = false
                                diagnosticError = e.message ?: "Export failed"
                                Toast.makeText(context, "Export failed. Please try again.", Toast.LENGTH_LONG).show()
                            }
                    } catch (e: Exception) {
                        diagnosticExporting = false
                        diagnosticError = e.message ?: "Export failed"
                    }
                }
            },
            onOpenGitHub = {
                val intent = android.content.Intent(
                    android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse(DiagnosticReportUtils.GITHUB_ISSUES_URL)
                )
                context.startActivity(intent)
            },
            onDismiss = {
                if (!diagnosticExporting) showDiagnosticSheet = false
            }
        )
    }
}
