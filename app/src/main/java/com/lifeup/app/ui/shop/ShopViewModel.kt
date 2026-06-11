package com.lifeup.app.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeup.app.data.db.ItemTier
import com.lifeup.app.data.db.SlotType
import com.lifeup.app.domain.model.Item
import com.lifeup.app.domain.repository.GoldRepository
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

data class ShopUiState(
    val goldBalance: Int = 1000,
    val shopItems: List<ShopItem> = emptyList(),
    val ownedItems: List<Item> = emptyList(),
    val skills: List<com.lifeup.app.domain.model.Skill> = emptyList(),
    val isLoading: Boolean = true,
    val showPurchaseDialog: Boolean = false,
    val pendingPurchase: ItemTemplate? = null,
    val purchaseSuccess: Boolean? = null
)

data class ShopItem(
    val template: ItemTemplate,
    val isOwned: Boolean = false,
    val canAfford: Boolean = false
)

data class ItemTemplate(
    val name: String,
    val itemTier: ItemTier,
    val slotType: SlotType,
    val attributeBonus: Int,
    val expBonusContribution: Float,
    val price: Int,
    val description: String
)

val ITEM_TEMPLATES = listOf(
    // COMMON tier (50-200G)
    ItemTemplate("学徒头巾", ItemTier.COMMON, SlotType.HEAD, 1, 0.02f, 100, "初学者的简易头饰，提供微弱属性加成"),
    ItemTemplate("练习手套", ItemTier.COMMON, SlotType.HANDS, 1, 0.02f, 80, "适合日常练习的防护手套"),
    ItemTemplate("布衣", ItemTier.COMMON, SlotType.BODY, 1, 0.02f, 120, "朴素的布制衣物，轻便舒适"),
    ItemTemplate("草鞋", ItemTier.COMMON, SlotType.FEET, 1, 0.02f, 60, "简单编织的草鞋，行走自如"),
    ItemTemplate("铜戒指", ItemTier.COMMON, SlotType.ACCESSORY, 1, 0.02f, 150, "普通铜质戒指，略带属性"),

    // FINE tier (200-500G)
    ItemTemplate("精进之靴", ItemTier.FINE, SlotType.FEET, 3, 0.05f, 300, "精心打造的靴子，助力前行"),
    ItemTemplate("专注头带", ItemTier.FINE, SlotType.HEAD, 3, 0.05f, 350, "帮助集中注意力的头带"),
    ItemTemplate("灵巧护腕", ItemTier.FINE, SlotType.HANDS, 3, 0.05f, 280, "增强手部灵活性的护腕"),
    ItemTemplate("学者长袍", ItemTier.FINE, SlotType.BODY, 3, 0.05f, 400, "适合学习的长袍，提升悟性"),
    ItemTemplate("翡翠吊坠", ItemTier.FINE, SlotType.ACCESSORY, 3, 0.05f, 450, "翡翠制成的吊坠，蕴含灵气"),

    // RARE tier (500-1500G)
    ItemTemplate("大师法袍", ItemTier.RARE, SlotType.BODY, 6, 0.08f, 800, "大师级法袍，蕴含深厚智慧"),
    ItemTemplate("洞察之眼", ItemTier.RARE, SlotType.ACCESSORY, 6, 0.08f, 1000, "提升洞察力的神秘饰品"),
    ItemTemplate("迅捷之靴", ItemTier.RARE, SlotType.FEET, 6, 0.08f, 700, "赋予超凡速度的靴子"),
    ItemTemplate("智慧之冠", ItemTier.RARE, SlotType.HEAD, 6, 0.08f, 900, "凝聚智慧的王冠"),
    ItemTemplate("力量护手", ItemTier.RARE, SlotType.HANDS, 6, 0.08f, 750, "增强力量的护手"),

    // EPIC tier (1500-3000G)
    ItemTemplate("英雄之冠", ItemTier.EPIC, SlotType.HEAD, 10, 0.12f, 2000, "英雄佩戴的王冠，光芒四射"),
    ItemTemplate("不朽护手", ItemTier.EPIC, SlotType.HANDS, 10, 0.12f, 2500, "传说中不朽的护手"),
    ItemTemplate("圣光铠甲", ItemTier.EPIC, SlotType.BODY, 10, 0.12f, 2200, "散发圣光的铠甲"),
    ItemTemplate("疾风之靴", ItemTier.EPIC, SlotType.FEET, 10, 0.12f, 1800, "如疾风般迅捷的靴子"),
    ItemTemplate("命运之环", ItemTier.EPIC, SlotType.ACCESSORY, 10, 0.12f, 2800, "改变命运的神秘指环"),

    // LEGENDARY tier (3000-5000G)
    ItemTemplate("传说圣铠", ItemTier.LEGENDARY, SlotType.BODY, 15, 0.18f, 4000, "传说中的圣铠，无坚不摧"),
    ItemTemplate("命运之靴", ItemTier.LEGENDARY, SlotType.FEET, 15, 0.18f, 4500, "掌控命运的靴子"),
    ItemTemplate("天命之冠", ItemTier.LEGENDARY, SlotType.HEAD, 15, 0.18f, 4200, "承载天命的王冠"),
    ItemTemplate("创世护手", ItemTier.LEGENDARY, SlotType.HANDS, 15, 0.18f, 4800, "拥有创世之力的护手"),
    ItemTemplate("永恒之心", ItemTier.LEGENDARY, SlotType.ACCESSORY, 15, 0.18f, 5000, "永恒不灭的心之饰品")
)

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val skillRepository: SkillRepository,
    private val goldRepository: GoldRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShopUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    init {
        loadShopItems()
    }

    private fun loadShopItems() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                itemRepository.getUnlockedItems(),
                skillRepository.getActiveSkills(),
                goldRepository.getGoldBalance()
            ) { ownedItems, skills, goldBalance ->
                Triple(ownedItems, skills, goldBalance)
            }.collect { (ownedItems, skills, goldBalance) ->
                val ownedNames = ownedItems.map { it.name }.toSet()

                val shopItems = ITEM_TEMPLATES.map { template ->
                    ShopItem(
                        template = template,
                        isOwned = template.name in ownedNames,
                        canAfford = template.price <= goldBalance
                    )
                }

                _uiState.update {
                    it.copy(
                        goldBalance = goldBalance,
                        ownedItems = ownedItems,
                        skills = skills,
                        shopItems = shopItems,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun showPurchaseDialog(template: ItemTemplate) {
        _uiState.update {
            it.copy(
                showPurchaseDialog = true,
                pendingPurchase = template,
                purchaseSuccess = null
            )
        }
    }

    fun dismissPurchaseDialog() {
        _uiState.update {
            it.copy(
                showPurchaseDialog = false,
                pendingPurchase = null,
                purchaseSuccess = null
            )
        }
    }

    fun purchaseItem(template: ItemTemplate) {
        viewModelScope.launch {
            val currentBalance = _uiState.value.goldBalance
            if (template.price > currentBalance) {
                _uiState.update { it.copy(purchaseSuccess = false) }
                return@launch
            }

            val ownedNames = _uiState.value.ownedItems.map { it.name }.toSet()
            if (template.name in ownedNames) {
                _uiState.update { it.copy(purchaseSuccess = false) }
                return@launch
            }

            val item = Item(
                name = template.name,
                skillId = 0L,
                itemTier = template.itemTier,
                attributeBonus = template.attributeBonus,
                expBonusContribution = template.expBonusContribution,
                description = template.description,
                slotType = template.slotType,
                isEquipped = false,
                equippedSlot = null,
                isUnlocked = true,
                price = template.price
            )

            itemRepository.insertItem(item)

            _uiState.update {
                it.copy(
                    goldBalance = currentBalance - template.price,
                    purchaseSuccess = true
                )
            }
        }
    }
}
