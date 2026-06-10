package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lifeup.app.data.db.entity.TimeAssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeAssetDao {
    @Query("SELECT * FROM time_assets WHERE date = :date")
    suspend fun getByDate(date: String): TimeAssetEntity?

    @Query("SELECT * FROM time_assets WHERE date = :date")
    fun getByDateFlow(date: String): Flow<TimeAssetEntity?>

    @Query("SELECT * FROM time_assets WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    suspend fun getByDateRange(startDate: String, endDate: String): List<TimeAssetEntity>

    @Query("SELECT * FROM time_assets WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    fun getByDateRangeFlow(startDate: String, endDate: String): Flow<List<TimeAssetEntity>>

    @Query("SELECT * FROM time_assets ORDER BY date DESC LIMIT 30")
    suspend fun getRecentAssets(): List<TimeAssetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(asset: TimeAssetEntity): Long

    @Query("UPDATE time_assets SET totalMinutes = :total, investedMinutes = :invested, consumedMinutes = :consumed, investmentRatio = :ratio, topSkillId = :topSkillId, topSkillMinutes = :topSkillMinutes, sessionsCount = :sessionsCount, expEarned = :expEarned, goldEarned = :goldEarned WHERE date = :date")
    suspend fun updateDailyStats(
        date: String,
        total: Long,
        invested: Long,
        consumed: Long,
        ratio: Double,
        topSkillId: Long?,
        topSkillMinutes: Long,
        sessionsCount: Int,
        expEarned: Long,
        goldEarned: Long
    )

    @Query("DELETE FROM time_assets")
    suspend fun deleteAll()
}
