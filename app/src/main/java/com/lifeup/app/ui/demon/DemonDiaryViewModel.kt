package com.lifeup.app.ui.demon

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.entity.DemonDiaryEntity
import com.lifeup.app.domain.model.DemonId
import com.lifeup.app.domain.model.DemonTemplate
import com.lifeup.app.domain.model.InnerDemon
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
data class DemonDiaryUiState(
    val demon: InnerDemon? = null,
    val diaries: List<DemonDiaryEntity> = emptyList()
)

@HiltViewModel
class DemonDiaryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val demonRepository: DemonRepository
) : ViewModel() {

    private val demonIdKey: String = savedStateHandle["demonId"] ?: DemonId.PROCRASTINATION_SERPENT.key
    private val demonId: DemonId = DemonId.fromKey(demonIdKey) ?: DemonId.PROCRASTINATION_SERPENT

    private val _uiState = MutableStateFlow(DemonDiaryUiState())
    val uiState: StateFlow<DemonDiaryUiState> = _uiState.asStateFlow()

    fun load(id: DemonId) {
        val demon = DemonTemplate.ALL.firstOrNull { it.id == id } ?: return
        _uiState.update { it.copy(demon = demon) }
        demonRepository.observeDiariesFor(id)
            .onEach { list ->
                _uiState.update { it.copy(diaries = list) }
            }
            .launchIn(viewModelScope)
    }

    fun addDiary(demonId: DemonId, date: String, content: String, takeaway: String) {
        viewModelScope.launch {
            demonRepository.insertDiary(
                DemonDiaryEntity(
                    demonId = demonId.key,
                    date = date,
                    content = content,
                    takeaway = takeaway.takeIf { it.isNotBlank() }
                )
            )
        }
    }

    fun deleteDiary(id: Long) {
        viewModelScope.launch {
            demonRepository.deleteDiary(id)
        }
    }
}
