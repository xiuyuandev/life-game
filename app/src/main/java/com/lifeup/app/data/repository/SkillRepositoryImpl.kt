package com.lifeup.app.data.repository

import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.data.db.SkillStatus
import com.lifeup.app.data.db.dao.SkillDao
import com.lifeup.app.data.db.entity.SkillEntity
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.repository.SkillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillRepositoryImpl @Inject constructor(
    private val skillDao: SkillDao
) : SkillRepository {

    override fun getActiveSkills(): Flow<List<Skill>> {
        return skillDao.getAllActive().map { list -> list.map { it.toDomain() } }
    }

    override fun getSkillsByStatus(status: SkillStatus): Flow<List<Skill>> {
        return skillDao.getAllByStatus(status.name).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getSkillById(id: Long): Skill? {
        return skillDao.getById(id)?.toDomain()
    }

    override suspend fun insertSkill(skill: Skill): Long {
        return skillDao.insert(skill.toEntity())
    }

    override suspend fun updateSkill(skill: Skill) {
        skillDao.update(skill.toEntity())
    }

    override suspend fun deleteSkill(skill: Skill) {
        skillDao.delete(skill.toEntity())
    }

    override suspend fun getActiveSkillCount(): Int {
        return skillDao.getActiveCount()
    }
}

fun SkillEntity.toDomain(): Skill {
    return Skill(
        id = id,
        name = name,
        category = SkillCategory.valueOf(category),
        boundAttribute = com.lifeup.app.data.db.BoundAttribute.valueOf(boundAttribute),
        totalMinutes = totalMinutes,
        level = level,
        masteryStars = masteryStars,
        customThresholds = parseThresholds(customThresholds),
        iconKey = iconKey,
        color = color,
        status = SkillStatus.valueOf(status),
        createdAt = createdAt,
        updatedAt = updatedAt,
        sortOrder = sortOrder,
        displayInShowcase = displayInShowcase
    )
}

fun Skill.toEntity(): SkillEntity {
    return SkillEntity(
        id = id,
        name = name,
        category = category.name,
        boundAttribute = boundAttribute.name,
        totalMinutes = totalMinutes,
        level = level,
        masteryStars = masteryStars,
        customThresholds = serializeThresholds(customThresholds),
        iconKey = iconKey,
        color = color,
        status = status.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        sortOrder = sortOrder,
        displayInShowcase = displayInShowcase
    )
}

private fun parseThresholds(json: String): Map<Int, Int> {
    if (json.isEmpty() || json == "{}") return emptyMap()
    val result = mutableMapOf<Int, Int>()
    try {
        val obj = JSONObject(json)
        val keys = obj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            result[key.toInt()] = obj.getInt(key)
        }
    } catch (_: Exception) {
        return emptyMap()
    }
    return result
}

private fun serializeThresholds(map: Map<Int, Int>): String {
    if (map.isEmpty()) return "{}"
    val json = JSONObject()
    for ((key, value) in map) {
        json.put(key.toString(), value)
    }
    return json.toString()
}
