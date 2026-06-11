package com.lifeup.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.preferences.SettingsPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val firstTimerBonusEnabled: Boolean = true,
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsPrefs: SettingsPrefs
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsPrefs.isSoundEnabled(),
        settingsPrefs.isHapticEnabled()
    ) { sound, haptic ->
        SettingsUiState(
            soundEnabled = sound,
            hapticEnabled = haptic,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.setSoundEnabled(enabled)
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.setHapticEnabled(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        // Kept as local-only for now (no DataStore backing field).
        // When notifications settings are added, pipe through SettingsPrefs.
    }

    fun setFirstTimerBonusEnabled(enabled: Boolean) {
        // Kept as local-only for now (no DataStore backing field).
    }
}
