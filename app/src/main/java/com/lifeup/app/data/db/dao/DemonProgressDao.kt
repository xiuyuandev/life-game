package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.DemonProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DemonProgressDao {

    @Query("SELECT * FROM demon_progress")
    fun getAll(): Flow<List<DemonProgressEntity>>

    @Query("SELECT * FROM demon_progress WHERE demonId = :id LIMIT 1")
    fun getById(id: String): Flow<DemonProgressEntity?>

    @Query("SELECT * FROM demon_progress WHERE demonId = :id LIMIT 1")
    suspend fun getByIdOnce(id: String): DemonProgressEntity?

    @Query("SELECT * FROM demon_progress WHERE isActive = 1")
    fun getActive(): Flow<List<DemonProgressEntity>>

    @Query("SELECT * FROM demon_progress WHERE isDefeated = 0")
    fun getUndefeated(): Flow<List<DemonProgressEntity>>

    @Query("SELECT * FROM demon_progress WHERE isDefeated = 1 ORDER BY defeatedAt DESC")
    fun getDefeated(): Flow<List<DemonProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: DemonProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(progress: List<DemonProgressEntity>)

    @Update
    suspend fun update(progress: DemonProgressEntity)

    @Query("UPDATE demon_progress SET currentHp = MAX(0, currentHp - :damage), isDefeated = CASE WHEN currentHp - :damage <= 0 THEN 1 ELSE 0 END, defeatedAt = CASE WHEN currentHp - :damage <= 0 AND defeatedAt IS NULL THEN :timestamp ELSE defeatedAt END, lastUpdated = :timestamp WHERE demonId = :id")
    suspend fun applyDamage(id: String, damage: Int, timestamp: Long = System.currentTimeMillis()): Int

    @Query("UPDATE demon_progress SET isActive = :active WHERE demonId = :id")
    suspend fun setActive(id: String, active: Boolean)

    @Query("UPDATE demon_progress SET progressFraction = :fraction, lastUpdated = :timestamp WHERE demonId = :id")
    suspend fun updateProgressFraction(id: String, fraction: Float, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM demon_progress WHERE isDefeated = 1")
    suspend fun getDefeatedCount(): Int

    @Query("DELETE FROM demon_progress")
    suspend fun deleteAll()
}
