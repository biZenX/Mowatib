package com.abdulrhman.mowatib.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.abdulrhman.mowatib.data.preferences.FontOption
import com.abdulrhman.mowatib.data.preferences.ThemeConfig
import com.abdulrhman.mowatib.data.preferences.UserPreferences
import com.abdulrhman.mowatib.ui.components.LanguageSwitchBar

@Composable
fun AppearanceSettings(
    preferences: UserPreferences,
    onThemeChange: (ThemeConfig) -> Unit,
    onFontChange: (FontOption) -> Unit,
    onLanguageChange: (String) -> Unit = {},
    onDynamicColorChange: (Boolean) -> Unit,
    onExpressiveColorsChange: (Boolean) -> Unit,
    onFloatingTabBarEnabledChange: (Boolean) -> Unit,
    onHeaderInfoButtonEnabledChange: (Boolean) -> Unit,
    onNavigateToGSFlexCustomizer: () -> Unit,
    onNavigateToOverlayAppearance: () -> Unit
) {
    val isAr = preferences.appLanguage == "ar"
    Column {
        PreferenceCategory(title = if (isAr) "اللغة والمظهر" else "Language & Theme")

        LanguageSwitchBar(
            currentLanguage = preferences.appLanguage,
            onLanguageSelected = onLanguageChange,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ThemeSelector(
            selectedTheme = preferences.themeConfig,
            onThemeChange = onThemeChange,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        FontSelector(
            selectedFont = preferences.fontOption,
            onFontChange = onFontChange,
            onCustomizeGSFlex = onNavigateToGSFlexCustomizer,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsToggle(
            title = if (isAr) "الألوان الديناميكية" else "Dynamic Color",
            description = if (isAr) "تطبيق ألوان خلفية النظام (أندرويد 12+)" else "Apply system wallpaper colors (Android 12+)",
            checked = preferences.dynamicColor,
            onCheckedChange = onDynamicColorChange,
            icon = Icons.Outlined.Palette,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsToggle(
            title = if (isAr) "مجموعة ألوان تعبيرية" else "Expressive Color Set",
            description = if (isAr) "تهدئة الخلفيات وإبراز الحاويات" else "Tone down backgrounds and make containers standout",
            checked = preferences.expressiveColors,
            onCheckedChange = onExpressiveColorsChange,
            icon = Icons.Outlined.Layers,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceCategory(title = if (isAr) "التخطيط والتنقل" else "Layout & Navigation")

        SettingsToggle(
            title = if (isAr) "شريط تبويب عائم" else "Floating Tab Bar",
            description = if (isAr) "استخدام شريط التنقل العائم المعبّر" else "Use the new Material 3 Expressive floating navigation",
            checked = preferences.floatingTabBarEnabled,
            onCheckedChange = onFloatingTabBarEnabledChange,
            icon = Icons.Outlined.Flaky,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsToggle(
            title = if (isAr) "زر معلومات العنوان" else "Header Info Button",
            description = if (isAr) "إظهار زر المعلومات في الشريط العلوي للمساعدة" else "Show the info button in the top bar for help about each screen",
            checked = preferences.headerInfoButtonEnabled,
            onCheckedChange = onHeaderInfoButtonEnabledChange,
            icon = Icons.Outlined.Info,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        SettingsActionItem(
            title = if (isAr) "مظهر شاشة الحظر" else "Overlay Appearance",
            summary = if (isAr) "تخصيص ألوان وشفافية وحجم شاشة الحظر" else "Customize bottom sheet colors, opacity, and sizing",
            onClick = onNavigateToOverlayAppearance,
            icon = Icons.Outlined.Style,
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        )
    }
}
