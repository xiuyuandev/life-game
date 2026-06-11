package com.lifeup.app.domain.model

import com.lifeup.app.data.db.ItemTier
import com.lifeup.app.data.db.SlotType

data class Item(
    val id: Long = 0,
    val name: String,
    val skillId: Long? = null,
    val itemTier: ItemTier = ItemTier.COMMON,
    val attributeBonus: Int = 0,
    val expBonusContribution: Float = 0f,
    val description: String? = null,
    val slotType: SlotType,
    val isEquipped: Boolean = false,
    val equippedSlot: SlotType? = null,
    val isUnlocked: Boolean = false,
    val price: Int = 0,
    val customIconKey: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
