package com.lifeup.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeup.app.ui.theme.SecondaryOrange
import com.lifeup.app.ui.theme.TertiaryRoseGold
import kotlin.math.cos
import kotlin.math.sin

/**
 * Brand illustration library — purely Compose-drawn.
 * These are used in empty states, hero areas, and onboarding screens.
 *
 * Each illustration is self-contained, scales to any size, and respects
 * the current color scheme by defaulting to MaterialTheme.colorScheme.primary
 * with optional accent colors.
 */

@Composable
fun BrandIllustrationSkills(
    modifier: Modifier = Modifier,
    primary: Color = MaterialTheme.colorScheme.primary,
    accent: Color = SecondaryOrange
) {
    val transition = rememberInfiniteTransition(label = "skills")
    val float by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ),
        label = "float"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.25f), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.5f
            ),
            radius = w * 0.5f,
            center = Offset(cx, cy)
        )

        // Central tree shape (growth metaphor)
        val trunkW = w * 0.06f
        val trunkH = h * 0.32f
        val trunkTop = cy + h * 0.22f
        drawRoundRect(
            color = primary.copy(alpha = 0.85f),
            topLeft = Offset(cx - trunkW / 2, trunkTop),
            size = Size(trunkW, trunkH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(trunkW / 3)
        )

        // Three leaves (skill nodes) orbiting
        val orbitR = w * 0.28f
        val leafR = w * 0.10f
        for (i in 0 until 3) {
            val angle = (float + i * 120f) * (Math.PI / 180f)
            val lx = cx + orbitR * cos(angle).toFloat()
            val ly = cy - h * 0.05f + orbitR * sin(angle).toFloat() * 0.6f
            // Leaf glow
            drawCircle(
                color = (if (i == 0) accent else primary).copy(alpha = 0.18f),
                radius = leafR * 1.6f,
                center = Offset(lx, ly)
            )
            // Leaf body
            drawCircle(
                color = if (i == 0) accent else primary,
                radius = leafR,
                center = Offset(lx, ly)
            )
            // Highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = leafR * 0.35f,
                center = Offset(lx - leafR * 0.3f, ly - leafR * 0.3f)
            )
        }

        // Roots
        val rootY = trunkTop + trunkH
        for (i in -2..2) {
            val rx = cx + i * trunkW * 0.8f
            drawLine(
                color = primary.copy(alpha = 0.4f),
                start = Offset(cx, rootY),
                end = Offset(rx, rootY + h * 0.05f),
                strokeWidth = trunkW * 0.25f
            )
        }
    }
}

@Composable
fun BrandIllustrationTasks(
    modifier: Modifier = Modifier,
    primary: Color = MaterialTheme.colorScheme.primary,
    accent: Color = TertiaryRoseGold
) {
    val transition = rememberInfiniteTransition(label = "tasks")
    val checkProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "check"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.5f
            ),
            radius = w * 0.5f,
            center = Offset(cx, cy)
        )

        // Three stacked list items
        val itemH = h * 0.14f
        val gap = h * 0.05f
        val startY = cy - (itemH * 1.5f + gap)
        val itemW = w * 0.65f
        for (i in 0 until 3) {
            val y = startY + i * (itemH + gap)
            val alpha = if (i == 0) 1f else if (i == 1) 0.7f else 0.45f
            // Card
            drawRoundRect(
                color = primary.copy(alpha = alpha * 0.18f),
                topLeft = Offset(cx - itemW / 2, y),
                size = Size(itemW, itemH),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(itemH * 0.35f)
            )
            // Checkbox
            val cbR = itemH * 0.32f
            val cbCx = cx - itemW / 2 + cbR * 1.5f
            val cbCy = y + itemH / 2
            drawCircle(
                color = if (i < 2) primary else Color.Transparent,
                radius = cbR,
                center = Offset(cbCx, cbCy)
            )
            // Checkmark on completed items
            if (i < 2) {
                val path = Path().apply {
                    moveTo(cbCx - cbR * 0.45f, cbCy)
                    lineTo(cbCx - cbR * 0.1f, cbCy + cbR * 0.3f)
                    lineTo(cbCx + cbR * 0.5f, cbCy - cbR * 0.3f)
                }
                drawPath(
                    path = path,
                    color = Color.White,
                    style = Stroke(width = cbR * 0.28f)
                )
            } else {
                drawCircle(
                    color = primary.copy(alpha = 0.35f),
                    radius = cbR,
                    center = Offset(cbCx, cbCy),
                    style = Stroke(width = cbR * 0.18f)
                )
            }
            // Text lines
            val lineX = cbCx + cbR * 2.2f
            val lineW = itemW * (0.5f + i * 0.1f)
            drawRoundRect(
                color = primary.copy(alpha = alpha * 0.5f),
                topLeft = Offset(lineX, y + itemH * 0.32f),
                size = Size(lineW, itemH * 0.13f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(itemH * 0.1f)
            )
            drawRoundRect(
                color = primary.copy(alpha = alpha * 0.3f),
                topLeft = Offset(lineX, y + itemH * 0.55f),
                size = Size(lineW * 0.7f, itemH * 0.1f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(itemH * 0.1f)
            )
        }
    }
}

