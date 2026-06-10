package com.lifeup.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeup.app.domain.game.DurabilityChange
import com.lifeup.app.domain.game.SessionResult
import com.lifeup.app.ui.theme.PixelColors
import kotlinx.coroutines.delay

// ============================================
// 精美游戏弹窗 — 华丽动画 + 视觉层次
// ============================================

@Composable
fun SessionCompleteOverlay(
    result: SessionResult?,
    skillName: String,
    onDismiss: () -> Unit
) {
    if (result == null) return

    var visible by remember { mutableStateOf(false) }
    var particles by remember { mutableStateOf(false) }

    LaunchedEffect(result) {
        delay(50)
        visible = true
        delay(300)
        particles = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(
            tween(400, easing = EaseOutBack),
            initialScale = 0.6f
        ),
        exit = fadeOut(tween(250)) + scaleOut(
            tween(250),
            targetScale = 0.85f
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PixelColors.DeepSpace.copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center
        ) {
            // 背景粒子
            if (particles) {
                FloatingParticles()
            }

            // 主卡片
            GlassCard(
                modifier = Modifier.padding(24.dp),
                glowColor = PixelColors.AccentGold.copy(alpha = 0.2f),
                contentPadding = PaddingValues(28.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 标题
                    Text(
                        text = "⚔️",
                        fontSize = 40.sp,
                        modifier = Modifier.alpha(0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "修炼完成",
                        style = MaterialTheme.typography.displaySmall,
                        color = PixelColors.AccentGold,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = skillName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = PixelColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 奖励面板
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                PixelColors.SurfaceElevated.copy(alpha = 0.5f),
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.06f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(20.dp)
                    ) {
                        // 经验
                        AnimatedRewardRow(
                            icon = "⭐",
                            label = "经验",
                            value = "+${result.totalExp}",
                            color = PixelColors.ExpBar,
                            delayMs = 100
                        )
                        if (result.bonusExp > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "基础 ${result.baseExp} + 加成 ${result.bonusExp}",
                                style = MaterialTheme.typography.labelSmall,
                                color = PixelColors.TextMuted,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 金币
                        AnimatedRewardRow(
                            icon = "💰",
                            label = "金币",
                            value = "+${result.goldEarned}",
                            color = PixelColors.AccentGold,
                            delayMs = 250
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 技能进度
                        AnimatedRewardRow(
                            icon = if (result.skillLevelUp) "🔥" else "📈",
                            label = if (result.skillLevelUp) "技能升级" else "技能经验",
                            value = if (result.skillLevelUp) "升级！" else "+${result.skillExpGained}",
                            color = if (result.skillLevelUp) PixelColors.AccentOrange else PixelColors.AccentGreen,
                            delayMs = 400
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 装备效果
                    if (result.activeEquipmentEffects.isNotEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🛡️ 装备加成",
                                style = MaterialTheme.typography.labelLarge,
                                color = PixelColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            result.activeEquipmentEffects.forEach { effect ->
                                Text(
                                    text = "• ${effect.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PixelColors.AccentBlue
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 耐久
                    if (result.durabilityChanges.isNotEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🛡️ 耐久维护",
                                style = MaterialTheme.typography.labelLarge,
                                color = PixelColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            result.durabilityChanges.forEach { change ->
                                val (icon, color) = if (change.maintained)
                                    "✅" to PixelColors.AccentGreen
                                else
                                    "⚠️" to PixelColors.Warning
                                Text(
                                    text = "$icon ${change.equipmentName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = color
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 确认按钮
                    GlowButton(
                        text = "确认",
                        onClick = {
                            visible = false
                            onDismiss()
                        },
                        brush = PixelColors.GradientPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedRewardRow(
    icon: String,
    label: String,
    value: String,
    color: Color,
    delayMs: Int
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(
            tween(400, easing = EaseOutQuart),
            initialOffsetY = { it / 2 }
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.bodyLarge,
                color = PixelColors.TextSecondary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = color.copy(alpha = 0.5f),
                        blurRadius = 12f
                    )
                ),
                color = color,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun LevelUpOverlay(
    level: Int?,
    onDismiss: () -> Unit
) {
    if (level == null) return

    var visible by remember { mutableStateOf(false) }
    var showSparkles by remember { mutableStateOf(false) }

    LaunchedEffect(level) {
        delay(100)
        visible = true
        delay(400)
        showSparkles = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + scaleIn(
            tween(600, easing = EaseOutBack),
            initialScale = 0.3f
        ),
        exit = fadeOut(tween(300))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PixelColors.DeepSpace.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            if (showSparkles) {
                FloatingParticles(count = 30, colors = listOf(
                    PixelColors.AccentGold,
                    PixelColors.Primary,
                    PixelColors.AccentOrange
                ))
            }

            GlassCard(
                modifier = Modifier.padding(32.dp),
                glowColor = PixelColors.AccentGold.copy(alpha = 0.3f),
                contentPadding = PaddingValues(40.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // 皇冠动画
                    val crownScale by rememberInfiniteTransition(label = "crown").animateFloat(
                        initialValue = 1f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            tween(800, easing = EaseInOutSine),
                            RepeatMode.Reverse
                        ),
                        label = "crownScale"
                    )
                    Text(
                        text = "👑",
                        fontSize = 72.sp,
                        modifier = Modifier.scale(crownScale)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "角色升级",
                        style = MaterialTheme.typography.displaySmall,
                        color = PixelColors.AccentGold,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 等级变化
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Lv.${level - 1}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = PixelColors.TextMuted
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "→",
                            style = MaterialTheme.typography.headlineMedium,
                            color = PixelColors.AccentGold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Lv.$level",
                            style = MaterialTheme.typography.displaySmall.copy(
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = PixelColors.AccentGold.copy(alpha = 0.6f),
                                    blurRadius = 16f
                                )
                            ),
                            color = PixelColors.AccentGold,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 属性加成
                    Column(
                        modifier = Modifier
                            .background(
                                PixelColors.SurfaceElevated.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "❤️ HP上限 +5",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PixelColors.HpBar
                        )
                        Text(
                            text = "⚡ 精力上限 +1",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PixelColors.SpBar
                        )
                        Text(
                            text = "💰 等级奖励 +${level * 10}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PixelColors.AccentGold
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    GlowButton(
                        text = "太棒了！",
                        onClick = {
                            visible = false
                            onDismiss()
                        },
                        brush = Brush.horizontalGradient(
                            listOf(PixelColors.AccentGold, PixelColors.AccentOrange)
                        ),
                        glowColor = PixelColors.AccentGoldGlow
                    )
                }
            }
        }
    }
}

/** 浮动粒子效果 */
@Composable
private fun FloatingParticles(
    count: Int = 20,
    colors: List<Color> = listOf(
        PixelColors.AccentGold,
        PixelColors.Primary,
        PixelColors.AccentOrange,
        PixelColors.AccentGreen
    )
) {
    val particles = remember {
        List(count) {
            ParticleData(
                x = (0..1000).random() / 1000f,
                y = (0..1000).random() / 1000f,
                size = (3..10).random().dp,
                speed = (2000..5000).random().toFloat(),
                delay = (0..2000).random().toLong(),
                color = colors.random()
            )
        }
    }

    particles.forEach { particle ->
        val infiniteTransition = rememberInfiniteTransition(label = "particle")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -1.5f,
            animationSpec = infiniteRepeatable(
                animation = tween(particle.speed.toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(particle.delay.toInt())
            ),
            label = "particleY"
        )
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween((particle.speed / 2).toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
                initialStartOffset = StartOffset(particle.delay.toInt())
            ),
            label = "particleAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha * 0.6f)
        ) {
            Box(
                modifier = Modifier
                    .size(particle.size)
                    .offset(
                        x = (particle.x * 1000).dp,
                        y = (particle.y * 1000 + offsetY * 500).dp
                    )
                    .background(particle.color, RoundedCornerShape(50))
            )
        }
    }
}

private data class ParticleData(
    val x: Float,
    val y: Float,
    val size: androidx.compose.ui.unit.Dp,
    val speed: Float,
    val delay: Long,
    val color: Color
)

// === 缓动函数 ===
private val EaseOutBack: Easing = Easing {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    1f + c3 * kotlin.math.pow(it - 1f, 3f) + c1 * kotlin.math.pow(it - 1f, 2f)
}
private val EaseOutQuart: Easing = Easing { 1 - (1 - it) * (1 - it) * (1 - it) * (1 - it) }
private val EaseInOutSine: Easing = Easing { -(kotlin.math.cos(Math.PI * it) - 1) / 2 }
