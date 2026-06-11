package com.lifeup.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.ui.theme.CategoryArt
import com.lifeup.app.ui.theme.CategoryLanguage
import com.lifeup.app.ui.theme.CategoryLife
import com.lifeup.app.ui.theme.CategoryLivelihood
import com.lifeup.app.ui.theme.CategoryMental
import com.lifeup.app.ui.theme.CategoryPhysical
import com.lifeup.app.ui.theme.CategorySocial

private val SkillCategory.color: Color
    get() = when (this) {
        SkillCategory.LIVELIHOOD -> CategoryLivelihood
        SkillCategory.SOCIAL -> CategorySocial
        SkillCategory.LANGUAGE -> CategoryLanguage
        SkillCategory.LIFE -> CategoryLife
        SkillCategory.PHYSICAL -> CategoryPhysical
        SkillCategory.MENTAL -> CategoryMental
        SkillCategory.ART -> CategoryArt
    }

private val SkillCategory.displayName: String
    get() = when (this) {
        SkillCategory.LIVELIHOOD -> "谋生"
        SkillCategory.SOCIAL -> "社交"
        SkillCategory.LANGUAGE -> "语言"
        SkillCategory.LIFE -> "生活"
        SkillCategory.PHYSICAL -> "体能"
        SkillCategory.MENTAL -> "心智"
        SkillCategory.ART -> "艺术"
    }

private val SkillCategory.emoji: String
    get() = when (this) {
        SkillCategory.LIVELIHOOD -> "💼"
        SkillCategory.SOCIAL -> "🤝"
        SkillCategory.LANGUAGE -> "🗣"
        SkillCategory.LIFE -> "🌿"
        SkillCategory.PHYSICAL -> "💪"
        SkillCategory.MENTAL -> "🧠"
        SkillCategory.ART -> "🎨"
    }

private val levelBorderColor: (Int) -> Color = { level ->
    when (level) {
        1 -> Color(0xFF9E9E9E)
        2 -> Color(0xFFCD7F32)
        3 -> Color(0xFFC0C0C0)
        4 -> Color(0xFFFFD700)
        else -> Color(0xFF9C27B0)
    }
}

private val levelBackgroundColor: (Int) -> Color = { level ->
    when (level) {
        1 -> Color(0xFF9E9E9E).copy(alpha = 0.12f)
        2 -> Color(0xFFCD7F32).copy(alpha = 0.12f)
        3 -> Color(0xFFC0C0C0).copy(alpha = 0.12f)
        4 -> Color(0xFFFFD700).copy(alpha = 0.12f)
        else -> Color(0xFF9C27B0).copy(alpha = 0.12f)
    }
}

private val levelLabel: (Int) -> String = { level ->
    when (level) {
        1 -> "入门"
        2 -> "熟练"
        3 -> "精通"
        4 -> "专家"
        else -> "大师"
    }
}

@Composable
fun SkillCard(
    skill: Skill,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isNew = (System.currentTimeMillis() - skill.createdAt) < 3 * 24 * 60 * 60 * 1000L
    val totalHours = skill.totalMinutes / 60
    val progress = skill.getProgressToNextLevel()
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "skillProgress"
    )
    val categoryColor = skill.category.color

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = "${skill.name}, LV${skill.level}, ${skill.category.displayName}"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            hoveredElevation = 2.dp
        ),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder(enabled = false)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onClick,
                        onClickLabel = "查看技能详情",
                        indication = rememberRipple(
                            color = categoryColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .then(
                        if (onLongClick != null) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        menuExpanded = true
                                    }
                                )
                            }
                        } else Modifier
                    )
            ) {
            // Category accent bar at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                categoryColor,
                                categoryColor.copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Top row: Level badge + NEW badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Level badge with tier color
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = levelBorderColor(skill.level).copy(alpha = 0.6f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .background(
                                color = levelBackgroundColor(skill.level),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "LV${skill.level}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = levelBorderColor(skill.level),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = levelLabel(skill.level),
                            style = MaterialTheme.typography.labelSmall,
                            color = levelBorderColor(skill.level).copy(alpha = 0.7f),
                            fontSize = 9.sp
                        )
                    }

                    if (isNew) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFFF5252).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "NEW",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5252),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Skill name
                Text(
                    text = skill.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Category emoji + dot + name
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = skill.category.emoji,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(skill.category.color)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = skill.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress bar to next level - custom canvas with gradient
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp))
                ) {
                    // Track
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.06f),
                        cornerRadius = CornerRadius(2.5.dp.toPx())
                    )
                    // Fill
                    val fillWidth = size.width * animatedProgress
                    if (fillWidth > 0f) {
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    categoryColor.copy(alpha = 0.6f),
                                    categoryColor
                                ),
                                startX = 0f,
                                endX = fillWidth
                            ),
                            topLeft = Offset.Zero,
                            size = Size(fillWidth, size.height),
                            cornerRadius = CornerRadius(2.5.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom row: total time + mastery stars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${totalHours}h",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                        if (skill.totalMinutes % 60 > 0) {
                            Text(
                                text = "${skill.totalMinutes % 60}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    if (skill.masteryStars > 0) {
                        Text(
                            text = "★".repeat(skill.masteryStars),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("暂停") },
                    onClick = {
                        menuExpanded = false
                        // 暂停逻辑由调用方处理，此处仅关闭菜单
                    }
                )
                DropdownMenuItem(
                    text = { Text("归档") },
                    onClick = {
                        menuExpanded = false
                        // 归档逻辑由调用方处理，此处仅关闭菜单
                    }
                )
                DropdownMenuItem(
                    text = { Text("删除") },
                    onClick = {
                        menuExpanded = false
                        onLongClick?.invoke()
                    }
                )
            }
        }
    }
}
