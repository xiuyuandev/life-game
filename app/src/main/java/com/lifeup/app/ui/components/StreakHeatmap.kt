package com.lifeup.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * GitHub-style 7-day streak heatmap.
 * Each cell represents a day, intensity = minutes invested.
 */
@Composable
fun StreakHeatmap(
    dailyMinutes: Map<LocalDate, Int>,
    modifier: Modifier = Modifier,
    weeks: Int = 7,
    cellSize: androidx.compose.ui.unit.Dp = 14.dp,
    cellSpacing: androidx.compose.ui.unit.Dp = 4.dp
) {
    val today = LocalDate.now()
    val startDate = today.minusDays((weeks * 7 - 1).toLong())

    // Build a grid: weeks columns × 7 days rows (Mon-Sun)
    val cells = mutableListOf<HeatCell>()
    var maxMinutes = 1
    var d = startDate
    while (!d.isAfter(today)) {
        val minutes = dailyMinutes[d] ?: 0
        if (minutes > maxMinutes) maxMinutes = minutes
        cells.add(HeatCell(d, minutes))
        d = d.plusDays(1)
    }

    // Group by week
    val cellList = cells
    val weekGroups = cellList.chunked(7)

    Column(modifier = modifier) {
        // Header row with day-of-week labels
        Row(
            modifier = Modifier.padding(start = 20.dp, bottom = 4.dp)
        ) {
            listOf("M", "W", "F").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(cellSize + cellSpacing)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(cellSpacing)
        ) {
            weekGroups.forEach { week ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(cellSpacing)
                ) {
                    week.forEach { cell ->
                        HeatmapCell(
                            cell = cell,
                            maxMinutes = maxMinutes,
                            size = cellSize
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Legend
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "少",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            listOf(0.0f, 0.25f, 0.5f, 0.75f, 1.0f).forEach { intensity ->
                Box(intensity, cellSize)
            }
            Text(
                "多",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Box(intensity: Float, cellSize: androidx.compose.ui.unit.Dp) {
    val color = heatColor(intensity)
    Canvas(modifier = Modifier.size(cellSize)) {
        drawRoundRect(
            color = color,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
        )
    }
}

@Composable
private fun HeatmapCell(
    cell: HeatCell,
    maxMinutes: Int,
    size: androidx.compose.ui.unit.Dp
) {
    val targetAlpha = when {
        cell.minutes == 0 -> 0.06f
        else -> {
            val raw = (cell.minutes.toFloat() / maxMinutes).coerceIn(0.1f, 1f)
            // Map to 5 levels: 0.2, 0.4, 0.6, 0.8, 1.0
            (raw * 5).toInt().coerceAtLeast(1) * 0.2f
        }
    }
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 400),
        label = "cellAlpha"
    )

    val color = heatColor(targetAlpha)
    val isToday = cell.date == LocalDate.now()

    Canvas(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(3.dp))
    ) {
        drawRoundRect(
            color = color,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
        )
        if (isToday) {
            // Highlight today with a border
            drawRoundRect(
                color = Color(0xFFFF6D00),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
            )
        }
    }
}

@Composable
private fun heatColor(intensity: Float): Color {
    val base = MaterialTheme.colorScheme.primary
    return base.copy(alpha = intensity.coerceIn(0.05f, 1f))
}

private data class HeatCell(
    val date: LocalDate,
    val minutes: Int
)
