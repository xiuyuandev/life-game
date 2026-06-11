package com.lifeup.app.domain.repository

import com.lifeup.app.domain.model.CharacterState
import kotlinx.coroutines.flow.Flow

interface CharacterStateRepository {
    fun getCharacterState(): Flow<CharacterState>
    suspend fun addExp(exp: Long)
    suspend fun addTime(minutes: Int)
    suspend fun updateSkillCount(count: Int)
    suspend fun updateMaxSkillLevel(level: Int)
    suspend fun updateAchievementsCount(count: Int)
    suspend fun initializeIfNeeded()
}
