package com.lifeup.app.domain.game

import com.lifeup.app.data.db.entity.AchievementEntity
import com.lifeup.app.data.repository.AchievementRepository
import com.lifeup.app.data.repository.CharacterRepository
import com.lifeup.app.data.repository.EquipmentRepository
import com.lifeup.app.data.repository.SkillRepository
import com.lifeup.app.data.repository.TimeAssetRepository
import com.lifeup.app.data.repository.TimeSessionRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementChecker @Inject constructor(
    private val achievementRepository: AchievementRepository,
    private val characterRepository: CharacterRepository,
    private val skillRepository: SkillRepository,
    private val timeSessionRepository: TimeSessionRepository,
    private val timeAssetRepository: TimeAssetRepository,
    private val equipmentRepository: EquipmentRepository
) {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    suspend fun checkAll(): List<AchievementEntity> {
        val locked = achievementRepository.getLockedAchievements()
        val unlocked = mutableListOf<AchievementEntity>()

        for (achievement in locked) {
            if (checkCondition(achievement)) {
                achievementRepository.unlock(achievement.id)
                unlocked.add(achievement)
            }
        }

        return unlocked
    }

    private suspend fun checkCondition(achievement: AchievementEntity): Boolean {
        return when (achievement.conditionType) {
            "TOTAL_INVESTED_HOURS" -> {
                val sessions = timeSessionRepository.getRecentSessions(10000)
                val totalHours = sessions.filter { it.isInvestment }.sumOf { it.durationMinutes } / 60
                totalHours >= achievement.conditionValue
            }
            "DAILY_INVESTED_HOURS" -> {
                val today = LocalDate.now().format(dateFormatter)
                val sessions = timeSessionRepository.getSessionsByDateSync(today)
                val hours = sessions.filter { it.isInvestment }.sumOf { it.durationMinutes } / 60
                hours >= achievement.conditionValue
            }
            "INVESTMENT_RATIO" -> {
                val assets = timeAssetRepository.getRecentAssets()
                val targetDays = achievement.conditionValue
                val count = assets.count { it.investmentRatio > 0.7 }
                count >= targetDays
            }
            "ANY_SKILL_LEVEL" -> {
                val skills = skillRepository.getAllSkills()
                skills.any { it.level >= achievement.conditionValue.toInt() }
            }
            "SKILL_COUNT_AT_LEVEL" -> {
                val skills = skillRepository.getAllSkills()
                skills.count { it.level >= 3 } >= achievement.conditionValue.toInt()
            }
            "TOTAL_SKILL_MINUTES" -> {
                val skills = skillRepository.getAllSkills()
                skills.sumOf { it.totalMinutesInvested } >= achievement.conditionValue
            }
            "STREAK_DAYS" -> {
                val character = characterRepository.getCharacter()
                (character?.streakDays ?: 0) >= achievement.conditionValue.toInt()
            }
            "EQUIPMENT_OWNED" -> {
                val ownedCount = equipmentRepository.getOwnedEquipment().size
                ownedCount >= achievement.conditionValue.toInt()
            }
            "EQUIPMENT_ACTIVE" -> {
                val activeCount = equipmentRepository.getActiveEquipment().size
                activeCount >= achievement.conditionValue.toInt()
            }
            "CHARACTER_LEVEL" -> {
                val character = characterRepository.getCharacter()
                (character?.level ?: 0) >= achievement.conditionValue.toInt()
            }
            "TOTAL_EXP" -> {
                val character = characterRepository.getCharacter()
                (character?.exp ?: 0) >= achievement.conditionValue
            }
            "TOTAL_GOLD" -> {
                val character = characterRepository.getCharacter()
                (character?.gold ?: 0) >= achievement.conditionValue
            }
            else -> false
        }
    }
}
