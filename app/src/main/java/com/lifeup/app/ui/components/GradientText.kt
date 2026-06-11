package com.lifeup.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/**
 * Text rendered with an animated linear gradient brush.
 * The gradient sweeps across the text continuously to create a subtle
 * "shimmer" effect — used for the brand wordmark, level-up banners, and
 * premium labels.
 */
@Composable
fun GradientText(
    text: String,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFF006B5E),
        Color(0xFF5EDFC0),
        Color(0xFFFF8A50)
    ),
    style: TextStyle = LocalTextStyle.current,
    animated: Boolean = true,
    speedMs: Int = 3200,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
    softWrap: Boolean = true,
    onTextLayout: (androidx.compose.ui.text.TextLayoutResult) -> Unit = {}
) {
    val transition = rememberInfiniteTransition(label = "gradient")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(speedMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "p"
    )
    val brush = if (animated) {
        SweepingGradient(colors = colors, progress = progress)
    } else {
        Brush.linearGradient(colors)
    }
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(brush = brush),
        overflow = overflow,
        maxLines = maxLines,
        softWrap = softWrap,
        onTextLayout = onTextLayout
    )
}

/**
 * Brush that sweeps a 3-color gradient across text.
 * Uses a linear gradient that moves from -1 to +1 in x.
 */
private class SweepingGradient(
    private val colors: List<Color>,
    private val progress: Float
) : ShaderBrush() {
    override fun createShader(size: androidx.compose.ui.geometry.Size): Shader {
        // Sweep from 2x left of the text to 2x right, looping
        val width = size.width
        val sweep = width * 1.5f
        val startX = -sweep + progress * (width + 2 * sweep)
        val endX = startX + sweep
        return Brush.linearGradient(
            colors = colors,
            start = androidx.compose.ui.geometry.Offset(startX, 0f),
            end = androidx.compose.ui.geometry.Offset(endX, 0f)
        ).createShader(size)
    }
}

/**
 * Static gradient text (no animation) — slightly cheaper.
 */
@Composable
fun StaticGradientText(
    text: String,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(Color(0xFF006B5E), Color(0xFF5EDFC0)),
    style: TextStyle = LocalTextStyle.current
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(brush = Brush.linearGradient(colors))
    )
}
