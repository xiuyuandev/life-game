package com.lifeup.app.ui.demon

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.domain.game.DamageBreakdown
import com.lifeup.app.domain.game.DemonBattleOutcome
import com.lifeup.app.domain.game.DemonEngine
import com.lifeup.app.domain.model.DemonId
import com.lifeup.app.domain.model.DemonTemplate
import com.lifeup.app.domain.model.DemonUnlockKey
import com.lifeup.app.domain.model.InnerDemon
import com.lifeup.app.domain.repository.DemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class DemonBattleUiState(
    val demon: InnerDemon? = null,
    val outcome: DemonBattleOutcome? = null,
    val breakdown: DamageBreakdown? = null,
    val isDefeated: Boolean = false,
    val unlockedFeature: DemonUnlockKey? = null,
    val lastBattleDate: String? = null
)

/**
 * 把"刚刚那次攻击"展示出来的 ViewModel。
 *
 * 真正的伤害计算与持久化由 [com.lifeup.app.ui.timer.TimerViewModel] 负责
 * （在计时结束时同步调用 [DemonEngine.applySessionResult]）。
 * 本 VM 只读取最新结果用于 UI 展示。
 */
@HiltViewModel
class DemonBattleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val demonRepository: DemonRepository,
    private val demonEngine: DemonEngine
) : ViewModel() {

    private val demonIdKey: String = savedStateHandle["demonId"] ?: DemonId.PROCRASTINATION_SERPENT.key

    private val _uiState = MutableStateFlow(DemonBattleUiState())
    val uiState: StateFlow<DemonBattleUiState> = _uiState.asStateFlow()

    /**
     * 读取最新一次会话结果。
     * 由 [com.lifeup.app.ui.timer.TimerViewModel] 在 stop 时把
     * (demonId, focusMinutes, skillCategory) 写入 SavedStateHandle / NavBackStackEntry。
     */
    fun load(id: DemonId) {
        viewModelScope.launch {
            val demon = DemonTemplate.ALL.firstOrNull { it.id == id } ?: return@launch
            val progress = demonRepository.getProgressOnce(id)
            _uiState.update {
                it.copy(
                    demon = demon,
                    isDefeated = progress?.isDefeated == true,
                    lastBattleDate = LocalDate.now().toString()
                )
            }
        }
    }

    /**
     * 由 TimerViewModel 在计时结束后调用，把结果直接灌入 UI。
     */
    fun submitOutcome(outcome: DemonBattleOutcome) {
        viewModelScope.launch {
            val demon = DemonTemplate.ALL.firstOrNull { it.id == outcome.demon.id } ?: return@launch
            val unlocked = if (outcome is DemonBattleOutcome.Defeated) outcome.unlockedFeature else null
            _uiState.update {
                it.copy(
                    demon = demon,
                    outcome = outcome,
                    breakdown = outcome.breakdown,
                    isDefeated = outcome is DemonBattleOutcome.Defeated ||
                        (it.demon?.let { d -> demonRepository.getProgressOnce(d.id)?.isDefeated == true } ?: false),
                    unlockedFeature = unlocked
                )
            }
        }
    }
}
