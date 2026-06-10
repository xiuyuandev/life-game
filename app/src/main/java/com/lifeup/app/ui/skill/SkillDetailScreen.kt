package com.lifeup.app.ui.skill

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeup.app.data.db.entity.SkillEntity
import com.lifeup.app.ui.components.*
import com.lifeup.app.ui.theme.PixelColors

@Composable
fun SkillDetailScreen(
    skillId: Long,
    viewModel: SkillViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onStartSession: () -> Unit
) {
    val skills by viewModel.skills.collectAsState()
    val skill = skills.find { it.id == skillId }
    val childSkills by viewModel.childSkills.collectAsState()
    val skillStats by viewModel.skillStats.collectAsState()
    val allSkills = skills

    LaunchedEffect(skillId) {
        viewModel.selectSkill(skillId)
    }

    if (skill == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(PixelColors.Background),
            contentAlignment = Alignment.Center
        ) {
            EmptyStateView(
                icon = "🔍",
                title = "技能不存在",
                subtitle = "该技能可能已被删除"
            )
        }
        return
    }

    val progress = if (skill.expToNext > 0) skill.exp.toFloat() / skill.expToNext.toFloat() else 0f
    val totalHours = skill.totalMinutesInvested / 60
    val totalMins = skill.totalMinutesInvested % 60
    val categoryColor = getSkillCategoryColor(skill.category)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelColors.Background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            categoryColor.copy(alpha = 0.15f),
                            PixelColors.Background
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Back button
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = PixelColors.TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Skill icon & name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                categoryColor.copy(alpha = 0.15f),
                                RoundedCornerShape(20.dp)
                            )
                            .border(
                                2.dp,
                                categoryColor.copy(alpha = 0.3f),
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = skill.icon, fontSize = 36.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = skill.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = PixelColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(
                                text = getCategoryName(skill.category),
                                color = categoryColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(
                                text = "Lv.${skill.level}",
                                color = PixelColors.AccentGold
                            )
                        }
                    }
                }
            }
        }

        // Progress Card
        GlassCard(glowColor = categoryColor.copy(alpha = 0.1f)) {
            Column {
                GlowProgressBar(
                    progress = progress,
                    label = "${skill.exp} / ${skill.expToNext} EXP",
                    progressBrush = Brush.horizontalGradient(
                        listOf(categoryColor, categoryColor.copy(alpha = 0.7f))
                    ),
                    glowColor = categoryColor.copy(alpha = 0.2f),
                    height = 12.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBadge(
                        icon = "⏱️",
                        value = "${totalHours}h${totalMins}m",
                        label = "累计投入",
                        color = PixelColors.TextSecondary
                    )
                    StatBadge(
                        icon = "📊",
                        value = "${skill.level}",
                        label = "当前等级",
                        color = categoryColor
                    )
                    StatBadge(
                        icon = "🎯",
                        value = "${skill.maxLevel}",
                        label = "等级上限",
                        color = PixelColors.TextMuted
                    )
                }
                if (skill.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = skill.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = PixelColors.TextMuted
                    )
                }
            }
        }

        // Weekly Stats
        if (skillStats != null && skillStats!!.weeklyMinutes.isNotEmpty()) {
            GlassCard(glowColor = categoryColor.copy(alpha = 0.08f)) {
                Column {
                    Text(
                        text = "📊 近7天投入",
                        style = MaterialTheme.typography.titleSmall,
                        color = PixelColors.TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    WeeklyBarChart(
                        data = skillStats!!.weeklyMinutes,
                        barColor = categoryColor
                    )
                }
            }
        }

        // Start Session Button
        GlowButton(
            text = "▶ 开始修炼",
            onClick = onStartSession,
            brush = Brush.horizontalGradient(
                listOf(categoryColor, categoryColor.copy(alpha = 0.7f))
            ),
            glowColor = categoryColor.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Unlock Path
        if (childSkills.isNotEmpty()) {
            SectionHeader(title = "🔓 解锁路径", accentColor = categoryColor)
            childSkills.forEach { child ->
                val parent = allSkills.find { it.id == child.parentSkillId }
                val canUnlock = parent != null && parent.level >= child.parentLevelRequired
                UnlockPathItem(
                    childSkill = child,
                    canUnlock = canUnlock,
                    parentLevel = parent?.level ?: 0,
                    categoryColor = categoryColor
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun UnlockPathItem(
    childSkill: SkillEntity,
    canUnlock: Boolean,
    parentLevel: Int,
    categoryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .background(
                if (canUnlock) PixelColors.SurfaceElevated.copy(alpha = 0.4f)
                else PixelColors.Surface.copy(alpha = 0.2f),
                RoundedCornerShape(14.dp)
            )
            .border(
                1.dp,
                if (canUnlock) PixelColors.AccentGreen.copy(alpha = 0.3f)
                else PixelColors.Border,
                RoundedCornerShape(14.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (canUnlock) categoryColor.copy(alpha = 0.15f)
                    else PixelColors.SurfaceVariant,
                    RoundedCornerShape(14.dp)
                )
                .border(
                    1.dp,
                    if (canUnlock) categoryColor.copy(alpha = 0.3f)
                    else PixelColors.Border,
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (childSkill.unlocked) childSkill.icon else "🔒",
                fontSize = 22.sp
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = childSkill.name,
                style = MaterialTheme.typography.bodyLarge,
                color = if (canUnlock) PixelColors.TextPrimary else PixelColors.TextMuted,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (childSkill.unlocked)
                    "已解锁 ✅"
                else if (canUnlock)
                    "条件满足，点击解锁"
                else
                    "需 Lv.${childSkill.parentLevelRequired} (当前 Lv.${parentLevel})",
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    childSkill.unlocked -> PixelColors.AccentGreen
                    canUnlock -> PixelColors.AccentBlue
                    else -> PixelColors.Warning
                }
            )
        }
        if (canUnlock && !childSkill.unlocked) {
            Box(
                modifier = Modifier
                    .background(PixelColors.AccentGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, PixelColors.AccentGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "可解锁",
                    style = MaterialTheme.typography.labelSmall,
                    color = PixelColors.AccentGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun WeeklyBarChart(
    data: Map<String, Long>,
    barColor: Color
) {
    val maxValue = data.values.maxOrNull()?.coerceAtLeast(1) ?: 1
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (day, minutes) ->
            val progress = (minutes.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
            val height = (progress * 80).dp.coerceAtLeast(4.dp)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (minutes > 0) "${minutes}" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = PixelColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(height)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(barColor, barColor.copy(alpha = 0.5f))
                            )
                        )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    color = PixelColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun getSkillCategoryColor(category: String): Color = when (category) {
    "professional" -> PixelColors.AccentBlue
    "language" -> PixelColors.AccentCyan
    "sport" -> PixelColors.AccentGreen
    "art" -> PixelColors.AccentPink
    "life" -> PixelColors.AccentOrange
    "social" -> PixelColors.AccentPurple
    else -> PixelColors.Secondary
}

@Composable
private fun getCategoryName(category: String): String =
    com.lifeup.app.data.SeedData.categoryNames[category] ?: category
