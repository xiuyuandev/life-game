package com.lifeup.app.data.repository

import com.lifeup.app.data.db.dao.CharacterDao
import com.lifeup.app.data.db.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepository @Inject constructor(
    private val characterDao: CharacterDao
) {
    fun getCharacterFlow(): Flow<CharacterEntity?> = characterDao.getCharacterFlow()

    suspend fun getCharacter(): CharacterEntity? = characterDao.getCharacter()

    suspend fun createCharacter(name: String): Long {
        val character = CharacterEntity(name = name)
        return characterDao.insert(character)
    }

    suspend fun addExpAndGold(exp: Long, gold: Long) {
        val character = getCharacter() ?: return
        characterDao.addExpAndGold(character.id, exp, gold)
    }

    suspend fun levelUp(newLevel: Int, newExp: Long, newExpToNext: Long, newMaxHp: Int, newMaxSp: Int, rewardGold: Long) {
        val character = getCharacter() ?: return
        characterDao.updateLevelUp(
            id = character.id,
            exp = newExp,
            level = newLevel,
            expToNext = newExpToNext,
            maxHp = newMaxHp,
            maxSp = newMaxSp,
            gold = rewardGold
        )
    }

    suspend fun updateName(name: String) {
        val character = getCharacter() ?: return
        characterDao.updateName(character.id, name)
    }

    suspend fun updateAttributes(strength: Int, intelligence: Int, charm: Int, constitution: Int, agility: Int, luck: Int) {
        val character = getCharacter() ?: return
        characterDao.updateAttributes(character.id, strength, intelligence, charm, constitution, agility, luck)
    }

    suspend fun updateStreak(streakDays: Int, lastActiveDate: String) {
        val character = getCharacter() ?: return
        characterDao.updateStreak(character.id, streakDays, lastActiveDate)
    }

    suspend fun updateHpSp(hp: Int, sp: Int) {
        val character = getCharacter() ?: return
        characterDao.updateHpSp(character.id, hp, sp)
    }

    suspend fun resetCharacter() {
        characterDao.deleteAll()
    }
}
