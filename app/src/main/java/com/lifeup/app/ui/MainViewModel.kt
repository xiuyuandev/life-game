package com.lifeup.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.preferences.SettingsPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsPrefs: SettingsPrefs
) : ViewModel() {

    /**
     * Default to false (show onboarding) until we confirm the user has completed it.
     * This prevents the brief flash of TodayScreen on first launch before the
     * DataStore Flow emits its first value.
     */
    val isOnboardingCompleted: StateFlow<Boolean> = settingsPrefs.isOnboardingCompleted()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
}
