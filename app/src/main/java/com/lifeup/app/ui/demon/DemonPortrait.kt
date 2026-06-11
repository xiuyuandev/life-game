package com.lifeup.app.ui.demon

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lifeup.app.domain.model.DemonArtShape
import com.lifeup.app.domain.model.InnerDemon
import kotlin.math.cos
import kotlin.math.sin

/**
 * 一只"心魔"的矢量艺术。
 * 全部由 Compose Canvas 绘制，13 种 [DemonArtShape] 各对应一种独特造型。
 *
 * 设计目标：
 *  - 与现有 BrandIllustrations 风格统一（柔和光晕 + 抽象轮廓 + 节奏呼吸）。
 *  - 击败状态下变成"光与羽毛"（白雾替代主色）。
 *  - 尺寸自适应，颜色取自 InnerDemon 自身。
 */
@Composable
fun DemonPortrait(
    demon: InnerDemon,
    isDefeated: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    glowAlpha: Float = if (isDefeated) 0.1f else 0.45f
) {
    val infinite = rememberInfiniteTransition(label = "demon_portrait")
    val breathe by infinite.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing)
        ),
        label = "drift"
    )
    val shake by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )

    val mainColor = if (isDefeated) Color(0xFFB0BEC5) else demon.color
    val accentColor = if (isDefeated) Color(0xFFCFD8DC) else demon.accent

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        mainColor.copy(alpha = glowAlpha),
                        mainColor.copy(alpha = glowAlpha * 0.45f),
                        Color.Transparent
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val baseScale = if (isDefeated) 0.6f else 1f
            val liveScale = breathe * baseScale

            when (demon.artShape) {
                DemonArtShape.SERPENT -> drawSerpent(cx, cy, w, h, mainColor, accentColor, drift, liveScale, shake)
                DemonArtShape.SHADOW -> drawShadow(cx, cy, w, h, mainColor, accentColor, drift, liveScale, shake)
                DemonArtShape.FOG -> drawFog(cx, cy, w, h, mainColor, accentColor, drift, liveScale)
                DemonArtShape.HORDE -> drawHorde(cx, cy, w, h, mainColor, accentColor, drift, liveScale, shake)
                DemonArtShape.TSUNAMI -> drawTsunami(cx, cy, w, h, mainColor, accentColor, drift, liveScale)
                DemonArtShape.QUAGMIRE -> drawQuagmire(cx, cy, w, h, mainColor, accentColor, drift, liveScale)
                DemonArtShape.OWL -> drawOwl(cx, cy, w, h, mainColor, accentColor, drift, liveScale, shake)
                DemonArtShape.MIST -> drawMist(cx, cy, w, h, mainColor, accentColor, drift, liveScale)
                DemonArtShape.GRAVITY -> drawGravity(cx, cy, w, h, mainColor, accentColor, drift, liveScale, shake)
                DemonArtShape.DRAGON -> drawDragon(cx, cy, w, h, mainColor, accentColor, drift, liveScale, shake)
                DemonArtShape.CLOUD -> drawCloud(cx, cy, w, h, mainColor, accentColor, drift, liveScale)
                DemonArtShape.NIHILISM -> drawNihilism(cx, cy, w, h, mainColor, accentColor, drift, liveScale)
                DemonArtShape.MIRROR -> drawMirror(cx, cy, w, h, mainColor, accentColor, drift, liveScale)
            }
        }
    }
}

