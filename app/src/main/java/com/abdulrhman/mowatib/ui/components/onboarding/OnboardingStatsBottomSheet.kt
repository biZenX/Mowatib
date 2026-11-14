package com.abdulrhman.mowatib.ui.components.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abdulrhman.mowatib.data.preferences.UserPreferencesRepository
import com.abdulrhman.mowatib.ui.components.MowatibButton
import com.abdulrhman.mowatib.ui.components.MowatibButtonType
import com.abdulrhman.mowatib.ui.components.LanguageSwitchBar
import com.abdulrhman.mowatib.ui.theme.LocalAppLanguage
import com.abdulrhman.mowatib.ui.theme.LocalIsArabic
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingStatsBottomSheet(
    repository: UserPreferencesRepository,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        OnboardingStatsContent(
            onLanguageSelected = { lang ->
                scope.launch {
                    repository.setAppLanguage(lang)
                }
            },
            onSelection = { useSystemUsage ->
                scope.launch {
                    repository.setPreferSystemUsageHistory(useSystemUsage)
                    repository.setOnboardingStatsCompleted(true)
                    sheetState.hide()
                    onDismiss()
                }
            },
            onSkip = {
                scope.launch {
                    repository.setOnboardingStatsCompleted(true)
                    sheetState.hide()
                    onDismiss()
                }
            }
        )
    }
}

@Composable
fun OnboardingStatsContent(
    onLanguageSelected: (String) -> Unit = {},
    onSelection: (Boolean) -> Unit,
    onSkip: () -> Unit
) {
    val isAr = LocalIsArabic.current
    val currentLang = LocalAppLanguage.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LanguageSwitchBar(
            currentLanguage = currentLang,
            onLanguageSelected = onLanguageSelected
        )
        Spacer(modifier = Modifier.height(20.dp))

        Icon(
            Icons.Outlined.BarChart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isAr) "تجربة الإحصائيات" else "Statistic Experience",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (isAr) "اختر طريقة عرض بيانات وسجلات الاستخدام بدءاً من اليوم." else "Choose how you want to see your usage history starting from today.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        OptionCard(
            title = if (isAr) "البدء من الصفر (موصى به)" else "Start Fresh (Recommended)",
            description = if (isAr) "عرض البيانات المسجلة عبر مواظب فقط؛ واجهة نظيفة ومتقنة." else "Only show data recorded by Mowatib. Previous days will appear empty.",
            pros = if (isAr) listOf("تتبع دقيق وفوري", "واجهة نظيفة وخالية من الشوائب") else listOf("Accurate Mowatib tracking", "Clean visualization"),
            cons = if (isAr) listOf("لا توجد بيانات للأيام السابقة") else listOf("No history for past days", "Needs more usage for better stats"),
            icon = Icons.Outlined.AutoAwesome,
            isHighlighted = true,
            onClick = { onSelection(false) },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        OptionCard(
            title = if (isAr) "استيراد بيانات النظام" else "Populate with System Data",
            description = if (isAr) "استخدام سجلات نظام أندرويد لملء الأيام السابقة فورياً." else "Use Android system usage to fill history for previous days.",
            pros = if (isAr) listOf("بيانات تاريخية فورية", "نظرة شاملة منذ اليوم الأول") else listOf("Instant historical data", "Complete overview"),
            cons = if (isAr) listOf("قد تشمل عمليات الخلفية", "أقل دقة من عداد مواظب") else listOf("Might not be accurate", "May include background usage", "Less precise than Mowatib"),
            icon = Icons.Outlined.History,
            isHighlighted = false,
            onClick = { onSelection(true) },
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        MowatibButton(
            onClick = {},
            onHoldComplete = onSkip,
            text = if (isAr) "اضغط مطولاً للتخطي" else "Hold to Skip",
            type = MowatibButtonType.Hold,
            fillMaxWidth = true,
            holdDuration = 2000L
        )
    }
}
