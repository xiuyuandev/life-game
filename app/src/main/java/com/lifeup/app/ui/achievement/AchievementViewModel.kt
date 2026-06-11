package com.lifeup.app.ui.achievement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.domain.model.Achievement
import com.lifeup.app.domain.repository.AchievementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

data class AchievementUiState(
    val achievements: List<Achievement> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 0,
    val newlyUnlocked: Achievement? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

    private val _newlyUnlocked = MutableSharedFlow<Achievement>()
    val newlyUnlocked: SharedFlow<Achievement> = _newlyUnlocked.asSharedFlow()

    init {
        viewModelScope.launch {
            achievementRepository.initializeAchievements()
            loadAchievements()
        }
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            achievementRepository.getAllAchievements().collect { achievements ->
                val unlockedCount = achievements.count { it.isUnlocked }
                val previousUnlocked = _uiState.value.achievements.filter { it.isUnlocked }.map { it.id }.toSet()
                val currentUnlocked = achievements.filter { it.isUnlocked }.map { it.id }.toSet()
                val newlyUnlockedIds = currentUnlocked - previousUnlocked
                val newlyUnlocked = if (newlyUnlockedIds.isNotEmpty() && _uiState.value.achievements.isNotEmpty()) {
                    achievements.find { it.id in newlyUnlockedIds }
                } else {
                    null
                }

                if (newlyUnlocked != null) {
                    _newlyUnlocked.emit(newlyUnlocked)
                }

                _uiState.update {
                    it.copy(
                        achievements = achievements,
                        unlockedCount = unlockedCount,
                        totalCount = achievements.size,
                        newlyUnlocked = newlyUnlocked,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun dismissNewAchievement() {
        _uiState.update { it.copy(newlyUnlocked = null) }
    }
}
