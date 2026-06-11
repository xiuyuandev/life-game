package com.lifeup.app.ui.demon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.data.db.entity.CustomDemonEntity
import com.lifeup.app.domain.repository.DemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class DemonCreatorUiState(
    val name: String = "",
    val description: String = "",
    val colorHex: String = PALETTE.first(),
    val weakCategories: Set<SkillCategory> = emptySet(),
    val resistCategories: Set<SkillCategory> = emptySet(),
    val weekHps: List<Int> = listOf(100, 100, 100, 100, 100, 100, 100)
) {
    val canSave: Boolean
        get() = name.isNotBlank() && weekHps.all { it in 50..200 }
}

@HiltViewModel
class DemonCreatorViewModel @Inject constructor(
    private val demonRepository: DemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DemonCreatorUiState())
    val uiState: StateFlow<DemonCreatorUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onDescriptionChange(value: String) = _uiState.update { it.copy(description = value) }
    fun onColorChange(value: String) = _uiState.update { it.copy(colorHex = value) }

    fun toggleWeak(category: SkillCategory) = _uiState.update {
        val newSet = if (category in it.weakCategories) it.weakCategories - category
        else it.weakCategories + category
        it.copy(weakCategories = newSet, resistCategories = it.resistCategories - category)
    }

    fun toggleResist(category: SkillCategory) = _uiState.update {
        val newSet = if (category in it.resistCategories) it.resistCategories - category
        else it.resistCategories + category
        it.copy(resistCategories = newSet, weakCategories = it.weakCategories - category)
    }

    fun setPartHp(index: Int, value: Int) = _uiState.update {
        val list = it.weekHps.toMutableList()
        if (index in list.indices) list[index] = value.coerceIn(50, 200)
        it.copy(weekHps = list)
    }

    fun save() {
        val s = _uiState.value
        if (!s.canSave) return
        viewModelScope.launch {
            demonRepository.insertCustomDemon(
                CustomDemonEntity(
                    displayName = s.name,
                    description = s.description,
                    colorHex = s.colorHex,
                    weakCategories = s.weakCategories.joinToString(",") { it.name },
                    resistCategories = s.resistCategories.joinToString(",") { it.name },
                    partHpMap = s.weekHps.mapIndexed { idx, hp -> "${idx + 1}:$hp" }.joinToString(","),
                    maxHp = s.weekHps.sum()
                )
            )
        }
    }

    companion object {
        val PALETTE: List<String> = listOf(
            "#5B6B7A", "#3B3F4E", "#7E8F9C", "#5468FF",
            "#1E88E5", "#7B1FA2", "#283593", "#6D4C41",
            "#90A4AE", "#C62828", "#5E35B1", "#212121"
        )
    }
}
