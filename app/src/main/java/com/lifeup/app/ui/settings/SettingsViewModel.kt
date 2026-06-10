package com.lifeup.app.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.backup.DataBackupManager
import com.lifeup.app.data.db.entity.AchievementEntity
import com.lifeup.app.data.db.entity.CharacterEntity
import com.lifeup.app.data.db.entity.EquipmentEntity
import com.lifeup.app.data.db.entity.TimeSessionEntity
import com.lifeup.app.data.preferences.SettingsPreferences
import com.lifeup.app.data.repository.AchievementRepository
import com.lifeup.app.data.repository.CharacterRepository
import com.lifeup.app.data.repository.EquipmentRepository
import com.lifeup.app.data.repository.SkillRepository
import com.lifeup.app.data.repository.TimeSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val equipmentRepository: EquipmentRepository,
    private val achievementRepository: AchievementRepository,
    private val timeSessionRepository: TimeSessionRepository,
    private val skillRepository: SkillRepository,
    private val settingsPreferences: SettingsPreferences,
    private val dataBackupManager: DataBackupManager
) : ViewModel() {

    private val _character = MutableStateFlow<CharacterEntity?>(null)
    val character: StateFlow<CharacterEntity?> = _character.asStateFlow()

    private val _equipment = MutableStateFlow<List<EquipmentEntity>>(emptyList())
    val equipment: StateFlow<List<EquipmentEntity>> = _equipment.asStateFlow()

    private val _achievements = MutableStateFlow<List<AchievementEntity>>(emptyList())
    val achievements: StateFlow<List<AchievementEntity>> = _achievements.asStateFlow()

    private val _unlockedCount = MutableStateFlow(0)
    val unlockedCount: StateFlow<Int> = _unlockedCount.asStateFlow()

    private val _sessionHistory = MutableStateFlow<List<TimeSessionEntity>>(emptyList())
    val sessionHistory: StateFlow<List<TimeSessionEntity>> = _sessionHistory.asStateFlow()

    // Preferences
    val soundEnabled = settingsPreferences.soundEnabled
    val vibrationEnabled = settingsPreferences.vibrationEnabled
    val dailyReminderEnabled = settingsPreferences.dailyReminderEnabled

    // Backup result
    private val _backupResult = MutableStateFlow<DataBackupManager.BackupResult?>(null)
    val backupResult: StateFlow<DataBackupManager.BackupResult?> = _backupResult.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        characterRepository.getCharacterFlow()
            .onEach { _character.value = it }
            .launchIn(viewModelScope)

        equipmentRepository.getAllEquipmentFlow()
            .onEach { _equipment.value = it }
            .launchIn(viewModelScope)

        achievementRepository.getAllAchievementsFlow()
            .onEach { _achievements.value = it }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            _unlockedCount.value = achievementRepository.getUnlockedCount()
        }

        viewModelScope.launch {
            _sessionHistory.value = timeSessionRepository.getRecentSessions(100)
        }
    }

    fun createCharacter(name: String) {
        viewModelScope.launch {
            characterRepository.createCharacter(name)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            characterRepository.resetCharacter()
            skillRepository.deleteAll()
            timeSessionRepository.deleteAll()
            equipmentRepository.deleteAll()
            achievementRepository.deleteAll()
        }
    }

    fun purchaseEquipment(equipmentId: Long) {
        viewModelScope.launch {
            val char = characterRepository.getCharacter() ?: return@launch
            equipmentRepository.purchaseEquipment(equipmentId, char.gold)
        }
    }

    fun equip(equipmentId: Long) {
        viewModelScope.launch {
            equipmentRepository.equip(equipmentId)
        }
    }

    fun unequip(equipmentId: Long) {
        viewModelScope.launch {
            equipmentRepository.unequip(equipmentId)
        }
    }

    fun refreshHistory() {
        viewModelScope.launch {
            _sessionHistory.value = timeSessionRepository.getRecentSessions(100)
        }
    }

    // Preferences
    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setSoundEnabled(enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setVibrationEnabled(enabled)
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.setDailyReminderEnabled(enabled)
        }
    }

    // Backup
    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            val result = dataBackupManager.exportToUri(uri)
            _backupResult.value = result
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            val result = dataBackupManager.importFromUri(uri)
            _backupResult.value = result
            // Refresh data after import
            if (result.success) {
                loadData()
            }
        }
    }

    fun clearBackupResult() {
        _backupResult.value = null
    }
}
