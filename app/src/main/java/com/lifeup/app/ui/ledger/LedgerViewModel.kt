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
import java.text.SimpleDateFormat
import java.util.Calendar
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

    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val displayMonthFormat = SimpleDateFormat("yyyy年MM月", Locale.CHINESE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("M月d日 EEEE", Locale.CHINESE)

    init {
        selectMonth(monthFormat.format(Calendar.getInstance().time))
    }

    fun selectMonth(month: String) {
        val displayMonth = try {
            val parsed = monthFormat.parse(month)
            if (parsed != null) displayMonthFormat.format(parsed) else month
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
        val cal = Calendar.getInstance()
        try {
            cal.time = monthFormat.parse(current)!!
        } catch (e: Exception) {
            return
        }
        cal.add(Calendar.MONTH, -1)
        selectMonth(monthFormat.format(cal.time))
    }

    fun selectNextMonth() {
        val current = _uiState.value.selectedMonth
        val cal = Calendar.getInstance()
        try {
            cal.time = monthFormat.parse(current)!!
        } catch (e: Exception) {
            return
        }
        cal.add(Calendar.MONTH, 1)
        selectMonth(monthFormat.format(cal.time))
    }

    private fun loadMonthData(month: String) {
        val cal = Calendar.getInstance()
        try {
            cal.time = monthFormat.parse(month)!!
        } catch (e: Exception) {
            return
        }

        // Start of month
        val startCal = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // End of month
        val endCal = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        val startMs = startCal.timeInMillis
        val endMs = endCal.timeInMillis
        val daysInMonth = endCal.get(Calendar.DAY_OF_MONTH)

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
                        date = dateFormat.format(record.startTime),
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
                        val parsed = dateFormat.parse(entry.date)
                        if (parsed != null) displayDateFormat.format(parsed) else entry.date
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
