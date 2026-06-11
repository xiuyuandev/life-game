package com.lifeup.app.ui.demon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeup.app.domain.model.InnerDemon
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * 战斗动画层。
 *
 * 三种模式：
 *  - [Mode.Hit]：受击，1.4 秒。从中心炸出裂痕，红色数字飘升。
 *  - [Mode.Miss]：未命中，0.8 秒。一道灰色斜杠从左上滑到右下。
 *  - [Mode.Defeat]：击败，4.0 秒。整只心魔被白光吞没。
 */
@Composable
fun DemonBattleAnimation(
    mode: Mode,
    demon: InnerDemon,
    damage: Int,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(true) }
    LaunchedEffect(mode) {
        visible = true
        delay(mode.durationMs)
        visible = false
        onComplete()
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(160)) + scaleIn(tween(160)),
        exit = fadeOut(tween(220)) + scaleOut(tween(220)),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (mode) {
                is Mode.Hit -> HitEffect(demon = demon, damage = damage)
                is Mode.Miss -> MissEffect()
                is Mode.Defeat -> DefeatEffect(demon = demon, damage = damage)
            }
        }
    }
}

sealed class Mode(val durationMs: Long) {
    data class Hit(val damage: Int) : Mode(1400L)
    data class Miss(val reason: String) : Mode(800L)
    data class Defeat(val unlocked: String) : Mode(4000L)
}

@Composable
private fun HitEffect(demon: InnerDemon, damage: Int) {
    val transition = rememberInfiniteTransition(label = "hit")
    val ringScale by transition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing)),
        label = "ringScale"
    )
    val ringAlpha by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing)),
        label = "ringAlpha"
    )
    val numRise = remember { Animatable(0f) }
    val numAlpha = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        numRise.animateTo(1f, tween(1300, easing = LinearEasing))
        numAlpha.animateTo(0f, tween(1300, easing = LinearEasing))
    }

    Box(contentAlignment = Alignment.Center) {
        // 红色冲击波
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val maxR = minOf(w, h) * 0.45f
            for (i in 0 until 3) {
                val ringR = maxR * (ringScale * (1f - i * 0.18f))
                val a = ringAlpha * (1f - i * 0.25f)
                drawCircle(
                    color = demon.accent.copy(alpha = a),
                    radius = ringR,
                    center = Offset(cx, cy - 80f * numRise.value),
                    style = Stroke(width = (6f - i * 1.5f) * (1f - numRise.value * 0.5f))
                )
            }
            // 火星点
            val rng = Random(damage)
            repeat(18) {
                val angle = rng.nextFloat() * 360f
                val r = maxR * 0.6f * numRise.value
                val px = cx + r * cos(angle * (Math.PI / 180f)).toFloat()
                val py = cy + r * sin(angle * (Math.PI / 180f)).toFloat()
                drawCircle(
                    color = demon.accent.copy(alpha = 1f - numRise.value),
                    radius = 2.5f * (1f - numRise.value),
                    center = Offset(px, py - 80f * numRise.value)
                )
            }
        }
        // 中心数字
        Text(
            text = "-$damage",
            color = demon.accent,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 56.sp,
            modifier = Modifier
        )
        Text(
            text = "命中！",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier
        )
    }
}

@Composable
private fun MissEffect() {
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawLine(
                color = Color(0xFF9E9E9E),
                start = Offset(w * 0.1f, h * 0.1f),
                end = Offset(w * 0.9f, h * 0.9f),
                strokeWidth = 6f
            )
            drawLine(
                color = Color(0xFF9E9E9E),
                start = Offset(w * 0.9f, h * 0.1f),
                end = Offset(w * 0.1f, h * 0.9f),
                strokeWidth = 6f
            )
        }
    }
}

@Composable
private fun DefeatEffect(demon: InnerDemon, damage: Int) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(3500, easing = LinearEasing))
    }
    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            // 白色爆炸
            val r = (minOf(w, h) * 0.50f) * progress.value
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 1f - progress.value * 0.3f),
                        demon.accent.copy(alpha = (1f - progress.value) * 0.4f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = r
                ),
                radius = r,
                center = Offset(cx, cy)
            )
            // 飞散的光点
            repeat(40) { i ->
                val angle = i * 9f
                val rad = (w * 0.45f) * progress.value
                val px = cx + rad * cos(angle * (Math.PI / 180f)).toFloat()
                val py = cy + rad * sin(angle * (Math.PI / 180f)).toFloat()
                drawCircle(
                    color = Color.White.copy(alpha = 1f - progress.value),
                    radius = 3f * (1f - progress.value),
                    center = Offset(px, py)
                )
            }
        }
        Text(
            text = "心魔已伏诛",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 36.sp
        )
    }
}
