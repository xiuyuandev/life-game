package com.lifeup.app.data.repository

import com.lifeup.app.data.db.dao.TimeAssetDao
import com.lifeup.app.data.db.entity.TimeAssetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeAssetRepository @Inject constructor(
    private val timeAssetDao: TimeAssetDao
) {
    suspend fun getByDate(date: String): TimeAssetEntity? = timeAssetDao.getByDate(date)

    fun getByDateFlow(date: String): Flow<TimeAssetEntity?> = timeAssetDao.getByDateFlow(date)

    suspend fun getByDateRange(startDate: String, endDate: String): List<TimeAssetEntity> =
        timeAssetDao.getByDateRange(startDate, endDate)

    fun getByDateRangeFlow(startDate: String, endDate: String): Flow<List<TimeAssetEntity>> =
        timeAssetDao.getByDateRangeFlow(startDate, endDate)

    suspend fun getRecentAssets(): List<TimeAssetEntity> = timeAssetDao.getRecentAssets()

    suspend fun insert(asset: TimeAssetEntity): Long = timeAssetDao.insert(asset)

    suspend fun updateDailyStats(
        date: String,
        totalMinutes: Long,
        investedMinutes: Long,
        consumedMinutes: Long,
        investmentRatio: Double,
        topSkillId: Long?,
        topSkillMinutes: Long,
        sessionsCount: Int,
        expEarned: Long,
        goldEarned: Long
    ) = timeAssetDao.updateDailyStats(
        date, totalMinutes, investedMinutes, consumedMinutes, investmentRatio,
        topSkillId, topSkillMinutes, sessionsCount, expEarned, goldEarned
    )

    suspend fun deleteAll() = timeAssetDao.deleteAll()
}
