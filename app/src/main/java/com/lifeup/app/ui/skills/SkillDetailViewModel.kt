package com.lifeup.app.ui.skills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.SkillStatus
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SkillDetailUiState(
    val skill: Skill? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class SkillDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val skillRepository: SkillRepository
) : ViewModel() {

    private val skillId: Long = savedStateHandle["skillId"] ?: 0L

    private val _uiState = MutableStateFlow(SkillDetailUiState())
    val uiState: StateFlow<SkillDetailUiState> = _uiState.asStateFlow()

    init {
        loadSkill()
    }

    private fun loadSkill() {
        viewModelScope.launch {
            val skill = skillRepository.getSkillById(skillId)
            _uiState.update { it.copy(skill = skill, isLoading = false) }
        }
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
