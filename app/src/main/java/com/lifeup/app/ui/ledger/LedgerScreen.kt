package com.lifeup.app.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.lifeup.app.ui.components.ScreenScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.data.db.RecordType
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
fun LedgerScreen(
    onNavigateBack: () -> Unit,
    viewModel: LedgerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val filteredGroupedEntries = if (uiState.filterType != null) {
        uiState.groupedEntries.mapValues { (_, entries) ->
            entries.filter { it.recordType == uiState.filterType }
        }.filter { (_, entries) -> entries.isNotEmpty() }
    } else {
        uiState.groupedEntries
    }

    ScreenScaffold(
        title = "📒 时间账本",
        onNavigateBack = onNavigateBack
    ) {
        // Month selector
        item {
            MonthSelector(
                displayMonth = uiState.displayMonth,
                onPrevious = { viewModel.selectPreviousMonth() },
                onNext = { viewModel.selectNextMonth() }
            )
        }

        // Filter chips
        item {
            FilterChipRow(
                selectedFilter = uiState.filterType,
                onFilterSelected = { viewModel.setFilterType(it) }
            )
        }

        when {
            uiState.isLoading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            filteredGroupedEntries.isEmpty() -> {
                item {
                    EmptyStateMessage(
                        text = if (uiState.filterType != null) "该筛选条件下暂无记录" else "该月暂无时间记录"
                    )
                }
            }
            else -> {
                // Monthly summary card
                item {
                    MonthlySummaryCard(
                        summary = uiState.monthlySummary,
                        formatMinutes = { viewModel.formatMinutes(it) }
                    )
                }

                // Daily entries
                filteredGroupedEntries.forEach { (dateHeader, entries) ->
                    item {
                        DateHeader(
                            date = dateHeader,
                            totalMinutes = entries.sumOf { it.durationMinutes },
                            netGold = entries.sumOf {
                                if (it.recordType == RecordType.INVESTMENT) it.goldAmount else -it.goldAmount
                            },
                            formatMinutes = { viewModel.formatMinutes(it) }
                        )
                    }

                    items(
                        items = entries,
                        key = { "entry-${it.timestamp}-${it.skillName}" }
                    ) { entry ->
                        LedgerEntryItem(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSelector(
    displayMonth: String,
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
                contentDescription = "上一月",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = displayMonth,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "下一月",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    summary: MonthlySummary,
    formatMinutes: (Int) -> String
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
                text = "📊 月度概览",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Investment vs Consumption row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatMinutes(summary.totalInvestmentMinutes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00BFA5)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "投资时长",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatMinutes(summary.totalConsumptionMinutes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF8A50)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "消耗时长",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${summary.totalGoldEarned}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB300)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "金币收入",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${summary.totalGoldConsumed}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF8A50)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "金币支出",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Net gold and stats row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (summary.netGold >= 0) Color(0xFF00BFA5).copy(alpha = 0.1f)
                        else Color(0xFFFF5252).copy(alpha = 0.1f)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "净金币",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (summary.netGold >= 0) "+${summary.netGold}" else "${summary.netGold}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.netGold >= 0) Color(0xFF00BFA5) else Color(0xFFFF5252)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "活跃 ${summary.daysActive} 天",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "日均投资 ${formatMinutes(summary.averageDailyInvestment)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateHeader(
    date: String,
    totalMinutes: Int,
    netGold: Int,
    formatMinutes: (Int) -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatMinutes(totalMinutes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (netGold >= 0) "+${netGold}金" else "${netGold}金",
                style = MaterialTheme.typography.labelMedium,
                color = if (netGold >= 0) Color(0xFF00BFA5) else Color(0xFFFF5252),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LedgerEntryItem(entry: LedgerEntry) {
    val isInvestment = entry.recordType == RecordType.INVESTMENT
    val categoryColor = entry.skillCategory.color

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(categoryColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Skill name and category
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.skillName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = entry.skillCategory.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Duration
            Text(
                text = "${entry.durationMinutes}分钟",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Type badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isInvestment) Color(0xFF00BFA5).copy(alpha = 0.15f)
                        else Color(0xFFFF8A50).copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isInvestment) "投资" else "消耗",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isInvestment) Color(0xFF00BFA5) else Color(0xFFFF8A50),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Gold amount
            Text(
                text = if (isInvestment) "+${entry.goldAmount}" else "-${entry.goldAmount}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isInvestment) Color(0xFF00BFA5) else Color(0xFFFF8A50)
            )
        }
    }
}

@Composable
private fun FilterChipRow(
    selectedFilter: RecordType?,
    onFilterSelected: (RecordType?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            label = "全部",
            selected = selectedFilter == null,
            onClick = { onFilterSelected(null) },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            label = "投资",
            selected = selectedFilter == RecordType.INVESTMENT,
            onClick = { onFilterSelected(RecordType.INVESTMENT) },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            label = "消耗",
            selected = selectedFilter == RecordType.CONSUMPTION,
            onClick = { onFilterSelected(RecordType.CONSUMPTION) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
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
