package com.lifeup.app.ui.demon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.lifeup.app.domain.model.DemonUnlockKey
import com.lifeup.app.ui.theme.SecondaryOrange

/**
 * 战果展示页：在计时器结束（且启用了"为心魔而战"模式）后弹出。
 *
 * - 顶部：受击动画（Hit / Miss / Defeat）
 * - 中部：战果汇总（伤害 / 命中部位 / 是否击杀）
 * - 底部：奖励（金币、解锁）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemonBattleScreen(
    demonId: DemonId,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (DemonId) -> Unit = {},
    onWriteDiary: (DemonId) -> Unit = {},
    viewModel: DemonBattleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(demonId) {
        viewModel.load(demonId)
    }

    var showAnimation by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "战果",
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
                    IconButton(onClick = { demonId.let { onWriteDiary(it) } }) {
                        Icon(imageVector = Icons.Filled.Edit, contentDescription = "写战记")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        val demon = state.demon
        if (demon == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("载入中…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            // 动画层
            AnimatedVisibility(
                visible = showAnimation && state.outcome != null,
                enter = fadeIn(tween(160)) + scaleIn(tween(160)),
                exit = fadeOut(tween(220)) + scaleOut(tween(220))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    when (val outcome = state.outcome) {
                        is com.lifeup.app.domain.game.DemonBattleOutcome.Defeated -> {
                            DemonBattleAnimation(
                                mode = Mode.Defeat(outcome.unlockedFeature.title),
                                demon = demon,
                                damage = outcome.breakdown.totalDamage,
                                onComplete = { showAnimation = false }
                            )
                        }
                        is com.lifeup.app.domain.game.DemonBattleOutcome.Damaged -> {
                            val reason = outcome.breakdown.reason
                            if (reason != null) {
                                DemonBattleAnimation(
                                    mode = Mode.Miss(reason),
                                    demon = demon,
                                    damage = 0,
                                    onComplete = { showAnimation = false }
                                )
                            } else {
                                DemonBattleAnimation(
                                    mode = Mode.Hit(outcome.breakdown.totalDamage),
                                    demon = demon,
                                    damage = outcome.breakdown.totalDamage,
                                    onComplete = { showAnimation = false }
                                )
                            }
                        }
                        is com.lifeup.app.domain.game.DemonBattleOutcome.AlreadyDefeated -> {
                            DemonBattleAnimation(
                                mode = Mode.Miss("它已被你伏诛"),
                                demon = demon,
                                damage = 0,
                                onComplete = { showAnimation = false }
                            )
                        }
                        null -> Unit
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    BattleHeader(state = state, demon = demon)
                }
                item {
                    DamageBreakdownCard(state = state)
                }
                if (state.unlockedFeature != null) {
                    item {
                        UnlockCelebration(unlock = state.unlockedFeature!!)
                    }
                }
                item {
                    ActionsRow(
                        demonId = demon.id,
                        isDefeated = state.isDefeated,
                        onWriteDiary = onWriteDiary,
                        onBack = onNavigateBack,
                        onDetail = onNavigateToDetail
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun BattleHeader(
    state: DemonBattleUiState,
    demon: com.lifeup.app.domain.model.InnerDemon
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = if (state.isDefeated) {
                            listOf(Color(0xFF1B5E20), Color(0xFF388E3C))
                        } else {
                            listOf(demon.color, demon.accent)
                        }
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = demon.emoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = demon.displayName,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                    DemonPortrait(
                        demon = demon,
                        isDefeated = state.isDefeated,
                        size = 120.dp
                    )
                }
                Text(
                    text = if (state.isDefeated) "已伏诛 ✦" else "继续战斗",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DamageBreakdownCard(state: DemonBattleUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "伤害结算",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            BreakdownRow("本次基础", "${state.breakdown?.baseDamage ?: 0}")
            BreakdownRow("分类克制", "×${"%.2f".format(state.breakdown?.typeMultiplier ?: 1f)}")
            BreakdownRow("部位系数", "×${"%.2f".format(state.breakdown?.partMultiplier ?: 1f)}")
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "总伤害",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${state.breakdown?.totalDamage ?: 0}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            if (state.breakdown?.reason != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.breakdown.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun UnlockCelebration(unlock: DemonUnlockKey) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFD700).copy(alpha = 0.18f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD700)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "新能力解锁",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFB8860B),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = unlock.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = unlock.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActionsRow(
    demonId: DemonId,
    isDefeated: Boolean,
    onWriteDiary: (DemonId) -> Unit,
    onBack: () -> Unit,
    onDetail: (DemonId) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { onDetail(demonId) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryOrange)
        ) {
            Text("查看心魔档案")
        }
        OutlinedButton(
            onClick = { onWriteDiary(demonId) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isDefeated) "写下你的反思" else "记录战记")
        }
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("回到心魔列表")
        }
    }
}
