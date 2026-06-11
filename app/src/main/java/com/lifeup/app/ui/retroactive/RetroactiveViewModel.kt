package com.lifeup.app.ui.retroactive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.FocusType
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.domain.game.GameEngine
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.model.TimeRecord
import com.lifeup.app.domain.repository.AchievementRepository
import com.lifeup.app.domain.repository.CharacterStateRepository
import com.lifeup.app.domain.repository.ComboRepository
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.GoldRepository
import com.lifeup.app.domain.repository.ItemRepository
import com.lifeup.app.domain.repository.SkillRepository
import com.lifeup.app.domain.repository.TimeRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
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
    val isLoading: Boolean = true,
    val dateValidationError: String? = null,
    val durationValidationError: String? = null
)

@HiltViewModel
class RetroactiveViewModel @Inject constructor(
    private val skillRepository: SkillRepository,
    private val timeRecordRepository: TimeRecordRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val comboRepository: ComboRepository,
    private val itemRepository: ItemRepository,
    private val characterStateRepository: CharacterStateRepository,
    private val achievementRepository: AchievementRepository,
    private val goldRepository: GoldRepository,
    private val settingsPrefs: com.lifeup.app.data.preferences.SettingsPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(RetroactiveUiState())
    val uiState: StateFlow<RetroactiveUiState> = _uiState.asStateFlow()

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

    init {
        loadSkills()
    }

    private fun loadSkills() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            skillRepository.getActiveSkills().collect { skills ->
                val todayStr = LocalDate.now().format(dateFormat)
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
        val today = LocalDate.now()
        val selected = try {
            LocalDate.parse(date, dateFormat)
        } catch (_: Exception) { null }
        val error = if (selected != null && selected.isAfter(today)) "不能选择未来日期" else null
        _uiState.update { it.copy(selectedDate = date, dateValidationError = error) }
    }

    fun setStartTime(hour: Int, minute: Int) {
        _uiState.update { it.copy(startHour = hour, startMinute = minute) }
    }

    fun setDuration(minutes: Int) {
        val error = when {
            minutes < 1 -> "时长至少1分钟"
            minutes > 480 -> "时长最多480分钟（8小时）"
            else -> null
        }
        _uiState.update { it.copy(durationMinutes = minutes, durationValidationError = error) }
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

        val today = LocalDate.now()
        val selected = try {
            LocalDate.parse(state.selectedDate, dateFormat)
        } catch (_: Exception) { null }
        val dateError = if (selected != null && selected.isAfter(today)) "不能选择未来日期" else null
        val durationError = when {
            state.durationMinutes < 1 -> "时长至少1分钟"
            state.durationMinutes > 480 -> "时长最多480分钟（8小时）"
            else -> null
        }
        if (dateError != null || durationError != null) {
            _uiState.update { it.copy(dateValidationError = dateError, durationValidationError = durationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                // Compute startTime from date + hour + minute
                val startTime = LocalDateTime.of(
                    selected,
                    java.time.LocalTime.of(state.startHour, state.startMinute)
                ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
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
                    itemRepository = itemRepository,
                    characterStateRepository = characterStateRepository,
                    achievementRepository = achievementRepository,
                    goldRepository = goldRepository,
                    settingsPrefs = settingsPrefs
                )

                _uiState.update { it.copy(isSaving = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }
}
