package com.lifeup.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.domain.calculator.AttributeCalculator
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.SkillRepository
import com.lifeup.app.domain.repository.TimeRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val totalTime: Long = 0L,
    val skillCount: Int = 0,
    val maxLevel: Int = 1,
    val characterLevel: Int = 1,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val skillRepository: SkillRepository,
    private val timeRecordRepository: TimeRecordRepository,
    private val dailyStateRepository: DailyStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            skillRepository.getActiveSkills().collect { skills ->
                val totalTime = skills.sumOf { it.totalMinutes }
                val skillCount = skills.size
                val maxLevel = skills.maxOfOrNull { it.level } ?: 1
                val totalExp = totalTime * 10L
                val characterLevel = AttributeCalculator.calculateCharacterLevel(totalExp).coerceAtLeast(1)

                _uiState.update {
                    it.copy(
                        totalTime = totalTime,
                        skillCount = skillCount,
                        maxLevel = maxLevel,
                        characterLevel = characterLevel,
                        isLoading = false
                    )
                }
            }
        }
    }
}
