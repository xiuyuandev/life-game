package com.lifeup.app.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.domain.calculator.GoldCalculator
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
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject

data class LedgerEntry(
    val date: String,
    val skillName: String,
    val skillCategory: SkillCategory,
    val durationMinutes: Int,
    val recordType: RecordType,
    val goldAmount: Int,
    val timestamp: Long
)

data class MonthlySummary(
    val totalInvestmentMinutes: Int = 0,
    val totalConsumptionMinutes: Int = 0,
    val totalGoldEarned: Int = 0,
    val totalGoldConsumed: Int = 0,
    val netGold: Int = 0,
    val daysActive: Int = 0,
    val averageDailyInvestment: Int = 0
)

data class LedgerUiState(
    val selectedMonth: String = "",
    val displayMonth: String = "",
    val entries: List<LedgerEntry> = emptyList(),
    val groupedEntries: Map<String, List<LedgerEntry>> = emptyMap(),
    val monthlySummary: MonthlySummary = MonthlySummary(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val timeRecordRepository: TimeRecordRepository,
    private val skillRepository: SkillRepository,
    private val dailyStateRepository: DailyStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LedgerUiState())
    val uiState: StateFlow<LedgerUiState> = _uiState.asStateFlow()

    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM", Locale.getDefault())
    private val displayMonthFormatter = DateTimeFormatter.ofPattern("yyyy年MM月", Locale.CHINESE)
    private val dateFormatter = DateTimeFormatter.ISO_DATE
    private val displayDateFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE)

    init {
        selectMonth(LocalDate.now().format(monthFormatter))
    }

    fun selectMonth(month: String) {
        val displayMonth = try {
            val parsed = LocalDate.parse(month + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()))
            parsed.format(displayMonthFormatter)
        } catch (e: Exception) {
            month
        }

        _uiState.update {
            it.copy(
                selectedMonth = month,
                displayMonth = displayMonth,
                isLoading = true
            )
        }
        loadMonthData(month)
    }

    fun selectPreviousMonth() {
        val current = _uiState.value.selectedMonth
        val date = try {
            val parsed = LocalDate.parse(current + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()))
            parsed.minusMonths(1).format(monthFormatter)
        } catch (e: Exception) {
            return
        }
        selectMonth(date)
    }

    fun selectNextMonth() {
        val current = _uiState.value.selectedMonth
        val date = try {
            val parsed = LocalDate.parse(current + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()))
            parsed.plusMonths(1).format(monthFormatter)
        } catch (e: Exception) {
            return
        }
        selectMonth(date)
    }

    private fun loadMonthData(month: String) {
        val localDate = try {
            LocalDate.parse(month + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()))
        } catch (e: Exception) {
            return
        }

        // Start of month
        val startDate = localDate.with(TemporalAdjusters.firstDayOfMonth())
        val startMs = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // End of month
        val endDate = localDate.with(TemporalAdjusters.lastDayOfMonth())
        val endMs = endDate.atTime(23, 59, 59, 999_999_999)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val daysInMonth = endDate.dayOfMonth

        viewModelScope.launch {
            combine(
                timeRecordRepository.getRecordsByDateRange(startMs, endMs),
                skillRepository.getActiveSkills()
            ) { records, skills ->
                Pair(records, skills)
            }.collect { (records, skills) ->
                val skillMap = skills.associateBy { it.id }

                // Build ledger entries
                val entries = records.mapNotNull { record ->
                    val skill = skillMap[record.skillId] ?: return@mapNotNull null
                    val isInvestment = record.recordType == RecordType.INVESTMENT
                    val gold = GoldCalculator.calculateGold(
                        minutes = record.durationMinutes,
                        isInvestment = isInvestment,
                        isFirstTimerToday = false,
                        skillLevel = skill.level
                    )
                    LedgerEntry(
                        date = java.time.Instant.ofEpochMilli(record.startTime)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(dateFormatter),
                        skillName = skill.name,
                        skillCategory = skill.category,
                        durationMinutes = record.durationMinutes,
                        recordType = record.recordType,
                        goldAmount = gold,
                        timestamp = record.startTime
                    )
                }.sortedByDescending { it.timestamp }

                // Group by date
                val groupedEntries = entries.groupBy { entry ->
                    try {
                        val parsed = LocalDate.parse(entry.date, dateFormatter)
                        parsed.format(displayDateFormatter)
                    } catch (e: Exception) {
                        entry.date
                    }
                }

                // Calculate monthly summary
                val totalInvestmentMinutes = entries
                    .filter { it.recordType == RecordType.INVESTMENT }
                    .sumOf { it.durationMinutes }
                val totalConsumptionMinutes = entries
                    .filter { it.recordType == RecordType.CONSUMPTION }
                    .sumOf { it.durationMinutes }
                val totalGoldEarned = entries
                    .filter { it.recordType == RecordType.INVESTMENT }
                    .sumOf { it.goldAmount }
                val totalGoldConsumed = entries
                    .filter { it.recordType == RecordType.CONSUMPTION }
                    .sumOf { it.goldAmount }
                val netGold = totalGoldEarned - totalGoldConsumed
                val activeDays = entries.map { it.date }.distinct().size
                val averageDailyInvestment = if (activeDays > 0) {
                    totalInvestmentMinutes / activeDays
                } else {
                    0
                }

                val summary = MonthlySummary(
                    totalInvestmentMinutes = totalInvestmentMinutes,
                    totalConsumptionMinutes = totalConsumptionMinutes,
                    totalGoldEarned = totalGoldEarned,
                    totalGoldConsumed = totalGoldConsumed,
                    netGold = netGold,
                    daysActive = activeDays,
                    averageDailyInvestment = averageDailyInvestment
                )

                _uiState.update {
                    it.copy(
                        entries = entries,
                        groupedEntries = groupedEntries,
                        monthlySummary = summary,
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
            hours > 0 -> "${hours}时${mins}分"
            else -> "${mins}分"
        }
    }
}
