package com.lifeup.app.ui.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.BoundAttribute
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.data.db.SkillStatus
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.model.SkillTemplate
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class CreateSkillUiState(
    val name: String = "",
    val category: SkillCategory = SkillCategory.LIFE,
    val boundAttribute: BoundAttribute = BoundAttribute.WILLPOWER,
    val customThresholdsEnabled: Boolean = false,
    val thresholdLv2: Int = 60,
    val thresholdLv3: Int = 120,
    val thresholdLv4: Int = 240,
    val thresholdLv5: Int = 480,
    val energy: Float = 0f,
    val energyCap: Float = 100f,
    val canCreate: Boolean = false,
    val isCreating: Boolean = false,
    val createSuccess: Boolean = false,
    val errorMessage: String? = null,
    val nameValidationError: String? = null
)

val SKILL_TEMPLATES = listOf(
    SkillTemplate(
        name = "编程",
        category = SkillCategory.LIVELIHOOD,
        boundAttribute = BoundAttribute.INTELLIGENCE,
        suggestedThresholds = mapOf(2 to 60, 3 to 150, 4 to 300, 5 to 600),
        suggestedIconKey = "code"
    ),
    SkillTemplate(
        name = "英语",
        category = SkillCategory.LANGUAGE,
        boundAttribute = BoundAttribute.INTELLIGENCE,
        suggestedThresholds = mapOf(2 to 60, 3 to 150, 4 to 300, 5 to 600),
        suggestedIconKey = "language"
    ),
    SkillTemplate(
        name = "跑步",
        category = SkillCategory.PHYSICAL,
        boundAttribute = BoundAttribute.STRENGTH,
        suggestedThresholds = mapOf(2 to 30, 3 to 90, 4 to 180, 5 to 360),
        suggestedIconKey = "run"
    ),
    SkillTemplate(
        name = "烹饪",
        category = SkillCategory.LIFE,
        boundAttribute = BoundAttribute.DEXTERITY,
        suggestedThresholds = mapOf(2 to 40, 3 to 100, 4 to 200, 5 to 400),
        suggestedIconKey = "cook"
    ),
    SkillTemplate(
        name = "冥想",
        category = SkillCategory.MENTAL,
        boundAttribute = BoundAttribute.WILLPOWER,
        suggestedThresholds = mapOf(2 to 30, 3 to 90, 4 to 180, 5 to 360),
        suggestedIconKey = "meditate"
    ),
    SkillTemplate(
        name = "绘画",
        category = SkillCategory.ART,
        boundAttribute = BoundAttribute.CREATIVITY,
        suggestedThresholds = mapOf(2 to 60, 3 to 150, 4 to 300, 5 to 600),
        suggestedIconKey = "paint"
    )
)

val CATEGORY_ATTRIBUTE_MAP: Map<SkillCategory, BoundAttribute> = mapOf(
    SkillCategory.LIVELIHOOD to BoundAttribute.INTELLIGENCE,
    SkillCategory.SOCIAL to BoundAttribute.CHARISMA,
    SkillCategory.LANGUAGE to BoundAttribute.INTELLIGENCE,
    SkillCategory.LIFE to BoundAttribute.DEXTERITY,
    SkillCategory.PHYSICAL to BoundAttribute.STRENGTH,
    SkillCategory.MENTAL to BoundAttribute.WILLPOWER,
    SkillCategory.ART to BoundAttribute.CREATIVITY
)

@HiltViewModel
class CreateSkillViewModel @Inject constructor(
    private val skillRepository: SkillRepository,
    private val dailyStateRepository: DailyStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateSkillUiState())
    val uiState: StateFlow<CreateSkillUiState> = _uiState.asStateFlow()

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

    init {
        loadEnergy()
    }

    private fun loadEnergy() {
        viewModelScope.launch {
            try {
                val todayStr = LocalDate.now().format(dateFormat)
                dailyStateRepository.getStateByDate(todayStr).collect { dailyState ->
                    val state = dailyState ?: return@collect
                    _uiState.update {
                        it.copy(
                            energy = state.energy,
                            energyCap = state.energyCap,
                            canCreate = it.name.isNotBlank() && state.energy >= 2f
                        )
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun updateName(name: String) {
        val trimmed = name.trim()
        val validationError = when {
            trimmed.isEmpty() -> "技能名称不能为空"
            trimmed.length > 20 -> "技能名称最多20个字符"
            !trimmed.matches(Regex("^[\\u4e00-\\u9fa5a-zA-Z0-9 ]+$")) -> "名称不能包含特殊字符"
            else -> null
        }
        _uiState.update {
            it.copy(
                name = name,
                canCreate = validationError == null && trimmed.isNotBlank() && it.energy >= 2f,
                errorMessage = null,
                nameValidationError = validationError
            )
        }
    }

    fun updateCategory(category: SkillCategory) {
        val autoAttribute = CATEGORY_ATTRIBUTE_MAP[category] ?: BoundAttribute.WILLPOWER
        _uiState.update {
            it.copy(
                category = category,
                boundAttribute = autoAttribute
            )
        }
    }

    fun updateBoundAttribute(attribute: BoundAttribute) {
        _uiState.update { it.copy(boundAttribute = attribute) }
    }

    fun updateCustomThresholdsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(customThresholdsEnabled = enabled) }
    }

    fun updateThreshold(level: Int, hours: Int) {
        _uiState.update { state ->
            when (level) {
                2 -> state.copy(thresholdLv2 = hours)
                3 -> state.copy(thresholdLv3 = hours)
                4 -> state.copy(thresholdLv4 = hours)
                5 -> state.copy(thresholdLv5 = hours)
                else -> state
            }
        }
    }

    fun applyTemplate(template: SkillTemplate) {
        _uiState.update {
            it.copy(
                name = template.name,
                category = template.category,
                boundAttribute = template.boundAttribute
            )
        }
    }

    fun createSkill() {
        val state = _uiState.value
        val trimmed = state.name.trim()
        val validationError = when {
            trimmed.isEmpty() -> "请输入技能名称"
            trimmed.length > 20 -> "技能名称最多20个字符"
            !trimmed.matches(Regex("^[\\u4e00-\\u9fa5a-zA-Z0-9 ]+$")) -> "名称不能包含特殊字符"
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError, nameValidationError = validationError) }
            return
        }
        if (state.energy < 2f) {
            _uiState.update { it.copy(errorMessage = "能量不足，创建技能需要2点能量") }
            return
        }

        _uiState.update { it.copy(isCreating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val customThresholds = if (state.customThresholdsEnabled) {
                    mapOf(
                        2 to state.thresholdLv2,
                        3 to state.thresholdLv3,
                        4 to state.thresholdLv4,
                        5 to state.thresholdLv5
                    )
                } else {
                    emptyMap()
                }

                val skill = Skill(
                    name = state.name.trim(),
                    category = state.category,
                    boundAttribute = state.boundAttribute,
                    customThresholds = customThresholds,
                    status = SkillStatus.ACTIVE
                )

                skillRepository.insertSkill(skill)

                // Deduct energy
                val todayStr = LocalDate.now().format(dateFormat)
                val dailyState = try {
                    withTimeout(5000) { dailyStateRepository.getStateByDate(todayStr).first() }
                } catch (_: Exception) { null }
                if (dailyState != null) {
                    dailyStateRepository.insertOrUpdateState(
                        dailyState.copy(energy = dailyState.energy - 2f)
                    )
                }

                _uiState.update { it.copy(isCreating = false, createSuccess = true) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isCreating = false, errorMessage = "创建失败，请重试") }
            }
        }
    }
}
