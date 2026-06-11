package com.lifeup.app.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lifeup.app.ui.components.GradientText
import com.lifeup.app.ui.components.MeshGradientBackground
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Brand splash screen — full-screen animated experience shown while the
 * app boots. Combines:
 *  - Mesh gradient background (iOS 18 / Material 3 expressive style)
 *  - Animated brand mark with rays + particles
 *  - Sweeping gradient wordmark
 *  - Subtle progress dots
 */
@Composable
fun BrandSplashScreen(
    modifier: Modifier = Modifier,
    onReady: () -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }
    val raysRotation = remember { Animatable(0f) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val dotsAlpha = remember { Animatable(0f) }
    val haloPulse = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Halo pulse
        haloPulse.animateTo(1f, tween(600, easing = LinearEasing))
        // 2. Logo in
        logoAlpha.animateTo(1f, tween(400))
        logoScale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
        // 3. Wordmark + tagline + dots stagger
        delay(150)
        wordmarkAlpha.animateTo(1f, tween(500))
        delay(120)
        taglineAlpha.animateTo(1f, tween(500))
        delay(120)
        dotsAlpha.animateTo(1f, tween(400))
        // 4. Hold, then call onReady
        delay(1600)
        onReady()
    }
    // Continuous rays rotation
    val infinite = rememberInfiniteTransition(label = "rays")
    val continuousRays by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "raysRot"
    )

    Box(modifier = modifier.fillMaxSize().background(cs.background)) {
        // Mesh background
        MeshGradientBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val w = this.size.width
                    val h = this.size.height
                    val cx = w / 2f
                    val cy = h / 2f

                    // Halo
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF5EDFC0).copy(alpha = 0.45f * haloPulse.value),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = w * 0.55f
                        ),
                        radius = w * 0.55f,
                        center = Offset(cx, cy)
                    )

                    // 8 rays
                    val baseRays = raysRotation.value
                    for (i in 0 until 8) {
                        val angle = ((baseRays + i * 45f) * Math.PI / 180f)
                        val r0 = w * 0.30f
                        val r1 = w * 0.48f
                        val sx = cx + r0 * cos(angle).toFloat()
                        val sy = cy + r0 * sin(angle).toFloat()
                        val ex = cx + r1 * cos(angle).toFloat()
                        val ey = cy + r1 * sin(angle).toFloat()
                        drawLine(
                            color = if (i % 2 == 0) Color(0xFF5EDFC0) else Color(0xFFFF8A50),
                            start = Offset(sx, sy),
                            end = Offset(ex, ey),
                            strokeWidth = w * 0.018f
                        )
                    }

                    // Outer ring
                    drawCircle(
                        color = Color(0xFF5EDFC0).copy(alpha = 0.5f),
                        radius = w * 0.30f,
                        center = Offset(cx, cy),
                        style = Stroke(width = w * 0.014f)
                    )
                    drawCircle(
                        color = Color(0xFFFF8A50).copy(alpha = 0.35f),
                        radius = w * 0.36f,
                        center = Offset(cx, cy),
                        style = Stroke(width = w * 0.008f)
                    )

                    // Star core
                    val starR = w * 0.18f
                    val starPath = Path().apply {
                        for (i in 0 until 5) {
                            val outerA = (-90f + i * 72f) * (Math.PI / 180f)
                            val innerA = (-90f + 36f + i * 72f) * (Math.PI / 180f)
                            if (i == 0) {
                                moveTo(cx + starR * cos(outerA).toFloat(), cy + starR * sin(outerA).toFloat())
                            } else {
                                lineTo(cx + starR * cos(outerA).toFloat(), cy + starR * sin(outerA).toFloat())
                            }
                            lineTo(
                                cx + starR * 0.45f * cos(innerA).toFloat(),
                                cy + starR * 0.45f * sin(innerA).toFloat()
                            )
                        }
                        close()
                    }
                    drawPath(
                        path = starPath,
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF5EDFC0), Color(0xFF006B5E)),
                            center = Offset(cx, cy),
                            radius = starR
                        )
                    )
                    // Star highlight
                    drawCircle(
                        color = Color.White.copy(alpha = 0.65f),
                        radius = starR * 0.10f,
                        center = Offset(cx - starR * 0.25f, cy - starR * 0.25f)
                    )
                }
                Canvas(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(logoScale.value)
                ) {
                    // Empty — actual star is drawn above so it scales together
                }
            }
            Spacer(Modifier.height(20.dp))
            AnimatedVisibility(
                visible = wordmarkAlpha.value > 0.01f,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                GradientText(
                    text = "LifeUp",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.ExtraBold
                    ),
                    speedMs = 3600
                )
            }
            Spacer(Modifier.height(6.dp))
            AnimatedVisibility(
                visible = taglineAlpha.value > 0.01f,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = "把时间变成成长的轨迹",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Spacer(Modifier.height(36.dp))
            AnimatedVisibility(
                visible = dotsAlpha.value > 0.01f,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                DotRow()
            }
        }
    }
}

@Composable
private fun DotRow() {
    val transition = rememberInfiniteTransition(label = "dots")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dotT"
    )
    Canvas(modifier = Modifier.size(60.dp, 12.dp)) {
        val w = size.width
        val h = size.height
        val r = h * 0.30f
        for (i in 0 until 3) {
            val cx = w * (0.25f + i * 0.25f)
            val cy = h / 2
            val phase = ((t + i * 0.33f) % 1f)
            val active = phase < 0.5f
            drawCircle(
                color = if (active) Color(0xFF5EDFC0) else Color.White.copy(alpha = 0.5f),
                radius = r,
                center = Offset(cx, cy)
            )
        }
    }
}
