package com.lifeup.app.domain.game

import com.lifeup.app.data.db.entity.CharacterEntity
import com.lifeup.app.data.db.entity.EquipmentEntity
import com.lifeup.app.data.db.entity.SkillEntity
import com.lifeup.app.data.db.entity.TimeSessionEntity
import com.lifeup.app.data.repository.AchievementRepository
import com.lifeup.app.data.repository.CharacterRepository
import com.lifeup.app.data.repository.EquipmentRepository
import com.lifeup.app.data.repository.SkillRepository
import com.lifeup.app.data.repository.TimeAssetRepository
import com.lifeup.app.data.repository.TimeSessionRepository
import com.lifeup.app.domain.calculator.AttributeCalculator
import com.lifeup.app.domain.calculator.EquipmentEffect
import com.lifeup.app.domain.calculator.ExpBreakdown
import com.lifeup.app.domain.calculator.ExpCalculator
import com.lifeup.app.domain.calculator.GoldCalculator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameEngine @Inject constructor(
    private val expCalculator: ExpCalculator,
    private val goldCalculator: GoldCalculator,
    private val attributeCalculator: AttributeCalculator,
    private val achievementChecker: AchievementChecker,
    private val characterRepository: CharacterRepository,
    private val skillRepository: SkillRepository,
    private val timeSessionRepository: TimeSessionRepository,
    private val timeAssetRepository: TimeAssetRepository,
    private val equipmentRepository: EquipmentRepository,
) {
    private val _events = MutableSharedFlow<GameEvent>()
    val events: SharedFlow<GameEvent> = _events

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    /**
     * 时间会话结算 — 核心方法
     */
    suspend fun settleSession(session: TimeSessionEntity): SessionResult {
        val character = characterRepository.getCharacter()
            ?: throw IllegalStateException("No character found")
        val skills = skillRepository.getAllSkills()
        val activeEquipment = equipmentRepository.getActiveEquipment()
        val today = LocalDate.now().format(dateFormatter)
        val isFirstSession = timeSessionRepository.getSessionCountByDate(today) == 0

        // 1. 查找关联技能
        val linkedSkill = session.linkedSkillId?.let { skillId ->
            skills.find { it.id == skillId }
        }
        val skillLevel = linkedSkill?.level ?: 1

        // 2. 计算经验
        val expBreakdown = expCalculator.calculate(
            durationMinutes = session.durationMinutes,
            isInvestment = session.isInvestment,
            skillLevel = skillLevel,
            category = session.category,
            isFirstSessionToday = isFirstSession,
            streakDays = character.streakDays,
            activeEquipment = activeEquipment
        )

        // 3. 计算金币
        val goldEarned = goldCalculator.calculate(
            durationMinutes = session.durationMinutes,
            isInvestment = session.isInvestment,
            category = session.category,
            activeEquipment = activeEquipment
        )

        // 4. 更新技能经验和等级
        var skillLevelUp = false
        var newSkillLevel = skillLevel
        if (linkedSkill != null && session.isInvestment) {
            val newSkillExp = linkedSkill.exp + expBreakdown.total
            var currentLevel = linkedSkill.level
            var currentExp = newSkillExp
            var expToNext = expCalculator.calculateSkillExpToNext(currentLevel)

            while (currentExp >= expToNext && currentLevel < linkedSkill.maxLevel) {
                currentExp -= expToNext
                currentLevel++
                expToNext = expCalculator.calculateSkillExpToNext(currentLevel)
                skillLevelUp = true
            }

            newSkillLevel = currentLevel
            skillRepository.updateLevel(linkedSkill.id, currentLevel, currentExp, expToNext)
            skillRepository.addExpAndTime(linkedSkill.id, 0, session.durationMinutes)
        }

        // 5. 更新角色经验和等级
        val newCharacterExp = character.exp + expBreakdown.total
        var characterLevelUp = false
        var newCharacterLevel = character.level
        var currentExp = newCharacterExp
        var expToNext = expCalculator.calculateCharacterExpToNext(character.level)

        while (currentExp >= expToNext) {
            currentExp -= expToNext
            newCharacterLevel++
            expToNext = expCalculator.calculateCharacterExpToNext(newCharacterLevel)
            characterLevelUp = true
        }

        if (characterLevelUp) {
            val newMaxHp = character.maxHp + (newCharacterLevel - character.level) * 5
            val newMaxSp = character.maxSp + (newCharacterLevel - character.level)
            val levelUpGold = (character.level + 1..newCharacterLevel).sumOf { it * 10L }
            characterRepository.levelUp(
                newLevel = newCharacterLevel,
                newExp = currentExp,
                newExpToNext = expToNext,
                newMaxHp = newMaxHp,
                newMaxSp = newMaxSp,
                rewardGold = levelUpGold + goldEarned
            )
            _events.emit(GameEvent.CharacterLevelUp(newCharacterLevel))
        } else {
            characterRepository.addExpAndGold(expBreakdown.total, goldEarned)
        }

        // 6. 更新角色六维属性
        val achievements = achievementRepository.getAllAchievements()
        val unlockedCount = achievements.count { it.unlocked }
        val attrs = attributeCalculator.calculateAttributes(skills, character.streakDays, unlockedCount)
        characterRepository.updateAttributes(
            attrs.strength, attrs.intelligence, attrs.charm,
            attrs.constitution, attrs.agility, attrs.luck
        )

        // 7. 维护装备耐久
        val durabilityChanges = mutableListOf<DurabilityChange>()
        for (equip in activeEquipment) {
            if (equip.maintenanceActivity.isNotEmpty() && equip.maintenanceActivity == session.category) {
                // 今日执行了维护活动，耐久不减少
                durabilityChanges.add(DurabilityChange(equip.name, 0, true))
            } else {
                val newDurability = equip.currentDurability - 1
                equipmentRepository.updateDurability(equip.id, newDurability)
                if (newDurability <= 0) {
                    equipmentRepository.unequip(equip.id)
                    _events.emit(GameEvent.EquipmentBroken(equip.name))
                }
                durabilityChanges.add(DurabilityChange(equip.name, -1, false))
            }
        }

        // 8. 更新时间资产
        updateTimeAsset(session, expBreakdown.total, goldEarned)

        // 9. 检查成就
        val unlockedAchievements = achievementChecker.checkAll()
        for (achievement in unlockedAchievements) {
            _events.emit(GameEvent.AchievementUnlocked(achievement.title))
        }

        // 10. 检查子技能解锁
        val unlockedSkills = mutableListOf<SkillEntity>()
        if (linkedSkill != null && skillLevelUp) {
            val childSkills = skillRepository.getChildSkills(linkedSkill.id)
            for (child in childSkills) {
                if (!child.unlocked && newSkillLevel >= child.parentLevelRequired) {
                    skillRepository.unlockSkill(child.id)
                    unlockedSkills.add(child.copy(unlocked = true))
                    _events.emit(GameEvent.SkillUnlocked(child.name))
                }
            }
        }

        return SessionResult(
            baseExp = expBreakdown.base,
            bonusExp = expBreakdown.skillBonus + expBreakdown.equipmentBonus,
            totalExp = expBreakdown.total,
            goldEarned = goldEarned,
            skillExpGained = expBreakdown.total,
            skillLevelUp = skillLevelUp,
            characterLevelUp = characterLevelUp,
            newCharacterLevel = newCharacterLevel,
            unlockedSkills = unlockedSkills,
            unlockedAchievements = unlockedAchievements,
            activeEquipmentEffects = expBreakdown.equipmentDetails.map {
                com.lifeup.app.domain.calculator.EquipmentEffect("", com.lifeup.app.domain.calculator.EffectType.EXP_MULTIPLIER, 0.0, it)
            },
            durabilityChanges = durabilityChanges
        )
    }

    private suspend fun updateTimeAsset(session: TimeSessionEntity, expEarned: Long, goldEarned: Long) {
        val today = session.date
        val existing = timeAssetRepository.getByDate(today)

        // Get today's sessions to compute top skill
        val todaySessions = timeSessionRepository.getSessionsByDateSync(today)
        val skillMinutes = todaySessions
            .filter { it.linkedSkillId != null }
            .groupBy { it.linkedSkillId }
            .mapValues { entry -> entry.value.sumOf { it.durationMinutes } }
        val topSkillEntry = skillMinutes.maxByOrNull { it.value }

        if (existing != null) {
            val totalMinutes = existing.totalMinutes + session.durationMinutes
            val investedMinutes = if (session.isInvestment) {
                existing.investedMinutes + session.durationMinutes
            } else existing.investedMinutes
            val consumedMinutes = if (!session.isInvestment) {
                existing.consumedMinutes + session.durationMinutes
            } else existing.consumedMinutes
            val ratio = if (totalMinutes > 0) investedMinutes.toDouble() / totalMinutes else 0.0

            timeAssetRepository.updateDailyStats(
                date = today,
                totalMinutes = totalMinutes,
                investedMinutes = investedMinutes,
                consumedMinutes = consumedMinutes,
                investmentRatio = ratio,
                topSkillId = topSkillEntry?.key,
                topSkillMinutes = topSkillEntry?.value ?: 0,
                sessionsCount = existing.sessionsCount + 1,
                expEarned = existing.expEarned + expEarned,
                goldEarned = existing.goldEarned + goldEarned
            )
        } else {
            timeAssetRepository.insert(
                TimeAssetEntity(
                    date = today,
                    totalMinutes = session.durationMinutes,
                    investedMinutes = if (session.isInvestment) session.durationMinutes else 0,
                    consumedMinutes = if (!session.isInvestment) session.durationMinutes else 0,
                    investmentRatio = if (session.isInvestment) 1.0 else 0.0,
                    topSkillId = session.linkedSkillId,
                    topSkillMinutes = session.durationMinutes,
                    sessionsCount = 1,
                    expEarned = expEarned,
                    goldEarned = goldEarned
                )
            )
        }
    }
}

data class SessionResult(
    val baseExp: Long,
    val bonusExp: Long,
    val totalExp: Long,
    val goldEarned: Long,
    val skillExpGained: Long,
    val skillLevelUp: Boolean,
    val characterLevelUp: Boolean,
    val newCharacterLevel: Int,
    val unlockedSkills: List<SkillEntity>,
    val unlockedAchievements: List<com.lifeup.app.data.db.entity.AchievementEntity>,
    val activeEquipmentEffects: List<EquipmentEffect>,
    val durabilityChanges: List<DurabilityChange>
)

data class DurabilityChange(
    val equipmentName: String,
    val change: Int,
    val maintained: Boolean
)

sealed class GameEvent {
    data class CharacterLevelUp(val newLevel: Int) : GameEvent()
    data class SkillLevelUp(val skillName: String, val newLevel: Int) : GameEvent()
    data class SkillUnlocked(val skillName: String) : GameEvent()
    data class AchievementUnlocked(val title: String) : GameEvent()
    data class EquipmentBroken(val equipmentName: String) : GameEvent()
}
