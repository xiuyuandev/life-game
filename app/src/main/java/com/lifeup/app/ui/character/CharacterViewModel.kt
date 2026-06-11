package com.lifeup.app.ui.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.BoundAttribute
import com.lifeup.app.data.db.SlotType
import com.lifeup.app.data.preferences.OutfitPreset
import com.lifeup.app.data.preferences.SettingsPrefs
import com.lifeup.app.domain.calculator.AttributeCalculator
import com.lifeup.app.domain.model.Item
import com.lifeup.app.domain.repository.CharacterStateRepository
import com.lifeup.app.domain.repository.ComboRepository
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.ItemRepository
import com.lifeup.app.domain.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import androidx.compose.runtime.Immutable

@Immutable
data class CharacterUiState(
    val characterLevel: Int = 1,
    val totalExp: Long = 0L,
    val attributes: Map<String, Int> = emptyMap(),
    val equippedItems: List<Item> = emptyList(),
    val backpackItems: List<Item> = emptyList(),
    val title: String = "初学者",
    val isLoading: Boolean = true,
    val outfitPresets: List<OutfitPreset> = emptyList(),
    val totalAttributeBonus: Int = 0,
    val outfitName: String = "默认装束"
)

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val skillRepository: SkillRepository,
    private val itemRepository: ItemRepository,
    private val comboRepository: ComboRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val characterStateRepository: CharacterStateRepository,
    private val settingsPrefs: SettingsPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharacterUiState())
    val uiState: StateFlow<CharacterUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadCharacterData()
    }

    private fun loadCharacterData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                characterStateRepository.initializeIfNeeded()
            } catch (_: Exception) {
            }

            // Pre-fetch latest streak (suspend call must be outside combine lambda)
            val maxStreak = try {
                withTimeout(5000) { dailyStateRepository.getLatestStreak() ?: 0 }
            } catch (_: Exception) {
                0
            }

            try {
                combine(
                    characterStateRepository.getCharacterState(),
                    skillRepository.getActiveSkills(),
                    itemRepository.getEquippedItems(),
                    itemRepository.getUnlockedItems(),
                    settingsPrefs.getOutfitPresets()
                ) { characterState, skills, equippedItems, unlockedItems, presets ->
                    val attributes = try {
                        calculateAttributes(skills, equippedItems, maxStreak)
                    } catch (_: Exception) {
                        emptyMap()
                    }
                    val backpackItems = unlockedItems.filter { !it.isEquipped }
                    val totalAttributeBonus = equippedItems.sumOf { it.attributeBonus }
                    val outfitName = if (equippedItems.isNotEmpty()) {
                        equippedItems.joinToString(" + ") { it.name }
                    } else {
                        "默认装束"
                    }

                    _uiState.update {
                        it.copy(
                            characterLevel = characterState.characterLevel,
                            totalExp = characterState.totalExp,
                            attributes = attributes,
                            equippedItems = equippedItems,
                            backpackItems = backpackItems,
                            title = characterState.title,
                            isLoading = false,
                            outfitPresets = presets,
                            totalAttributeBonus = totalAttributeBonus,
                            outfitName = outfitName
                        )
                    }
                }.collect { }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun calculateAttributes(
        skills: List<com.lifeup.app.domain.model.Skill>,
        equippedItems: List<Item>,
        maxStreak: Int
    ): Map<String, Int> {
        return try {
            val baseMap = BoundAttribute.entries.associate { attr ->
                attr.name to skills
                    .filter { it.boundAttribute == attr }
                    .sumOf { it.getAttributeBonus() }
            }

            val itemBonus = equippedItems.sumOf { it.attributeBonus }

            val endurance = AttributeCalculator.calculateEndurance(
                maxStreak = maxStreak,
                activeSkillCount = skills.size
            )

            val luck = (baseMap.values.sum() / 7) + itemBonus / 2

            baseMap + mapOf(
                "ENDURANCE" to endurance,
                "LUCK" to luck
            )
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun equipItem(item: Item) {
        viewModelScope.launch {
            try {
                val updated = item.copy(
                    isEquipped = true,
                    equippedSlot = item.slotType.name
                )
                itemRepository.updateItem(updated)
            } catch (_: Exception) {
            }
        }
    }

    fun unequipItem(item: Item) {
        viewModelScope.launch {
            try {
                val updated = item.copy(
                    isEquipped = false,
                    equippedSlot = null
                )
                itemRepository.updateItem(updated)
            } catch (_: Exception) {
            }
        }
    }

    fun savePreset(name: String) {
        viewModelScope.launch {
            try {
                val currentPresets = _uiState.value.outfitPresets
                if (currentPresets.size >= 5) return@launch

                val equipped = _uiState.value.equippedItems
                val newId = (currentPresets.maxOfOrNull { it.id } ?: 0) + 1
                val preset = OutfitPreset(
                    id = newId,
                    name = name,
                    headItemId = equipped.find { it.slotType == SlotType.HEAD }?.id,
                    bodyItemId = equipped.find { it.slotType == SlotType.BODY }?.id,
                    handsItemId = equipped.find { it.slotType == SlotType.HANDS }?.id,
                    feetItemId = equipped.find { it.slotType == SlotType.FEET }?.id,
                    accessoryItemId = equipped.find { it.slotType == SlotType.ACCESSORY }?.id
                )
                settingsPrefs.saveOutfitPresets(currentPresets + preset)
            } catch (_: Exception) {
            }
        }
    }

    fun applyPreset(preset: OutfitPreset) {
        viewModelScope.launch {
            try {
                _uiState.value.equippedItems.forEach { item ->
                    val updated = item.copy(isEquipped = false, equippedSlot = null)
                    itemRepository.updateItem(updated)
                }

                val allItems = _uiState.value.equippedItems + _uiState.value.backpackItems
                val slotItemIdMap = mapOf(
                    SlotType.HEAD to preset.headItemId,
                    SlotType.BODY to preset.bodyItemId,
                    SlotType.HANDS to preset.handsItemId,
                    SlotType.FEET to preset.feetItemId,
                    SlotType.ACCESSORY to preset.accessoryItemId
                )

                slotItemIdMap.forEach { (slotType, itemId) ->
                    if (itemId != null) {
                        val item = allItems.find { it.id == itemId }
                        if (item != null) {
                            val updated = item.copy(
                                isEquipped = true,
                                equippedSlot = slotType.name
                            )
                            itemRepository.updateItem(updated)
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun deletePreset(presetId: Long) {
        viewModelScope.launch {
            try {
                val updated = _uiState.value.outfitPresets.filter { it.id != presetId }
                settingsPrefs.saveOutfitPresets(updated)
            } catch (_: Exception) {
            }
        }
    }
}
