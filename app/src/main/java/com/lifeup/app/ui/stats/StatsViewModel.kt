package com.lifeup.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.domain.calculator.GoldCalculator
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.model.TimeRecord
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.SkillRepository
import com.lifeup.app.domain.repository.TimeRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

import androidx.compose.runtime.Immutable

enum class StatsPeriod { WEEK, MONTH, YEAR }

@Immutable
data class SkillStat(
    val skill: Skill,
    val investmentMinutes: Int,
    val consumptionMinutes: Int,
    val totalMinutes: Int,
    val sessions: Int,
    val goldEarned: Int
)

@Immutable
data class DailyData(
    val date: String,
    val investmentMinutes: Int,
    val consumptionMinutes: Int
)

@Immutable
data class StatsUiState(
    val period: StatsPeriod = StatsPeriod.WEEK,
    val startDate: String = "",
    val endDate: String = "",
    val displayDateRange: String = "",
    val totalInvestmentMinutes: Int = 0,
    val totalConsumptionMinutes: Int = 0,
    val totalGoldEarned: Int = 0,
    val totalSessions: Int = 0,
    val skillStats: List<SkillStat> = emptyList(),
    val dailyData: List<DailyData> = emptyList(),
    val categoryDistribution: List<CategoryDistribution> = emptyList(),
    val previousPeriodInvestment: Int = 0,
    val investmentTrend: TrendIndicator = TrendIndicator.NEUTRAL,
    val isLoading: Boolean = true
)

@Immutable
data class CategoryDistribution(
    val category: SkillCategory,
    val totalMinutes: Int,
    val percentage: Float
)

