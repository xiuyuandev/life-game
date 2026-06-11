package com.lifeup.app.ui.timer

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.FocusType
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.domain.game.GameEngine
import com.lifeup.app.domain.game.TimerResult
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.repository.ComboRepository
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.ItemRepository
import com.lifeup.app.domain.repository.SkillRepository
import com.lifeup.app.domain.repository.TimeRecordRepository
import com.lifeup.app.service.TimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimerUiState(
    val skill: Skill? = null,
    val isLoading: Boolean = true,
    val elapsedSeconds: Long = 0L,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val recordType: RecordType = RecordType.INVESTMENT,
    val settlementResult: TimerResult? = null,
    val showSettlement: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TimerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val skillRepository: SkillRepository,
    private val timeRecordRepository: TimeRecordRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val comboRepository: ComboRepository,
    private val itemRepository: ItemRepository,
    private val application: Application
) : ViewModel() {

    private val skillId: Long = savedStateHandle["skillId"] ?: 0L

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private val timerManager = TimerManager

    init {
        loadSkill()
        observeTimerState()
    }

    private fun loadSkill() {
        viewModelScope.launch {
            if (skillId == 0L) {
                _uiState.update { it.copy(isLoading = false, error = "未选择技能") }
                return@launch
            }
            val skill = skillRepository.getSkillById(skillId)
            _uiState.update { it.copy(skill = skill, isLoading = false) }
        }
    }

    private fun observeTimerState() {
        viewModelScope.launch {
            combine(
                timerManager.isRunning,
                timerManager.elapsedSeconds,
                timerManager.isPaused
            ) { isRunning, elapsedSeconds, isPaused ->
                _uiState.update {
                    it.copy(
                        isRunning = isRunning,
                        elapsedSeconds = elapsedSeconds,
                        isPaused = isPaused
                    )
                }
            }.collect {}
        }
    }

    fun startTimer() {
        val skill = _uiState.value.skill ?: return
        viewModelScope.launch {
            val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val dailyState = dailyStateRepository.getStateByDate(todayStr).first()
            val durationMinutes = 25 // default focus session length
            val energyCost = durationMinutes.coerceAtMost(20).coerceAtLeast(5)
            if ((dailyState?.energy ?: 0f) < energyCost) {
                _uiState.update { it.copy(error = "能量不足，需要 $energyCost 能量") }
                return@launch
            }
            timerManager.startTimer(application, skill.id, skill.name)
        }
    }

    fun stopTimer() {
        val skill = _uiState.value.skill ?: return
        val durationSeconds = timerManager.stopTimer()
        val durationMinutes = timerManager.toDurationMinutes(durationSeconds)
        val recordType = _uiState.value.recordType

        viewModelScope.launch {
            try {
                val result = GameEngine.processTimerResult(
                    skillId = skill.id,
                    durationMinutes = durationMinutes,
                    recordType = recordType,
                    focusType = FocusType.FOCUSED,
                    skillRepository = skillRepository,
                    timeRecordRepository = timeRecordRepository,
                    dailyStateRepository = dailyStateRepository,
                    comboRepository = comboRepository,
                    itemRepository = itemRepository
                )
                _uiState.update {
                    it.copy(
                        settlementResult = result,
                        showSettlement = true
                    )
                }
            } catch (_: Exception) {
                // If processing fails, still show a basic settlement
                _uiState.update {
                    it.copy(
                        settlementResult = TimerResult(
                            expGained = durationMinutes.toLong(),
                            goldGained = durationMinutes,
                            leveledUp = false,
                            newLevel = skill.level,
                            itemsUnlocked = emptyList()
                        ),
                        showSettlement = true
                    )
                }
            }
        }
    }

    fun pauseTimer() {
        timerManager.pauseTimer()
    }

    fun resumeTimer() {
        timerManager.resumeTimer()
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

    fun dismissSettlement() {
        _uiState.update { it.copy(showSettlement = false, settlementResult = null) }
    }

    fun formatElapsedTime(seconds: Long): String {
        return timerManager.formatElapsedTime(seconds)
    }
}
