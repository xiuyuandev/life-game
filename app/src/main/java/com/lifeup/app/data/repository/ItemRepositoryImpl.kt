package com.lifeup.app.data.repository

import com.lifeup.app.data.db.ItemTier
import com.lifeup.app.data.db.SlotType
import com.lifeup.app.data.db.dao.ItemDao
import com.lifeup.app.data.db.entity.ItemEntity
import com.lifeup.app.domain.model.Item
import com.lifeup.app.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemRepositoryImpl @Inject constructor(
    private val itemDao: ItemDao
) : ItemRepository {

    override fun getEquippedItems(): Flow<List<Item>> {
        return itemDao.getEquipped().map { list -> list.map { it.toDomain() } }
    }

    override fun getUnlockedItems(): Flow<List<Item>> {
        return itemDao.getUnlocked().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertItem(item: Item): Long {
        return itemDao.insert(item.toEntity())
    }

    override suspend fun updateItem(item: Item) {
        itemDao.update(item.toEntity())
    }
}

fun ItemEntity.toDomain(): Item {
    return Item(
        id = id,
        name = name,
        skillId = skillId,
        itemTier = ItemTier.valueOf(itemTier),
        attributeBonus = attributeBonus,
        expBonusContribution = expBonusContribution,
        description = description,
        slotType = SlotType.valueOf(slotType),
        isEquipped = isEquipped,
        equippedSlot = equippedSlot?.let { runCatching { SlotType.valueOf(it) }.getOrNull() },
        isUnlocked = isUnlocked,
        price = price,
        customIconKey = customIconKey,
        createdAt = createdAt
    )
}

fun Item.toEntity(): ItemEntity {
    return ItemEntity(
        id = id,
        name = name,
        skillId = skillId,
        itemTier = itemTier.name,
        attributeBonus = attributeBonus,
        expBonusContribution = expBonusContribution,
        description = description,
        slotType = slotType.name,
        isEquipped = isEquipped,
        equippedSlot = equippedSlot?.name,
        isUnlocked = isUnlocked,
        price = price,
        customIconKey = customIconKey,
        createdAt = createdAt
    )
}
