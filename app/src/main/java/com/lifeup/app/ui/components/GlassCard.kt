package com.lifeup.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Glassmorphism card.
 *
 * Renders a frosted-glass card with:
 *  - a translucent white/black fill
 *  - a subtle vertical highlight
 *  - a 1dp hairline border
 *  - a soft elevation shadow
 *
 * Use over MeshGradientBackground or any colorful backdrop to get the
 * premium "iOS/macOS 26 / Material 3 expressive" look.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    tint: Color = Color.White.copy(alpha = 0.18f),
    borderColor: Color = Color.White.copy(alpha = 0.30f),
    content: @Composable () -> Unit
) {
    val highlight = Color.White.copy(alpha = 0.10f)
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(highlight, tint)
                )
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape
            )
            .padding(padding)
    ) {
        content()
    }
}

/**
 * GlassCard tuned for dark content (e.g. over a light mesh background).
 */
@Composable
fun GlassCardDark(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        padding = padding,
        tint = Color.Black.copy(alpha = 0.18f),
        borderColor = Color.Black.copy(alpha = 0.30f),
        content = content
    )
}

/**
 * Subtle elevated card with theme-aware tint and a hairline border.
 * Drop-in replacement for plain `Card` when you need more polish.
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cs = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        cs.surface,
                        cs.surfaceVariant.copy(alpha = 0.45f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = cs.outlineVariant.copy(alpha = 0.4f),
                shape = shape
            )
            .padding(padding)
    ) {
        content()
    }
}
