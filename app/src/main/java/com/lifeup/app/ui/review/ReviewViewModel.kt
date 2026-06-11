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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
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

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("M月d日 EEEE", Locale.CHINESE)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    init {
        selectDate(dateFormat.format(Date()))
    }

    fun selectDate(date: String) {
        val displayDate = try {
            val parsed = dateFormat.parse(date)
            if (parsed != null) displayDateFormat.format(parsed) else date
        } catch (e: Exception) {
            date
        }

        _uiState.update { it.copy(selectedDate = date, displayDate = displayDate, isLoading = true) }
        loadDateData(date)
    }

    fun selectPreviousDay() {
        val current = _uiState.value.selectedDate
        val date = try {
            val parsed = dateFormat.parse(current)
            val cal = Calendar.getInstance()
            cal.time = parsed!!
            cal.add(Calendar.DAY_OF_YEAR, -1)
            dateFormat.format(cal.time)
        } catch (e: Exception) {
            current
        }
        selectDate(date)
    }

    fun selectNextDay() {
        val current = _uiState.value.selectedDate
        val date = try {
            val parsed = dateFormat.parse(current)
            val cal = Calendar.getInstance()
            cal.time = parsed!!
            cal.add(Calendar.DAY_OF_YEAR, 1)
            dateFormat.format(cal.time)
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
        val cal = Calendar.getInstance()
        try {
            cal.time = dateFormat.parse(date)!!
        } catch (e: Exception) {
            cal.time = Date()
        }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startMs = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endMs = cal.timeInMillis

        return Pair(startMs, endMs)
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
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
