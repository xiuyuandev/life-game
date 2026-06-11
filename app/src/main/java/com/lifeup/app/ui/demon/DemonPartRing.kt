package com.lifeup.app.ui.demon

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lifeup.app.domain.model.InnerDemon
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * 7 段心魔部位环。
 *
 * 环上 7 个弧段对应头/颈/胸/腹/背/尾/翼，
 *  - 已击破的部位 = 深灰底
 *  - 未击破的部位 = 主色
 *  - 今日对应部位 = 加粗 + 副色描边
 */
@Composable
fun DemonPartRing(
    demon: InnerDemon,
    partProgress: List<Pair<Int, Float>>,
    todayDayOfWeek: Int,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp
) {
    val fraction by animateFloatAsState(
        targetValue = partProgress.map { it.second }.average().toFloat().coerceIn(0f, 1f),
        animationSpec = tween(700),
        label = "ringFraction"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = (minOf(w, h) - strokeWidth.toPx()) / 2f
            val gapAngle = 6f
            val segSweep = (360f - gapAngle * 7) / 7f
            val startBase = -90f + gapAngle / 2f

            // 底环
            drawCircle(
                color = demon.color.copy(alpha = 0.10f),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            for (i in 0 until 7) {
                val day = i + 1
                val (maxHp, frac) = partProgress.getOrNull(i) ?: (0 to 0f)
                val partFraction = if (maxHp <= 0) 0f else frac.coerceIn(0f, 1f)
                val isBroken = partFraction >= 1f
                val isToday = day == todayDayOfWeek
                val startAngle = startBase + (segSweep + gapAngle) * i
                val sweepFull = segSweep
                val sweepDone = segSweep * partFraction
                val color = when {
                    isBroken -> Color(0xFF607D8B)
                    isToday -> demon.accent
                    else -> demon.color
                }
                // 完整段（半透明背景）
                drawArc(
                    color = color.copy(alpha = 0.18f),
                    startAngle = startAngle,
                    sweepAngle = sweepFull,
                    useCenter = false,
                    topLeft = Offset(cx - radius, cy - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = if (isToday) strokeWidth.toPx() * 1.15f else strokeWidth.toPx(), cap = StrokeCap.Round)
                )
                // 进度
                if (sweepDone > 0) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(color.copy(alpha = 0.85f), color, color.copy(alpha = 0.85f))
                        ),
                        startAngle = startAngle,
                        sweepAngle = sweepDone,
                        useCenter = false,
                        topLeft = Offset(cx - radius, cy - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = if (isToday) strokeWidth.toPx() * 1.15f else strokeWidth.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 中心文字
            val centerText = "${(fraction * 100).toInt()}%"
            // 用 drawIntoCanvas 在此不可用，留给上层 Box 中放 Text
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(fraction * 100).toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "进度",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 7 个部位的标签条（横向排布，标注今日激活）
 */
@Composable
fun DemonPartLabels(
    todayDayOfWeek: Int,
    partProgress: List<Pair<Int, Float>>,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val labels = listOf("头", "颈", "胸", "腹", "背", "尾", "翼")
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        labels.forEachIndexed { idx, label ->
            val day = idx + 1
            val isToday = day == todayDayOfWeek
            val isBroken = (partProgress.getOrNull(idx)?.second ?: 0f) >= 1f
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .padding(2.dp)
                ) {
                    Canvas(modifier = Modifier.size(16.dp)) {
                        val w = size.width
                        val h = size.height
                        drawCircle(
                            color = when {
                                isBroken -> Color(0xFF607D8B)
                                isToday -> accent
                                else -> accent.copy(alpha = 0.4f)
                            },
                            radius = w * 0.35f,
                            center = Offset(w / 2, h / 2)
                        )
                        if (isToday) {
                            drawCircle(
                                color = Color.White,
                                radius = w * 0.18f,
                                center = Offset(w / 2, h / 2)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal
                )
                Text(
                    text = DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, Locale.SIMPLIFIED_CHINESE),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
