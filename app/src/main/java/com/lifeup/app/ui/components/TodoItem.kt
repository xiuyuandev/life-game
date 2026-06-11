package com.lifeup.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeup.app.data.db.Priority
import com.lifeup.app.domain.model.Todo

private val Priority.color: Color
    get() = when (this) {
        Priority.HIGH -> Color(0xFFFF5252)
        Priority.MEDIUM -> Color(0xFFFFB300)
        Priority.LOW -> Color(0xFF66BB6A)
        Priority.NONE -> Color(0xFFBDBDBD)
    }

private val Priority.label: String
    get() = when (this) {
        Priority.HIGH -> "高"
        Priority.MEDIUM -> "中"
        Priority.LOW -> "低"
        Priority.NONE -> ""
    }

@Composable
fun TodoItem(
    todo: Todo,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleColor by animateColorAsState(
        targetValue = if (todo.isCompleted) {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(300),
        label = "titleColor"
    )

    val strikethroughWidth by animateFloatAsState(
        targetValue = if (todo.isCompleted) 1f else 0f,
        animationSpec = tween(400),
        label = "strikethrough"
    )

    val checkScale by animateFloatAsState(
        targetValue = if (todo.isCompleted) 1f else 0f,
        animationSpec = tween(300),
        label = "checkScale"
    )

    val containerColor by animateColorAsState(
        targetValue = if (todo.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300),
        label = "containerColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = containerColor,
                shape = RoundedCornerShape(12.dp)
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "${todo.title}, ${if (todo.isCompleted) "已完成" else "未完成"}"
            }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Custom checkbox with priority color
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .clickable(onClick = onToggle),
            contentAlignment = Alignment.Center
        ) {
            // Circle outline / filled
            Canvas(modifier = Modifier.size(22.dp)) {
                val strokeWidth = 2.dp.toPx()
                if (todo.isCompleted) {
                    // Filled circle
                    drawCircle(
                        color = todo.priority.color,
                        radius = size.minDimension / 2f,
                    )
                    // Check mark
                    val scale = checkScale
                    if (scale > 0f) {
                        drawCircle(
                            color = Color.White,
                            radius = size.minDimension / 2f * 0.4f * scale,
                        )
                    }
                } else {
                    // Outline circle
                    drawCircle(
                        color = todo.priority.color.copy(alpha = 0.5f),
                        radius = (size.minDimension - strokeWidth) / 2f,
                        style = Stroke(width = strokeWidth)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Title with strikethrough animation
        Text(
            text = todo.title,
            modifier = Modifier
                .weight(1f)
                .drawBehind {
                    if (strikethroughWidth > 0f) {
                        val strokeWidth = 1.5.dp.toPx()
                        val y = size.height / 2
                        val endX = size.width * strikethroughWidth
                        drawLine(
                            color = titleColor,
                            start = Offset(0f, y),
                            end = Offset(endX, y),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                },
            color = titleColor,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (todo.isCompleted) FontWeight.Normal else FontWeight.Medium
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // Priority indicator dot
        if (todo.priority != Priority.NONE) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = todo.priority.color,
                        shape = CircleShape
                    )
            )
        }

        Spacer(modifier = Modifier.width(2.dp))

        // Delete button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(28.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "删除",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
