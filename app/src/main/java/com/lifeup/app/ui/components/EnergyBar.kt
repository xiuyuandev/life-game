package com.lifeup.app.ui.components

import androidx.compose.animation.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifeup.app.ui.theme.EnergyAmber
import com.lifeup.app.ui.theme.EnergyAmberDark

@Composable
fun EnergyBar(
    current: Float,
    cap: Float,
    modifier: Modifier = Modifier
) {
    val progress = if (cap > 0f) (current / cap).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "energyProgress"
    )

    // Pulse animation when energy is low
    val isLow = progress < 0.25f
    val infiniteTransition = rememberInfiniteTransition(label = "energyPulse")
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
    val barColor = when {
        progress < 0.2f -> Color(0xFFFF5252)
        progress < 0.4f -> Color(0xFFFF9800)
        else -> EnergyAmber
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "能量条: ${current.toInt()}/${cap.toInt()}"
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ 能量",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = barColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${current.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
            Text(
                text = " / ${cap.toInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Custom canvas-based progress bar with gradient and glow
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        ) {
            val cornerRadius = CornerRadius(5.dp.toPx())
            val barWidth = size.width
            val barHeight = size.height

            // Track background
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.08f),
                cornerRadius = cornerRadius
            )

            // Filled bar
            val fillWidth = barWidth * animatedProgress
            if (fillWidth > 0f) {
                // Main gradient fill
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            barColor.copy(alpha = 0.7f),
                            barColor,
                            barColor.copy(alpha = 0.85f)
                        ),
                        startX = 0f,
                        endX = fillWidth
                    ),
                    topLeft = Offset.Zero,
                    size = Size(fillWidth, barHeight),
                    cornerRadius = cornerRadius
                )

                // Shine highlight on top half
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.05f)
                        ),
                        startY = 0f,
                        endY = barHeight * 0.5f
                    ),
                    topLeft = Offset.Zero,
                    size = Size(fillWidth, barHeight * 0.5f),
                    cornerRadius = cornerRadius
                )

                // Glow effect at the leading edge
                if (animatedProgress > 0.02f) {
                    val glowWidth = 12.dp.toPx()
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                barColor.copy(alpha = if (isLow) pulseAlpha else 0.6f)
                            ),
                            startX = (fillWidth - glowWidth).coerceAtLeast(0f),
                            endX = fillWidth
                        ),
                        topLeft = Offset((fillWidth - glowWidth).coerceAtLeast(0f), 0f),
                        size = Size(glowWidth, barHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }
        }
    }
}
