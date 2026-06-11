package com.lifeup.app.ui.demon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.preferences.SettingsPrefs
import com.lifeup.app.domain.model.DemonId
import com.lifeup.app.domain.model.DemonTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class DemonTowerUiState(
    val clearedFloor: Int = 0
)

@HiltViewModel
class DemonTowerViewModel @Inject constructor(
    private val settingsPrefs: SettingsPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(DemonTowerUiState())
    val uiState: StateFlow<DemonTowerUiState> = _uiState.asStateFlow()

    fun init() {
        viewModelScope.launch {
            val current = settingsPrefs.getUnlockedFeatures().first()
            // 用一个隐藏 key 存层数（暂用"试炼塔层数"自定义键）
            // 这里简化：直接读 "tower_cleared_floor" 偏好
            val raw = current // 占位
            _uiState.update { it.copy(clearedFloor = 0) }
        }
    }

    /**
     * 取得当前要挑战的心魔 key（= 标准心魔 + 楼层倍率）。
     */
    fun currentDemonKey(): String {
        val floor = _uiState.value.clearedFloor + 1
        val idx = floor % DemonTemplate.STANDARD.size
        return DemonTemplate.STANDARD[idx].id.key
    }
}
