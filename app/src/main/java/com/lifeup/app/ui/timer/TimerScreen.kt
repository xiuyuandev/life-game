package com.lifeup.app.ui.timer

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.data.db.ItemTier
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.domain.game.TimerResult
import com.lifeup.app.service.TimerManager
import com.lifeup.app.ui.components.AnimatedCounter
import com.lifeup.app.ui.components.ConfettiAnimation
import com.lifeup.app.ui.components.PremiumLevelUpAnimation
import com.lifeup.app.ui.feedback.rememberHapticController
import com.lifeup.app.ui.feedback.rememberSoundController
import com.lifeup.app.ui.theme.MonospaceFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    skillId: Long,
    onNavigateBack: () -> Unit,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = rememberHapticController(viewModel.settingsPrefs)
    val sound = rememberSoundController(viewModel.settingsPrefs)
    var showConfetti by remember { mutableStateOf(false) }
    var showLevelUp by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        TimerManager.bindService(context)
        onDispose {
            TimerManager.unbindService(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.skill?.name ?: "计时器",
                        fontWeight = FontWeight.SemiBold
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
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
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.error!!,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Skill name label
                Text(
                    text = uiState.skill?.name ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 心魔模式横幅
                if (uiState.demonId != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DemonModeBanner(
                        demonId = uiState.demonId!!,
                        todayDayOfWeek = java.time.LocalDate.now().dayOfWeek.value
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Timer ring + time display
                TimerDisplay(
                    elapsedSeconds = uiState.elapsedSeconds,
                    isRunning = uiState.isRunning,
                    isPaused = uiState.isPaused,
                    formatTime = { viewModel.formatElapsedTime(it) }
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Record type toggle
                val recordTypeIndex = remember(uiState.recordType) {
                    if (uiState.recordType == RecordType.INVESTMENT) 0 else 1
                }

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = recordTypeIndex == 0,
                        onClick = { viewModel.toggleRecordType() },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("投资性")
                    }
                    SegmentedButton(
                        selected = recordTypeIndex == 1,
                        onClick = { viewModel.toggleRecordType() },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("消耗性")
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Control buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when {
                        !uiState.isRunning -> {
                            // Start button - large and prominent
                            Button(
                                onClick = {
                                    haptic.heavy()
                                    sound.tap()
                                    viewModel.startTimer()
                                },
                                modifier = Modifier.size(80.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "开始",
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        uiState.isRunning -> {
                            // Pause / Resume button
                            OutlinedButton(
                                onClick = {
                                    haptic.light()
                                    sound.tap()
                                    if (uiState.isPaused) {
                                        viewModel.resumeTimer()
                                    } else {
                                        viewModel.pauseTimer()
                                    }
                                },
                                modifier = Modifier.size(68.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (uiState.isPaused) "继续" else "暂停",
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(32.dp))

                            // Stop button
                            Button(
                                onClick = {
                                    haptic.success()
                                    viewModel.stopTimer()
                                },
                                modifier = Modifier.size(68.dp),
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "停止",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Settlement sheet
    if (uiState.showSettlement && uiState.settlementResult != null) {
        val result = uiState.settlementResult!!
        LaunchedEffect(result) {
            if (result.leveledUp) {
                showConfetti = true
                showLevelUp = true
                sound.levelUp()
            } else {
                sound.success()
            }
        }
        TimerSettlementSheet(
            result = result,
            durationSeconds = uiState.elapsedSeconds,
            onDismiss = { viewModel.dismissSettlement() },
            haptic = haptic
        )
    }

    if (showConfetti) {
        ConfettiAnimation(
            modifier = Modifier.fillMaxSize(),
            onComplete = { showConfetti = false }
        )
    }

    if (showLevelUp) {
        val result = uiState.settlementResult!!
        PremiumLevelUpAnimation(
            newLevel = result.newLevel,
            modifier = Modifier.fillMaxSize(),
            onComplete = { showLevelUp = false }
        )
    }

    // 心魔战果展示
    if (uiState.showDemonOutcome && uiState.demonOutcome != null) {
        DemonOutcomeSheet(
            outcome = uiState.demonOutcome!!,
            onDismiss = { viewModel.dismissDemonOutcome() }
        )
    }
}

/**
 * 心魔模式横幅：在计时器顶部显示"你正在与 X 战斗"
 */
@Composable
private fun DemonModeBanner(demonId: com.lifeup.app.domain.model.DemonId, todayDayOfWeek: Int) {
    val demon = com.lifeup.app.domain.model.DemonTemplate.ALL.firstOrNull { it.id == demonId }
        ?: return
    val isToday = demonId != com.lifeup.app.domain.model.DemonId.MIRROR_OF_SELF
    val partName = com.lifeup.app.domain.model.DemonTemplate.PART_NAMES.getOrNull(todayDayOfWeek - 1) ?: ""
    androidx.compose.material3.Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        color = demon.color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, demon.color.copy(alpha = 0.4f))
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(text = demon.emoji, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(10.dp))
            androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.weight(1f)) {
                Text(
                    text = "为心魔而战 · ${demon.displayName}",
                    style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                    color = demon.accent,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
                Text(
                    text = "本次专注将伤害它的「$partName」（${isToday.let { "今日" }}）",
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DemonOutcomeSheet(
    outcome: com.lifeup.app.domain.game.DemonBattleOutcome,
    onDismiss: () -> Unit
) {
    val demon = outcome.demon
    val breakdown = outcome.breakdown
    androidx.compose.material3.Surface(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        color = demon.color.copy(alpha = 0.92f)
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier.padding(20.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = "${demon.emoji} ${demon.displayName}",
                color = androidx.compose.ui.graphics.Color.White,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))
            val isHit = breakdown.hitPart && breakdown.totalDamage > 0
            val isDefeat = outcome is com.lifeup.app.domain.game.DemonBattleOutcome.Defeated
            Text(
                text = when {
                    isDefeat -> "已伏诛 ✦"
                    isHit -> "-${breakdown.totalDamage} HP"
                    else -> "未命中"
                },
                color = androidx.compose.ui.graphics.Color.White,
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
            )
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
            Text(
                text = breakdown.reason ?: "${breakdown.focusMinutes} 分钟 × 分类 × 部位",
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            if (isDefeat) {
                val unlock = (outcome as com.lifeup.app.domain.game.DemonBattleOutcome.Defeated).unlockedFeature
                androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                androidx.compose.material3.Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    color = androidx.compose.ui.graphics.Color(0xFFFFD700)
                ) {
                    androidx.compose.foundation.layout.Row(
                        modifier = androidx.compose.ui.Modifier.padding(12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(text = unlock.emoji, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                        androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.width(8.dp))
                        androidx.compose.foundation.layout.Column {
                            Text(
                                text = "新能力：${unlock.title}",
                                style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
                                color = androidx.compose.ui.graphics.Color(0xFF1B1B1B),
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                            Text(
                                text = unlock.description,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color(0xFF1B1B1B).copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(20.dp))
            androidx.compose.material3.Button(
                onClick = onDismiss,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color.White,
                    contentColor = demon.color
                )
            ) {
                Text("继续", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun TimerDisplay(
    elapsedSeconds: Long,
    isRunning: Boolean,
    isPaused: Boolean,
    formatTime: (Long) -> String
) {
    // Pulsing ring animation when running
    val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringAlpha"
    )

    val displayScale = if (isRunning && !isPaused) pulseScale else 1f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(260.dp * displayScale)
    ) {
        // Outer ring
        val ringColor = when {
            !isRunning -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            isPaused -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.primary.copy(alpha = ringAlpha)
        }

        Canvas(modifier = Modifier.size(240.dp)) {
            val strokeWidth = 4.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            drawCircle(
                color = ringColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc based on elapsed time (1 hour = full circle)
            if (isRunning) {
                val progress = ((elapsedSeconds % 3600) / 3600f)
                val sweepAngle = progress * 360f
                drawArc(
                    color = MaterialTheme.colorScheme.primary,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Inner glow when running
        if (isRunning && !isPaused) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Time text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatTime(elapsedSeconds),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = MonospaceFontFamily,
                    fontSize = 52.sp,
                    lineHeight = 60.sp
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (isPaused) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "已暂停",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimerSettlementSheet(
    result: TimerResult,
    durationSeconds: Long,
    onDismiss: () -> Unit,
    haptic: com.lifeup.app.ui.feedback.HapticController = com.lifeup.app.ui.feedback.NoopHapticController
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    LaunchedEffect(result) {
        if (result.leveledUp) haptic.levelUp()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "计时结算",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Duration display
            val h = durationSeconds / 3600
            val m = (durationSeconds % 3600) / 60
            val s = durationSeconds % 60
            Text(
                text = String.format("时长 %02d:%02d:%02d", h, m, s),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Results card with gradient background
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // EXP gained
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⭐", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "经验值",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        AnimatedCounter(
                            count = result.expGained,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Gold gained
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🪙", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "金币",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "+",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFB300)
                        )
                        AnimatedCounter(
                            count = result.goldGained,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFB300)
                            )
                        )
                    }

                    // Level up
                    if (result.leveledUp) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(text = "🎉", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "等级提升! LV${result.newLevel}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Items unlocked
                    if (result.itemsUnlocked.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "解锁物品",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        result.itemsUnlocked.forEach { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = itemTierColor(item.itemTier),
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = itemTierColor(item.itemTier)
                                )
                            }
                        }
                    }
                }
            }

            // Confirm button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("确认", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun itemTierColor(tier: ItemTier): Color = when (tier) {
    ItemTier.COMMON -> MaterialTheme.colorScheme.onSurfaceVariant
    ItemTier.FINE -> Color(0xFF4CAF50)
    ItemTier.RARE -> Color(0xFF2196F3)
    ItemTier.EPIC -> Color(0xFF9C27B0)
    ItemTier.LEGENDARY -> Color(0xFFFF9800)
}
