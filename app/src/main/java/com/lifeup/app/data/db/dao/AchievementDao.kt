package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY category, id")
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements ORDER BY category, id")
    suspend fun getAllAchievements(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE unlocked = 0")
    suspend fun getLockedAchievements(): List<AchievementEntity>

    @Query("SELECT * FROM achievements WHERE unlocked = 1")
    suspend fun getUnlockedAchievements(): List<AchievementEntity>

    @Query("SELECT COUNT(*) FROM achievements WHERE unlocked = 1")
    suspend fun getUnlockedCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(achievement: AchievementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET unlocked = 1, unlockedAt = :timestamp WHERE id = :id")
    suspend fun unlock(id: Long, timestamp: Long = System.currentTimeMillis())

    @Update
    suspend fun update(achievement: AchievementEntity)

    @Query("DELETE FROM achievements")
    suspend fun deleteAll()
}
