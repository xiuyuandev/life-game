package com.lifeup.app.ui.adventure

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeup.app.data.SeedData
import com.lifeup.app.data.db.entity.SkillEntity
import com.lifeup.app.data.db.entity.TimeSessionEntity
import com.lifeup.app.ui.components.*
import com.lifeup.app.ui.theme.PixelColors
import kotlinx.coroutines.delay

@Composable
fun AdventureScreen(
    viewModel: AdventureViewModel = hiltViewModel(),
    onNavigateToSkill: (Long) -> Unit
) {
    val character by viewModel.character.collectAsState()
    val skills by viewModel.skills.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val todaySessions by viewModel.todaySessions.collectAsState()
    val timeAsset by viewModel.timeAsset.collectAsState()
    val sessionResult by viewModel.sessionResult.collectAsState()
    val showLevelUp by viewModel.showLevelUp.collectAsState()
    val activeEquipment by viewModel.activeEquipment.collectAsState()
    val lastSkillName by viewModel.lastSkillName.collectAsState()

    val scrollState = rememberScrollState()
    var showActivitySelector by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelColors.Background)
            .verticalScroll(scrollState)
    ) {
        // Hero Section — 渐变背景 + 角色信息
        HeroSection(character = character)

        // Timer Section
        AnimatedContent(
            targetState = activeSession != null,
            transitionSpec = {
                fadeIn(tween(400)) + scaleIn(tween(400, easing = EaseOutBack), initialScale = 0.9f) togetherWith
                        fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.95f)
            },
            label = "timer"
        ) { hasSession ->
            if (hasSession && activeSession != null) {
                TimerDisplay(
                    seconds = timerSeconds,
                    session = activeSession,
                    isRunning = isTimerRunning,
                    linkedSkill = viewModel.getSkillById(activeSession.linkedSkillId),
                    onPause = { viewModel.pauseTimer() },
                    onResume = { viewModel.resumeTimer() },
                    onStop = { viewModel.stopSession() }
                )
            } else {
                StartSessionCard(onStartClick = { showActivitySelector = true })
            }
        }

        // Today's Stats
        if (todaySessions.isNotEmpty()) {
            TodayStatsCard(sessions = todaySessions, timeAsset = timeAsset)
        }

        // Streak
        if ((character?.streakDays ?: 0) > 0) {
            StreakBadge(days = character?.streakDays ?: 0)
        }

        // Equipment Effects
        if (activeEquipment.isNotEmpty()) {
            val totalBonus = activeEquipment.sumOf {
                when (it.effectType) {
                    "exp_multiplier", "first_daily_bonus", "long_session_bonus" -> (it.effectValue * 100).toInt()
                    else -> 0
                }
            }
            if (totalBonus > 0) {
                EquipmentEffectBadge(bonusPercent = totalBonus)
            }
        }

        // Skill Progress
        if (todaySessions.any { it.isInvestment }) {
            SectionHeader(title = "今日技能进度", accentColor = PixelColors.AccentBlue)

            val skillProgress = todaySessions
                .filter { it.isInvestment && it.linkedSkillId != null }
                .groupBy { it.linkedSkillId }
                .mapValues { entry ->
                    val skill = skills.find { it.id == entry.key }
                    val totalMinutes = entry.value.sumOf { it.durationMinutes }
                    skill to totalMinutes
                }
                .filter { it.value.first != null }

            skillProgress.forEach { (_, pair) ->
                val (skill, minutes) = pair
                skill?.let {
                    SkillProgressRow(
                        skill = it,
                        todayMinutes = minutes,
                        onClick = { onNavigateToSkill(it.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Overlays
    if (showActivitySelector) {
        ActivitySelectorDialog(
            skills = skills,
            onDismiss = { showActivitySelector = false },
            onStart = { title, category, skillId ->
                viewModel.startSession(title, category, skillId)
                showActivitySelector = false
            }
        )
    }

    SessionCompleteOverlay(
        result = sessionResult,
        skillName = lastSkillName,
        onDismiss = { viewModel.dismissSessionResult() }
    )

    LevelUpOverlay(
        level = showLevelUp,
        onDismiss = { viewModel.dismissLevelUp() }
    )
}

// ===== Hero Section =====
@Composable
private fun HeroSection(character: com.lifeup.app.data.db.entity.CharacterEntity?) {
    if (character == null) return

    val expProgress = if (character.expToNext > 0) {
        character.exp.toFloat() / character.expToNext.toFloat()
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PixelColors.GradientHero)
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlowAvatar(
                    emoji = "🧙",
                    size = 64.dp,
                    glowColor = PixelColors.AccentGold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = PixelColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(
                            text = "Lv.${character.level}",
                            color = PixelColors.AccentGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "冒险者",
                            style = MaterialTheme.typography.labelMedium,
                            color = PixelColors.TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // EXP Bar
            GlowProgressBar(
                progress = expProgress,
                label = "EXP  ${character.exp} / ${character.expToNext}",
                progressBrush = PixelColors.GradientExp,
                glowColor = PixelColors.AccentGoldGlow,
                height = 10.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBadge(
                    icon = "❤️",
                    value = "${character.hp}/${character.maxHp}",
                    label = "生命",
                    color = PixelColors.HpBar
                )
                StatBadge(
                    icon = "⚡",
                    value = "${character.sp}/${character.maxSp}",
                    label = "精力",
                    color = PixelColors.SpBar
                )
                StatBadge(
                    icon = "💰",
                    value = "${character.gold}",
                    label = "金币",
                    color = PixelColors.AccentGold
                )
            }
        }
    }
}

// ===== Timer Display =====
@Composable
private fun TimerDisplay(
    seconds: Long,
    session: TimeSessionEntity,
    isRunning: Boolean,
    linkedSkill: SkillEntity?,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    val timeString = String.format("%02d:%02d:%02d", hours, minutes, secs)
    val expectedExp = (seconds / 60) * 2

    val timerColor = if (isRunning) PixelColors.AccentGreen else PixelColors.AccentOrange
    val timerGlow = if (isRunning) PixelColors.InvestmentGlow else Color(0x40FF8C42)

    GlassCard(glowColor = timerColor.copy(alpha = 0.15f)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pulse indicator when running
            if (isRunning) {
                PulseIndicator(color = PixelColors.AccentGreen, size = 10.dp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Session title
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium,
                color = PixelColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Big timer
            Text(
                text = timeString,
                style = MaterialTheme.typography.displayLarge.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = timerColor.copy(alpha = 0.4f),
                        blurRadius = 20f
                    )
                ),
                color = timerColor,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Linked skill
            if (linkedSkill != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = linkedSkill.icon, fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${linkedSkill.name}  Lv.${linkedSkill.level}",
                        style = MaterialTheme.typography.labelLarge,
                        color = PixelColors.AccentBlue
                    )
                }
            }

            if (expectedExp > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "预计获得 $expectedExp 经验",
                    style = MaterialTheme.typography.labelMedium,
                    color = PixelColors.TextMuted
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isRunning) {
                    OutlinedButton(
                        onClick = onPause,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PixelColors.TextSecondary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                listOf(PixelColors.Border, PixelColors.BorderStrong)
                            )
                        )
                    ) {
                        Text("⏸ 暂停", fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Button(
                        onClick = onResume,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PixelColors.AccentGreen.copy(alpha = 0.2f)
                        )
                    ) {
                        Text("▶ 继续", fontWeight = FontWeight.SemiBold)
                    }
                }
                Button(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PixelColors.AccentRed.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("⏹ 结束", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ===== Start Session Card =====
@Composable
private fun StartSessionCard(onStartClick: () -> Unit) {
    GlassCard(glowColor = PixelColors.PrimaryGlow) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "⚔️",
                fontSize = 48.sp,
                modifier = Modifier.alpha(0.8f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "准备开始今日冒险？",
                style = MaterialTheme.typography.headlineSmall,
                color = PixelColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "选择一项活动，投入你的时间",
                style = MaterialTheme.typography.bodyMedium,
                color = PixelColors.TextMuted
            )
            Spacer(modifier = Modifier.height(20.dp))
            GlowButton(
                text = "选择活动",
                onClick = onStartClick,
                brush = Brush.horizontalGradient(
                    listOf(PixelColors.Primary, PixelColors.PrimaryVariant)
                )
            )
        }
    }
}

// ===== Today's Stats =====
@Composable
private fun TodayStatsCard(
    sessions: List<TimeSessionEntity>,
    timeAsset: com.lifeup.app.data.db.entity.TimeAssetEntity?
) {
    val investedHours = (timeAsset?.investedMinutes ?: 0) / 60.0
    val consumedHours = (timeAsset?.consumedMinutes ?: 0) / 60.0
    val totalHours = investedHours + consumedHours
    val ratio = if (totalHours > 0) investedHours / totalHours else 0.0

    GlassCard(glowColor = if (ratio >= 0.7) PixelColors.AccentGreen.copy(alpha = 0.15f)
    else PixelColors.AccentOrange.copy(alpha = 0.15f)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 今日时间投资",
                    style = MaterialTheme.typography.titleMedium,
                    color = PixelColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(
                    text = String.format("%.0f%%", ratio * 100),
                    color = if (ratio >= 0.7) PixelColors.AccentGreen else PixelColors.AccentOrange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TodayStatItem(
                    icon = "📈",
                    value = String.format("%.1f", investedHours),
                    unit = "小时",
                    label = "投资",
                    color = PixelColors.Investment
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(PixelColors.Divider)
                )
                TodayStatItem(
                    icon = "📉",
                    value = String.format("%.1f", consumedHours),
                    unit = "小时",
                    label = "消耗",
                    color = PixelColors.Consumption
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlowProgressBar(
                progress = ratio.toFloat(),
                progressBrush = if (ratio >= 0.7) Brush.horizontalGradient(
                    listOf(PixelColors.AccentGreen, PixelColors.TertiaryVariant)
                ) else Brush.horizontalGradient(
                    listOf(PixelColors.AccentOrange, PixelColors.Primary)
                ),
                glowColor = if (ratio >= 0.7) PixelColors.InvestmentGlow else Color(0x40FF8C42),
                height = 8.dp,
                label = "投资比率"
            )
        }
    }
}

@Composable
private fun TodayStatItem(
    icon: String,
    value: String,
    unit: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = PixelColors.TextMuted
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PixelColors.TextMuted
        )
    }
}

// ===== Streak Badge =====
@Composable
private fun StreakBadge(days: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "streak")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            tween(1500, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "streakGlow"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    PixelColors.AccentOrange.copy(alpha = glowAlpha * 0.2f),
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    PixelColors.AccentOrange.copy(alpha = glowAlpha * 0.5f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔥", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "连续投资 $days 天",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixelColors.AccentOrange,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ===== Equipment Effect Badge =====
@Composable
private fun EquipmentEffectBadge(bonusPercent: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    PixelColors.AccentBlue.copy(alpha = 0.1f),
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    PixelColors.AccentBlue.copy(alpha = 0.2f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🛡️", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "装备加成: 经验 +$bonusPercent%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PixelColors.AccentBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ===== Skill Progress Row =====
@Composable
private fun SkillProgressRow(
    skill: SkillEntity,
    todayMinutes: Long,
    onClick: () -> Unit
) {
    val progress = if (skill.expToNext > 0) skill.exp.toFloat() / skill.expToNext.toFloat() else 0f
    val hours = todayMinutes / 60
    val mins = todayMinutes % 60
    val timeStr = if (hours > 0) "${hours}h${mins}m" else "${mins}m"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
            .background(PixelColors.Surface.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .border(1.dp, PixelColors.Border, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = skill.icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = skill.name,
                style = MaterialTheme.typography.bodyMedium,
                color = PixelColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            GlowProgressBar(
                progress = progress,
                progressBrush = Brush.horizontalGradient(
                    listOf(PixelColors.AccentBlue, PixelColors.SecondaryVariant)
                ),
                glowColor = PixelColors.SecondaryGlow,
                height = 6.dp,
                label = null,
                animate = false
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Lv.${skill.level}",
                style = MaterialTheme.typography.labelMedium,
                color = PixelColors.AccentGold,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall,
                color = PixelColors.TextMuted
            )
        }
    }
}

// ===== Activity Selector Dialog =====
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivitySelectorDialog(
    skills: List<SkillEntity>,
    onDismiss: () -> Unit,
    onStart: (String, String, Long?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedSkill by remember { mutableStateOf<SkillEntity?>(null) }

    val categories = SeedData.activityNames.keys.toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PixelColors.Surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "选择活动",
                style = MaterialTheme.typography.headlineSmall,
                color = PixelColors.AccentGold,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "活动类型",
                    style = MaterialTheme.typography.labelLarge,
                    color = PixelColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        val name = SeedData.activityNames[category] ?: category
                        val isSelected = selectedCategory == category
                        val isInvestment = SeedData.investmentActivities.contains(category)
                        val bgColor = if (isSelected) {
                            if (isInvestment) PixelColors.AccentGreen.copy(alpha = 0.2f)
                            else PixelColors.AccentRed.copy(alpha = 0.2f)
                        } else PixelColors.SurfaceElevated
                        val borderColor = if (isSelected) {
                            if (isInvestment) PixelColors.AccentGreen.copy(alpha = 0.5f)
                            else PixelColors.AccentRed.copy(alpha = 0.5f)
                        } else PixelColors.Border

                        Box(
                            modifier = Modifier
                                .background(bgColor, RoundedCornerShape(12.dp))
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedCategory = category
                                    selectedSkill = null
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) {
                                    if (isInvestment) PixelColors.AccentGreen else PixelColors.AccentRed
                                } else PixelColors.TextSecondary,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                if (selectedCategory != null) {
                    val categorySkills = skills.filter { skill ->
                        val skillCat = SeedData.activityToSkillCategory[selectedCategory]
                        skill.category == skillCat && skill.unlocked
                    }
                    if (categorySkills.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "关联技能（可选）",
                            style = MaterialTheme.typography.labelLarge,
                            color = PixelColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categorySkills.forEach { skill ->
                                val isSelected = selectedSkill?.id == skill.id
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) PixelColors.Secondary.copy(alpha = 0.2f)
                                            else PixelColors.SurfaceElevated,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) PixelColors.Secondary.copy(alpha = 0.5f)
                                            else PixelColors.Border,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedSkill = if (isSelected) null else skill
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "${skill.icon} ${skill.name} Lv.${skill.level}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) PixelColors.SecondaryVariant
                                        else PixelColors.TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            GlowButton(
                text = "开始计时",
                onClick = {
                    selectedCategory?.let { category ->
                        val title = SeedData.activityNames[category] ?: category
                        onStart(title, category, selectedSkill?.id)
                    }
                },
                brush = Brush.horizontalGradient(
                    listOf(PixelColors.AccentGreen, PixelColors.Tertiary)
                ),
                glowColor = PixelColors.TertiaryGlow
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = PixelColors.TextMuted)
            }
        }
    )
}

// === Easing ===
private val EaseOutBack: Easing = Easing {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    1f + c3 * kotlin.math.pow(it - 1f, 3f) + c1 * kotlin.math.pow(it - 1f, 2f)
}
private val EaseInOutSine: Easing = Easing { -(kotlin.math.cos(Math.PI * it) - 1) / 2 }
private val EaseOutQuart: Easing = Easing { 1 - (1 - it) * (1 - it) * (1 - it) * (1 - it) }
