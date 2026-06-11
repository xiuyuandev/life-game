package com.lifeup.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.lifeup.app.ui.theme.SecondaryOrange
import com.lifeup.app.ui.theme.TertiaryRoseGold
import kotlin.math.cos
import kotlin.math.sin

/**
 * Material 3 expressive / iOS 18 style mesh gradient background.
 *
 * The component renders 4-6 large radial blobs that drift in slow circular
 * orbits and overlap additively to produce a soft mesh effect.
 *
 * Colors default to the LifeUp brand palette but can be customized.
 */
@Composable
fun MeshGradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFF5EDFC0),
        Color(0xFF006B5E),
        SecondaryOrange,
        TertiaryRoseGold,
        Color(0xFF7E57C2)
    ),
    speedSeconds: Int = 24
) {
    val transition = rememberInfiniteTransition(label = "mesh")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(speedSeconds * 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t"
    )
    val t2 by transition.animateFloat(
        initialValue = 180f,
        targetValue = 540f,
        animationSpec = infiniteRepeatable(
            animation = tween((speedSeconds * 1.3f).toInt() * 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t2"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val palette = colors.ifEmpty {
            listOf(Color(0xFF5EDFC0), Color(0xFF006B5E))
        }
        val orbitR = w * 0.30f

        // 5 blobs in different orbits
        for (i in palette.indices) {
            val angleBase = i * (360f / palette.size)
            val angleA = (t + angleBase) * (Math.PI / 180f)
            val angleB = (t2 + angleBase * 1.4f) * (Math.PI / 180f)
            val bx = cx + orbitR * cos(angleA).toFloat()
            val by = cy + orbitR * 0.7f * sin(angleB).toFloat()
            val radius = w * 0.65f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        palette[i].copy(alpha = 0.55f),
                        palette[i].copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(bx, by),
                    radius = radius
                ),
                radius = radius,
                center = Offset(bx, by),
                blendMode = BlendMode.Plus
            )
        }
    }
}

/**
 * Static (non-animated) mesh background for performance-sensitive areas.
 */
@Composable
fun StaticMeshGradientBackground(
    modifier: Modifier = Modifier,
    primary: Color = Color(0xFF5EDFC0),
    secondary: Color = Color(0xFF006B5E),
    accent: Color = SecondaryOrange
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Diagonal sweep
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(0f, 0f),
                radius = w * 0.9f
            ),
            radius = w * 0.9f,
            center = Offset(0f, 0f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(secondary.copy(alpha = 0.55f), Color.Transparent),
                center = Offset(w, h),
                radius = w * 0.9f
            ),
            radius = w * 0.9f,
            center = Offset(w, h)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.30f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.7f
            ),
            radius = w * 0.7f,
            center = Offset(w * 0.5f, h * 0.5f)
        )
    }
}

/**
 * Subtle topographic background pattern — thin curved lines that don't compete
 * with foreground content. Inspired by Apple Health and Strava.
 */
@Composable
fun TopographicBackground(
    modifier: Modifier = Modifier,
    color: Color = Color.Black.copy(alpha = 0.04f),
    amplitude: Float = 0.04f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val lines = 18
        for (i in 0 until lines) {
            val phase = i * 0.6f
            val path = androidx.compose.ui.graphics.Path()
            val yBase = h * (i + 1) / (lines + 1f)
            path.moveTo(0f, yBase)
            var x = 0f
            while (x <= w) {
                val y = yBase + (sin((x / w) * 6.28f * 2f + phase) * h * amplitude).toFloat()
                path.lineTo(x, y)
                x += 8f
            }
            drawPath(
                path = path,
                color = color,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
        }
    }
}
