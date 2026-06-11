package com.lifeup.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class OutfitPreset(
    val id: Long = 0,
    val name: String,
    val headItemId: Long? = null,
    val bodyItemId: Long? = null,
    val handsItemId: Long? = null,
    val feetItemId: Long? = null,
    val accessoryItemId: Long? = null
)

@Singleton
class SettingsPrefs @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val FIRST_TIMER_USED_TODAY = booleanPreferencesKey("first_timer_used_today")
        val OUTFIT_PRESETS = stringPreferencesKey("outfit_presets")
        val LAST_BACKUP_DATE = stringPreferencesKey("last_backup_date")
        val DISMISSED_TIPS = stringPreferencesKey("dismissed_tips")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val DAILY_QUOTE_INDEX = intPreferencesKey("daily_quote_index")
        val UNLOCKED_FEATURES = stringPreferencesKey("unlocked_features")
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

    suspend fun resetFirstTimerUsedToday() {
        dataStore.edit { preferences ->
            preferences[Keys.FIRST_TIMER_USED_TODAY] = false
        }
    }

    fun getOutfitPresets(): Flow<List<OutfitPreset>> {
        return dataStore.data.map { preferences ->
            val jsonString = preferences[Keys.OUTFIT_PRESETS] ?: "[]"
            try {
                json.decodeFromString<List<OutfitPreset>>(jsonString)
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveOutfitPresets(presets: List<OutfitPreset>) {
        val jsonString = json.encodeToString(presets)
        dataStore.edit { preferences ->
            preferences[Keys.OUTFIT_PRESETS] = jsonString
        }
    }

    fun getLastBackupDate(): Flow<String?> {
        return dataStore.data.map { preferences ->
            preferences[Keys.LAST_BACKUP_DATE]
        }
    }

    suspend fun setLastBackupDate(date: String) {
        dataStore.edit { preferences ->
            preferences[Keys.LAST_BACKUP_DATE] = date
        }
    }

    fun getDismissedTips(): Flow<Set<String>> {
        return dataStore.data.map { preferences ->
            val raw = preferences[Keys.DISMISSED_TIPS] ?: ""
            if (raw.isBlank()) emptySet() else raw.split(",").toSet()
        }
    }

    suspend fun dismissTip(tipId: String) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.DISMISSED_TIPS] ?: ""
            val set = if (current.isBlank()) mutableSetOf() else current.split(",").toMutableSet()
            set.add(tipId)
            preferences[Keys.DISMISSED_TIPS] = set.joinToString(",")
        }
    }

    // ---- Sound & Haptic feedback preferences ----

    fun isSoundEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[Keys.SOUND_ENABLED] ?: true
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.SOUND_ENABLED] = enabled
        }
    }

    fun isHapticEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[Keys.HAPTIC_ENABLED] ?: true
        }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.HAPTIC_ENABLED] = enabled
        }
    }

    fun getDailyQuoteIndex(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[Keys.DAILY_QUOTE_INDEX] ?: 0
        }
    }

    suspend fun setDailyQuoteIndex(index: Int) {
        dataStore.edit { preferences ->
            preferences[Keys.DAILY_QUOTE_INDEX] = index
        }
    }

    // ---- 真实功能解锁（击败心魔后写入） ----

    fun getUnlockedFeatures(): Flow<Set<String>> {
        return dataStore.data.map { preferences ->
            val raw = preferences[Keys.UNLOCKED_FEATURES] ?: ""
            if (raw.isBlank()) emptySet() else raw.split(",").toSet()
        }
    }

    suspend fun unlockFeature(featureKey: String) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.UNLOCKED_FEATURES] ?: ""
            val set = if (current.isBlank()) mutableSetOf() else current.split(",").toMutableSet()
            set.add(featureKey)
            preferences[Keys.UNLOCKED_FEATURES] = set.joinToString(",")
        }
    }

    suspend fun unlockFeature(feature: com.lifeup.app.domain.model.DemonUnlockKey) {
        unlockFeature(feature.key)
    }

    suspend fun isFeatureUnlocked(featureKey: String): Boolean {
        return getUnlockedFeatures().first().contains(featureKey)
    }
}
