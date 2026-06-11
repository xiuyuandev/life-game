package com.lifeup.app.ui.demon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.entity.DemonProgressEntity
import com.lifeup.app.domain.game.DemonEngine
import com.lifeup.app.domain.model.DemonId
import com.lifeup.app.domain.model.DemonTemplate
import com.lifeup.app.domain.repository.DemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class DemonListUiState(
    val isLoading: Boolean = true,
    val progressMap: Map<DemonId, DemonProgressEntity> = emptyMap(),
    val defeatedIds: Set<DemonId> = emptySet(),
    val totalDefeated: Int = 0,
    val totalHpSum: Int = 0,
    val currentHpSum: Int = 0
)

@HiltViewModel
class DemonListViewModel @Inject constructor(
    private val demonRepository: DemonRepository,
    private val demonEngine: DemonEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(DemonListUiState())
    val uiState: StateFlow<DemonListUiState> = _uiState.asStateFlow()

    init {
        observeAll()
    }

    /**
     * 首次进入时确保 12 只标准心魔的行已被创建。
     */
    fun seed() {
        viewModelScope.launch {
            demonEngine.seedStandardDemonsIfNeeded()
            demonEngine.ensureMirrorIfUnlocked()
        }
    }

    private fun observeAll() {
        demonRepository.observeAllProgress()
            .onEach { list ->
                val map = buildMap<DemonId, DemonProgressEntity> {
                    list.forEach { entity ->
                        val id = DemonId.fromKey(entity.demonId) ?: return@forEach
                        put(id, entity)
                    }
                }
                val defeated = map.filterValues { it.isDefeated }.keys
                val totalHp = DemonTemplate.STANDARD.sumOf { it.totalHp }
                val currentHp = map.values
                    .filter { it.demonId != DemonId.MIRROR_OF_SELF.key }
                    .sumOf { it.currentHp }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        progressMap = map,
                        defeatedIds = defeated,
                        totalDefeated = defeated.size,
                        totalHpSum = totalHp,
                        currentHpSum = currentHp
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
