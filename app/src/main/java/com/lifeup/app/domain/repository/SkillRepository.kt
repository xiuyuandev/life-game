package com.lifeup.app.domain.repository

import com.lifeup.app.data.db.SkillStatus
import com.lifeup.app.domain.model.Skill
import kotlinx.coroutines.flow.Flow

interface SkillRepository {

    fun getActiveSkills(): Flow<List<Skill>>

    fun getSkillsByStatus(status: SkillStatus): Flow<List<Skill>>

    suspend fun getSkillById(id: Long): Skill?

    suspend fun insertSkill(skill: Skill): Long

    suspend fun updateSkill(skill: Skill)

    suspend fun deleteSkill(skill: Skill)

    suspend fun getActiveSkillCount(): Int

    suspend fun getEarliestCreatedAt(): Long?
}
