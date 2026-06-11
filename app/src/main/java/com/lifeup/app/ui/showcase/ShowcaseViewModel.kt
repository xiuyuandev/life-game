package com.lifeup.app.ui.showcase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class ShowcaseUiState(
    val skills: List<Skill> = emptyList(),
    val showcaseSkills: List<Skill> = emptyList(),
    val halls: List<ShowcaseHall> = emptyList(),
    val isLoading: Boolean = true,
    val isReorderMode: Boolean = false,
    val reorderHallIndex: Int = -1
)

@Immutable
data class ShowcaseHall(
    val name: String,
    val category: SkillCategory?,
    val skills: List<Skill>,
    val centerSkill: Skill?
)

@Immutable
private data class HallDefinition(
    val name: String,
    val category: SkillCategory
)

private val HALL_DEFINITIONS = listOf(
    HallDefinition("谋生之道", SkillCategory.LIVELIHOOD),
    HallDefinition("社交达人", SkillCategory.SOCIAL),
    HallDefinition("语言天赋", SkillCategory.LANGUAGE),
    HallDefinition("生活美学", SkillCategory.LIFE),
    HallDefinition("体能锻造", SkillCategory.PHYSICAL),
    HallDefinition("心智修炼", SkillCategory.MENTAL),
    HallDefinition("艺术殿堂", SkillCategory.ART)
)

@HiltViewModel
class ShowcaseViewModel @Inject constructor(
    private val skillRepository: SkillRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowcaseUiState())
    val uiState: StateFlow<ShowcaseUiState> = _uiState.asStateFlow()

    init {
        loadSkills()
    }

    private fun loadSkills() {
        viewModelScope.launch {
            try {
                skillRepository.getActiveSkills().collect { skills ->
                    val showcaseSkills = skills.filter { it.displayInShowcase }
                    val halls = buildHalls(showcaseSkills)
                    _uiState.update {
                        it.copy(
                            skills = skills,
                            showcaseSkills = showcaseSkills,
                            halls = halls,
                            isLoading = false
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun buildHalls(skills: List<Skill>): List<ShowcaseHall> {
        return HALL_DEFINITIONS.map { definition ->
            val hallSkills = skills
                .filter { it.category == definition.category }
                .sortedWith(compareByDescending<Skill> { it.level }.thenByDescending { it.totalMinutes })
            val centerSkill = hallSkills.firstOrNull()
            ShowcaseHall(
                name = definition.name,
                category = definition.category,
                skills = hallSkills,
                centerSkill = centerSkill
            )
        }
    }

    fun enterReorderMode(hallIndex: Int) {
        _uiState.update {
            it.copy(isReorderMode = true, reorderHallIndex = hallIndex)
        }
    }

    fun exitReorderMode() {
        _uiState.update {
            it.copy(isReorderMode = false, reorderHallIndex = -1)
        }
    }

    fun moveSkillUp(skillId: Long) {
        val state = _uiState.value
        val hall = state.halls.getOrNull(state.reorderHallIndex) ?: return
        val skills = hall.skills.toMutableList()
        val currentIndex = skills.indexOfFirst { it.id == skillId }
        if (currentIndex <= 0) return
        skills.add(currentIndex - 1, skills.removeAt(currentIndex))
        saveReorderedSkills(skills)
    }

    fun moveSkillDown(skillId: Long) {
        val state = _uiState.value
        val hall = state.halls.getOrNull(state.reorderHallIndex) ?: return
        val skills = hall.skills.toMutableList()
        val currentIndex = skills.indexOfFirst { it.id == skillId }
        if (currentIndex < 0 || currentIndex >= skills.size - 1) return
        skills.add(currentIndex + 1, skills.removeAt(currentIndex))
        saveReorderedSkills(skills)
    }

    private fun saveReorderedSkills(skills: List<Skill>) {
        viewModelScope.launch {
            try {
                skills.forEachIndexed { index, skill ->
                    val newSortOrder = skills.size - index
                    if (skill.sortOrder != newSortOrder) {
                        skillRepository.updateSkill(skill.copy(sortOrder = newSortOrder))
                    }
                }
            } catch (_: Exception) { }
        }
    }

    fun getShareText(): String {
        val state = _uiState.value
        val totalSkills = state.showcaseSkills.size
        val totalHours = state.showcaseSkills.sumOf { it.totalMinutes } / 60
        val highestSkill = state.showcaseSkills.maxByOrNull { it.level }
        val highestLine = if (highestSkill != null) {
            "最高等级: ${highestSkill.name} LV${highestSkill.level}"
        } else {
            "最高等级: 暂无"
        }
        return "🎯 人生升级 | 技能图鉴\n已修炼 $totalSkills 个技能，累计 $totalHours 小时\n$highestLine"
    }
}
