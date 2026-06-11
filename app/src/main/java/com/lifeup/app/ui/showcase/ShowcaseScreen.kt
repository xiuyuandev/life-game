package com.lifeup.app.ui.showcase

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.domain.model.Skill

private val categoryColorMap: Map<SkillCategory, Color> = mapOf(
    SkillCategory.LIVELIHOOD to Color(0xFFFF8D6E),
    SkillCategory.SOCIAL to Color(0xFFFF7043),
    SkillCategory.LANGUAGE to Color(0xFF42A5F5),
    SkillCategory.LIFE to Color(0xFF66BB6A),
    SkillCategory.PHYSICAL to Color(0xFFEF5350),
    SkillCategory.MENTAL to Color(0xFFAB47BC),
    SkillCategory.ART to Color(0xFFFFA726)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowcaseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToCreateSkill: () -> Unit = {},
    viewModel: ShowcaseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📖 技能图鉴") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (uiState.isReorderMode) {
                        IconButton(onClick = { viewModel.exitReorderMode() }) {
                            Icon(Icons.Default.Check, contentDescription = "完成排序")
                        }
                    } else {
                        IconButton(onClick = { shareShowcase(context, viewModel) }) {
                            Icon(Icons.Default.Share, contentDescription = "分享")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            ) {
                // Overview card
                item {
                    OverviewCard(
                        totalSkills = uiState.showcaseSkills.size,
                        totalHours = uiState.showcaseSkills.sumOf { it.totalMinutes } / 60,
                        highestSkill = uiState.showcaseSkills.maxByOrNull { it.level }
                    )
                }

                // Hall sections
                itemsIndexed(
                    items = uiState.halls,
                    key = { index, hall -> hall.category?.name ?: index }
                ) { index, hall ->
                    HallSection(
                        hall = hall,
                        isReorderMode = uiState.isReorderMode && uiState.reorderHallIndex == index,
                        onSkillClick = { skillId ->
                            if (!uiState.isReorderMode) {
                                onNavigateToDetail(skillId)
                            }
                        },
                        onSkillLongClick = { skillId ->
                            if (!uiState.isReorderMode) {
                                viewModel.enterReorderMode(index)
                            }
                        },
                        onMoveUp = { skillId -> viewModel.moveSkillUp(skillId) },
                        onMoveDown = { skillId -> viewModel.moveSkillDown(skillId) },
                        onAddClick = onNavigateToCreateSkill
                    )
                }

                // Bottom spacing
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }
    }
}

private fun shareShowcase(context: Context, viewModel: ShowcaseViewModel) {
    val shareText = viewModel.getShareText()
    val intent = Intent.createChooser(
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        },
        "分享技能图鉴"
    )
    context.startActivity(intent)
}

@Composable
private fun OverviewCard(
    totalSkills: Int,
    totalHours: Long,
    highestSkill: Skill?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "📊 图鉴总览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewStat(label = "已修炼技能", value = "$totalSkills")
                OverviewStat(label = "累计时长", value = "${totalHours}h")
                OverviewStat(
                    label = "最高等级",
                    value = highestSkill?.let { "LV${it.level}" } ?: "--"
                )
            }

            if (highestSkill != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🏆 ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${highestSkill.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = categoryColorMap[highestSkill.category] ?: MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " LV${highestSkill.level}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HallSection(
    hall: ShowcaseHall,
    isReorderMode: Boolean,
    onSkillClick: (Long) -> Unit,
    onSkillLongClick: (Long) -> Unit,
    onMoveUp: (Long) -> Unit,
    onMoveDown: (Long) -> Unit,
    onAddClick: () -> Unit
) {
    val categoryColor = hall.category?.let { categoryColorMap[it] } ?: Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Hall name with color accent
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(categoryColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = hall.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (hall.skills.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${hall.skills.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (hall.skills.isEmpty()) {
                // Empty hall
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "暂无技能",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onAddClick) {
                            Text("添加技能", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            } else {
                // C位 skill (center, highlighted)
                hall.centerSkill?.let { center ->
                    CenterSkillCard(
                        skill = center,
                        categoryColor = categoryColor,
                        isReorderMode = isReorderMode,
                        onClick = { onSkillClick(center.id) },
                        onLongClick = { onSkillLongClick(center.id) },
                        onMoveUp = { onMoveUp(center.id) },
                        onMoveDown = { onMoveDown(center.id) }
                    )
                }

                // Other skills in FlowRow
                val otherSkills = hall.skills.drop(1)
                if (otherSkills.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        otherSkills.forEach { skill ->
                            SmallSkillCard(
                                skill = skill,
                                categoryColor = categoryColor,
                                isReorderMode = isReorderMode,
                                onClick = { onSkillClick(skill.id) },
                                onLongClick = { onSkillLongClick(skill.id) },
                                onMoveUp = { onMoveUp(skill.id) },
                                onMoveDown = { onMoveDown(skill.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CenterSkillCard(
    skill: Skill,
    categoryColor: Color,
    isReorderMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val totalHours = skill.totalMinutes / 60

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = Color(0xFFFFD700),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFD700).copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "👑",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = skill.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Level badge
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFFFFD700),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LV${skill.level}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${totalHours}h",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (skill.masteryStars > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "★".repeat(skill.masteryStars),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isReorderMode) {
                ReorderButtons(onMoveUp = onMoveUp, onMoveDown = onMoveDown)
            }
        }
    }
}

@Composable
private fun SmallSkillCard(
    skill: Skill,
    categoryColor: Color,
    isReorderMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val totalHours = skill.totalMinutes / 60

    Card(
        modifier = Modifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = skill.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "LV${skill.level}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${totalHours}h",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    if (skill.masteryStars > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "★".repeat(skill.masteryStars),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (isReorderMode) {
                Spacer(modifier = Modifier.width(4.dp))
                ReorderButtons(onMoveUp = onMoveUp, onMoveDown = onMoveDown)
            }
        }
    }
}

@Composable
private fun ReorderButtons(onMoveUp: () -> Unit, onMoveDown: () -> Unit) {
    Row {
        IconButton(
            onClick = onMoveUp,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowUpward,
                contentDescription = "上移",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(
            onClick = onMoveDown,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "下移",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
