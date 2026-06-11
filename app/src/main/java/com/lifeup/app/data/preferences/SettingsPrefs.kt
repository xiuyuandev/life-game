package com.lifeup.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsPrefs @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val FIRST_TIMER_USED_TODAY = booleanPreferencesKey("first_timer_used_today")
    }

    fun getThemeMode(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[Keys.THEME_MODE] ?: "system"
        }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode
        }
    }

    fun isOnboardingCompleted(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] ?: false
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    fun isFirstTimerUsedToday(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[Keys.FIRST_TIMER_USED_TODAY] ?: false
        }
    }

    suspend fun setFirstTimerUsedToday(used: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.FIRST_TIMER_USED_TODAY] = used
        }
    }
}
