package com.lifeup.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Premium level-up animation with:
 * - Light rays rotating in the background
 * - Particle burst
 * - Screen flash effect
 * - Spring-physics scale entry
 *
 * Inspired by Duolingo's level-up screen and game-style RPG celebrations.
 */
@Composable
fun PremiumLevelUpAnimation(
    newLevel: Int,
    modifier: Modifier = Modifier,
    onComplete: () -> Unit = {}
) {
    var startAnimation by remember { mutableStateOf(false) }

    // Scale spring entry
    val scale = remember { Animatable(0.3f) }
    val titleAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val flashAlpha = remember { Animatable(0f) }
    val particles = remember { generateParticles(20) }

    val infiniteTransition = rememberInfiniteTransition(label = "levelUpRays")
    val rayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rayRotation"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        flashAlpha.animateTo(0.6f, tween(150))
        flashAlpha.animateTo(0f, tween(400))
        scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        titleAlpha.animateTo(1f, tween(300))
        delay(300)
        subtitleAlpha.animateTo(1f, tween(300))
        delay(1500)
        onComplete()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background screen flash
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(flashAlpha.value)
                .background(Color(0xFFFFD700))
        )

        // Rotating light rays
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.4f)
        ) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val maxRadius = size.minDimension * 0.6f
            rotate(degrees = rayRotation, pivot = Offset(centerX, centerY)) {
                for (i in 0 until 8) {
                    val angle = (i * 45f) * (Math.PI / 180f).toFloat()
                    val endX = centerX + cos(angle) * maxRadius
                    val endY = centerY + sin(angle) * maxRadius
                    drawLine(
                        color = Color(0xFFFFB300).copy(alpha = 0.5f),
                        start = Offset(centerX, centerY),
                        end = Offset(endX, endY),
                        strokeWidth = 8f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 60f), 0f)
                    )
                }
            }
        }

        // Particle burst
        particles.forEach { particle ->
            val particleAlpha = particle.alpha * (1f - particle.progress.value)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(particleAlpha)
            ) {
                val px = center.x + cos(particle.angle) * particle.distance.value
                val py = center.y + sin(particle.angle) * particle.distance.value
                drawCircle(
                    color = particle.color,
                    radius = particle.radius,
                    center = Offset(px, py)
                )
            }
        }

        // Central content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(scale.value)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = 0.4f),
                                Color(0xFFFFB300).copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎉",
                    fontSize = 80.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "LEVEL UP!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB300),
                modifier = Modifier.alpha(titleAlpha.value)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Lv.$newLevel",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "✨ 继续加油 ✨",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
        }
    }
}

private data class Particle(
    val angle: Float,
    val color: Color,
    val radius: Float,
    val alpha: Float,
    val distance: Animatable<Float, *> = Animatable(0f),
    val progress: Animatable<Float, *> = Animatable(0f)
)

private fun generateParticles(count: Int): List<Particle> {
    val colors = listOf(
        Color(0xFFFFD700),
        Color(0xFFFFB300),
        Color(0xFFFF8A50),
        Color(0xFFFFC107),
        Color(0xFFFFEB3B)
    )
    return List(count) {
        Particle(
            angle = (it * (360f / count) + Random.nextFloat() * 20f) * (Math.PI / 180f).toFloat(),
            color = colors[Random.nextInt(colors.size)],
            radius = Random.nextFloat() * 4f + 2f,
            alpha = Random.nextFloat() * 0.5f + 0.5f
        )
    }
}
