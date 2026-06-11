package com.lifeup.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Circular energy ring with animated progress.
 * Inspired by Apple Health's activity rings and Strava's effort rings.
 */
@Composable
fun EnergyRing(
    current: Float,
    cap: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    centerText: String? = null,
    centerSubText: String? = null
) {
    val progress = if (cap > 0f) (current / cap).coerceIn(0f, 1f) else 0f

    // Smooth animated progress
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    // Pulse for low energy
    val isLow = progress < 0.2f
    val infiniteTransition = rememberInfiniteTransition(label = "ringPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Color based on energy level
    val ringColor = when {
        progress < 0.2f -> Color(0xFFFF5252)
        progress < 0.4f -> Color(0xFFFF9800)
        else -> Color(0xFFFFB300)
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val diameter = this.size.minDimension - strokeWidthPx
            val topLeft = Offset(
                (this.size.width - diameter) / 2,
                (this.size.height - diameter) / 2
            )
            val arcSize = Size(diameter, diameter)

            // Background ring
            drawArc(
                color = Color.Black.copy(alpha = 0.06f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Progress arc with gradient
            val sweep = 360f * animatedProgress.value
            if (sweep > 0f) {
                // Glow for low energy
                if (isLow) {
                    drawArc(
                        color = ringColor.copy(alpha = pulseAlpha * 0.3f),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx + 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                // Main gradient arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            ringColor.copy(alpha = 0.7f),
                            ringColor,
                            ringColor.copy(alpha = 0.9f),
                            ringColor.copy(alpha = 0.7f)
                        ),
                        center = Offset(this.size.width / 2, this.size.height / 2)
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }

        // Center text
        if (centerText != null) {
            Text(
                text = centerText,
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = ringColor
                )
            )
        }
        if (centerSubText != null) {
            Text(
                text = centerSubText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            )
        }
    }
}
