package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.ComboEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComboDao {

    @Query("SELECT * FROM combos")
    fun getAll(): Flow<List<ComboEntity>>

    @Query("SELECT * FROM combos WHERE is_active = 1")
    fun getActive(): Flow<List<ComboEntity>>

    @Query("SELECT * FROM combos WHERE primary_skill_id = :skillId OR secondary_skill_id = :skillId")
    fun getBySkillId(skillId: Long): Flow<List<ComboEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(combo: ComboEntity): Long

    @Update
    suspend fun update(combo: ComboEntity)

    @Delete
    suspend fun delete(combo: ComboEntity)
}