// ---------------- 13 个 art shape 的具体绘制 ----------------

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSerpent(
    cx: Float, cy: Float, w: Float, h: Float,
    main: Color, accent: Color, drift: Float, scale: Float, shake: Float
) {
    val headR = w * 0.13f * scale
    val bodyW = w * 0.07f * scale
    // 蛇身：6 个圆弧段
    for (i in 0 until 7) {
        val t = i / 6f
        val px = cx + w * 0.32f * (t - 0.5f) + shake * (if (i % 2 == 0) 1f else -1f)
        val py = cy + h * 0.12f * sin((drift * (Math.PI / 180f) + t * 4f).toFloat()) - h * 0.18f * t
        drawCircle(
            color = if (i == 0) accent else main,
            radius = bodyW * (1f - t * 0.5f),
            center = Offset(px, py)
        )
    }
    // 头
    drawCircle(
        color = accent,
        radius = headR,
        center = Offset(cx + w * 0.34f, cy - h * 0.18f)
    )
    // 眼睛
    drawCircle(
        color = Color.Black,
        radius = headR * 0.18f,
        center = Offset(cx + w * 0.30f, cy - h * 0.20f)
    )
    drawCircle(
        color = Color.Black,
        radius = headR * 0.18f,
        center = Offset(cx + w * 0.38f, cy - h * 0.20f)
    )
    // 信子
    val tonguePath = Path().apply {
        moveTo(cx + w * 0.34f, cy - h * 0.10f)
        lineTo(cx + w * 0.30f, cy - h * 0.05f)
        moveTo(cx + w * 0.34f, cy - h * 0.10f)
        lineTo(cx + w * 0.38f, cy - h * 0.05f)
    }
    drawPath(tonguePath, color = Color(0xFFE57373), style = Stroke(width = w * 0.012f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawShadow(
    cx: Float, cy: Float, w: Float, h: Float,
    main: Color, accent: Color, drift: Float, scale: Float, shake: Float
) {
    // 抽象人形剪影 + 时钟指针
    val silhouettePath = Path().apply {
        moveTo(cx, cy - h * 0.25f * scale)
        cubicTo(
            cx + w * 0.18f, cy - h * 0.20f * scale,
            cx + w * 0.25f, cy + h * 0.05f * scale,
            cx + w * 0.20f, cy + h * 0.25f * scale
        )
        lineTo(cx - w * 0.20f, cy + h * 0.25f * scale)
        cubicTo(
            cx - w * 0.25f, cy + h * 0.05f * scale,
            cx - w * 0.18f, cy - h * 0.20f * scale,
            cx, cy - h * 0.25f * scale
        )
        close()
    }
    drawPath(silhouettePath, color = main)
    // 红色秒针
    val angle = drift
    rotate(angle, pivot = Offset(cx, cy)) {
        drawLine(
            color = Color(0xFFEF5350),
            start = Offset(cx, cy),
            end = Offset(cx, cy - h * 0.30f * scale),
            strokeWidth = w * 0.012f
        )
        drawCircle(
            color = Color(0xFFEF5350),
            radius = w * 0.04f,
            center = Offset(cx, cy)
        )
    }
    // 抖动的光点
    for (i in 0 until 6) {
        val angleRad = (drift + i * 60f) * (Math.PI / 180f)
        val r = w * 0.36f
        val px = cx + r * cos(angleRad).toFloat() * scale
        val py = cy + r * sin(angleRad).toFloat() * scale
        drawCircle(
            color = accent.copy(alpha = 0.7f),
            radius = w * 0.015f,
            center = Offset(px, py)
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFog(
    cx: Float, cy: Float, w: Float, h: Float,
    main: Color, accent: Color, drift: Float, scale: Float
) {
    // 多层雾团
    for (i in 0 until 5) {
        val radius = w * (0.18f + i * 0.05f) * scale
        val ox = sin((drift * (Math.PI / 180f) + i)).toFloat() * w * 0.04f
        val oy = cos((drift * (Math.PI / 180f) + i)).toFloat() * h * 0.04f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(main.copy(alpha = 0.8f - i * 0.13f), Color.Transparent),
                center = Offset(cx + ox, cy + oy),
                radius = radius
            ),
            radius = radius,
            center = Offset(cx + ox, cy + oy)
        )
    }
    // 露出的眼睛
    drawCircle(color = Color.White, radius = w * 0.04f, center = Offset(cx - w * 0.08f, cy - h * 0.06f))
    drawCircle(color = Color.Black, radius = w * 0.02f, center = Offset(cx - w * 0.08f, cy - h * 0.06f))
    drawCircle(color = Color.White, radius = w * 0.04f, center = Offset(cx + w * 0.08f, cy - h * 0.06f))
    drawCircle(color = Color.Black, radius = w * 0.02f, center = Offset(cx + w * 0.08f, cy - h * 0.06f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHorde(
    cx: Float, cy: Float, w: Float, h: Float,
    main: Color, accent: Color, drift: Float, scale: Float, shake: Float
) {
    // 屏幕矩形
    val rectW = w * 0.45f * scale
    val rectH = h * 0.65f * scale
    drawRoundRect(
        color = main,
        topLeft = Offset(cx - rectW / 2, cy - rectH / 2),
        size = Size(rectW, rectH),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f)
    )
    // 多眼矩阵 4x3
    val cols = 4
    val rows = 3
    val eyeR = w * 0.025f
    val stepX = rectW / (cols + 1)
    val stepY = rectH / (rows + 1)
    for (r in 0 until rows) {
        for (c in 0 until cols) {
            val ex = cx - rectW / 2 + stepX * (c + 1)
            val ey = cy - rectH / 2 + stepY * (r + 1)
            val phase = ((drift + r * 60f + c * 30f) % 360f)
            val blink = if (phase in 0f..10f) 0.1f else 1f
            drawCircle(color = accent, radius = eyeR * 1.8f * blink, center = Offset(ex, ey))
            drawCircle(color = Color.Black, radius = eyeR * blink, center = Offset(ex, ey))
        }
    }
    // 小红点通知
    drawCircle(color = Color(0xFFEF5350), radius = w * 0.04f, center = Offset(cx + rectW * 0.30f, cy - rectH * 0.42f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTsunami(
    cx: Float, cy: Float, w: Float, h: Float,
    main: Color, accent: Color, drift: Float, scale: Float
) {
    // 3 层海浪
    for (layer in 0 until 3) {
        val yOffset = h * (0.05f + layer * 0.18f) * scale
        val path = Path()
        val amp = h * 0.08f * scale * (1f - layer * 0.2f)
        val phase = drift * (Math.PI / 180f) + layer
        path.moveTo(cx - w * 0.45f, cy + yOffset)
        val steps = 24
        for (i in 0..steps) {
            val t = i / steps.toFloat()
            val px = cx - w * 0.45f + t * w * 0.9f
            val py = cy + yOffset + amp * sin((phase + t * 6.28f).toFloat()).toFloat()
            path.lineTo(px, py)
        }
        path.lineTo(cx + w * 0.45f, cy + h * 0.45f * scale)
        path.lineTo(cx - w * 0.45f, cy + h * 0.45f * scale)
        path.close()
        val color = if (layer == 0) main else if (layer == 1) main.copy(alpha = 0.85f) else main.copy(alpha = 0.7f)
        drawPath(path, color = color)
    }
    // 顶部冲起的浪头
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = w * 0.05f,
        center = Offset(cx + w * 0.18f * sin(drift * 0.05f), cy - h * 0.18f * scale)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawQuagmire(
    cx: Float, cy: Float, w: Float, h: Float,
    main: Color, accent: Color, drift: Float, scale: Float
) {
    // 漩涡：螺旋
    val path = Path()
    val turns = 4
    val steps = 80
    for (i in 0..steps) {
        val t = i / steps.toFloat()
        val angle = t * turns * 2f * Math.PI + drift * (Math.PI / 180f)
        val r = (w * 0.4f * (1f - t * 0.85f)) * scale
        val px = cx + (r * cos(angle)).toFloat()
        val py = cy + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    drawPath(path, color = main, style = Stroke(width = w * 0.02f, pathEffect = PathEffect.cornerPathEffect(w * 0.02f)))
    // 中心下沉的洞
    drawCircle(color = Color.Black, radius = w * 0.06f, center = Offset(cx, cy))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOwl(
    cx: Float, cy: Float, w: Float, h: Float,
    main: Color, accent: Color, drift: Float, scale: Float, shake: Float
) {
    // 身体
    drawOval(
        color = main,
        topLeft = Offset(cx - w * 0.20f * scale, cy - h * 0.25f * scale),
        size = Size(w * 0.40f * scale, h * 0.55f * scale)
    )
    // 翅膀
    val wingPath = Path().apply {
        moveTo(cx - w * 0.18f, cy)
        cubicTo(
            cx - w * 0.30f, cy - h * 0.05f,
            cx - w * 0.32f, cy + h * 0.20f,
            cx - w * 0.20f, cy + h * 0.20f
        )
        close()
    }
    drawPath(wingPath, color = accent)
    val wingPath2 = Path().apply {
        moveTo(cx + w * 0.18f, cy)
        cubicTo(
            cx + w * 0.30f, cy - h * 0.05f,
            cx + w * 0.32f, cy + h * 0.20f,
            cx + w * 0.20f, cy + h * 0.20f
        )
        close()
    }
    drawPath(wingPath2, color = accent)
    // 眼睛：大圆 + 小瞳孔
    val eyeR = w * 0.09f
    drawCircle(color = Color(0xFFFFEB3B), radius = eyeR * scale, center = Offset(cx - w * 0.07f * scale, cy - h * 0.10f * scale))
    drawCircle(color = Color.Black, radius = eyeR * 0.4f * scale, center = Offset(cx - w * 0.07f * scale, cy - h * 0.10f * scale))
    drawCircle(color = Color(0xFFFFEB3B), radius = eyeR * scale, center = Offset(cx + w * 0.07f * scale, cy - h * 0.10f * scale))
    drawCircle(color = Color.Black, radius = eyeR * 0.4f * scale, center = Offset(cx + w * 0.07f * scale, cy - h * 0.10f * scale))
    // 喙
    val beak = Path().apply {
        moveTo(cx, cy - h * 0.04f)
        lineTo(cx - w * 0.02f, cy)
        lineTo(cx + w * 0.02f, cy)
        close()
    }
    drawPath(beak, color = Color(0xFFFFA000))
    // 月亮背景
    drawCircle(
        color = Color.White.copy(alpha = 0.15f),
        radius = w * 0.42f,
        center = Offset(cx + w * 0.25f, cy - h * 0.30f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMist(
    cx: Float, cy: Float, w: Float, h: Float,
    main: Color, accent: Color, drift: Float, scale: Float
) {
    // 太阳被云遮
    drawCircle(
        color = Color(0xFFFFD54F).copy(alpha = 0.7f),
        radius = w * 0.16f * scale,
        center = Offset(cx, cy - h * 0.08f)
    )
    // 雾层
    for (i in 0 until 4) {
        val phase = drift * (Math.PI / 180f) + i
        val ox = sin(phase).toFloat() * w * 0.05f
        drawOval(
            color = main.copy(alpha = 0.55f - i * 0.1f),
            topLeft = Offset(cx - w * 0.30f * scale + ox, cy - h * 0.10f + i * h * 0.06f * scale),
            size = Size(w * 0.60f * scale, h * 0.16f * scale)
        )
    }
    // 床
    drawRoundRect(
        color = accent.copy(alpha = 0.4f),
        topLeft = Offset(cx - w * 0.32f, cy + h * 0.30f),
        size = Size(w * 0.64f, h * 0.06f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGravity(
    cx: Float, cy: Float, w: Float, h: Float,
    main: Color, accent: Color, drift: Float, scale: Float, shake: Float
) {
    // 沙发轮廓
    val sofaPath = Path().apply {
        moveTo(cx - w * 0.30f, cy + h * 0.10f)
        cubicTo(
            cx - w * 0.30f, cy - h * 0.10f,
            cx + w * 0.30f, cy - h * 0.10f,
            cx + w * 0.30f, cy + h * 0.10f
        )
        lineTo(cx + w * 0.30f, cy + h * 0.20f)
        lineTo(cx - w * 0.30f, cy + h * 0.20f)
        close()
    }
    drawPath(sofaPath, color = main)
    // 靠背
    drawRoundRect(
        color = accent,
        topLeft = Offset(cx - w * 0.32f, cy - h * 0.18f),
        size = Size(w * 0.64f, h * 0.12f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f)
    )
    // 沙发上的"人"
    drawCircle(
        color = Color(0xFF8D6E63),
        radius = w * 0.07f,
        center = Offset(cx + shake * 0.5f, cy - h * 0.06f)
    )
    // 重力线
    for (i in 0 until 5) {
        val xLine = cx - w * 0.25f + i * w * 0.10f
        drawLine(
            color = Color(0xFF8D6E63).copy(alpha = 0.5f),
            start = Offset(xLine, cy - h * 0.25f),
            end = Offset(xLine, cy - h * 0.05f),
            strokeWidth = w * 0.012f
        )
    }
    // 遥控器
    drawRoundRect(
        color = Color(0xFF424242),
        topLeft = Offset(cx + w * 0.20f, cy + h * 0.05f),
        size = Size(w * 0.06f, h * 0.10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.01f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDragon(
    cx: Float, cy: Float, w: Float, h: Float,
    main: Color, accent: Color, drift: Float, scale: Float, shake: Float
) {
    // 龙头
    val headPath = Path().apply {
        moveTo(cx - w * 0.05f, cy - h * 0.20f * scale)
        cubicTo(
            cx + w * 0.20f, cy - h * 0.25f * scale,
            cx + w * 0.25f, cy - h * 0.05f * scale,
            cx + w * 0.10f, cy + h * 0.05f * scale
        )
        lineTo(cx - w * 0.15f, cy + h * 0.05f * scale)
        close()
    }
    drawPath(headPath, color = main)
    // 鳞片纹理
    for (i in 0 until 4) {
        val ox = i * w * 0.05f - w * 0.10f
        drawOval(
            color = accent.copy(alpha = 0.6f),
            topLeft = Offset(cx + ox - w * 0.025f, cy - h * 0.05f),
            size = Size(w * 0.05f, h * 0.04f)
        )
    }
    // 龙角
    val hornPath = Path().apply {
        moveTo(cx - w * 0.02f, cy - h * 0.18f)
        lineTo(cx - w * 0.10f, cy - h * 0.32f)
        lineTo(cx - w * 0.04f, cy - h * 0.18f)
        close()
    }
    drawPath(hornPath, color = accent)
    // 眼睛
    drawCircle(color = Color(0xFFFFEB3B), radius = w * 0.04f, center = Offset(cx + w * 0.05f, cy - h * 0.10f))
    drawCircle(color = Color.Black, radius = w * 0.02f, center = Offset(cx + w * 0.05f, cy - h * 0.10f))
    // 火焰吐息
    val breathPath = Path().apply {
        moveTo(cx + w * 0.20f, cy - h * 0.05f)
        cubicTo(
            cx + w * 0.30f, cy - h * 0.10f,
            cx + w * 0.35f, cy,
            cx + w * 0.30f, cy + h * 0.05f
        )
        close()
    }
    drawPath(breathPath, color = Color(0xFFFF8A65).copy(alpha = 0.85f))
    // 卷曲的尾巴
    val tailPath = Path().apply {
        moveTo(cx - w * 0.10f, cy + h * 0.10f)
        cubicTo(
            cx - w * 0.20f, cy + h * 0.20f,
            cx - w * 0.30f, cy + h * 0.10f,
            cx - w * 0.25f, cy + h * 0.05f
        )
    }
    drawPath(tailPath, color = main, style = Stroke(width = w * 0.05f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloud(
    cx: Float, cy: Float, w: Float, h: Float,
    main: Color, accent: Color, drift: Float, scale: Float
) {
    // 多片云组成的不安
    for (i in 0 until 4) {
        val px = cx + w * (0.0f + i * 0.10f - 0.15f)
        val py = cy + sin((drift * (Math.PI / 180f) + i)).toFloat() * h * 0.04f
        drawCircle(
            color = main.copy(alpha = 0.85f - i * 0.12f),
            radius = w * 0.16f * scale,
            center = Offset(px, py)
        )
    }
    // 雨点
    for (i in 0 until 6) {
        val rx = cx - w * 0.25f + i * w * 0.10f
        val ry = cy + h * 0.12f + ((drift + i * 60f) % 60f) * h * 0.005f
        drawLine(
            color = Color(0xFF42A5F5).copy(alpha = 0.7f),
            start = Offset(rx, ry),
            end = Offset(rx - w * 0.02f, ry + h * 0.06f),
            strokeWidth = w * 0.014f
        )
    }
    // 闪电
    val lightningPath = Path().apply {
        moveTo(cx + w * 0.05f, cy + h * 0.20f)
        lineTo(cx - w * 0.05f, cy + h * 0.05f)
        lineTo(cx + w * 0.05f, cy - h * 0.05f)
    }
    drawPath(lightningPath, color = Color(0xFFFFD600), style = Stroke(width = w * 0.018f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawNihilism(
    cx: Float, cy: Float, w: Float, h: Float,
    main: Color, accent: Color, drift: Float, scale: Float
) {
    // 中心黑洞
    drawCircle(
        color = Color.Black,
        radius = w * 0.10f * scale,
        center = Offset(cx, cy)
    )
    // 灰环
    for (i in 0 until 4) {
        drawCircle(
            color = main.copy(alpha = 0.4f - i * 0.1f),
            radius = w * (0.12f + i * 0.05f) * scale,
            center = Offset(cx, cy),
            style = Stroke(width = w * 0.014f)
        )
    }
    // 飘散的点
    for (i in 0 until 12) {
        val angle = (drift + i * 30f) * (Math.PI / 180f)
        val r = w * (0.20f + 0.05f * sin(drift * 0.05f + i).toFloat()) * scale
        val px = cx + r * cos(angle).toFloat()
        val py = cy + r * sin(angle).toFloat()
        drawCircle(
            color = accent.copy(alpha = 0.6f),
            radius = w * 0.012f,
            center = Offset(px, py)
        )
    }
    // 边框的"?"符号（用文字或小色块代替，这里用斜杠）
    drawLine(
        color = accent,
        start = Offset(cx - w * 0.32f, cy - h * 0.30f),
        end = Offset(cx - w * 0.22f, cy - h * 0.18f),
        strokeWidth = w * 0.02f
    )
    drawLine(
        color = accent,
        start = Offset(cx - w * 0.22f, cy - h * 0.18f),
        end = Offset(cx - w * 0.32f, cy - h * 0.06f),
        strokeWidth = w * 0.02f
    )
    drawCircle(
        color = accent,
        radius = w * 0.018f,
        center = Offset(cx - w * 0.32f, cy + h * 0.04f)
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMirror(
    cx: Float, cy: Float, w: Float, h: Float,
    main: Color, accent: Color, drift: Float, scale: Float
) {
    // 镜面椭圆
    drawOval(
        color = Color.White,
        topLeft = Offset(cx - w * 0.20f * scale, cy - h * 0.28f * scale),
        size = Size(w * 0.40f * scale, h * 0.56f * scale)
    )
    // 镜框
    drawOval(
        color = accent,
        topLeft = Offset(cx - w * 0.20f * scale, cy - h * 0.28f * scale),
        size = Size(w * 0.40f * scale, h * 0.56f * scale),
        style = Stroke(width = w * 0.024f)
    )
    // 镜中朦胧的脸
    drawCircle(
        color = main.copy(alpha = 0.6f),
        radius = w * 0.10f * scale,
        center = Offset(cx, cy - h * 0.08f * scale)
    )
    drawCircle(color = Color.Black, radius = w * 0.012f, center = Offset(cx - w * 0.04f, cy - h * 0.08f))
    drawCircle(color = Color.Black, radius = w * 0.012f, center = Offset(cx + w * 0.04f, cy - h * 0.08f))
    val mouthPath = Path().apply {
        moveTo(cx - w * 0.04f, cy + h * 0.02f)
        quadraticBezierTo(cx, cy + h * 0.06f, cx + w * 0.04f, cy + h * 0.02f)
    }
    drawPath(mouthPath, color = Color.Black, style = Stroke(width = w * 0.012f))
    // 高光
    drawOval(
        color = Color.White.copy(alpha = 0.85f),
        topLeft = Offset(cx - w * 0.16f * scale, cy - h * 0.22f * scale),
        size = Size(w * 0.08f * scale, h * 0.16f * scale)
    )
    // 顶部"镜"柄
    drawRoundRect(
        color = accent,
        topLeft = Offset(cx - w * 0.04f, cy - h * 0.32f * scale),
        size = Size(w * 0.08f, h * 0.06f * scale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.02f)
    )
}
