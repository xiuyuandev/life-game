package com.lifeup.app.ui.review

import android.app.DatePickerDialog
import androidx.compose.foundation.IntrinsicSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.lifeup.app.ui.components.ScreenScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.domain.model.TimeRecord
import com.lifeup.app.ui.theme.CategoryArt
import com.lifeup.app.ui.theme.CategoryLanguage
import com.lifeup.app.ui.theme.CategoryLife
import com.lifeup.app.ui.theme.CategoryLivelihood
import com.lifeup.app.ui.theme.CategoryMental
import com.lifeup.app.ui.theme.CategoryPhysical
import com.lifeup.app.ui.theme.CategorySocial
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val SkillCategory.color: Color
    get() = when (this) {
        SkillCategory.LIVELIHOOD -> CategoryLivelihood
        SkillCategory.SOCIAL -> CategorySocial
        SkillCategory.LANGUAGE -> CategoryLanguage
        SkillCategory.LIFE -> CategoryLife
        SkillCategory.PHYSICAL -> CategoryPhysical
        SkillCategory.MENTAL -> CategoryMental
        SkillCategory.ART -> CategoryArt
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
fun ReviewScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenScaffold(
        title = "📊 柳比歇夫复盘",
        onNavigateBack = onNavigateBack
    ) {
        // Date Selector
        item {
            DateSelector(
                displayDate = uiState.displayDate,
                onPreviousDay = { viewModel.selectPreviousDay() },
                onNextDay = { viewModel.selectNextDay() },
                selectedDate = uiState.selectedDate,
                onDateSelected = { viewModel.selectDate(it) }
            )
        }

        // Daily Summary Card
        item {
            DailySummaryCard(
                investmentMinutes = uiState.investmentMinutes,
                consumptionMinutes = uiState.consumptionMinutes,
                totalMinutes = uiState.totalMinutes,
                investmentRatio = uiState.investmentRatio,
                consumptionRatio = uiState.consumptionRatio,
                streakCount = uiState.streakCount,
                formatMinutes = { viewModel.formatMinutes(it) }
            )
        }

        // Daily Insight Card
        item {
            DailyInsightCard(
                investmentMinutes = uiState.investmentMinutes,
                investmentToConsumptionRatio = uiState.investmentToConsumptionRatio,
                mostFocusedSkill = uiState.mostFocusedSkill,
                streakCount = uiState.streakCount,
                formatMinutes = { viewModel.formatMinutes(it) }
            )
        }

        // Timeline Section
        item {
            Text(
                text = "⏱️ 时间线",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        if (uiState.timeRecords.isEmpty()) {
            item {
                EmptyStateMessage(text = "这一天没有时间记录")
            }
        } else {
            items(
                items = uiState.timeRecords,
                key = { it.id }
            ) { record ->
                val skill = uiState.skills.find { it.id == record.skillId }
                TimelineItem(
                    record = record,
                    skillName = skill?.name ?: "未知技能",
                    skillCategory = skill?.category,
                    formatTime = { viewModel.formatTime(it) },
                    formatMinutes = { viewModel.formatMinutes(it) }
                )
            }
        }

        // Skill Breakdown Section
        if (uiState.skillBreakdowns.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📈 技能分布",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(
                items = uiState.skillBreakdowns,
                key = { "breakdown-${it.skill.id}" }
            ) { breakdown ->
                SkillBreakdownItem(
                    breakdown = breakdown,
                    maxMinutes = uiState.skillBreakdowns.first().minutes,
                    formatMinutes = { viewModel.formatMinutes(it) }
                )
            }
        }
    }
}

@Composable
private fun DateSelector(
    displayDate: String,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousDay) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "前一天",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        TextButton(onClick = {
            val cal = Calendar.getInstance()
            try {
                cal.time = dateFormat.parse(selectedDate)!!
            } catch (e: Exception) {
                // use today
            }
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val pickedCal = Calendar.getInstance()
                    pickedCal.set(year, month, dayOfMonth)
                    onDateSelected(dateFormat.format(pickedCal.time))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }) {
            Text(
                text = displayDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(onClick = onNextDay) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "后一天",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DailySummaryCard(
    investmentMinutes: Int,
    consumptionMinutes: Int,
    totalMinutes: Int,
    investmentRatio: Float,
    consumptionRatio: Float,
    streakCount: Int,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 每日概览",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (streakCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "连续打卡",
                            tint = Color(0xFFFF6D00),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "连续 $streakCount 天",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFF6D00),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Investment vs Consumption bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                if (totalMinutes > 0) {
                    val investWeight = investmentRatio.coerceIn(0.05f, 0.95f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(investWeight)
                            .background(
                                color = Color(0xFF00BFA5),
                                shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (investmentRatio > 0.15f) {
                            Text(
                                text = "投资",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f - investWeight)
                            .background(
                                color = Color(0xFFFF8A50),
                                shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (consumptionRatio > 0.15f) {
                            Text(
                                text = "消耗",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00BFA5))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "投资 ${formatMinutes(investmentMinutes)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF8A50))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "消耗 ${formatMinutes(consumptionMinutes)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Text(
                    text = "总计 ${formatMinutes(totalMinutes)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DailyInsightCard(
    investmentMinutes: Int,
    investmentToConsumptionRatio: String,
    mostFocusedSkill: com.lifeup.app.domain.model.Skill?,
    streakCount: Int,
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
                text = "💡 今日洞察",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            InsightRow(
                label = "今日投资时长",
                value = formatMinutes(investmentMinutes)
            )

            Spacer(modifier = Modifier.height(8.dp))

            InsightRow(
                label = "投资/消耗比",
                value = "${investmentToConsumptionRatio}:1"
            )

            Spacer(modifier = Modifier.height(8.dp))

            InsightRow(
                label = "最专注技能",
                value = mostFocusedSkill?.name ?: "无"
            )

            if (streakCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                InsightRow(
                    label = "连续打卡",
                    value = "$streakCount 天"
                )
            }
        }
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TimelineItem(
    record: TimeRecord,
    skillName: String,
    skillCategory: SkillCategory?,
    formatTime: (Long) -> String,
    formatMinutes: (Int) -> String
) {
    val categoryColor = skillCategory?.color ?: MaterialTheme.colorScheme.primary
    val isInvestment = record.recordType == RecordType.INVESTMENT

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Timeline line and dot
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(categoryColor)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${formatTime(record.startTime)} - ${formatTime(record.endTime)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (skillCategory != null) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = skillName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!record.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = record.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatMinutes(record.durationMinutes),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
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
                }
            }
        }
    }
}

@Composable
private fun SkillBreakdownItem(
    breakdown: ReviewUiState.SkillBreakdown,
    maxMinutes: Int,
    formatMinutes: (Int) -> String
) {
    val categoryColor = breakdown.skill.category.color
    val progress = if (maxMinutes > 0) breakdown.minutes.toFloat() / maxMinutes else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = breakdown.skill.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = breakdown.skill.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = categoryColor
                    )
                }
                Text(
                    text = formatMinutes(breakdown.minutes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
