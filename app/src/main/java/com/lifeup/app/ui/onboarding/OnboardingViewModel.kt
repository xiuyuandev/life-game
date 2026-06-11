package com.lifeup.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.Priority
import com.lifeup.app.data.db.SkillStatus
import com.lifeup.app.domain.model.DailyState
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.model.Todo
import com.lifeup.app.domain.repository.AchievementRepository
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.SkillRepository
import com.lifeup.app.domain.repository.TodoRepository
import com.lifeup.app.data.preferences.SettingsPrefs
import com.lifeup.app.ui.skills.SKILL_TEMPLATES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class OnboardingUiState(
    val currentStep: Int = 1,
    val selectedTemplateIndex: Int = -1,
    val isCreating: Boolean = false,
    val createdSkillId: Long = 0L,
    val habitCompleted: Boolean = false,
    val isCompleting: Boolean = false,
    val isComplete: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val skillRepository: SkillRepository,
    private val todoRepository: TodoRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val achievementRepository: AchievementRepository,
    private val settingsPrefs: SettingsPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

    fun nextStep() {
        val current = _uiState.value.currentStep
        if (current < 3) {
            _uiState.update { it.copy(currentStep = current + 1) }
        }
    }

    fun selectTemplate(index: Int) {
        _uiState.update { it.copy(selectedTemplateIndex = index) }
    }

    fun createFirstSkill(templateIndex: Int) {
        if (templateIndex < 0 || templateIndex >= SKILL_TEMPLATES.size) {
            _uiState.update { it.copy(errorMessage = "请选择一个技能模板") }
            return
        }

        val template = SKILL_TEMPLATES[templateIndex]
        _uiState.update { it.copy(isCreating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // Onboarding: free skill creation, no energy cost
                val skill = Skill(
                    name = template.name,
                    category = template.category,
                    boundAttribute = template.boundAttribute,
                    customThresholds = template.suggestedThresholds,
                    iconKey = template.suggestedIconKey,
                    status = SkillStatus.ACTIVE
                )

                val skillId = skillRepository.insertSkill(skill)

                // Initialize today's daily state if it doesn't exist
                val todayStr = LocalDate.now().format(dateFormat)
                val existingState = dailyStateRepository.getStateByDate(todayStr).firstOrNull()
                if (existingState == null) {
                    dailyStateRepository.insertOrUpdateState(
                        DailyState(date = todayStr, lastUpdated = System.currentTimeMillis())
                    )
                }

                // Unlock first_skill achievement
                achievementRepository.unlockAchievement("first_skill")

                _uiState.update {
                    it.copy(
                        isCreating = false,
                        createdSkillId = skillId,
                        currentStep = 2
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isCreating = false, errorMessage = "创建失败，请重试") }
            }
        }
    }

    fun completeFirstHabit() {
        if (_uiState.value.habitCompleted) return
        _uiState.update { it.copy(isCompleting = true) }

        viewModelScope.launch {
            try {
                val todayStr = LocalDate.now().format(dateFormat)

                // Create and complete the "喝杯水" habit
                val habit = Todo(
                    title = "喝杯水",
                    isHabit = true,
                    priority = Priority.NONE,
                    isCompleted = true,
                    completedAt = System.currentTimeMillis(),
                    date = todayStr,
                    sortOrder = 0
                )

                todoRepository.insertTodo(habit)

                // Update daily state's habitsCompleted counter and reward gold
                val currentState = dailyStateRepository.getStateByDate(todayStr).firstOrNull()
                if (currentState != null) {
                    dailyStateRepository.insertOrUpdateState(
                        currentState.copy(
                            habitsCompleted = currentState.habitsCompleted + 1,
                            goldEarned = currentState.goldEarned + 5,
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                }

                // Unlock first_habit achievement
                achievementRepository.unlockAchievement("first_habit")

                _uiState.update {
                    it.copy(
                        isCompleting = false,
                        habitCompleted = true,
                        currentStep = 3
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isCompleting = false) }
            }
        }
    }

    fun markOnboardingCompleted() {
        viewModelScope.launch {
            try {
                settingsPrefs.setOnboardingCompleted(true)
            } catch (_: Exception) { }
        }
    }
}
