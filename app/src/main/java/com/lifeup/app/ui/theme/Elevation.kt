package com.lifeup.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 elevation token system for LifeUp.
 *
 * Use these tokens instead of raw Dp values to ensure a consistent,
 * premium visual hierarchy across all screens.
 *
 *  Level 0  — flat surfaces (e.g. primary content cards on the same elevation)
 *  Level 1  — minor elevation (chips, small cards)
 *  Level 2  — default cards
 *  Level 3  — emphasized cards / dialogs
 *  Level 4  — navigation, modal sheets
 *  Level 5  — tooltips, popovers, full-screen overlays
 */
@Immutable
data class LifeUpElevation(
    val level0: Dp = 0.dp,
    val level1: Dp = 2.dp,
    val level2: Dp = 6.dp,
    val level3: Dp = 12.dp,
    val level4: Dp = 20.dp,
    val level5: Dp = 32.dp
)

val LocalLifeUpElevation = compositionLocalOf { LifeUpElevation() }

object LifeUpElevationTokens {
    val level0: Dp = 0.dp
    val level1: Dp = 2.dp
    val level2: Dp = 6.dp
    val level3: Dp = 12.dp
    val level4: Dp = 20.dp
    val level5: Dp = 32.dp
}

/**
 * Access the current [LifeUpElevation] inside a composable.
 */
val MaterialTheme.elevation: LifeUpElevation
    @Composable
    @ReadOnlyComposable
    get() = LocalLifeUpElevation.current
