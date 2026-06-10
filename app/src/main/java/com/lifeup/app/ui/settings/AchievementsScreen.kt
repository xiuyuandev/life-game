package com.lifeup.app.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.lifeup.app.data.db.entity.AchievementEntity
import com.lifeup.app.ui.components.*
import com.lifeup.app.ui.theme.PixelColors

@Composable
fun AchievementsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val achievements by viewModel.achievements.collectAsState()
    val scrollState = rememberScrollState()

    val achievementsByCategory = achievements.groupBy { it.category }
    val categoryNames = mapOf(
        "time_invested" to "⏰ 时间投资",
        "skill_mastery" to "📚 技能精通",
        "streak" to "🔥 坚持连续",
        "diversity" to "🎭 多样性",
        "equipment" to "🛡️ 装备收集",
        "life_event" to "⚔️ 人生事件"
    )
    val categoryColors = mapOf(
        "time_invested" to PixelColors.AccentCyan,
        "skill_mastery" to PixelColors.AccentBlue,
        "streak" to PixelColors.AccentOrange,
        "diversity" to PixelColors.AccentPurple,
        "equipment" to PixelColors.AccentPink,
        "life_event" to PixelColors.AccentGold
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelColors.Background)
            .verticalScroll(scrollState)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PixelColors.GradientHero)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = PixelColors.TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "🏆 成就",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PixelColors.AccentGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Summary
        val unlockedCount = achievements.count { it.unlocked }
        val totalCount = achievements.size
        val progress = if (totalCount > 0) unlockedCount.toFloat() / totalCount.toFloat() else 0f

        GlassCard(glowColor = PixelColors.AccentGoldGlow) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$unlockedCount",
                    style = MaterialTheme.typography.displayMedium.copy(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = PixelColors.AccentGold.copy(alpha = 0.5f),
                            blurRadius = 16f
                        )
                    ),
                    color = PixelColors.AccentGold,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "/ $totalCount 已解锁",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixelColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                GlowProgressBar(
                    progress = progress,
                    progressBrush = PixelColors.GradientExp,
                    glowColor = PixelColors.AccentGoldGlow,
                    height = 10.dp,
                    label = String.format("%.0f%%", progress * 100)
                )
            }
        }

        // Achievements by category
        achievementsByCategory.forEach { (category, categoryAchievements) ->
            val catName = categoryNames[category] ?: category
            val catColor = categoryColors[category] ?: PixelColors.TextMuted

            GlassCard(glowColor = catColor.copy(alpha = 0.08f)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(catColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = catName,
                            style = MaterialTheme.typography.titleMedium,
                            color = catColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    categoryAchievements.forEach { achievement ->
                        AchievementItem(achievement = achievement)
                        if (achievement != categoryAchievements.last()) {
                            GradientDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AchievementItem(achievement: AchievementEntity) {
    val isUnlocked = achievement.unlocked
    val bgAlpha = if (isUnlocked) 0.08f else 0f
    val borderAlpha = if (isUnlocked) 0.2f else 0.08f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                PixelColors.AccentGold.copy(alpha = bgAlpha),
                RoundedCornerShape(12.dp)
            )
            .border(
                1.dp,
                PixelColors.AccentGold.copy(alpha = borderAlpha),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (isUnlocked) PixelColors.AccentGold.copy(alpha = 0.15f)
                    else PixelColors.SurfaceVariant,
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    if (isUnlocked) PixelColors.AccentGold.copy(alpha = 0.3f)
                    else PixelColors.Border,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isUnlocked) achievement.icon else "🔒",
                fontSize = if (isUnlocked) 22.sp else 18.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUnlocked) PixelColors.TextPrimary else PixelColors.TextMuted,
                    fontWeight = if (isUnlocked) FontWeight.SemiBold else FontWeight.Normal
                )
                if (achievement.isMilestone) {
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadge(
                        text = "里程碑",
                        color = PixelColors.AccentGold
                    )
                }
            }
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.labelSmall,
                color = if (isUnlocked) PixelColors.TextSecondary else PixelColors.TextMuted
            )
            if (isUnlocked && (achievement.rewardExp > 0 || achievement.rewardGold > 0)) {
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    if (achievement.rewardExp > 0) {
                        Text(
                            text = "⭐ +${achievement.rewardExp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PixelColors.ExpBar
                        )
                    }
                    if (achievement.rewardGold > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💰 +${achievement.rewardGold}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PixelColors.AccentGold
                        )
                    }
                }
            }
        }
    }
}
