package com.lifeup.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeup.app.ui.theme.PixelColors
import kotlinx.coroutines.delay

// ============================================
// 精致组件库 — 玻璃拟态 + 发光效果 + 精致动画
// ============================================

/** 玻璃拟态卡片 — 半透明背景 + 微妙边框 + 顶部高光 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color = PixelColors.Border,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = glowColor.copy(alpha = 0.15f),
                spotColor = glowColor.copy(alpha = 0.1f)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PixelColors.Surface.copy(alpha = 0.95f),
                        PixelColors.Surface.copy(alpha = 0.85f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

/** 带发光效果的进度条 */
@Composable
fun GlowProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = PixelColors.SurfaceVariant,
    progressBrush: Brush = PixelColors.GradientExp,
    glowColor: Color = PixelColors.AccentGoldGlow,
    height: Dp = 10.dp,
    label: String? = null,
    labelColor: Color = PixelColors.TextSecondary,
    animate: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = if (animate) 800 else 0, easing = EaseOutQuart),
        label = "progress"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = labelColor
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            // 轨道
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(height / 2))
                    .background(trackColor)
            )
            // 发光层
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(height / 2))
                    .background(glowColor)
                    .padding(end = 4.dp)
            )
            // 进度条
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(height / 2))
                    .background(progressBrush)
            )
            // 高光线
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(2.dp)
                    .clip(RoundedCornerShape(height / 2))
                    .background(Color.White.copy(alpha = 0.3f))
                    .align(Alignment.TopCenter)
            )
        }
    }
}

/** 圆形发光头像/图标容器 */
@Composable
fun GlowAvatar(
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    glowColor: Color = PixelColors.AccentGold,
    backgroundColor: Color = PixelColors.SurfaceElevated,
    borderWidth: Dp = 2.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = glowColor.copy(alpha = 0.3f),
                spotColor = glowColor.copy(alpha = 0.2f)
            )
            .background(backgroundColor, CircleShape)
            .border(borderWidth, glowColor.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = (size.value * 0.45).sp)
    }
}

/** 渐变发光按钮 */
@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    brush: Brush = PixelColors.GradientPrimary,
    glowColor: Color = PixelColors.PrimaryGlow
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.98f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = glowColor,
                spotColor = glowColor.copy(alpha = 0.5f)
            )
            .background(brush, RoundedCornerShape(16.dp))
            .clickable(
                enabled = enabled,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = Color.White.copy(alpha = 0.2f))
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

/** 统计数字 — 带发光效果 */
@Composable
fun StatBadge(
    icon: String,
    value: String,
    label: String? = null,
    color: Color = PixelColors.AccentGold,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    shadow = Shadow(color = color.copy(alpha = 0.5f), blurRadius = 8f)
                ),
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        if (label != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = PixelColors.TextMuted
            )
        }
    }
}

/** 章节标题 — 带渐变下划线 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    accentColor: Color = PixelColors.AccentGold
) {
    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = accentColor,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor.copy(alpha = 0.6f))
        )
    }
}

/** 空状态 — 带呼吸动画 */
@Composable
fun EmptyStateView(
    icon: String,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathe")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 56.sp,
            modifier = Modifier.scale(scale)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = PixelColors.TextSecondary.copy(alpha = alpha)
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = PixelColors.TextMuted
            )
        }
    }
}

/** 脉冲动画指示器 */
@Composable
fun PulseIndicator(
    color: Color = PixelColors.AccentGreen,
    size: Dp = 8.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size * scale)
                .background(color.copy(alpha = alpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(size)
                .background(color, CircleShape)
        )
    }
}

/** 标签/徽章 */
@Composable
fun StatusBadge(
    text: String,
    color: Color = PixelColors.AccentGreen,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/** 分隔线 — 渐变 */
@Composable
fun GradientDivider(
    modifier: Modifier = Modifier,
    colorStart: Color = PixelColors.Border,
    colorEnd: Color = Color.Transparent
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                brush = Brush.horizontalGradient(listOf(colorStart, colorEnd)),
                shape = RoundedCornerShape(0.5.dp)
            )
    )
}

// === 缓动函数 ===
private val EaseOutQuart: Easing = Easing { 1 - (1 - it) * (1 - it) * (1 - it) * (1 - it) }
private val EaseInOutSine: Easing = Easing { -(kotlin.math.cos(Math.PI * it) - 1) / 2 }.toFloat()
private val EaseOutQuad: Easing = Easing { 1 - (1 - it) * (1 - it) }
private val EaseInOutQuart: Easing = Easing {
    if (it < 0.5f) 8 * it * it * it * it else 1 - kotlin.math.pow(-2 * it + 2, 4f) / 2
}
