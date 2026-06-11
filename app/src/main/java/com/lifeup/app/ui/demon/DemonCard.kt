package com.lifeup.app.ui.demon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifeup.app.domain.model.InnerDemon
import com.lifeup.app.domain.model.DemonTemplate

/**
 * 列表中的"心魔卡片"。
 *
 *  - 左侧：DemonPortrait
 *  - 右侧：标题 / 一行描述 / 进度条 / 状态徽章
 *  - 已击败：显示 ✓ + 灰度
 *  - 未发现：显示 🔒 + 模糊
 */
@Composable
fun DemonCard(
    demon: InnerDemon,
    isDefeated: Boolean,
    progressFraction: Float,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isDefeated) Color(0xFF607D8B) else demon.color.copy(alpha = 0.35f),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDefeated) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                demon.color.copy(alpha = 0.10f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                DemonPortrait(
                    demon = demon,
                    isDefeated = isDefeated,
                    size = 80.dp,
                    glowAlpha = if (isDefeated) 0.15f else 0.4f
                )
                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = "未解锁",
                            tint = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = demon.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDefeated) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = demon.emoji,
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (isDefeated) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "已击败",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = demon.shortDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (!isLocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { progressFraction.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isDefeated) Color(0xFF607D8B) else demon.accent,
                            trackColor = demon.color.copy(alpha = 0.15f),
                            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "克制：${demon.recommendedCategories.joinToString("、") { categoryName(it) }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = demon.accent
                    )
                } else {
                    Text(
                        text = "击败全部前一章心魔后解锁",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun categoryName(category: com.lifeup.app.data.db.SkillCategory): String = when (category) {
    com.lifeup.app.data.db.SkillCategory.LIVELIHOOD -> "谋生"
    com.lifeup.app.data.db.SkillCategory.SOCIAL -> "社交"
    com.lifeup.app.data.db.SkillCategory.LANGUAGE -> "语言"
    com.lifeup.app.data.db.SkillCategory.LIFE -> "生活"
    com.lifeup.app.data.db.SkillCategory.PHYSICAL -> "体能"
    com.lifeup.app.data.db.SkillCategory.MENTAL -> "心智"
    com.lifeup.app.data.db.SkillCategory.ART -> "艺术"
}