@Composable
fun BrandIllustrationTime(
    modifier: Modifier = Modifier,
    primary: Color = MaterialTheme.colorScheme.primary,
    accent: Color = SecondaryOrange
) {
    val transition = rememberInfiniteTransition(label = "time")
    val sweep by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        ),
        label = "sweep"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.32f

        // Halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.5f
            ),
            radius = w * 0.5f,
            center = Offset(cx, cy)
        )

        // Outer ring
        drawCircle(
            color = primary.copy(alpha = 0.15f),
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = r * 0.06f)
        )

        // Tick marks (12)
        for (i in 0 until 12) {
            val angle = (i * 30f) * (Math.PI / 180f)
            val isMajor = i % 3 == 0
            val len = if (isMajor) r * 0.12f else r * 0.06f
            val sx = cx + (r - r * 0.06f) * cos(angle).toFloat()
            val sy = cy + (r - r * 0.06f) * sin(angle).toFloat()
            val ex = cx + (r - r * 0.06f - len) * cos(angle).toFloat()
            val ey = cy + (r - r * 0.06f - len) * sin(angle).toFloat()
            drawLine(
                color = primary.copy(alpha = if (isMajor) 0.8f else 0.4f),
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = if (isMajor) r * 0.04f else r * 0.02f
            )
        }

        // Sweep hand
        rotate(degrees = sweep, pivot = Offset(cx, cy)) {
            drawLine(
                color = accent,
                start = Offset(cx, cy),
                end = Offset(cx, cy - r * 0.8f),
                strokeWidth = r * 0.06f
            )
        }

        // Center hub
        drawCircle(
            color = primary,
            radius = r * 0.08f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color.White,
            radius = r * 0.03f,
            center = Offset(cx, cy)
        )
    }
}

