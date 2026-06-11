package com.lifeup.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.Priority
import com.lifeup.app.data.db.SkillStatus
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.model.Todo
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.SkillRepository
import com.lifeup.app.domain.repository.TodoRepository
import com.lifeup.app.ui.skills.SKILL_TEMPLATES
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

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
    private val dailyStateRepository: DailyStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

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
            _uiState.update {
                it.copy(
                    isCreating = false,
                    createdSkillId = skillId,
                    currentStep = 2
                )
            }
        }
    }

    fun completeFirstHabit() {
        if (_uiState.value.habitCompleted) return
        _uiState.update { it.copy(isCompleting = true) }

        viewModelScope.launch {
            val todayStr = dateFormat.format(Date())

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

            _uiState.update {
                it.copy(
                    isCompleting = false,
                    habitCompleted = true,
                    currentStep = 3
                )
            }
        }
    }
}
