package com.lifeup.app.ui.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.domain.model.DailyState
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

data class ReviewUiState(
    val selectedDate: String = "",
    val displayDate: String = "",
    val timeRecords: List<TimeRecord> = emptyList(),
    val skills: List<Skill> = emptyList(),
    val dailyState: DailyState? = null,
    val investmentMinutes: Int = 0,
    val consumptionMinutes: Int = 0,
    val streakCount: Int = 0,
    val isLoading: Boolean = true
) {
    val totalMinutes: Int get() = investmentMinutes + consumptionMinutes

    val investmentRatio: Float
        get() = if (totalMinutes > 0) investmentMinutes.toFloat() / totalMinutes else 0f

    val consumptionRatio: Float
        get() = if (totalMinutes > 0) consumptionMinutes.toFloat() / totalMinutes else 0f

    val investmentToConsumptionRatio: String
        get() = if (consumptionMinutes > 0) {
            val ratio = investmentMinutes.toFloat() / consumptionMinutes
            String.format("%.1f", ratio)
        } else if (investmentMinutes > 0) {
            "∞"
        } else {
            "0"
        }

    data class SkillBreakdown(
        val skill: Skill,
        val minutes: Int
    )

    val skillBreakdowns: List<SkillBreakdown>
        get() {
            val skillMap = skills.associateBy { it.id }
            return timeRecords
                .groupBy { it.skillId }
                .mapNotNull { (skillId, records) ->
                    val skill = skillMap[skillId] ?: return@mapNotNull null
                    SkillBreakdown(skill, records.sumOf { it.durationMinutes })
                }
                .sortedByDescending { it.minutes }
        }

    val mostFocusedSkill: Skill?
        get() = skillBreakdowns.firstOrNull()?.skill
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val timeRecordRepository: TimeRecordRepository,
    private val skillRepository: SkillRepository,
    private val dailyStateRepository: DailyStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ISO_DATE
    private val displayDateFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE)
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    init {
        selectDate(LocalDate.now().format(dateFormatter))
    }

    fun selectDate(date: String) {
        val displayDate = try {
            val parsed = LocalDate.parse(date, dateFormatter)
            parsed.format(displayDateFormatter)
        } catch (e: Exception) {
            date
        }

        _uiState.update { it.copy(selectedDate = date, displayDate = displayDate, isLoading = true) }
        loadDateData(date)
    }

    fun selectPreviousDay() {
        val current = _uiState.value.selectedDate
        val date = try {
            val parsed = LocalDate.parse(current, dateFormatter)
            parsed.minusDays(1).format(dateFormatter)
        } catch (e: Exception) {
            current
        }
        selectDate(date)
    }

    fun selectNextDay() {
        val current = _uiState.value.selectedDate
        val date = try {
            val parsed = LocalDate.parse(current, dateFormatter)
            parsed.plusDays(1).format(dateFormatter)
        } catch (e: Exception) {
            current
        }
        selectDate(date)
    }

    private fun loadDateData(date: String) {
        viewModelScope.launch {
            val (startMs, endMs) = getDayTimeRange(date)

            combine(
                timeRecordRepository.getRecordsByDateRange(startMs, endMs),
                skillRepository.getActiveSkills(),
                dailyStateRepository.getStateByDate(date)
            ) { records, skills, dailyState ->
                Triple(records, skills, dailyState)
            }.collect { (records, skills, dailyState) ->
                val investMins = records
                    .filter { it.recordType == RecordType.INVESTMENT }
                    .sumOf { it.durationMinutes }
                val consumeMins = records
                    .filter { it.recordType == RecordType.CONSUMPTION }
                    .sumOf { it.durationMinutes }
                val streak = dailyStateRepository.getLatestStreak()
                    ?: dailyState?.streakCount
                    ?: 0

                _uiState.update { it.copy(
                    timeRecords = records.sortedBy { r -> r.startTime },
                    skills = skills,
                    dailyState = dailyState,
                    investmentMinutes = investMins,
                    consumptionMinutes = consumeMins,
                    streakCount = streak,
                    isLoading = false
                )}
            }
        }
    }

    private fun getDayTimeRange(date: String): Pair<Long, Long> {
        val localDate = try {
            LocalDate.parse(date, dateFormatter)
        } catch (e: Exception) {
            LocalDate.now()
        }
        val startMs = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMs = localDate.atTime(23, 59, 59, 999_999_999)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        return Pair(startMs, endMs)
    }

    fun formatTime(timestamp: Long): String {
        val instant = Instant.ofEpochMilli(timestamp)
        val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        return localTime.format(timeFormatter)
    }

    fun formatMinutes(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return when {
            hours > 0 -> "${hours}h ${mins}m"
            else -> "${mins}m"
        }
    }
}
