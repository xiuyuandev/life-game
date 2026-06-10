package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.CharacterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters LIMIT 1")
    fun getCharacterFlow(): Flow<CharacterEntity?>

    @Query("SELECT * FROM characters LIMIT 1")
    suspend fun getCharacter(): CharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(character: CharacterEntity): Long

    @Update
    suspend fun update(character: CharacterEntity)

    @Query("UPDATE characters SET exp = exp + :exp, level = :level, expToNext = :expToNext, maxHp = :maxHp, maxSp = :maxSp, gold = gold + :gold WHERE id = :id")
    suspend fun updateLevelUp(id: Long, exp: Long, level: Int, expToNext: Long, maxHp: Int, maxSp: Int, gold: Long)

    @Query("UPDATE characters SET exp = exp + :exp, gold = gold + :gold WHERE id = :id")
    suspend fun addExpAndGold(id: Long, exp: Long, gold: Long)

    @Query("UPDATE characters SET name = :name WHERE id = :id")
    suspend fun updateName(id: Long, name: String)

    @Query("UPDATE characters SET strength = :strength, intelligence = :intelligence, charm = :charm, constitution = :constitution, agility = :agility, luck = :luck WHERE id = :id")
    suspend fun updateAttributes(id: Long, strength: Int, intelligence: Int, charm: Int, constitution: Int, agility: Int, luck: Int)

    @Query("UPDATE characters SET streakDays = :streakDays, lastActiveDate = :lastActiveDate WHERE id = :id")
    suspend fun updateStreak(id: Long, streakDays: Int, lastActiveDate: String)

    @Query("UPDATE characters SET hp = :hp, sp = :sp WHERE id = :id")
    suspend fun updateHpSp(id: Long, hp: Int, sp: Int)

    @Query("DELETE FROM characters")
    suspend fun deleteAll()
}
