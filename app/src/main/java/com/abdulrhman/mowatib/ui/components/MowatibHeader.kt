package com.abdulrhman.mowatib.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.abdulrhman.mowatib.ui.navigation.Screen
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MowatibHeader(
    currentRoute: String?,
    scrollBehavior: TopAppBarScrollBehavior,
    isNavRailVisible: Boolean = false,
    userName: String = "User",
    categoryName: String? = null,
    pausePointTypeName: String? = null,
    onBack: () -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null,
    showInfoButton: Boolean = false,
    onInfoClick: () -> Unit = {},
    infoNextToAction: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val isHome = currentRoute == Screen.Home.route
    val isDeepScreen =
        currentRoute == Screen.UsageStats.route || 
        currentRoute == Screen.Bedtime.route ||
        currentRoute == Screen.GracePeriod.route ||
        currentRoute == Screen.EyeCare.route ||
        currentRoute == Screen.Lockdown.route ||
        currentRoute == Screen.DatabaseDebug.route ||
        currentRoute == Screen.DataRepairment.route ||
        currentRoute == Screen.FontTest.route ||
        currentRoute == Screen.GSFlexCustomizer.route ||
        currentRoute == Screen.SystemUsageDebug.route ||
        currentRoute == Screen.OverlayAppearance.route ||
        currentRoute == Screen.Alarm.route ||
        currentRoute == Screen.Pomodoro.route ||
        currentRoute == Screen.PausePoint.route ||
        currentRoute?.startsWith("pause_point_type") == true ||
        currentRoute?.startsWith("settings_category") == true ||
        currentRoute?.startsWith("app_detail") == true ||
        currentRoute == Screen.Profile.route ||
        currentRoute == Screen.Achievements.route ||
        currentRoute == Screen.Level.route

    val sideSlotWidth = 68.dp

    val smoothedAlpha by animateFloatAsState(
        targetValue = (1f - scrollBehavior.state.collapsedFraction).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "SmoothedHeaderAlpha"
    )
    val smoothedOffset by animateFloatAsState(
        targetValue = scrollBehavior.state.heightOffset,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "SmoothedHeaderOffset"
    )
    val smoothedFraction = 1f - smoothedAlpha

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = smoothedAlpha }
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                )
            )
    ) {
        CenterAlignedTopAppBar(
            scrollBehavior = scrollBehavior,
            windowInsets = WindowInsets.statusBars,
            navigationIcon = {},
            title = {},
            actions = {},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            )
        )

        val titleBias by animateFloatAsState(
            targetValue = if (isDeepScreen) -1f else 0f,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            label = "TitleAlignment"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(64.dp)
                .offset { IntOffset(0, smoothedOffset.roundToInt()) }
                .graphicsLayer {
                    alpha = smoothedAlpha
                    val scale = 1f - (smoothedFraction * 0.08f)
                    scaleX = scale
                    scaleY = scale
                }
                .zIndex(1f),
            contentAlignment = BiasAlignment(horizontalBias = titleBias, verticalBias = 0f)
        ) {
            val titleXOffset by animateDpAsState(
                targetValue = if (isDeepScreen) 80.dp else 0.dp,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "TitleOffset"
            )

            val isAr = com.abdulrhman.mowatib.ui.theme.LocalIsArabic.current
            val title = when {
                isHome -> "home_header"
                currentRoute == Screen.Focus.route -> if (isAr) "التركيز" else "Focus"
                currentRoute == Screen.Settings.route -> if (isAr) "الإعدادات" else "Settings"
                currentRoute == Screen.UsageStats.route -> if (isAr) "إحصائيات الاستخدام" else "Usage Stats"
                currentRoute == Screen.Bedtime.route -> if (isAr) "وقت النوم" else "Bedtime"
                currentRoute == Screen.GracePeriod.route -> if (isAr) "فترة السماح" else "Grace Period"
                currentRoute == Screen.EyeCare.route -> if (isAr) "راحة العين" else "Eye Care"
                currentRoute == Screen.Lockdown.route -> if (isAr) "وضع الإغلاق" else "Lockdown"
                currentRoute == Screen.DatabaseDebug.route -> if (isAr) "سجلات البيانات" else "Database"
                currentRoute == Screen.DataRepairment.route -> if (isAr) "إصلاح البيانات" else "Data Repairment"
                currentRoute == Screen.FontTest.route -> if (isAr) "محرر الخطوط" else "Font Editor"
                currentRoute == Screen.GSFlexCustomizer.route -> if (isAr) "مخصص الخط" else "Font Customizer"
                currentRoute == Screen.SystemUsageDebug.route -> if (isAr) "فحص استخدام النظام" else "System Usage"
                currentRoute == Screen.OverlayAppearance.route -> if (isAr) "مظهر الواجهات" else "Overlay Appearance"
                currentRoute == Screen.Alarm.route -> if (isAr) "المنبه" else "Alarm"
                currentRoute == Screen.Pomodoro.route -> if (isAr) "جلسة التركيز" else "Pomodoro"
                currentRoute == Screen.PausePoint.route -> if (isAr) "نقطة التوقف" else "Pause Point"
                currentRoute == Screen.Profile.route -> if (isAr) "الملف الشخصي" else "Profile"
                currentRoute == Screen.Achievements.route -> if (isAr) "الإنجازات" else "Achievements"
                currentRoute == Screen.Level.route -> if (isAr) "المستوى" else "Level"
                currentRoute?.startsWith("pause_point_type") == true ->
                    pausePointTypeName?.ifEmpty { null } ?: (if (isAr) "نقطة التوقف" else "Pause Point")
                currentRoute?.startsWith("settings_category") == true -> {
                    val raw = categoryName ?: ""
                    when (raw.lowercase()) {
                        "developer" -> if (isAr) "المطور" else "Developer"
                        "features" -> if (isAr) "المميزات" else "Features"
                        "appearance" -> if (isAr) "المظهر" else "Appearance"
                        "performance" -> if (isAr) "الأداء" else "Performance"
                        "data management" -> if (isAr) "إدارة البيانات" else "Data Management"
                        "sync", "mowatib_sync", "telegram" -> if (isAr) "مزامنة سحابة مواظب" else "Mowatib Cloud Sync"
                        else -> raw.replaceFirstChar { if (it.isLowerCase()) it.uppercase() else it.toString() }
                    }.ifEmpty { if (isAr) "الإعدادات" else "Settings" }
                }
                currentRoute?.startsWith("app_detail") == true -> if (isAr) "تفاصيل التطبيق" else "App Detail"
                else -> if (isAr) "مواظب" else "Mowatib"
            }

            AnimatedContent(
                targetState = title,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.92f)).togetherWith(fadeOut() + scaleOut(targetScale = 0.92f))
                },
                label = "HeaderTitleAnimation",
                modifier = Modifier.offset(x = titleXOffset)
            ) { state ->
                if (state == "home_header") {
                    val greeting = remember(isAr) {
                        if (isAr) {
                            listOf(
                                "مرحباً بك مجدداً",
                                "واصل تركيزك",
                                "استمر في التقدم",
                                "حافظ على انضباطك",
                                "أنت قادر على الإنجاز"
                            ).random()
                        } else {
                            listOf(
                                "Welcome Back",
                                "Stay Focused",
                                "Keep Going",
                                "Stay Strong",
                                "You Got This"
                            ).random()
                        }
                    }
                    var stage by remember { mutableStateOf(0) }
                    LaunchedEffect(Unit) {
                        delay(1700)
                        stage = 1
                        delay(1700)
                        stage = 2
                    }

                    AnimatedContent(
                        targetState = stage,
                        transitionSpec = {
                            (fadeIn() + slideInVertically { it / 2 })
                                .togetherWith(fadeOut() + slideOutVertically { -it / 2 })
                        },
                        label = "HomeAppNameAnimation"
                    ) { currentStage ->
                        Text(
                            text = when (currentStage) {
                                0 -> greeting
                                1 -> userName
                                else -> if (isAr) "مواظب" else "Mowatib"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = if (currentStage == 2) FontWeight.SemiBold else FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                } else {
                    Text(
                        text = state,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = if (isDeepScreen) TextAlign.Start else TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset { IntOffset(0, smoothedOffset.roundToInt()) }
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(64.dp)
                .widthIn(min = sideSlotWidth)
                .graphicsLayer {
                    alpha = smoothedAlpha
                    val scale = 1f - (smoothedFraction * 0.12f)
                    scaleX = scale
                    scaleY = scale
                }
                .zIndex(2f),
            contentAlignment = Alignment.CenterStart
        ) {
            navigationIcon?.invoke()

            AnimatedVisibility(
                visible = isDeepScreen,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessLow)) + 
                        scaleIn(initialScale = 0.92f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)) +
                        slideInHorizontally(initialOffsetX = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessLow)),
                exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessLow)) + 
                       scaleOut(targetScale = 0.92f) +
                       slideOutHorizontally(targetOffsetX = { -it / 2 }, animationSpec = spring(stiffness = Spring.StiffnessLow))
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(0, smoothedOffset.roundToInt()) }
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(64.dp)
                .widthIn(min = sideSlotWidth)
                .graphicsLayer {
                    alpha = smoothedAlpha
                    val scale = 1f - (smoothedFraction * 0.12f)
                    scaleX = scale
                    scaleY = scale
                }
                .zIndex(3f),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AnimatedVisibility(
                    visible = showInfoButton,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            scaleIn(
                                initialScale = 0.7f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) +
                            slideInHorizontally(
                                initialOffsetX = { it / 2 },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) +
                            expandHorizontally(
                                expandFrom = Alignment.End,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ),
                    exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            scaleOut(
                                targetScale = 0.7f,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) +
                            slideOutHorizontally(
                                targetOffsetX = { it / 2 },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) +
                            shrinkHorizontally(
                                shrinkTowards = Alignment.End,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                ) {
                IconButton(
                    onClick = onInfoClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Screen info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                }
                actions()
            }
        }
    }
}
