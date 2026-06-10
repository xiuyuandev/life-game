package com.lifeup.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeup.app.data.db.entity.SkillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills ORDER BY category, id")
    fun getAllSkillsFlow(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills ORDER BY category, id")
    suspend fun getAllSkills(): List<SkillEntity>

    @Query("SELECT * FROM skills WHERE id = :id")
    suspend fun getSkillById(id: Long): SkillEntity?

    @Query("SELECT * FROM skills WHERE id = :id")
    fun getSkillByIdFlow(id: Long): Flow<SkillEntity?>

    @Query("SELECT * FROM skills WHERE parentSkillId = :parentId")
    suspend fun getChildSkills(parentId: Long): List<SkillEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(skill: SkillEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(skills: List<SkillEntity>)

    @Update
    suspend fun update(skill: SkillEntity)

    @Query("UPDATE skills SET exp = exp + :exp, totalMinutesInvested = totalMinutesInvested + :minutes WHERE id = :id")
    suspend fun addExpAndTime(id: Long, exp: Long, minutes: Long)

    @Query("UPDATE skills SET level = :level, exp = :exp, expToNext = :expToNext WHERE id = :id")
    suspend fun updateLevel(id: Long, level: Int, exp: Long, expToNext: Long)

    @Query("UPDATE skills SET unlocked = 1 WHERE id = :id")
    suspend fun unlockSkill(id: Long)

    @Query("DELETE FROM skills WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM skills")
    suspend fun deleteAll()
}
