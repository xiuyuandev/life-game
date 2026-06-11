package com.lifeup.app.ui.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.BoundAttribute
import com.lifeup.app.data.db.SlotType
import com.lifeup.app.data.preferences.OutfitPreset
import com.lifeup.app.data.preferences.SettingsPrefs
import com.lifeup.app.domain.calculator.AttributeCalculator
import com.lifeup.app.domain.model.Item
import com.lifeup.app.domain.repository.ComboRepository
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.ItemRepository
import com.lifeup.app.domain.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val settingsPrefs: SettingsPrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(CharacterUiState())
    val uiState: StateFlow<CharacterUiState> = _uiState.asStateFlow()

    init {
        loadCharacterData()
    }

    private fun loadCharacterData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                skillRepository.getActiveSkills(),
                itemRepository.getEquippedItems(),
                itemRepository.getUnlockedItems(),
                comboRepository.getActiveCombos(),
                settingsPrefs.getOutfitPresets()
            ) { skills, equippedItems, unlockedItems, _, presets ->
                val totalMinutes = skills.sumOf { it.totalMinutes }
                val totalExp = totalMinutes * 10L
                val characterLevel = AttributeCalculator.calculateCharacterLevel(totalExp).coerceAtLeast(1)

                val attributes = calculateAttributes(skills, equippedItems)

                val backpackItems = unlockedItems.filter { !it.isEquipped }

                val title = deriveTitle(characterLevel)

                val totalAttributeBonus = equippedItems.sumOf { it.attributeBonus }

                val outfitName = if (equippedItems.isNotEmpty()) {
                    equippedItems.joinToString(" + ") { it.name }
                } else {
                    "默认装束"
                }

                _uiState.update {
                    it.copy(
                        characterLevel = characterLevel,
                        totalExp = totalExp,
                        attributes = attributes,
                        equippedItems = equippedItems,
                        backpackItems = backpackItems,
                        title = title,
                        isLoading = false,
                        outfitPresets = presets,
                        totalAttributeBonus = totalAttributeBonus,
                        outfitName = outfitName
                    )
                }
            }.collect { }
        }
    }

    private fun calculateAttributes(
        skills: List<com.lifeup.app.domain.model.Skill>,
        equippedItems: List<Item>
    ): Map<String, Int> {
        val baseMap = BoundAttribute.entries.associate { attr ->
            attr.name to skills
                .filter { it.boundAttribute == attr }
                .sumOf { it.getAttributeBonus() }
        }

        val itemBonus = equippedItems.sumOf { it.attributeBonus }

        val endurance = AttributeCalculator.calculateEndurance(
            maxStreak = dailyStateRepository.getLatestStreak() ?: 0,
            activeSkillCount = skills.size
        )

        val luck = (baseMap.values.sum() / 7) + itemBonus / 2

        return baseMap + mapOf(
            "ENDURANCE" to endurance,
            "LUCK" to luck
        )
    }

    private fun deriveTitle(level: Int): String {
        return when {
            level >= 20 -> "传奇大师"
            level >= 15 -> "宗师"
            level >= 10 -> "专家"
            level >= 5 -> "熟练者"
            level >= 3 -> "学徒"
            else -> "初学者"
        }
    }

    fun equipItem(item: Item) {
        viewModelScope.launch {
            val updated = item.copy(
                isEquipped = true,
                equippedSlot = item.slotType.name
            )
            itemRepository.updateItem(updated)
        }
    }

    fun unequipItem(item: Item) {
        viewModelScope.launch {
            val updated = item.copy(
                isEquipped = false,
                equippedSlot = null
            )
            itemRepository.updateItem(updated)
        }
    }

    fun savePreset(name: String) {
        viewModelScope.launch {
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
        }
    }

    fun applyPreset(preset: OutfitPreset) {
        viewModelScope.launch {
            // Unequip all current items first
            _uiState.value.equippedItems.forEach { item ->
                val updated = item.copy(isEquipped = false, equippedSlot = null)
                itemRepository.updateItem(updated)
            }

            // Find and equip preset items from unlocked items
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
        }
    }

    fun deletePreset(presetId: Long) {
        viewModelScope.launch {
            val updated = _uiState.value.outfitPresets.filter { it.id != presetId }
            settingsPrefs.saveOutfitPresets(updated)
        }
    }
}
