package com.lifeup.app.ui.retroactive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.FocusType
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.domain.game.GameEngine
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.model.TimeRecord
import com.lifeup.app.domain.repository.ComboRepository
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.ItemRepository
import com.lifeup.app.domain.repository.SkillRepository
import com.lifeup.app.domain.repository.TimeRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class RetroactiveUiState(
    val skills: List<Skill> = emptyList(),
    val selectedSkillId: Long? = null,
    val selectedDate: String = "",
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val durationMinutes: Int = 30,
    val recordType: RecordType = RecordType.INVESTMENT,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class RetroactiveViewModel @Inject constructor(
    private val skillRepository: SkillRepository,
    private val timeRecordRepository: TimeRecordRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val comboRepository: ComboRepository,
    private val itemRepository: ItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RetroactiveUiState())
    val uiState: StateFlow<RetroactiveUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        loadSkills()
    }

    private fun loadSkills() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            skillRepository.getActiveSkills().collect { skills ->
                val todayStr = dateFormat.format(Calendar.getInstance().time)
                _uiState.update {
                    it.copy(
                        skills = skills,
                        selectedSkillId = skills.firstOrNull()?.id,
                        selectedDate = todayStr,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectSkill(id: Long) {
        _uiState.update { it.copy(selectedSkillId = id) }
    }

    fun selectDate(date: String) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun setStartTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(startHour = hour, startMinute = minute) }
    }

    fun setDuration(minutes: Int) {
        _uiState.update { it.copy(durationMinutes = minutes) }
    }

    fun toggleRecordType() {
        _uiState.update {
            it.copy(
                recordType = if (it.recordType == RecordType.INVESTMENT) {
                    RecordType.CONSUMPTION
                } else {
                    RecordType.INVESTMENT
                }
            )
        }
    }

    fun saveRetroactive() {
        val state = _uiState.value
        val skillId = state.selectedSkillId ?: return
        if (state.selectedDate.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                // Compute startTime from date + hour + minute
                val calendar = Calendar.getInstance().apply {
                    val dateParts = state.selectedDate.split("-")
                    set(Calendar.YEAR, dateParts[0].toInt())
                    set(Calendar.MONTH, dateParts[1].toInt() - 1)
                    set(Calendar.DAY_OF_MONTH, dateParts[2].toInt())
                    set(Calendar.HOUR_OF_DAY, state.startHour)
                    set(Calendar.MINUTE, state.startMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startTime = calendar.timeInMillis
                val endTime = startTime + state.durationMinutes * 60_000L

                // Create TimeRecord with retroactive timestamps
                val record = TimeRecord(
                    skillId = skillId,
                    startTime = startTime,
                    endTime = endTime,
                    durationMinutes = state.durationMinutes,
                    recordType = state.recordType,
                    focusType = FocusType.FOCUSED
                )
                timeRecordRepository.insertRecord(record)

                // Process timer result: updates skill totalMinutes, level up, exp/gold, daily state, item unlocks
                GameEngine.processTimerResult(
                    skillId = skillId,
                    durationMinutes = state.durationMinutes,
                    recordType = state.recordType,
                    focusType = FocusType.FOCUSED,
                    skillRepository = skillRepository,
                    timeRecordRepository = timeRecordRepository,
                    dailyStateRepository = dailyStateRepository,
                    comboRepository = comboRepository,
                    itemRepository = itemRepository
                )

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
