package com.lifeup.app.ui.components

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
        1 -> Color(0xFF9E9E9E).copy(alpha = 0.15f)
        2 -> Color(0xFFCD7F32).copy(alpha = 0.15f)
        3 -> Color(0xFFC0C0C0).copy(alpha = 0.15f)
        4 -> Color(0xFFFFD700).copy(alpha = 0.15f)
        else -> Color(0xFF9C27B0).copy(alpha = 0.15f)
    }
}

@Composable
fun SkillCard(
    skill: Skill,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNew = (System.currentTimeMillis() - skill.createdAt) < 3 * 24 * 60 * 60 * 1000L
    val totalHours = skill.totalMinutes / 60
    val progress = skill.getProgressToNextLevel()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) {
                contentDescription = "${skill.name}, LV${skill.level}, ${skill.category.displayName}"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Top row: Level badge + NEW badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level badge
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.5.dp,
                            color = levelBorderColor(skill.level),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .background(
                            color = levelBackgroundColor(skill.level),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LV${skill.level}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = levelBorderColor(skill.level),
                        fontSize = 10.sp
                    )
                }

                if (isNew) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFF5252).copy(alpha = 0.15f),
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

            Spacer(modifier = Modifier.height(8.dp))

            // Skill name
            Text(
                text = skill.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Category dot + name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
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

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar to next level
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = skill.category.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom row: total time + mastery stars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${totalHours}h",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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
    }
}
