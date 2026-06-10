package com.lifeup.app.data.repository

import com.lifeup.app.data.db.dao.SkillDao
import com.lifeup.app.data.db.entity.SkillEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillRepository @Inject constructor(
    private val skillDao: SkillDao
) {
    fun getAllSkillsFlow(): Flow<List<SkillEntity>> = skillDao.getAllSkillsFlow()

    suspend fun getAllSkills(): List<SkillEntity> = skillDao.getAllSkills()

    suspend fun getSkillById(id: Long): SkillEntity? = skillDao.getSkillById(id)

    fun getSkillByIdFlow(id: Long): Flow<SkillEntity?> = skillDao.getSkillByIdFlow(id)

    suspend fun getChildSkills(parentId: Long): List<SkillEntity> = skillDao.getChildSkills(parentId)

    suspend fun insert(skill: SkillEntity): Long = skillDao.insert(skill)

    suspend fun insertAll(skills: List<SkillEntity>) = skillDao.insertAll(skills)

    suspend fun update(skill: SkillEntity) = skillDao.update(skill)

    suspend fun addExpAndTime(id: Long, exp: Long, minutes: Long) = skillDao.addExpAndTime(id, exp, minutes)

    suspend fun updateLevel(id: Long, level: Int, exp: Long, expToNext: Long) =
        skillDao.updateLevel(id, level, exp, expToNext)

    suspend fun unlockSkill(id: Long) = skillDao.unlockSkill(id)

    suspend fun deleteById(id: Long) = skillDao.deleteById(id)

    suspend fun deleteAll() = skillDao.deleteAll()

    fun calculateExpToNext(level: Int): Long = (60 * level * 1.3).toLong()

    fun calculateMaxExpToNext(): Long = (60 * 20 * 1.3).toLong()
}
