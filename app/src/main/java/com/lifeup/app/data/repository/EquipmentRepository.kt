package com.lifeup.app.data.repository

import com.lifeup.app.data.db.dao.EquipmentDao
import com.lifeup.app.data.db.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipmentRepository @Inject constructor(
    private val equipmentDao: EquipmentDao
) {
    fun getAllEquipmentFlow(): Flow<List<EquipmentEntity>> = equipmentDao.getAllEquipmentFlow()

    suspend fun getAllEquipment(): List<EquipmentEntity> = equipmentDao.getAllEquipment()

    suspend fun getActiveEquipment(): List<EquipmentEntity> = equipmentDao.getActiveEquipment()

    suspend fun getOwnedEquipment(): List<EquipmentEntity> = equipmentDao.getOwnedEquipment()

    suspend fun getById(id: Long): EquipmentEntity? = equipmentDao.getById(id)

    suspend fun insert(equipment: EquipmentEntity): Long = equipmentDao.insert(equipment)

    suspend fun insertAll(equipment: List<EquipmentEntity>) = equipmentDao.insertAll(equipment)

    suspend fun update(equipment: EquipmentEntity) = equipmentDao.update(equipment)

    suspend fun setOwned(id: Long) = equipmentDao.setOwned(id)

    suspend fun setActive(id: Long, active: Boolean) = equipmentDao.setActive(id, active)

    suspend fun updateDurability(id: Long, durability: Int) = equipmentDao.updateDurability(id, durability)

    suspend fun purchaseEquipment(id: Long, characterGold: Long): Boolean {
        val equipment = getById(id) ?: return false
        if (equipment.owned || equipment.price > characterGold) return false
        equipmentDao.setOwned(id)
        return true
    }

    suspend fun equip(id: Long): Boolean {
        val equipment = getById(id) ?: return false
        if (!equipment.owned) return false
        // Deactivate any other equipment in the same slot
        val allEquipment = getAllEquipment()
        allEquipment.filter { it.slot == equipment.slot && it.active && it.id != id }
            .forEach { equipmentDao.setActive(it.id, false) }
        equipmentDao.setActive(id, true)
        return true
    }

    suspend fun unequip(id: Long) = equipmentDao.setActive(id, false)

    suspend fun deleteAll() = equipmentDao.deleteAll()
}