@Composable
fun BrandIllustrationTrophy(
    modifier: Modifier = Modifier,
    primary: Color = MaterialTheme.colorScheme.primary,
    accent: Color = TertiaryRoseGold
) {
    val transition = rememberInfiniteTransition(label = "trophy")
    val shine by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing)
        ),
        label = "shine"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h * 0.45f

        // Halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.25f), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.5f
            ),
            radius = w * 0.5f,
            center = Offset(cx, cy)
        )

        // Trophy cup
        val cupW = w * 0.45f
        val cupH = h * 0.35f
        val cupPath = Path().apply {
            moveTo(cx - cupW / 2, cy - cupH / 2)
            lineTo(cx + cupW / 2, cy - cupH / 2)
            lineTo(cx + cupW * 0.4f, cy + cupH * 0.15f)
            quadraticBezierTo(cx, cy + cupH * 0.45f, cx - cupW * 0.4f, cy + cupH * 0.15f)
            close()
        }
        drawPath(
            path = cupPath,
            brush = Brush.verticalGradient(
                colors = listOf(accent, accent.copy(alpha = 0.7f), primary)
            )
        )

        // Handles
        drawArc(
            color = accent,
            startAngle = 270f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - cupW * 0.7f, cy - cupH * 0.4f),
            size = Size(cupW * 0.3f, cupH * 0.5f),
            style = Stroke(width = w * 0.025f)
        )
        drawArc(
            color = accent,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx + cupW * 0.4f, cy - cupH * 0.4f),
            size = Size(cupW * 0.3f, cupH * 0.5f),
            style = Stroke(width = w * 0.025f)
        )

        // Stem and base
        drawRect(
            color = primary,
            topLeft = Offset(cx - w * 0.04f, cy + cupH * 0.4f),
            size = Size(w * 0.08f, h * 0.1f)
        )
        drawRoundRect(
            color = primary,
            topLeft = Offset(cx - w * 0.18f, cy + cupH * 0.55f),
            size = Size(w * 0.36f, h * 0.06f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.02f)
        )

        // Star on cup
        val starY = cy - cupH * 0.05f
        val starR = w * 0.06f
        val starPath = Path().apply {
            for (i in 0 until 5) {
                val outerA = (-90f + i * 72f) * (Math.PI / 180f)
                val innerA = (-90f + 36f + i * 72f) * (Math.PI / 180f)
                if (i == 0) {
                    moveTo(cx + starR * cos(outerA).toFloat(), starY + starR * sin(outerA).toFloat())
                } else {
                    lineTo(cx + starR * cos(outerA).toFloat(), starY + starR * sin(outerA).toFloat())
                }
                lineTo(
                    cx + starR * 0.4f * cos(innerA).toFloat(),
                    starY + starR * 0.4f * sin(innerA).toFloat()
                )
            }
            close()
        }
        drawPath(path = starPath, color = Color.White.copy(alpha = 0.92f))

        // Shine sweep
        val shineX = -w * 0.4f + shine * (w * 1.8f)
        drawCircle(
            color = Color.White.copy(alpha = 0.18f),
            radius = w * 0.08f,
            center = Offset(shineX, cy)
        )
    }
}

@Composable
fun BrandIllustrationFocus(
    modifier: Modifier = Modifier,
    primary: Color = MaterialTheme.colorScheme.primary,
    accent: Color = SecondaryOrange
) {
    val transition = rememberInfiniteTransition(label = "focus")
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Concentric rings
        for (i in 0 until 3) {
            val r = w * (0.18f + i * 0.10f) * pulse
            drawCircle(
                color = primary.copy(alpha = 0.3f - i * 0.08f),
                radius = r,
                center = Offset(cx, cy),
                style = Stroke(width = w * 0.012f)
            )
        }

        // Target dot
        drawCircle(
            color = accent,
            radius = w * 0.12f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.4f),
            radius = w * 0.05f,
            center = Offset(cx - w * 0.03f, cy - w * 0.03f)
        )
    }
}

