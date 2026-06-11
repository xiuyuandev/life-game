package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.TimeRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeRecordDao {

    @Query("SELECT * FROM time_records WHERE skill_id = :skillId ORDER BY start_time DESC")
    fun getBySkillId(skillId: Long): Flow<List<TimeRecordEntity>>

    @Query("SELECT * FROM time_records WHERE start_time >= :startMs AND end_time <= :endMs ORDER BY start_time DESC")
    fun getByDateRange(startMs: Long, endMs: Long): Flow<List<TimeRecordEntity>>

    @Query("SELECT * FROM time_records WHERE skill_id = :skillId AND start_time >= :startMs AND end_time <= :endMs ORDER BY start_time DESC")
    fun getBySkillAndDateRange(skillId: Long, startMs: Long, endMs: Long): Flow<List<TimeRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(timeRecord: TimeRecordEntity): Long

    @Update
    suspend fun update(timeRecord: TimeRecordEntity)

    @Query("SELECT COALESCE(SUM(duration_minutes), 0) FROM time_records WHERE date(start_time / 1000, 'unixepoch') = :date AND record_type = 'INVESTMENT'")
    suspend fun getInvestmentMinutesByDate(date: String): Int

    @Query("SELECT * FROM time_records WHERE skill_id = :skillId ORDER BY created_at DESC LIMIT :limit")
    fun getRecentBySkill(skillId: Long, limit: Int = 10): Flow<List<TimeRecordEntity>>

    @Query("SELECT * FROM time_records")
    suspend fun getAll(): List<TimeRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<TimeRecordEntity>)

    @Query("DELETE FROM time_records")
    suspend fun deleteAll()
}
