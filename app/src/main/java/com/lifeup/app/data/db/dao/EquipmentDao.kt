package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.EquipmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment ORDER BY slot")
    fun getAllEquipmentFlow(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment ORDER BY slot")
    suspend fun getAllEquipment(): List<EquipmentEntity>

    @Query("SELECT * FROM equipment WHERE active = 1")
    suspend fun getActiveEquipment(): List<EquipmentEntity>

    @Query("SELECT * FROM equipment WHERE owned = 1")
    suspend fun getOwnedEquipment(): List<EquipmentEntity>

    @Query("SELECT * FROM equipment WHERE id = :id")
    suspend fun getById(id: Long): EquipmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipment: EquipmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(equipment: List<EquipmentEntity>)

    @Update
    suspend fun update(equipment: EquipmentEntity)

    @Query("UPDATE equipment SET owned = 1 WHERE id = :id")
    suspend fun setOwned(id: Long)

    @Query("UPDATE equipment SET active = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)

    @Query("UPDATE equipment SET currentDurability = :durability WHERE id = :id")
    suspend fun updateDurability(id: Long, durability: Int)

    @Query("DELETE FROM equipment")
    suspend fun deleteAll()
}
