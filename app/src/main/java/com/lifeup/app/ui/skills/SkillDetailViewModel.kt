package com.lifeup.app.ui.skills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.SkillStatus
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.model.TimeRecord
import com.lifeup.app.domain.repository.SkillRepository
import com.lifeup.app.domain.repository.TimeRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class DailyFrequency(
    val date: String, // "MM/dd"
    val count: Int,
    val totalMinutes: Int
)

@Immutable
data class GrowthPoint(
    val month: String, // "yyyy-MM"
    val cumulativeMinutes: Long
)

@Immutable
data class SkillDetailUiState(
    val skill: Skill? = null,
    val timeRecords: List<TimeRecord> = emptyList(),
    val weeklyFrequency: List<DailyFrequency> = emptyList(),
    val growthCurve: List<GrowthPoint> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class SkillDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val skillRepository: SkillRepository,
    private val timeRecordRepository: TimeRecordRepository
) : ViewModel() {

    private val skillId: Long = savedStateHandle["skillId"] ?: 0L

    private val _uiState = MutableStateFlow(SkillDetailUiState())
    val uiState: StateFlow<SkillDetailUiState> = _uiState.asStateFlow()

    init {
        loadSkill()
        loadTimeRecords()
    }

    private fun loadSkill() {
        viewModelScope.launch {
            try {
                val skill = skillRepository.getSkillById(skillId)
                _uiState.update { it.copy(skill = skill, isLoading = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadTimeRecords() {
        viewModelScope.launch {
            try {
                timeRecordRepository.getRecordsBySkill(skillId).collect { records ->
                    val weekly = calculateWeeklyFrequency(records)
                    val growth = calculateGrowthCurve(records)
                    _uiState.update {
                        it.copy(
                            timeRecords = records,
                            weeklyFrequency = weekly,
                            growthCurve = growth
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun calculateWeeklyFrequency(records: List<TimeRecord>): List<DailyFrequency> {
        val dateFormat = DateTimeFormatter.ofPattern("MM/dd", Locale.getDefault())
        val dayFormat = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.getDefault())
        val now = Instant.now().atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS)

        // Build last 7 days map
        val dayMap = mutableMapOf<String, DailyFrequency>()
        for (i in 6 downTo 0) {
            val day = now.minusDays(i.toLong())
            val key = day.format(dayFormat)
            dayMap[key] = DailyFrequency(
                date = day.format(dateFormat),
                count = 0,
                totalMinutes = 0
            )
        }

        // Count records per day
        for (record in records) {
            val day = Instant.ofEpochMilli(record.startTime).atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS)
            val key = day.format(dayFormat)
            dayMap[key]?.let { existing ->
                dayMap[key] = existing.copy(
                    count = existing.count + 1,
                    totalMinutes = existing.totalMinutes + record.durationMinutes
                )
            }
        }

        return dayMap.values.toList()
    }

    private fun calculateGrowthCurve(records: List<TimeRecord>): List<GrowthPoint> {
        if (records.isEmpty()) return emptyList()

        val monthFormat = DateTimeFormatter.ofPattern("yyyy-MM", Locale.getDefault())
        val sorted = records.sortedBy { it.startTime }

        // Group by month and accumulate
        val monthlyMinutes = mutableMapOf<String, Long>()
        for (record in sorted) {
            val month = Instant.ofEpochMilli(record.startTime).atZone(ZoneId.systemDefault()).format(monthFormat)
            monthlyMinutes[month] = (monthlyMinutes[month] ?: 0L) + record.durationMinutes
        }

        // Sort months and compute cumulative
        val sortedMonths = monthlyMinutes.keys.sorted()
        val result = mutableListOf<GrowthPoint>()
        var cumulative = 0L
        for (month in sortedMonths) {
            cumulative += monthlyMinutes[month] ?: 0L
            result.add(GrowthPoint(month = month, cumulativeMinutes = cumulative))
        }

        return result
    }

    fun pauseSkill() {
        val skill = _uiState.value.skill ?: return
        viewModelScope.launch {
            skillRepository.updateSkill(skill.copy(status = SkillStatus.PAUSED))
            val updated = skillRepository.getSkillById(skillId)
            _uiState.update { it.copy(skill = updated) }
        }
    }

    fun archiveSkill() {
        val skill = _uiState.value.skill ?: return
        viewModelScope.launch {
            skillRepository.updateSkill(skill.copy(status = SkillStatus.ARCHIVED))
            val updated = skillRepository.getSkillById(skillId)
            _uiState.update { it.copy(skill = updated) }
        }
    }

    fun resumeSkill() {
        val skill = _uiState.value.skill ?: return
        viewModelScope.launch {
            skillRepository.updateSkill(skill.copy(status = SkillStatus.ACTIVE))
            val updated = skillRepository.getSkillById(skillId)
            _uiState.update { it.copy(skill = updated) }
        }
    }
}
