package com.abdulrhman.mowatib.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LanguageSwitchBar(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAr = currentLanguage == "ar"

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isAr) "لغة التطبيق" else "App Language",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Segmented pill
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val arBg by animateColorAsState(
                    targetValue = if (isAr) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "arBg"
                )
                val arText by animateColorAsState(
                    targetValue = if (isAr) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "arText"
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(arBg)
                        .clickable { onLanguageSelected("ar") }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "العربية",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isAr) FontWeight.Bold else FontWeight.Normal,
                        color = arText,
                        fontSize = 13.sp
                    )
                }

                val enBg by animateColorAsState(
                    targetValue = if (!isAr) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "enBg"
                )
                val enText by animateColorAsState(
                    targetValue = if (!isAr) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "enText"
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(enBg)
                        .clickable { onLanguageSelected("en") }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "English",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (!isAr) FontWeight.Bold else FontWeight.Normal,
                        color = enText,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
