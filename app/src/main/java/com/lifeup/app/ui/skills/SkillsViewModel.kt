package com.lifeup.app.ui.skills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.SkillStatus
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.repository.ComboRepository
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.ItemRepository
import com.lifeup.app.domain.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class SkillsUiState(
    val skills: List<Skill> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val energy: Float = 0f,
    val energyCap: Float = 100f
)

@HiltViewModel
class SkillsViewModel @Inject constructor(
    private val skillRepository: SkillRepository,
    private val comboRepository: ComboRepository,
    private val itemRepository: ItemRepository,
    private val dailyStateRepository: DailyStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkillsUiState())
    val uiState: StateFlow<SkillsUiState> = _uiState.asStateFlow()

    private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

    private var skillsJob: Job? = null
    private var energyJob: Job? = null

    init {
        loadActiveSkills()
        loadEnergy()
    }

    fun refresh() {
        loadActiveSkills(isRefresh = true)
        loadEnergy()
    }

    private fun loadActiveSkills(isRefresh: Boolean = false) {
        skillsJob?.cancel()
        skillsJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = !isRefresh, isRefreshing = isRefresh, error = null) }
            try {
                skillRepository.getActiveSkills().collect { skills ->
                    _uiState.update { it.copy(skills = skills, isLoading = false, isRefreshing = false, error = null) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, isRefreshing = false, error = e.message ?: "加载失败") }
            }
        }
    }

    private fun loadEnergy() {
        energyJob?.cancel()
        energyJob = viewModelScope.launch {
            val todayStr = LocalDate.now().format(dateFormat)
            try {
                dailyStateRepository.getStateByDate(todayStr).collect { dailyState ->
                    val state = dailyState ?: return@collect
                    _uiState.update { it.copy(energy = state.energy, energyCap = state.energyCap) }
                }
            } catch (_: Exception) {
                // Energy load failure is non-critical
            }
        }
    }

    fun pauseSkill(id: Long) {
        viewModelScope.launch {
            try {
                val skill = skillRepository.getSkillById(id) ?: return@launch
                skillRepository.updateSkill(skill.copy(status = SkillStatus.PAUSED))
            } catch (_: Exception) { }
        }
    }

    fun archiveSkill(id: Long) {
        viewModelScope.launch {
            try {
                val skill = skillRepository.getSkillById(id) ?: return@launch
                skillRepository.updateSkill(skill.copy(status = SkillStatus.ARCHIVED))
            } catch (_: Exception) { }
        }
    }

    fun resumeSkill(id: Long) {
        viewModelScope.launch {
            try {
                val skill = skillRepository.getSkillById(id) ?: return@launch
                skillRepository.updateSkill(skill.copy(status = SkillStatus.ACTIVE))
            } catch (_: Exception) { }
        }
    }
}
