package com.etrisad.zenith.ui.components.onboarding

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
import com.etrisad.zenith.data.preferences.UserPreferencesRepository
import com.etrisad.zenith.ui.components.ZenithButton
import com.etrisad.zenith.ui.components.ZenithButtonType
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
    onSelection: (Boolean) -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.BarChart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Statistic Experience",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Choose how you want to see your usage history starting from today.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        OptionCard(
            title = "Start Fresh (Recommended)",
            description = "Only show data recorded by Zenith. Previous days will appear empty.",
            pros = listOf("Accurate Zenith tracking", "Clean visualization"),
            cons = listOf("No history for past days", "Needs more usage for better stats"),
            icon = Icons.Outlined.AutoAwesome,
            isHighlighted = true,
            onClick = { onSelection(false) },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 8.dp, bottomEnd = 8.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        OptionCard(
            title = "Populate with System Data",
            description = "Use Android system usage to fill history for previous days.",
            pros = listOf("Instant historical data", "Complete overview"),
            cons = listOf("Might not be accurate", "May include background usage", "Less precise than Zenith"),
            icon = Icons.Outlined.History,
            isHighlighted = false,
            onClick = { onSelection(true) },
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        ZenithButton(
            onClick = {},
            onHoldComplete = onSkip,
            text = "Hold to Skip",
            type = ZenithButtonType.Hold,
            fillMaxWidth = true,
            holdDuration = 2000L
        )
    }
}
