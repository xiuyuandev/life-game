package com.lifeup.app.ui.demon

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.domain.model.DemonId
import com.lifeup.app.ui.theme.SecondaryOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemonDetailScreen(
    demonId: DemonId,
    onNavigateBack: () -> Unit,
    onStartBattle: (DemonId) -> Unit = {},
    onOpenDiary: (DemonId) -> Unit = {},
    viewModel: DemonDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(demonId) {
        viewModel.load(demonId)
    }

    val demon = state.demon
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = demon?.displayName ?: "心魔",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { demon?.let { onOpenDiary(it.id) } }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "战记"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        if (demon == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "心魔不存在",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeroPortrait(
                    demon = demon,
                    isDefeated = state.isDefeated,
                    todayDayOfWeek = state.todayDayOfWeek,
                    partProgress = state.partProgress
                )
            }
            item {
                StatsRow(
                    state = state,
                    demon = demon
                )
            }
            item {
                StoryCard(demon = demon)
            }
            item {
                PartsDetail(
                    demon = demon,
                    partProgress = state.partProgress,
                    todayDayOfWeek = state.todayDayOfWeek
                )
            }
            item {
                ActionButtons(
                    demonId = demon.id,
                    isDefeated = state.isDefeated,
                    canAttackToday = state.canAttackToday,
                    onStartBattle = onStartBattle
                )
            }
            item {
                UnlockCard(demon = demon, isUnlocked = state.isUnlocked)
            }
            item {
                ReflectionCard(demon = demon)
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun HeroPortrait(
    demon: com.lifeup.app.domain.model.InnerDemon,
    isDefeated: Boolean,
    todayDayOfWeek: Int,
    partProgress: List<Pair<Int, Float>>
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            demon.color.copy(alpha = 0.85f),
                            demon.accent.copy(alpha = 0.65f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                    DemonPortrait(
                        demon = demon,
                        isDefeated = isDefeated,
                        size = 180.dp
                    )
                    if (isDefeated) {
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .clip(CircleShape)
                                .background(Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "已击败",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                DemonPartRing(
                    demon = demon,
                    partProgress = partProgress,
                    todayDayOfWeek = todayDayOfWeek,
                    size = 140.dp
                )
            }
        }
    }
}

@Composable
private fun StatsRow(
    state: DemonDetailUiState,
    demon: com.lifeup.app.domain.model.InnerDemon
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatPill(label = "难度", value = "★".repeat(demon.difficulty), valueColor = demon.accent, modifier = Modifier.weight(1f))
        StatPill(label = "克制", value = demon.recommendedCategories.joinToString("、") { categoryName(it) }, valueColor = demon.accent, modifier = Modifier.weight(1f))
        StatPill(label = "最少时长", value = "${demon.minFocusMinutes}m", valueColor = demon.accent, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatPill(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = valueColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StoryCard(demon: com.lifeup.app.domain.model.InnerDemon) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = demon.color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, demon.color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "背景",
                style = MaterialTheme.typography.titleSmall,
                color = demon.accent,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = demon.story,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PartsDetail(
    demon: com.lifeup.app.domain.model.InnerDemon,
    partProgress: List<Pair<Int, Float>>,
    todayDayOfWeek: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "七曜之躯",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            DemonPartLabels(
                todayDayOfWeek = todayDayOfWeek,
                partProgress = partProgress,
                accent = demon.accent,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            partProgress.forEachIndexed { idx, (maxHp, frac) ->
                val partName = com.lifeup.app.domain.model.DemonTemplate.PART_NAMES[idx]
                val day = idx + 1
                val isToday = day == todayDayOfWeek
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = partName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) demon.accent else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        text = "Day $day",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(40.dp)
                    )
                    LinearProgressIndicator(
                        progress = { frac.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .padding(horizontal = 6.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (frac >= 1f) Color(0xFF607D8B) else demon.accent,
                        trackColor = demon.color.copy(alpha = 0.12f)
                    )
                    Text(
                        text = "${(frac * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    demonId: DemonId,
    isDefeated: Boolean,
    canAttackToday: Boolean,
    onStartBattle: (DemonId) -> Unit
) {
    if (isDefeated) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1B5E20).copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "心魔已伏诛",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "你已掌握这个内心的敌人。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        return
    }
    Column {
        Button(
            onClick = { onStartBattle(demonId) },
            enabled = canAttackToday,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = SecondaryOrange,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (canAttackToday) "为心魔而战 · 去计时" else "今日不是它的活跃日",
                fontWeight = FontWeight.SemiBold
            )
        }
        if (!canAttackToday) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onStartBattle(demonId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("以"干扰伤害"开启（15% 效率）")
            }
        }
    }
}

@Composable
private fun UnlockCard(demon: com.lifeup.app.domain.model.InnerDemon, isUnlocked: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (isUnlocked) Color(0xFF4CAF50).copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) Color(0xFF4CAF50).copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = demon.unlock.emoji,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (isUnlocked) "已解锁：${demon.unlock.title}" else "击败后解锁：${demon.unlock.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = demon.unlock.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ReflectionCard(demon: com.lifeup.app.domain.model.InnerDemon) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "今日一问",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = demon.reflectionPrompt,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
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
