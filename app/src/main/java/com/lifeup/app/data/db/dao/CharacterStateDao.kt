package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.CharacterStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterStateDao {

    @Query("SELECT * FROM character_state WHERE id = 1 LIMIT 1")
    fun getState(): Flow<CharacterStateEntity?>

    @Query("SELECT * FROM character_state WHERE id = 1 LIMIT 1")
    suspend fun getStateSync(): CharacterStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: CharacterStateEntity)

    @Update
    suspend fun update(state: CharacterStateEntity)

    @Query("UPDATE character_state SET totalExp = totalExp + :exp, lastUpdated = :timestamp WHERE id = 1")
    suspend fun addExp(exp: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE character_state SET totalTimeMinutes = totalTimeMinutes + :minutes, lastUpdated = :timestamp WHERE id = 1")
    suspend fun addTime(minutes: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE character_state SET skillCount = :count, lastUpdated = :timestamp WHERE id = 1")
    suspend fun updateSkillCount(count: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE character_state SET maxSkillLevel = :level, lastUpdated = :timestamp WHERE id = 1")
    suspend fun updateMaxSkillLevel(level: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE character_state SET achievementsUnlocked = :count, lastUpdated = :timestamp WHERE id = 1")
    suspend fun updateAchievementsCount(count: Int, timestamp: Long = System.currentTimeMillis())
}
