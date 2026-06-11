package com.lifeup.app.ui.demon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.entity.DemonPartDamageEntity
import com.lifeup.app.data.db.entity.DemonProgressEntity
import com.lifeup.app.data.preferences.SettingsPrefs
import com.lifeup.app.domain.model.DemonId
import com.lifeup.app.domain.model.DemonTemplate
import com.lifeup.app.domain.model.InnerDemon
import com.lifeup.app.domain.repository.DemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class DemonDetailUiState(
    val demon: InnerDemon? = null,
    val progress: DemonProgressEntity? = null,
    val partProgress: List<Pair<Int, Float>> = emptyList(),
    val isDefeated: Boolean = false,
    val isUnlocked: Boolean = false,
    val canAttackToday: Boolean = false,
    val todayDayOfWeek: Int = LocalDate.now().dayOfWeek.value
)

@HiltViewModel
class DemonDetailViewModel @Inject constructor(
    private val demonRepository: DemonRepository,
    private val settingsPrefs: SettingsPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(DemonDetailUiState())
    val uiState: StateFlow<DemonDetailUiState> = _uiState.asStateFlow()

    private var currentId: DemonId? = null

    fun load(id: DemonId) {
        if (currentId == id) return
        currentId = id
        viewModelScope.launch {
            val demon = DemonTemplate.ALL.firstOrNull { it.id == id } ?: return@launch
            val progress = demonRepository.getProgressOnce(id)
            val parts = mutableListOf<DemonPartDamageEntity>()
            for (d in 1..7) {
                demonRepository.getPart(id, d)?.let { parts += it }
            }
            val partProgress = demon.basePartHps.mapIndexed { idx, hp ->
                val p = parts.firstOrNull { it.dayOfWeek == idx + 1 }
                val frac = if (hp <= 0) 0f else
                    if (p == null) 0f else 1f - (p.currentHp.toFloat() / p.maxHp)
                hp to frac.coerceIn(0f, 1f)
            }
            val unlocked = settingsPrefs.getUnlockedFeatures().first().contains(demon.unlock.key)
            val today = LocalDate.now().dayOfWeek.value
            _uiState.update {
                it.copy(
                    demon = demon,
                    progress = progress,
                    partProgress = partProgress,
                    isDefeated = progress?.isDefeated == true,
                    isUnlocked = unlocked,
                    canAttackToday = true, // 干扰伤害 15% 始终可以
                    todayDayOfWeek = today
                )
            }
        }
        // 同时观察进度变化
        demonRepository.observeProgress(id)
            .onEach { newProgress ->
                val demon = DemonTemplate.ALL.firstOrNull { it.id == id } ?: return@onEach
                val parts = mutableListOf<DemonPartDamageEntity>()
                for (d in 1..7) {
                    demonRepository.getPart(id, d)?.let { parts += it }
                }
                val partProgress = demon.basePartHps.mapIndexed { idx, hp ->
                    val p = parts.firstOrNull { it.dayOfWeek == idx + 1 }
                    val frac = if (hp <= 0) 0f else
                        if (p == null) 0f else 1f - (p.currentHp.toFloat() / p.maxHp)
                    hp to frac.coerceIn(0f, 1f)
                }
                _uiState.update {
                    it.copy(
                        progress = newProgress,
                        partProgress = partProgress,
                        isDefeated = newProgress?.isDefeated == true
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
