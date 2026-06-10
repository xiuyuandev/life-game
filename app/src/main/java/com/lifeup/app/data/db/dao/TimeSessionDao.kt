package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.TimeSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeSessionDao {
    @Query("SELECT * FROM time_sessions WHERE endTime IS NULL LIMIT 1")
    suspend fun getActiveSession(): TimeSessionEntity?

    @Query("SELECT * FROM time_sessions WHERE endTime IS NULL LIMIT 1")
    fun getActiveSessionFlow(): Flow<TimeSessionEntity?>

    @Query("SELECT * FROM time_sessions WHERE date = :date ORDER BY startTime DESC")
    fun getSessionsByDate(date: String): Flow<List<TimeSessionEntity>>

    @Query("SELECT * FROM time_sessions WHERE date = :date ORDER BY startTime DESC")
    suspend fun getSessionsByDateSync(date: String): List<TimeSessionEntity>

    @Query("SELECT * FROM time_sessions WHERE linkedSkillId = :skillId ORDER BY startTime DESC")
    suspend fun getSessionsBySkill(skillId: Long): List<TimeSessionEntity>

    @Query("SELECT * FROM time_sessions ORDER BY startTime DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int): List<TimeSessionEntity>

    @Query("SELECT * FROM time_sessions ORDER BY startTime DESC")
    fun getAllSessionsFlow(): Flow<List<TimeSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: TimeSessionEntity): Long

    @Update
    suspend fun update(session: TimeSessionEntity)

    @Query("SELECT COUNT(*) FROM time_sessions WHERE date = :date")
    suspend fun getSessionCountByDate(date: String): Int

    @Query("DELETE FROM time_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM time_sessions")
    suspend fun deleteAll()
}
