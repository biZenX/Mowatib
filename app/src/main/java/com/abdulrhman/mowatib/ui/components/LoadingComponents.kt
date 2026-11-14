package com.abdulrhman.mowatib.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MowatibLoadingIndicator(
    modifier: Modifier = Modifier
) {
    val indicatorColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.onPrimaryContainer,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "MowatibLoadingIndicatorColor"
    )
    
    LoadingIndicator(
        modifier = modifier,
        color = indicatorColor
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MowatibContainedLoadingIndicator(
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.primary,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "MowatibContainedLoadingContainerColor"
    )
    val indicatorColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.onPrimary,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "MowatibContainedLoadingIndicatorColor"
    )

    ContainedLoadingIndicator(
        modifier = modifier,
        containerColor = containerColor,
        indicatorColor = indicatorColor
    )
}
