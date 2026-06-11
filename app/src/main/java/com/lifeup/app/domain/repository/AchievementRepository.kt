package com.lifeup.app.domain.repository

import com.lifeup.app.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

interface AchievementRepository {
    fun getAllAchievements(): Flow<List<Achievement>>
    fun getUnlockedAchievements(): Flow<List<Achievement>>
    suspend fun unlockAchievement(id: String)
    suspend fun updateProgress(id: String, progress: Int)
    suspend fun initializeAchievements()
    suspend fun getUnlockedCount(): Int
}
