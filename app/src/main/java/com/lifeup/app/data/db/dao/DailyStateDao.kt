package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.DailyStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStateDao {

    @Query("SELECT * FROM daily_states WHERE date = :date LIMIT 1")
    fun getByDate(date: String): Flow<DailyStateEntity?>

    @Query("SELECT * FROM daily_states WHERE date = :date LIMIT 1")
    suspend fun getByDateSync(date: String): DailyStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dailyState: DailyStateEntity): Long

    @Update
    suspend fun update(dailyState: DailyStateEntity)

    @Query("SELECT streak_count FROM daily_states ORDER BY date DESC LIMIT 1")
    suspend fun getLatestStreak(): Int?

    @Query("SELECT * FROM daily_states")
    suspend fun getAll(): List<DailyStateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<DailyStateEntity>)

    @Query("DELETE FROM daily_states")
    suspend fun deleteAll()
}
