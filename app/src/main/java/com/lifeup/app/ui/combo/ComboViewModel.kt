package com.lifeup.app.ui.combo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.domain.model.Combo
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.repository.ComboRepository
import com.lifeup.app.domain.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class ComboUiState(
    val combos: List<Combo> = emptyList(),
    val skills: List<Skill> = emptyList(),
    val recommendedCombos: List<RecommendedCombo> = emptyList(),
    val showCreateDialog: Boolean = false,
    val isLoading: Boolean = true,
    val selectedPrimarySkillId: Long? = null,
    val selectedSecondarySkillId: Long? = null,
    val comboName: String = "",
    val errorMessage: String? = null
)

@Immutable
data class RecommendedCombo(
    val name: String,
    val primaryCategory: SkillCategory,
    val secondaryCategory: SkillCategory,
    val expBonus: Float,
    val suggestion: String
)

val RECOMMENDED_COMBOS = listOf(
    RecommendedCombo(
        "身心合一",
        SkillCategory.PHYSICAL,
        SkillCategory.MENTAL,
        1.08f,
        "身体锻炼配合冥想，身心同步提升"
    ),
    RecommendedCombo(
        "社交艺术",
        SkillCategory.SOCIAL,
        SkillCategory.ART,
        1.06f,
        "在社交中展现艺术才华"
    ),
    RecommendedCombo(
        "语言思维",
        SkillCategory.LANGUAGE,
        SkillCategory.MENTAL,
        1.07f,
        "语言学习锻炼思维逻辑"
    ),
    RecommendedCombo(
        "生活美学",
        SkillCategory.LIFE,
        SkillCategory.ART,
        1.05f,
        "将艺术融入日常生活"
    ),
    RecommendedCombo(
        "谋生技能",
        SkillCategory.LIVELIHOOD,
        SkillCategory.PHYSICAL,
        1.06f,
        "强健体魄支撑事业发展"
    ),
    RecommendedCombo(
        "社交语言",
        SkillCategory.SOCIAL,
        SkillCategory.LANGUAGE,
        1.07f,
        "语言能力助力社交沟通"
    ),
    RecommendedCombo(
        "创意生活",
        SkillCategory.ART,
        SkillCategory.MENTAL,
        1.08f,
        "创意思维点亮生活"
    ),
    RecommendedCombo(
        "职场社交",
        SkillCategory.LIVELIHOOD,
        SkillCategory.SOCIAL,
        1.06f,
        "社交能力助力职场发展"
    )
)

@HiltViewModel
class ComboViewModel @Inject constructor(
    private val comboRepository: ComboRepository,
    private val skillRepository: SkillRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComboUiState())
    val uiState: StateFlow<ComboUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                combine(
                    comboRepository.getAll(),
                    skillRepository.getActiveSkills()
                ) { combos, skills ->
                    Pair(combos, skills)
                }.collect { (combos, skills) ->
                    _uiState.update {
                        it.copy(
                            combos = combos,
                            skills = skills,
                            recommendedCombos = RECOMMENDED_COMBOS,
                            isLoading = false
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun showCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = true,
                selectedPrimarySkillId = null,
                selectedSecondarySkillId = null,
                comboName = "",
                errorMessage = null
            )
        }
    }

    fun showCreateDialogWithRecommendation(recommended: RecommendedCombo) {
        val primarySkill = _uiState.value.skills.find { it.category == recommended.primaryCategory }
        val secondarySkill = _uiState.value.skills.find { it.category == recommended.secondaryCategory }

        _uiState.update {
            it.copy(
                showCreateDialog = true,
                selectedPrimarySkillId = primarySkill?.id,
                selectedSecondarySkillId = secondarySkill?.id,
                comboName = recommended.name,
                errorMessage = null
            )
        }
    }

    fun dismissCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateDialog = false,
                errorMessage = null
            )
        }
    }

    fun selectPrimarySkill(skillId: Long) {
        _uiState.update { it.copy(selectedPrimarySkillId = skillId, errorMessage = null) }
    }

    fun selectSecondarySkill(skillId: Long) {
        _uiState.update { it.copy(selectedSecondarySkillId = skillId, errorMessage = null) }
    }

    fun updateComboName(name: String) {
        _uiState.update { it.copy(comboName = name) }
    }

    fun createCombo() {
        val state = _uiState.value
        val primaryId = state.selectedPrimarySkillId
        val secondaryId = state.selectedSecondarySkillId

        if (primaryId == null || secondaryId == null) {
            _uiState.update { it.copy(errorMessage = "请选择两个技能") }
            return
        }

        if (primaryId == secondaryId) {
            _uiState.update { it.copy(errorMessage = "请选择不同的技能") }
            return
        }

        val primarySkill = state.skills.find { it.id == primaryId }
        val secondarySkill = state.skills.find { it.id == secondaryId }

        if (primarySkill == null || secondarySkill == null) {
            _uiState.update { it.copy(errorMessage = "技能不存在") }
            return
        }

        if (primarySkill.category == secondarySkill.category) {
            _uiState.update { it.copy(errorMessage = "组合技能必须属于不同类别") }
            return
        }

        val name = state.comboName.ifBlank {
            "${primarySkill.name} + ${secondarySkill.name}"
        }

        val expBonus = calculateExpBonus(primarySkill.category, secondarySkill.category)

        viewModelScope.launch {
            try {
                comboRepository.insertCombo(
                    Combo(
                        name = name,
                        primarySkillId = primaryId,
                        secondarySkillId = secondaryId,
                        expBonus = expBonus,
                        suggestion = null,
                        isActive = true
                    )
                )
                _uiState.update { it.copy(showCreateDialog = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = "创建失败，请重试") }
            }
        }
    }

    fun deleteCombo(id: Long) {
        viewModelScope.launch {
            try {
                val combo = _uiState.value.combos.find { it.id == id } ?: return@launch
                comboRepository.deleteCombo(combo)
            } catch (_: Exception) { }
        }
    }

    fun toggleComboActive(id: Long) {
        viewModelScope.launch {
            try {
                val combo = _uiState.value.combos.find { it.id == id } ?: return@launch
                comboRepository.updateCombo(combo.copy(isActive = !combo.isActive))
            } catch (_: Exception) { }
        }
    }

    private fun calculateExpBonus(cat1: SkillCategory, cat2: SkillCategory): Float {
        return RECOMMENDED_COMBOS.find {
            (it.primaryCategory == cat1 && it.secondaryCategory == cat2) ||
            (it.primaryCategory == cat2 && it.secondaryCategory == cat1)
        }?.expBonus ?: 1.05f
    }
}