enum class TrendIndicator { UP, DOWN, NEUTRAL }

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val timeRecordRepository: TimeRecordRepository,
    private val skillRepository: SkillRepository,
    private val dailyStateRepository: DailyStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ISO_DATE
    private val displayRangeFormatter = DateTimeFormatter.ofPattern("M月d日", Locale.CHINESE)
    private val dayKeyFormatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.getDefault())
    private val dailyDisplayFormatter = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())

    init {
        selectPeriod(StatsPeriod.WEEK)
    }

    fun selectPeriod(period: StatsPeriod) {
        val today = LocalDate.now()
        val (startDate, endDate) = when (period) {
            StatsPeriod.WEEK -> {
                val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val end = today
                start to end
            }
            StatsPeriod.MONTH -> {
                val start = today.with(TemporalAdjusters.firstDayOfMonth())
                val end = today
                start to end
            }
            StatsPeriod.YEAR -> {
                val start = today.with(TemporalAdjusters.firstDayOfYear())
                val end = today
                start to end
            }
        }

        _uiState.update {
            it.copy(
                period = period,
                startDate = startDate.format(dateFormatter),
                endDate = endDate.format(dateFormatter),
                displayDateRange = "${startDate.format(displayRangeFormatter)} - ${endDate.format(displayRangeFormatter)}",
                isLoading = true
            )
        }
        loadPeriodData(startDate, endDate)
    }

    fun selectPreviousPeriod() {
        val current = _uiState.value
        val period = current.period
        val startDate = LocalDate.parse(current.startDate, dateFormatter)
        val endDate = LocalDate.parse(current.endDate, dateFormatter)

        val (newStart, newEnd) = when (period) {
            StatsPeriod.WEEK -> {
                startDate.minusWeeks(1) to endDate.minusWeeks(1)
            }
            StatsPeriod.MONTH -> {
                val newStart = startDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
                val newEnd = startDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth())
                newStart to newEnd
            }
            StatsPeriod.YEAR -> {
                val newStart = startDate.minusYears(1).with(TemporalAdjusters.firstDayOfYear())
                val newEnd = startDate.minusYears(1).with(TemporalAdjusters.lastDayOfYear())
                newStart to newEnd
            }
        }

        _uiState.update {
            it.copy(
                startDate = newStart.format(dateFormatter),
                endDate = newEnd.format(dateFormatter),
                displayDateRange = "${newStart.format(displayRangeFormatter)} - ${newEnd.format(displayRangeFormatter)}",
                isLoading = true
            )
        }
        loadPeriodData(newStart, newEnd)
    }

    fun selectNextPeriod() {
        val current = _uiState.value
        val period = current.period
        val startDate = LocalDate.parse(current.startDate, dateFormatter)
        val endDate = LocalDate.parse(current.endDate, dateFormatter)

        val (newStart, newEnd) = when (period) {
            StatsPeriod.WEEK -> {
                startDate.plusWeeks(1) to endDate.plusWeeks(1)
            }
            StatsPeriod.MONTH -> {
                val newStart = startDate.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth())
                val newEnd = startDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth())
                newStart to newEnd
            }
            StatsPeriod.YEAR -> {
                val newStart = startDate.plusYears(1).with(TemporalAdjusters.firstDayOfYear())
                val newEnd = startDate.plusYears(1).with(TemporalAdjusters.lastDayOfYear())
                newStart to newEnd
            }
        }

        _uiState.update {
            it.copy(
                startDate = newStart.format(dateFormatter),
                endDate = newEnd.format(dateFormatter),
                displayDateRange = "${newStart.format(displayRangeFormatter)} - ${newEnd.format(displayRangeFormatter)}",
                isLoading = true
            )
        }
        loadPeriodData(newStart, newEnd)
    }

    private fun loadPeriodData(startDate: LocalDate, endDate: LocalDate) {
        val startMs = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMs = endDate.atTime(23, 59, 59, 999_999_999)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        // Calculate previous period for comparison
        val prevStartDate = when (_uiState.value.period) {
            StatsPeriod.WEEK -> startDate.minusWeeks(1)
            StatsPeriod.MONTH -> startDate.minusMonths(1)
            StatsPeriod.YEAR -> startDate.minusYears(1)
        }
        val prevEndDate = when (_uiState.value.period) {
            StatsPeriod.WEEK -> endDate.minusWeeks(1)
            StatsPeriod.MONTH -> startDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth())
            StatsPeriod.YEAR -> startDate.minusYears(1).with(TemporalAdjusters.lastDayOfYear())
        }
        val prevStartMs = prevStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val prevEndMs = prevEndDate.atTime(23, 59, 59, 999_999_999)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        viewModelScope.launch {
            try {
                combine(
                    timeRecordRepository.getRecordsByDateRange(startMs, endMs),
                    skillRepository.getActiveSkills()
                ) { records, skills ->
                    Pair(records, skills)
                }.collect { (records, skills) ->
                    val skillMap = skills.associateBy { it.id }

                    // Calculate skill stats
                    val skillStats = records
                        .groupBy { it.skillId }
                        .mapNotNull { (skillId, skillRecords) ->
                            val skill = skillMap[skillId] ?: return@mapNotNull null
                            val investMins = skillRecords
                                .filter { it.recordType == RecordType.INVESTMENT }
                                .sumOf { it.durationMinutes }
                            val consumeMins = skillRecords
                                .filter { it.recordType == RecordType.CONSUMPTION }
                                .sumOf { it.durationMinutes }
                            val totalMins = investMins + consumeMins
                            val sessions = skillRecords.size
                            val gold = skillRecords.sumOf { record ->
                                GoldCalculator.calculateGold(
                                    minutes = record.durationMinutes,
                                    isInvestment = record.recordType == RecordType.INVESTMENT,
                                    isFirstTimerToday = false,
                                    skillLevel = skill.level
                                )
                            }
                            SkillStat(
                                skill = skill,
                                investmentMinutes = investMins,
                                consumptionMinutes = consumeMins,
                                totalMinutes = totalMins,
                                sessions = sessions,
                                goldEarned = gold
                            )
                        }
                        .sortedByDescending { it.totalMinutes }

                    // Calculate daily data
                    val dailyMap = mutableMapOf<String, DailyData>()
                    var iterDate = startDate
                    while (!iterDate.isAfter(endDate)) {
                        val key = iterDate.format(dayKeyFormatter)
                        dailyMap[key] = DailyData(
                            date = iterDate.format(dailyDisplayFormatter),
                            investmentMinutes = 0,
                            consumptionMinutes = 0
                        )
                        iterDate = iterDate.plusDays(1)
                    }

                    for (record in records) {
                        val recDate = java.time.Instant.ofEpochMilli(record.startTime)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        val key = recDate.format(dayKeyFormatter)
                        dailyMap[key]?.let { existing ->
                            dailyMap[key] = if (record.recordType == RecordType.INVESTMENT) {
                                existing.copy(investmentMinutes = existing.investmentMinutes + record.durationMinutes)
                            } else {
                                existing.copy(consumptionMinutes = existing.consumptionMinutes + record.durationMinutes)
                            }
                        }
                    }

                    val dailyData = dailyMap.values.toList()

                    // Calculate category distribution
                    val categoryMinutes = mutableMapOf<SkillCategory, Int>()
                    for (stat in skillStats) {
                        categoryMinutes[stat.skill.category] =
                            (categoryMinutes[stat.skill.category] ?: 0) + stat.totalMinutes
                    }
                    val totalAllMinutes = categoryMinutes.values.sum().coerceAtLeast(1)
                    val categoryDistribution = categoryEntries.mapNotNull { cat ->
                        val mins = categoryMinutes[cat] ?: return@mapNotNull null
                        CategoryDistribution(
                            category = cat,
                            totalMinutes = mins,
                            percentage = mins.toFloat() / totalAllMinutes
                        )
                    }.sortedByDescending { it.totalMinutes }

                    val totalInvestment = records
                        .filter { it.recordType == RecordType.INVESTMENT }
                        .sumOf { it.durationMinutes }
                    val totalConsumption = records
                        .filter { it.recordType == RecordType.CONSUMPTION }
                        .sumOf { it.durationMinutes }
                    val totalGold = skillStats.sumOf { it.goldEarned }
                    val totalSessions = records.size

                    // Load previous period data for comparison
                    val prevRecords = try {
                        val flow = timeRecordRepository.getRecordsByDateRange(prevStartMs, prevEndMs)
                        var result: List<TimeRecord> = emptyList()
                        flow.collect { result = it }
                        result
                    } catch (e: Exception) {
                        emptyList()
                    }
                    val prevInvestment = prevRecords
                        .filter { it.recordType == RecordType.INVESTMENT }
                        .sumOf { it.durationMinutes }

                    val trend = when {
                        prevInvestment == 0 && totalInvestment > 0 -> TrendIndicator.UP
                        prevInvestment == 0 -> TrendIndicator.NEUTRAL
                        totalInvestment > prevInvestment -> TrendIndicator.UP
                        totalInvestment < prevInvestment -> TrendIndicator.DOWN
                        else -> TrendIndicator.NEUTRAL
                    }

                    _uiState.update {
                        it.copy(
                            totalInvestmentMinutes = totalInvestment,
                            totalConsumptionMinutes = totalConsumption,
                            totalGoldEarned = totalGold,
                            totalSessions = totalSessions,
                            skillStats = skillStats,
                            dailyData = dailyData,
                            categoryDistribution = categoryDistribution,
                            previousPeriodInvestment = prevInvestment,
                            investmentTrend = trend,
                            isLoading = false
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun formatMinutes(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours >= 24 -> {
                val days = hours / 24
                val remainHours = hours % 24
                if (remainHours > 0) "${days}天${remainHours}时" else "${days}天"
            }
            hours > 0 -> "${hours}时${mins}分"
            else -> "${mins}分"
        }
    }

    fun getTrendPercentage(): Int {
        val current = _uiState.value.totalInvestmentMinutes
        val previous = _uiState.value.previousPeriodInvestment
        if (previous == 0) return if (current > 0) 100 else 0
        return ((current - previous) * 100 / previous).let { diff ->
            diff.coerceIn(-999, 999)
        }
    }

    companion object {
        private val categoryEntries = SkillCategory.values()
    }
}
