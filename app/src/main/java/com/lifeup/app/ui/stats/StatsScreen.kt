package com.lifeup.app.ui.stats

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import com.lifeup.app.ui.components.ScreenScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.data.db.SkillCategory

private val SkillCategory.color: Color
    get() = when (this) {
        SkillCategory.LIVELIHOOD -> Color(0xFFFF8D6E)
        SkillCategory.SOCIAL -> Color(0xFFFF7043)
        SkillCategory.LANGUAGE -> Color(0xFF42A5F5)
        SkillCategory.LIFE -> Color(0xFF66BB6A)
        SkillCategory.PHYSICAL -> Color(0xFFEF5350)
        SkillCategory.MENTAL -> Color(0xFFAB47BC)
        SkillCategory.ART -> Color(0xFFFFA726)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenScaffold(
        title = "📊 技能统计",
        onNavigateBack = onNavigateBack
    ) {
        // Period selector
        item {
            PeriodSelector(
                selectedPeriod = uiState.period,
                onPeriodSelected = { viewModel.selectPeriod(it) }
            )
        }

        // Period navigation
        item {
            PeriodNavigation(
                displayRange = uiState.displayDateRange,
                onPrevious = { viewModel.selectPreviousPeriod() },
                onNext = { viewModel.selectNextPeriod() }
            )
        }

        // Summary cards
        item {
            SummaryCardsRow(
                totalInvestmentMinutes = uiState.totalInvestmentMinutes,
                totalGoldEarned = uiState.totalGoldEarned,
                totalSessions = uiState.totalSessions,
                investmentTrend = uiState.investmentTrend,
                trendPercentage = viewModel.getTrendPercentage(),
                formatMinutes = { viewModel.formatMinutes(it) }
            )
        }

        // Daily activity chart
        if (uiState.dailyData.isNotEmpty()) {
            item {
                DailyActivityChartCard(
                    dailyData = uiState.dailyData,
                    period = uiState.period
                )
            }
        }

        // Category distribution
        if (uiState.categoryDistribution.isNotEmpty()) {
            item {
                CategoryDistributionCard(
                    distribution = uiState.categoryDistribution
                )
            }
        }

        // Skill breakdown
        if (uiState.skillStats.isNotEmpty()) {
            item {
                Text(
                    text = "📋 技能明细",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }

            items(
                items = uiState.skillStats,
                key = { "stat-${it.skill.id}" }
            ) { stat ->
                SkillStatItem(
                    stat = stat,
                    maxMinutes = uiState.skillStats.first().totalMinutes,
                    formatMinutes = { viewModel.formatMinutes(it) }
                )
            }
        } else {
            item {
                EmptyStateMessage(text = "该时段暂无技能记录")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    selectedPeriod: StatsPeriod,
    onPeriodSelected: (StatsPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = selectedPeriod == StatsPeriod.WEEK,
                onClick = { onPeriodSelected(StatsPeriod.WEEK) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
            ) {
                Text("周")
            }
            SegmentedButton(
                selected = selectedPeriod == StatsPeriod.MONTH,
                onClick = { onPeriodSelected(StatsPeriod.MONTH) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
            ) {
                Text("月")
            }
            SegmentedButton(
                selected = selectedPeriod == StatsPeriod.YEAR,
                onClick = { onPeriodSelected(StatsPeriod.YEAR) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
            ) {
                Text("年")
            }
        }
    }
}

@Composable
private fun PeriodNavigation(
    displayRange: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "上一时段",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = displayRange,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "下一时段",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SummaryCardsRow(
    totalInvestmentMinutes: Int,
    totalGoldEarned: Int,
    totalSessions: Int,
    investmentTrend: TrendIndicator,
    trendPercentage: Int,
    formatMinutes: (Int) -> String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SummaryCard(
            icon = Icons.Default.Schedule,
            title = "投资时长",
            value = formatMinutes(totalInvestmentMinutes),
            trend = investmentTrend,
            trendPercentage = trendPercentage,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            icon = Icons.Default.MonetizationOn,
            title = "获得金币",
            value = "$totalGoldEarned",
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            icon = Icons.Default.Timer,
            title = "总次数",
            value = "$totalSessions",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    trend: TrendIndicator = TrendIndicator.NEUTRAL,
    trendPercentage: Int = 0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (trend != TrendIndicator.NEUTRAL && trendPercentage != 0) {
                Spacer(modifier = Modifier.height(4.dp))
                TrendBadge(trend = trend, percentage = trendPercentage)
            }
        }
    }
}

@Composable
private fun TrendBadge(trend: TrendIndicator, percentage: Int) {
    val isUp = trend == TrendIndicator.UP
    val color = if (isUp) Color(0xFF00BFA5) else Color(0xFFFF5252)
    val icon = if (isUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown
    val arrow = if (isUp) "↑" else "↓"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$arrow${kotlin.math.abs(percentage)}%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DailyActivityChartCard(
    dailyData: List<DailyData>,
    period: StatsPeriod
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📈 每日活动",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            DailyBarChart(
                data = dailyData,
                maxBars = when (period) {
                    StatsPeriod.WEEK -> 7
                    StatsPeriod.MONTH -> 31
                    StatsPeriod.YEAR -> 12
                }
            )
        }
    }
}

@Composable
private fun DailyBarChart(
    data: List<DailyData>,
    maxBars: Int
) {
    val maxMinutes = data.maxOfOrNull {
        it.investmentMinutes + it.consumptionMinutes
    }?.coerceAtLeast(1) ?: 1

    val displayData = if (data.size > maxBars) data.takeLast(maxBars) else data

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barCount = displayData.size.coerceAtLeast(1)
        val labelHeight = 28f
        val chartHeight = canvasHeight - labelHeight
        val barSpacing = 4f
        val totalSpacing = barSpacing * (barCount + 1)
        val barWidth = (canvasWidth - totalSpacing) / barCount

        displayData.forEachIndexed { index, item ->
            val x = barSpacing + index * (barWidth + barSpacing)
            val totalMins = item.investmentMinutes + item.consumptionMinutes
            val totalBarHeight = if (maxMinutes > 0) {
                (totalMins.toFloat() / maxMinutes) * (chartHeight - 20f)
            } else {
                0f
            }

            // Investment bar (bottom)
            val investHeight = if (maxMinutes > 0) {
                (item.investmentMinutes.toFloat() / maxMinutes) * (chartHeight - 20f)
            } else {
                0f
            }
            val consumeHeight = totalBarHeight - investHeight

            // Draw consumption bar (top part)
            if (consumeHeight > 0) {
                drawRoundRect(
                    color = Color(0xFFFF8A50).copy(alpha = 0.7f),
                    topLeft = Offset(x, chartHeight - totalBarHeight),
                    size = Size(barWidth, consumeHeight),
                    cornerRadius = if (investHeight == 0f) CornerRadius(4f, 4f) else CornerRadius(0f, 0f)
                )
            }

            // Draw investment bar (bottom part)
            if (investHeight > 0) {
                drawRoundRect(
                    color = Color(0xFF00BFA5).copy(alpha = 0.85f),
                    topLeft = Offset(x, chartHeight - investHeight),
                    size = Size(barWidth, investHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }

            // Date label at bottom
            if (barCount <= 12 || index % (barCount / 6).coerceAtLeast(1) == 0 || index == barCount - 1) {
                drawContext.canvas.nativeCanvas.drawText(
                    item.date,
                    x + barWidth / 2f,
                    canvasHeight,
                    android.graphics.Paint().apply {
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 18f
                        color = android.graphics.Color.GRAY
                        isAntiAlias = true
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryDistributionCard(
    distribution: List<CategoryDistribution>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "🍩 分类分布",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            distribution.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(item.category.color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.category.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(40.dp)
                    )
                    LinearProgressIndicator(
                        progress = { item.percentage },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = item.category.color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(item.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(36.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillStatItem(
    stat: SkillStat,
    maxMinutes: Int,
    formatMinutes: (Int) -> String
) {
    val categoryColor = stat.skill.category.color
    val progress = if (maxMinutes > 0) stat.totalMinutes.toFloat() / maxMinutes else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stat.skill.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stat.skill.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatMinutes(stat.totalMinutes),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = categoryColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "投资 ${formatMinutes(stat.investmentMinutes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF00BFA5)
                )
                Text(
                    text = "消耗 ${formatMinutes(stat.consumptionMinutes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFF8A50)
                )
                Text(
                    text = "${stat.sessions}次",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${stat.goldEarned}金",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFFFB300),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
