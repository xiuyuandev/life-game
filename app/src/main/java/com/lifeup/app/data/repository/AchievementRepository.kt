package com.lifeup.app.data.repository

import com.lifeup.app.data.db.dao.AchievementDao
import com.lifeup.app.data.db.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val achievementDao: AchievementDao
) {
    fun getAllAchievementsFlow(): Flow<List<AchievementEntity>> = achievementDao.getAllAchievementsFlow()

    suspend fun getAllAchievements(): List<AchievementEntity> = achievementDao.getAllAchievements()

    suspend fun getLockedAchievements(): List<AchievementEntity> = achievementDao.getLockedAchievements()

    suspend fun getUnlockedAchievements(): List<AchievementEntity> = achievementDao.getUnlockedAchievements()

    suspend fun getUnlockedCount(): Int = achievementDao.getUnlockedCount()

    suspend fun insert(achievement: AchievementEntity): Long = achievementDao.insert(achievement)

    suspend fun insertAll(achievements: List<AchievementEntity>) = achievementDao.insertAll(achievements)

    suspend fun unlock(id: Long) = achievementDao.unlock(id)

    suspend fun update(achievement: AchievementEntity) = achievementDao.update(achievement)

    suspend fun deleteAll() = achievementDao.deleteAll()
}
