package com.lifeup.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "lifeup_settings")

@Singleton
class SettingsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val KEY_DAILY_REMINDER = booleanPreferencesKey("daily_reminder")
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND_ENABLED] ?: true
    }

    val vibrationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_VIBRATION_ENABLED] ?: true
    }

    val dailyReminderEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DAILY_REMINDER] ?: false
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SOUND_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_DAILY_REMINDER] = enabled
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
