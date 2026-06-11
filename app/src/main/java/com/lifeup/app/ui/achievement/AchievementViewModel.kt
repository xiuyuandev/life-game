package com.lifeup.app.ui.achievement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.domain.game.Achievement
import com.lifeup.app.domain.game.AchievementChecker
import com.lifeup.app.domain.repository.ComboRepository
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.ItemRepository
import com.lifeup.app.domain.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementUiState(
    val achievements: List<Achievement> = emptyList(),
    val unlockedCount: Int = 0,
    val totalCount: Int = 14,
    val newlyUnlocked: Achievement? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val skillRepository: SkillRepository,
    private val itemRepository: ItemRepository,
    private val comboRepository: ComboRepository,
    private val dailyStateRepository: DailyStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementUiState())
    val uiState: StateFlow<AchievementUiState> = _uiState.asStateFlow()

    init {
        loadAchievements()
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                skillRepository.getActiveSkills(),
                itemRepository.getUnlockedItems(),
                comboRepository.getActiveCombos()
            ) { skills, unlockedItems, activeCombos ->
                Triple(skills, unlockedItems, activeCombos)
            }.collect { (skills, unlockedItems, activeCombos) ->
                val streakCount = dailyStateRepository.getLatestStreak() ?: 0
                val achievements = AchievementChecker.checkAchievements(
                    skills, streakCount, unlockedItems, activeCombos
                )
                val unlockedCount = achievements.count { it.isUnlocked }

                val previousUnlocked = _uiState.value.achievements.filter { it.isUnlocked }.map { it.id }.toSet()
                val currentUnlocked = achievements.filter { it.isUnlocked }.map { it.id }.toSet()
                val newlyUnlockedIds = currentUnlocked - previousUnlocked
                val newlyUnlocked = if (newlyUnlockedIds.isNotEmpty() && _uiState.value.achievements.isNotEmpty()) {
                    achievements.find { it.id in newlyUnlockedIds }
                } else {
                    null
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
