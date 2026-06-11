package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.SkillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {

    @Query("SELECT * FROM skills WHERE status = 'ACTIVE' ORDER BY sort_order ASC")
    fun getAllActive(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE status = :status ORDER BY sort_order ASC")
    fun getAllByStatus(status: String): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE id = :id")
    suspend fun getById(id: Long): SkillEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(skill: SkillEntity): Long

    @Update
    suspend fun update(skill: SkillEntity)

    @Delete
    suspend fun delete(skill: SkillEntity)

    @Query("SELECT * FROM skills WHERE category = :category AND status = 'ACTIVE' ORDER BY sort_order ASC")
    fun getByCategory(category: String): Flow<List<SkillEntity>>

    @Query("SELECT COUNT(*) FROM skills WHERE status = 'ACTIVE'")
    suspend fun getActiveCount(): Int

    @Query("SELECT * FROM skills")
    suspend fun getAll(): List<SkillEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SkillEntity>)

    @Query("DELETE FROM skills")
    suspend fun deleteAll()
}
