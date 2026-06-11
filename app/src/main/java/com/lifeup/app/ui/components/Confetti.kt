package com.lifeup.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.runtime.Immutable

@Immutable
data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    particleCount: Int = 50,
    durationMillis: Int = 2000,
    onComplete: () -> Unit = {}
) {
    var particles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
    var progress by remember { mutableStateOf(0f) }
    
    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis, easing = LinearEasing),
        finishedListener = { onComplete() },
        label = "confetti"
    )
    
    LaunchedEffect(Unit) {
        particles = List(particleCount) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = -0.1f,
                color = listOf(
                    Color(0xFFFF5252), Color(0xFFFFB300), Color(0xFF66BB6A),
                    Color(0xFF448AFF), Color(0xFFE040FB), Color(0xFF00BFA5)
                ).random(),
                size = Random.nextFloat() * 8 + 4,
                velocityX = (Random.nextFloat() - 0.5f) * 0.4f,
                velocityY = Random.nextFloat() * 0.5f + 0.3f,
                rotation = Random.nextFloat() * 360,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 10
            )
        }
    }
    
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            particles.forEach { particle ->
                val currentX = particle.x * width + particle.velocityX * width * animatedProgress
                val currentY = particle.y * height + particle.velocityY * height * animatedProgress + 
                    0.5f * 0.3f * (animatedProgress * animatedProgress) * height
                val currentRotation = particle.rotation + particle.rotationSpeed * animatedProgress * 360
                
                if (currentY < height + particle.size) {
                    drawCircle(
                        color = particle.color.copy(alpha = 1f - animatedProgress * 0.7f),
                        radius = particle.size,
                        center = Offset(currentX, currentY)
                    )
                }
            }
        }
    }
}
