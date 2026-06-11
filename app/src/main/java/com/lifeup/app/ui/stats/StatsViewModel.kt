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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

enum class StatsPeriod { WEEK, MONTH, YEAR }

data class SkillStat(
    val skill: Skill,
    val investmentMinutes: Int,
    val consumptionMinutes: Int,
    val totalMinutes: Int,
    val sessions: Int,
    val goldEarned: Int
)

data class DailyData(
    val date: String,
    val investmentMinutes: Int,
    val consumptionMinutes: Int
)

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

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayRangeFormat = SimpleDateFormat("M月d日", Locale.CHINESE)

    init {
        selectPeriod(StatsPeriod.WEEK)
    }

    fun selectPeriod(period: StatsPeriod) {
        val cal = Calendar.getInstance()
        val (startCal, endCal) = when (period) {
            StatsPeriod.WEEK -> {
                // Current week (Monday to Sunday)
                val end = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val start = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (after(end)) add(Calendar.WEEK_OF_YEAR, -1)
                }
                start to end
            }
            StatsPeriod.MONTH -> {
                val end = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val start = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                start to end
            }
            StatsPeriod.YEAR -> {
                val end = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val start = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                start to end
            }
        }

        _uiState.update {
            it.copy(
                period = period,
                startDate = dateFormat.format(startCal.time),
                endDate = dateFormat.format(endCal.time),
                displayDateRange = "${displayRangeFormat.format(startCal.time)} - ${displayRangeFormat.format(endCal.time)}",
                isLoading = true
            )
        }
        loadPeriodData(startCal, endCal)
    }

    fun selectPreviousPeriod() {
        val current = _uiState.value
        val period = current.period
        val startCal = Calendar.getInstance().apply {
            time = dateFormat.parse(current.startDate)!!
        }
        val endCal = Calendar.getInstance().apply {
            time = dateFormat.parse(current.endDate)!!
        }

        when (period) {
            StatsPeriod.WEEK -> {
                startCal.add(Calendar.WEEK_OF_YEAR, -1)
                endCal.add(Calendar.WEEK_OF_YEAR, -1)
                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)
                endCal.set(Calendar.MILLISECOND, 999)
            }
            StatsPeriod.MONTH -> {
                startCal.add(Calendar.MONTH, -1)
                endCal.add(Calendar.MONTH, -1)
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)
                endCal.set(Calendar.MILLISECOND, 999)
            }
            StatsPeriod.YEAR -> {
                startCal.add(Calendar.YEAR, -1)
                endCal.add(Calendar.YEAR, -1)
                endCal.set(Calendar.DAY_OF_YEAR, endCal.getActualMaximum(Calendar.DAY_OF_YEAR))
                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)
                endCal.set(Calendar.MILLISECOND, 999)
            }
        }

        _uiState.update {
            it.copy(
                startDate = dateFormat.format(startCal.time),
                endDate = dateFormat.format(endCal.time),
                displayDateRange = "${displayRangeFormat.format(startCal.time)} - ${displayRangeFormat.format(endCal.time)}",
                isLoading = true
            )
        }
        loadPeriodData(startCal, endCal)
    }

    fun selectNextPeriod() {
        val current = _uiState.value
        val period = current.period
        val startCal = Calendar.getInstance().apply {
            time = dateFormat.parse(current.startDate)!!
        }
        val endCal = Calendar.getInstance().apply {
            time = dateFormat.parse(current.endDate)!!
        }

        when (period) {
            StatsPeriod.WEEK -> {
                startCal.add(Calendar.WEEK_OF_YEAR, 1)
                endCal.add(Calendar.WEEK_OF_YEAR, 1)
                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)
                endCal.set(Calendar.MILLISECOND, 999)
            }
            StatsPeriod.MONTH -> {
                startCal.add(Calendar.MONTH, 1)
                endCal.add(Calendar.MONTH, 1)
                endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)
                endCal.set(Calendar.MILLISECOND, 999)
            }
            StatsPeriod.YEAR -> {
                startCal.add(Calendar.YEAR, 1)
                endCal.add(Calendar.YEAR, 1)
                endCal.set(Calendar.DAY_OF_YEAR, endCal.getActualMaximum(Calendar.DAY_OF_YEAR))
                endCal.set(Calendar.HOUR_OF_DAY, 23)
                endCal.set(Calendar.MINUTE, 59)
                endCal.set(Calendar.SECOND, 59)
                endCal.set(Calendar.MILLISECOND, 999)
            }
        }

        _uiState.update {
            it.copy(
                startDate = dateFormat.format(startCal.time),
                endDate = dateFormat.format(endCal.time),
                displayDateRange = "${displayRangeFormat.format(startCal.time)} - ${displayRangeFormat.format(endCal.time)}",
                isLoading = true
            )
        }
        loadPeriodData(startCal, endCal)
    }

    private fun loadPeriodData(startCal: Calendar, endCal: Calendar) {
        val startMs = startCal.timeInMillis
        val endMs = endCal.timeInMillis

        // Calculate previous period for comparison
        val prevStartCal = startCal.clone() as Calendar
        val prevEndCal = endCal.clone() as Calendar
        when (_uiState.value.period) {
            StatsPeriod.WEEK -> {
                prevStartCal.add(Calendar.WEEK_OF_YEAR, -1)
                prevEndCal.add(Calendar.WEEK_OF_YEAR, -1)
            }
            StatsPeriod.MONTH -> {
                prevStartCal.add(Calendar.MONTH, -1)
                prevEndCal.add(Calendar.MONTH, -1)
            }
            StatsPeriod.YEAR -> {
                prevStartCal.add(Calendar.YEAR, -1)
                prevEndCal.add(Calendar.YEAR, -1)
            }
        }
        val prevStartMs = prevStartCal.timeInMillis
        val prevEndMs = prevEndCal.timeInMillis

        viewModelScope.launch {
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
                val dayFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val dailyMap = mutableMapOf<String, DailyData>()
                val iterCal = (startCal.clone() as Calendar)
                while (iterCal.timeInMillis <= endCal.timeInMillis) {
                    val key = dayFormat.format(iterCal.time)
                    dailyMap[key] = DailyData(
                        date = SimpleDateFormat("MM/dd", Locale.getDefault()).format(iterCal.time),
                        investmentMinutes = 0,
                        consumptionMinutes = 0
                    )
                    iterCal.add(Calendar.DAY_OF_YEAR, 1)
                }

                for (record in records) {
                    val recCal = Calendar.getInstance().apply { timeInMillis = record.startTime }
                    val key = dayFormat.format(recCal.time)
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
