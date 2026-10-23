package com.etrisad.zenith.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.etrisad.zenith.ui.viewmodel.ShieldSortType

@Composable
fun ShieldSortHeader(
    title: String,
    currentSortType: ShieldSortType,
    onSortTypeChange: (ShieldSortType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        ZenithToggleButtonGroup(
            modifier = Modifier.width(140.dp),
            size = ZenithButtonSize.Medium,
            options = listOf(
                ZenithToggleOption(
                    icon = if (currentSortType == ShieldSortType.ALPHABETICAL) Icons.Filled.SortByAlpha else Icons.Outlined.SortByAlpha,
                ),
                ZenithToggleOption(
                    icon = if (currentSortType == ShieldSortType.REMAINING_TIME) Icons.Filled.Timer else Icons.Outlined.Timer,
                )
            ),
            selectedIndices = setOf(if (currentSortType == ShieldSortType.ALPHABETICAL) 0 else 1),
            onToggle = { index ->
                onSortTypeChange(if (index == 0) ShieldSortType.ALPHABETICAL else ShieldSortType.REMAINING_TIME)
            }
        )
    }
}
