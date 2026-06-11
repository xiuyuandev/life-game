package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items")
    fun getAll(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE is_equipped = 1")
    fun getEquipped(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE skill_id = :skillId")
    fun getBySkillId(skillId: Long): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE is_unlocked = 1")
    fun getUnlocked(): Flow<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity)

    @Query("SELECT * FROM items WHERE slot_type = :slotType")
    fun getBySlot(slotType: String): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items")
    suspend fun getAllList(): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<ItemEntity>)

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
