package com.lifeup.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

@Composable
fun StreakFlame(
    streakCount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "flame")
    
    val flameHeight by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameHeight"
    )
    
    val flameShift by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameShift"
    )
    
    Box(modifier = modifier.size(24.dp)) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val baseWidth = size.width * 0.6f
            val baseHeight = size.height * flameHeight
            val shift = flameShift
            
            // Outer flame
            drawCircle(
                color = Color(0xFFFF6D00).copy(alpha = 0.3f),
                radius = baseWidth * 0.8f,
                center = Offset(size.width / 2 + shift, size.height / 2)
            )
            
            // Inner flame
            drawCircle(
                color = Color(0xFFFFB300).copy(alpha = 0.6f),
                radius = baseWidth * 0.5f,
                center = Offset(size.width / 2 + shift * 0.5f, size.height / 2 - 2f)
            )
            
            // Core
            drawCircle(
                color = Color(0xFFFFD740).copy(alpha = 0.9f),
                radius = baseWidth * 0.25f,
                center = Offset(size.width / 2, size.height / 2 - 3f)
            )
        }
    }
}
