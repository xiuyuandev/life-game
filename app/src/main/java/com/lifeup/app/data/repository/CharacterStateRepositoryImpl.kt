package com.lifeup.app.data.repository

import com.lifeup.app.data.db.dao.CharacterStateDao
import com.lifeup.app.data.db.entity.CharacterStateEntity
import com.lifeup.app.domain.model.CharacterState
import com.lifeup.app.domain.repository.CharacterStateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterStateRepositoryImpl @Inject constructor(
    private val characterStateDao: CharacterStateDao
) : CharacterStateRepository {

    override fun getCharacterState(): Flow<CharacterState> {
        return characterStateDao.getState().map { entity ->
            entity?.toDomain() ?: CharacterState(
                characterLevel = 1,
                totalExp = 0,
                attributes = emptyMap(),
                equippedItems = emptyList(),
                title = "初学者"
            )
        }
    }

    override suspend fun addExp(exp: Long) {
        ensureInitialized()
        characterStateDao.addExp(exp)
    }

    override suspend fun addTime(minutes: Int) {
        ensureInitialized()
        characterStateDao.addTime(minutes)
    }

    override suspend fun updateSkillCount(count: Int) {
        ensureInitialized()
        characterStateDao.updateSkillCount(count)
    }

    override suspend fun updateMaxSkillLevel(level: Int) {
        ensureInitialized()
        characterStateDao.updateMaxSkillLevel(level)
    }

    override suspend fun updateAchievementsCount(count: Int) {
        ensureInitialized()
        characterStateDao.updateAchievementsCount(count)
    }

    override suspend fun initializeIfNeeded() {
        ensureInitialized()
    }

    private suspend fun ensureInitialized() {
        val existing = characterStateDao.getStateSync()
        if (existing == null) {
            characterStateDao.insert(CharacterStateEntity())
        }
    }

    private fun CharacterStateEntity.toDomain(): CharacterState {
        return CharacterState(
            characterLevel = characterLevel,
            totalExp = totalExp,
            attributes = emptyMap(),
            equippedItems = emptyList(),
            title = title
        )
    }
}