@Composable
fun BrandIllustrationCelebration(
    modifier: Modifier = Modifier,
    primary: Color = MaterialTheme.colorScheme.primary,
    accent: Color = TertiaryRoseGold
) {
    val transition = rememberInfiniteTransition(label = "celebration")
    val rotateA by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing)
        ),
        label = "rotA"
    )
    val rotateB by transition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing)
        ),
        label = "rotB"
    )
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.28f), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.5f
            ),
            radius = w * 0.5f,
            center = Offset(cx, cy)
        )

        // Burst rays (8)
        for (i in 0 until 8) {
            val angle = (i * 45f) * (Math.PI / 180f)
            val r0 = w * 0.18f
            val r1 = w * (0.32f + (i % 2) * 0.08f)
            drawLine(
                color = (if (i % 2 == 0) accent else primary).copy(alpha = 0.55f),
                start = Offset(cx + r0 * cos(angle).toFloat(), cy + r0 * sin(angle).toFloat()),
                end = Offset(cx + r1 * cos(angle).toFloat(), cy + r1 * sin(angle).toFloat()),
                strokeWidth = w * 0.018f,
                pathEffect = PathEffect.cornerPathEffect(w * 0.04f)
            )
        }

        // Orbiting dots
        for (i in 0 until 6) {
            val angle = (rotateA + i * 60f) * (Math.PI / 180f)
            val r = w * 0.30f
            val dx = cx + r * cos(angle).toFloat()
            val dy = cy + r * 0.5f * sin(angle).toFloat()
            drawCircle(
                color = if (i % 2 == 0) accent else primary,
                radius = w * 0.025f,
                center = Offset(dx, dy)
            )
        }

        // Central star
        val starR = w * 0.18f
        val starPath = Path().apply {
            for (i in 0 until 5) {
                val outerA = (-90f + rotateB / 30f + i * 72f) * (Math.PI / 180f)
                val innerA = (-90f + 36f + rotateB / 30f + i * 72f) * (Math.PI / 180f)
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
                colors = listOf(accent, primary),
                center = Offset(cx, cy),
                radius = starR
            )
        )
    }
}

@Composable
fun BrandIllustrationCalendar(
    modifier: Modifier = Modifier,
    primary: Color = MaterialTheme.colorScheme.primary,
    accent: Color = SecondaryOrange
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = 0.2f), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.5f
            ),
            radius = w * 0.5f,
            center = Offset(cx, cy)
        )

        // Calendar body
        val calW = w * 0.6f
        val calH = h * 0.55f
        val calTop = cy - calH / 2
        val calLeft = cx - calW / 2
        drawRoundRect(
            color = Color.White.copy(alpha = 0.95f),
            topLeft = Offset(calLeft, calTop),
            size = Size(calW, calH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f)
        )
        drawRoundRect(
            color = primary.copy(alpha = 0.4f),
            topLeft = Offset(calLeft, calTop),
            size = Size(calW, calH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f),
            style = Stroke(width = w * 0.012f)
        )

        // Top header bar
        drawRoundRect(
            color = primary,
            topLeft = Offset(calLeft, calTop),
            size = Size(calW, calH * 0.18f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f)
        )

        // Rings
        for (i in 0 until 2) {
            val ringX = calLeft + calW * (0.25f + i * 0.5f)
            val ringY = calTop - h * 0.02f
            drawLine(
                color = primary,
                start = Offset(ringX, ringY),
                end = Offset(ringX, ringY + h * 0.05f),
                strokeWidth = w * 0.018f
            )
        }

        // Date grid (5x4)
        val gridX = calLeft + calW * 0.12f
        val gridY = calTop + calH * 0.30f
        val cellW = calW * 0.16f
        val cellH = (calH * 0.65f) / 4
        val highlightDay = 7
        for (row in 0 until 4) {
            for (col in 0 until 5) {
                val day = row * 5 + col + 1
                val x = gridX + col * cellW
                val y = gridY + row * cellH
                if (day == highlightDay) {
                    drawCircle(
                        color = accent,
                        radius = cellW * 0.4f,
                        center = Offset(x + cellW / 2, y + cellH / 2)
                    )
                } else {
                    drawCircle(
                        color = primary.copy(alpha = if ((day + row) % 3 == 0) 0.7f else 0.25f),
                        radius = cellW * 0.18f,
                        center = Offset(x + cellW / 2, y + cellH / 2)
                    )
                }
            }
        }
    }
}

@Composable
fun BrandIllustrationEmpty(
    emoji: String = "✨",
    title: String,
    subtitle: String? = null,
    illustration: @Composable (Modifier) -> Unit,
    size: Dp = 160.dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "empty")
    val float by transition.animateFloat(
        initialValue = 0f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(24.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .alpha(1f)
        ) {
            illustration(Modifier.fillMaxSize().alpha(0.85f))
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size * 0.18f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = (size.value * 0.38f).sp,
                    modifier = Modifier.alpha(1f)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
