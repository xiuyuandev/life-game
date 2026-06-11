package com.lifeup.app.ui.timer

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.FocusType
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.domain.game.DemonBattleOutcome
import com.lifeup.app.domain.game.DemonEngine
import com.lifeup.app.domain.game.GameEngine
import com.lifeup.app.domain.game.TimerResult
import com.lifeup.app.domain.model.DemonId
import com.lifeup.app.domain.model.DemonTemplate
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.repository.AchievementRepository
import com.lifeup.app.domain.repository.CharacterStateRepository
import com.lifeup.app.domain.repository.ComboRepository
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.DemonRepository
import com.lifeup.app.domain.repository.ItemRepository
import com.lifeup.app.domain.repository.SkillRepository
import com.lifeup.app.domain.repository.TimeRecordRepository
import com.lifeup.app.service.TimerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class TimerUiState(
    val skill: Skill? = null,
    val isLoading: Boolean = true,
    val elapsedSeconds: Long = 0L,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val recordType: RecordType = RecordType.INVESTMENT,
    val settlementResult: TimerResult? = null,
    val showSettlement: Boolean = false,
    val error: String? = null,
    /** 启用了"为心魔而战"模式时，记录目标心魔 id。 */
    val demonId: DemonId? = null,
    /** 最近一次攻击心魔的结算。 */
    val demonOutcome: DemonBattleOutcome? = null,
    val showDemonOutcome: Boolean = false
)

@HiltViewModel
class TimerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val skillRepository: SkillRepository,
    private val timeRecordRepository: TimeRecordRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val comboRepository: ComboRepository,
    private val itemRepository: ItemRepository,
    private val characterStateRepository: CharacterStateRepository,
    private val achievementRepository: AchievementRepository,
    private val demonRepository: DemonRepository,
    private val demonEngine: DemonEngine,
    val settingsPrefs: com.lifeup.app.data.preferences.SettingsPrefs,
    private val application: Application
) : ViewModel() {

    private val skillId: Long = savedStateHandle["skillId"] ?: 0L
    private val demonIdKey: String? = savedStateHandle.get<String>("demonId")
    private val demonId: DemonId? = demonIdKey?.let { DemonId.fromKey(it) }

    private val _uiState = MutableStateFlow(TimerUiState(demonId = demonId))
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private val timerManager = TimerManager

    private var observeJob: Job? = null

    init {
        loadSkill()
        observeTimerState()
    }

    private fun loadSkill() {
        viewModelScope.launch {
            if (skillId == 0L) {
                // "为心魔而战"模式可能没有具体技能 —— 用一个合成的伪技能占位
                if (demonId != null) {
                    val pseudo = Skill(
                        id = 0L,
                        name = "为心魔而战",
                        description = "与内心敌人的缠斗。",
                        category = com.lifeup.app.data.db.SkillCategory.MENTAL,
                        level = 1,
                        totalMinutes = 0L,
                        colorHex = "#5B6B7A",
                        iconName = "demon",
                        isArchived = false,
                        createdAt = System.currentTimeMillis(),
                        isHabit = false
                    )
                    _uiState.update { it.copy(skill = pseudo, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "未选择技能") }
                }
                return@launch
            }
            val skill = skillRepository.getSkillById(skillId)
            _uiState.update { it.copy(skill = skill, isLoading = false) }
        }
    }

    private fun observeTimerState() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
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
            try {
                val todayStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_DATE)
                val dailyState = try {
                    withTimeout(5000) { dailyStateRepository.getStateByDate(todayStr).first() }
                } catch (_: Exception) {
                    null
                }
                val durationMinutes = 25 // default focus session length
                val energyCost = durationMinutes.coerceAtMost(20).coerceAtLeast(5)

                // Calculate regenerated energy based on time since last update (5 per hour)
                val regeneratedEnergy = if (dailyState != null) {
                    val hoursSinceUpdate = (System.currentTimeMillis() - dailyState.lastUpdated) / 3_600_000f
                    (hoursSinceUpdate * 5f).toInt()
                } else {
                    0
                }
                val effectiveEnergy = (dailyState?.energy ?: 0f) + regeneratedEnergy
                val energyCap = dailyState?.energyCap ?: 100f
                val availableEnergy = effectiveEnergy.coerceAtMost(energyCap)

                if (availableEnergy < energyCost) {
                    _uiState.update { it.copy(error = "能量不足，需要 $energyCost 能量，当前可用 ${availableEnergy.toInt()}") }
                    return@launch
                }
                timerManager.startTimer(application, skill.id, skill.name)
            } catch (_: Exception) {
                _uiState.update { it.copy(error = "启动计时器失败") }
            }
        }
    }

    fun stopTimer() {
        val skill = _uiState.value.skill ?: return
        val durationSeconds = timerManager.stopTimer()
        val durationMinutes = timerManager.toDurationMinutes(durationSeconds)
        val recordType = _uiState.value.recordType
        val targetDemonId = _uiState.value.demonId

        viewModelScope.launch {
            var settlement: TimerResult? = null
            try {
                if (skill.id > 0L) {
                    settlement = GameEngine.processTimerResult(
                        skillId = skill.id,
                        durationMinutes = durationMinutes,
                        recordType = recordType,
                        focusType = FocusType.FOCUSED,
                        skillRepository = skillRepository,
                        timeRecordRepository = timeRecordRepository,
                        dailyStateRepository = dailyStateRepository,
                        comboRepository = comboRepository,
                        itemRepository = itemRepository,
                        characterStateRepository = characterStateRepository,
                        achievementRepository = achievementRepository,
                        settingsPrefs = settingsPrefs
                    )
                }
            } catch (_: Exception) {
                // ignore - in demon mode the GameEngine may throw (no real skill)
            }
            if (settlement == null) {
                settlement = TimerResult(
                    expGained = durationMinutes.toLong(),
                    goldGained = durationMinutes,
                    leveledUp = false,
                    newLevel = skill.level,
                    itemsUnlocked = emptyList()
                )
            }

            // 启用了"为心魔而战"模式时，把结果喂给 DemonEngine
            var demonOutcome: DemonBattleOutcome? = null
            if (targetDemonId != null) {
                val demon = DemonTemplate.ALL.firstOrNull { it.id == targetDemonId }
                if (demon != null) {
                    demonOutcome = demonEngine.applySessionResult(
                        demon = demon,
                        focusMinutes = durationMinutes,
                        skillCategory = skill.category
                    )
                    demonEngine.ensureMirrorIfUnlocked()
                }
            }

            _uiState.update {
                it.copy(
                    settlementResult = settlement,
                    showSettlement = true,
                    demonOutcome = demonOutcome,
                    showDemonOutcome = demonOutcome != null
                )
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

    fun dismissDemonOutcome() {
        _uiState.update { it.copy(showDemonOutcome = false, demonOutcome = null) }
    }

    fun formatElapsedTime(seconds: Long): String {
        return timerManager.formatElapsedTime(seconds)
    }
}
