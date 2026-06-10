package com.lifeup.app.ui.ledger

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeup.app.data.db.entity.TimeAssetEntity
import com.lifeup.app.data.db.entity.TimeSessionEntity
import com.lifeup.app.ui.components.*
import com.lifeup.app.ui.theme.PixelColors

@Composable
fun LedgerScreen(
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val todayAsset by viewModel.todayAsset.collectAsState()
    val todaySessions by viewModel.todaySessions.collectAsState()
    val weeklyAssets by viewModel.weeklyAssets.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val scrollState = rememberScrollState()

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
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📊 时间账本",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PixelColors.AccentGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "追踪你的时间投资与消耗",
                        style = MaterialTheme.typography.labelMedium,
                        color = PixelColors.TextMuted
                    )
                }
                IconButton(
                    onClick = { viewModel.refresh() },
                    modifier = Modifier
                        .background(PixelColors.SurfaceElevated, RoundedCornerShape(12.dp))
                        .border(1.dp, PixelColors.Border, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "刷新",
                        tint = PixelColors.TextSecondary
                    )
                }
            }
        }

        // View Mode Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LedgerViewModel.ViewMode.entries.forEach { mode ->
                val isSelected = viewMode == mode
                val label = when (mode) {
                    LedgerViewModel.ViewMode.DAILY -> "日"
                    LedgerViewModel.ViewMode.WEEKLY -> "周"
                    LedgerViewModel.ViewMode.MONTHLY -> "月"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) PixelColors.Primary.copy(alpha = 0.2f)
                            else PixelColors.SurfaceElevated,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) PixelColors.Primary.copy(alpha = 0.5f)
                            else PixelColors.Border,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.setViewMode(mode) }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) PixelColors.Primary else PixelColors.TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content based on view mode
        if (viewMode == LedgerViewModel.ViewMode.DAILY) {
            todayAsset?.let { asset ->
                TimeBalanceCard(asset = asset)
            } ?: EmptyStateView(
                icon = "📊",
                title = "暂无今日数据",
                subtitle = "开始一项活动来记录时间"
            )

            if (todaySessions.isNotEmpty()) {
                SectionHeader(title = "今日时间分布", accentColor = PixelColors.AccentBlue)
                TimeDistributionChart(sessions = todaySessions)
            }
        } else {
            if (weeklyAssets.isNotEmpty()) {
                SectionHeader(
                    title = if (viewMode == LedgerViewModel.ViewMode.WEEKLY) "近7天趋势" else "近30天趋势",
                    accentColor = PixelColors.AccentPurple
                )
                TrendChart(assets = weeklyAssets)
            } else {
                EmptyStateView(
                    icon = "📈",
                    title = "暂无趋势数据",
                    subtitle = "坚持记录时间，查看趋势"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TimeBalanceCard(asset: TimeAssetEntity) {
    val investedHours = asset.investedMinutes / 60.0
    val investedMins = asset.investedMinutes % 60
    val consumedHours = asset.consumedMinutes / 60.0
    val consumedMins = asset.consumedMinutes % 60
    val totalMinutes = asset.totalMinutes
    val ratio = if (totalMinutes > 0) asset.investmentRatio else 0.0

    GlassCard(glowColor = if (ratio >= 0.7)
        PixelColors.AccentGreen.copy(alpha = 0.1f)
    else PixelColors.AccentOrange.copy(alpha = 0.1f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 时间资产负债表",
                    style = MaterialTheme.typography.titleMedium,
                    color = PixelColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(
                    text = String.format("%.0f%%", ratio * 100),
                    color = if (ratio >= 0.7) PixelColors.AccentGreen else PixelColors.AccentOrange
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BigStat(
                    icon = "📈",
                    value = String.format("%.1f", investedHours),
                    unit = "小时",
                    label = "投资",
                    color = PixelColors.Investment
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(60.dp)
                        .background(PixelColors.Divider)
                )
                BigStat(
                    icon = "📉",
                    value = String.format("%.1f", consumedHours),
                    unit = "小时",
                    label = "消耗",
                    color = PixelColors.Consumption
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress bar
            GlowProgressBar(
                progress = ratio.toFloat(),
                progressBrush = if (ratio >= 0.7) Brush.horizontalGradient(
                    listOf(PixelColors.AccentGreen, PixelColors.TertiaryVariant)
                ) else Brush.horizontalGradient(
                    listOf(PixelColors.AccentOrange, PixelColors.Primary)
                ),
                glowColor = if (ratio >= 0.7) PixelColors.InvestmentGlow else Color(0x40FF8C42),
                height = 10.dp,
                label = if (ratio >= 0.7) "投资比率 ✅" else "投资比率 ⏳"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Detail stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DetailStat(label = "会话", value = "${asset.sessionsCount}")
                DetailStat(label = "经验", value = "${asset.expEarned}")
                DetailStat(label = "金币", value = "${asset.goldEarned}")
            }
        }
    }
}

@Composable
private fun BigStat(
    icon: String,
    value: String,
    unit: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = color.copy(alpha = 0.4f),
                        blurRadius = 12f
                    )
                ),
                color = color,
                fontWeight = FontWeight.ExtraBold
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

@Composable
private fun DetailStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = PixelColors.AccentGold,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = PixelColors.TextMuted
        )
    }
}

@Composable
private fun TimeDistributionChart(sessions: List<TimeSessionEntity>) {
    val grouped = sessions.groupBy { it.linkedSkillId ?: it.category }
        .mapValues { entry -> entry.value.sumOf { it.durationMinutes } }
        .toList()
        .sortedByDescending { it.second }

    val maxMinutes = grouped.maxOfOrNull { it.second } ?: 1

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        grouped.forEach { (key, minutes) ->
            val progress = if (maxMinutes > 0) minutes.toFloat() / maxMinutes.toFloat() else 0f
            val hours = minutes / 60
            val mins = minutes % 60
            val timeStr = if (hours > 0) "${hours}h${mins}m" else "${mins}m"
            val session = sessions.find { (it.linkedSkillId ?: it.category) == key }
            val isInvestment = session?.isInvestment ?: true
            val barColor = if (isInvestment) PixelColors.AccentGreen else PixelColors.AccentRed

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isInvestment) "▮" else "▯",
                    color = barColor,
                    fontSize = 12.sp,
                    modifier = Modifier.width(20.dp)
                )
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelMedium,
                    color = PixelColors.TextSecondary,
                    modifier = Modifier.width(52.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(PixelColors.SurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(barColor, barColor.copy(alpha = 0.7f))
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun TrendChart(assets: List<TimeAssetEntity>) {
    if (assets.isEmpty()) return

    val maxRatio = assets.maxOfOrNull { it.investmentRatio.toFloat() }?.coerceAtLeast(0.1f) ?: 1f
    val days = listOf("一", "二", "三", "四", "五", "六", "日")

    GlassCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                assets.takeLast(7).forEachIndexed { index, asset ->
                    val height = (asset.investmentRatio / maxRatio.toDouble() * 120).toInt().coerceAtLeast(8)
                    val barColor = when {
                        asset.investmentRatio >= 0.7 -> PixelColors.AccentGreen
                        asset.investmentRatio >= 0.5 -> PixelColors.AccentOrange
                        else -> PixelColors.AccentRed
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%.0f%%", asset.investmentRatio * 100),
                            style = MaterialTheme.typography.labelSmall,
                            color = PixelColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(height.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(barColor, barColor.copy(alpha = 0.5f))
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = days.getOrElse(index) { "${index + 1}" },
                            style = MaterialTheme.typography.labelSmall,
                            color = PixelColors.TextMuted
                        )
                    }
                }
            }
        }
    }
}
