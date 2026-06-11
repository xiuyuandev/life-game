package com.lifeup.app.domain.game

import com.lifeup.app.data.db.FocusType
import com.lifeup.app.data.db.ItemTier
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.data.db.SlotType
import com.lifeup.app.data.preferences.SettingsPrefs
import com.lifeup.app.domain.calculator.ExpCalculator
import com.lifeup.app.domain.calculator.GoldCalculator
import com.lifeup.app.domain.model.Combo
import com.lifeup.app.domain.model.DailyState
import com.lifeup.app.domain.model.Item
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.model.TimeRecord
import com.lifeup.app.domain.repository.CharacterStateRepository
import com.lifeup.app.domain.repository.ComboRepository
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.ItemRepository
import com.lifeup.app.domain.repository.SkillRepository
import com.lifeup.app.domain.repository.AchievementRepository
import com.lifeup.app.domain.repository.TimeRecordRepository
import com.lifeup.app.domain.repository.TodoRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TimerResult(
    val expGained: Long,
    val goldGained: Int,
    val leveledUp: Boolean,
    val newLevel: Int,
    val itemsUnlocked: List<Item>
)

object GameEngine {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    suspend fun processTimerResult(
        skillId: Long,
        durationMinutes: Int,
        recordType: RecordType,
        focusType: FocusType,
        skillRepository: SkillRepository,
        timeRecordRepository: TimeRecordRepository,
        dailyStateRepository: DailyStateRepository,
        comboRepository: ComboRepository,
        itemRepository: ItemRepository,
        characterStateRepository: CharacterStateRepository,
        achievementRepository: AchievementRepository,
        settingsPrefs: SettingsPrefs,
        customStartTime: Long? = null,
        customEndTime: Long? = null
    ): TimerResult {
        return try {
            // Input validation
            if (durationMinutes <= 0) {
                throw IllegalArgumentException("durationMinutes must be > 0")
            }
            if (skillId <= 0) {
                throw IllegalArgumentException("skillId must be > 0")
            }

            val now = System.currentTimeMillis()
            val skill = skillRepository.getSkillById(skillId)
                ?: throw IllegalArgumentException("Skill not found: $skillId")

            val today = LocalDate.now().format(dateFormatter)
            val dailyState = withTimeout(5000) {
                dailyStateRepository.getStateByDate(today).first()
            } ?: DailyState(date = today)

            // Calculate energy cost and validate (skip for retroactive records to allow backfilling)
            val energyCost = if (customStartTime == null) {
                val cost = durationMinutes.coerceAtMost(20).coerceAtLeast(5)
                if (dailyState.energy < cost) {
                    throw IllegalStateException("能量不足，需要 $cost 能量，当前 ${dailyState.energy.toInt()}")
                }
                cost
            } else {
                0
            }

            // Use SettingsPrefs as single source of truth for first-timer flag
            val isFirstTimerToday = withTimeout(5000) {
                settingsPrefs.isFirstTimerUsedToday().first()
            }

            // a. Create a TimeRecord (use custom times for retroactive records)
            val (recordStartTime, recordEndTime) = if (customStartTime != null && customEndTime != null) {
                customStartTime to customEndTime
            } else {
                (now - durationMinutes * 60_000L) to now
            }
            val record = TimeRecord(
                skillId = skillId,
                startTime = recordStartTime,
                endTime = recordEndTime,
                durationMinutes = durationMinutes,
                recordType = recordType,
                focusType = focusType
            )
            timeRecordRepository.insertRecord(record)

            // b. Update skill totalMinutes
            val updatedSkill = skill.copy(
                totalMinutes = skill.totalMinutes + durationMinutes,
                updatedAt = now
            )

            // c. Check for level up
            val leveledSkill = checkLevelUp(updatedSkill)
            val leveledUp = leveledSkill.level > skill.level
            skillRepository.updateSkill(leveledSkill)

            // For retroactive records, never apply first-timer bonus
            // (the first-timer of today should only be granted to a live timer session)
            val isFirstTimerTodayForCalc = if (customStartTime == null) isFirstTimerToday else false

            // d. Calculate exp using ExpCalculator
            val equippedItems = withTimeout(5000) {
                itemRepository.getEquippedItems().first()
            }
            val activeCombos = withTimeout(5000) {
                comboRepository.getActiveCombos().first()
            }
            val streakDays = dailyState.streakCount
            val dailyInvestmentMinutes = dailyState.investmentMinutes

            val expGained = ExpCalculator.calculateExp(
                baseMinutes = durationMinutes,
                skillLevel = leveledSkill.level,
                equippedItems = equippedItems,
                activeCombos = activeCombos,
                streakDays = streakDays,
                isFirstTimerToday = isFirstTimerTodayForCalc,
                dailyInvestmentMinutes = dailyInvestmentMinutes
            )

            // c2. Persist character exp and time
            characterStateRepository.addExp(expGained)
            characterStateRepository.addTime(durationMinutes)

            // e. Calculate gold using GoldCalculator
            val goldGained = GoldCalculator.calculateGold(
                minutes = durationMinutes,
                isInvestment = recordType == RecordType.INVESTMENT,
                isFirstTimerToday = isFirstTimerTodayForCalc,
                skillLevel = leveledSkill.level
            )

            // f. Update daily state (investment/consumption minutes + energy + first-timer flag)
            // For retroactive records, don't consume energy or mark first-timer as used.
            val updatedDailyState = when (recordType) {
                RecordType.INVESTMENT -> dailyState.copy(
                    investmentMinutes = dailyState.investmentMinutes + durationMinutes,
                    goldEarned = dailyState.goldEarned + goldGained,
                    energy = if (customStartTime == null) (dailyState.energy - energyCost).coerceAtLeast(0f) else dailyState.energy,
                    isFirstTimerUsed = if (customStartTime == null) true else dailyState.isFirstTimerUsed,
                    lastUpdated = System.currentTimeMillis()
                )
                RecordType.CONSUMPTION -> dailyState.copy(
                    consumptionMinutes = dailyState.consumptionMinutes + durationMinutes,
                    goldEarned = dailyState.goldEarned + goldGained,
                    energy = if (customStartTime == null) (dailyState.energy - energyCost).coerceAtLeast(0f) else dailyState.energy,
                    isFirstTimerUsed = if (customStartTime == null) true else dailyState.isFirstTimerUsed,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            dailyStateRepository.insertOrUpdateState(updatedDailyState)

            // Mark first-timer as used in SettingsPrefs (only for live timers, not retroactive)
            if (customStartTime == null) {
                settingsPrefs.setFirstTimerUsedToday(true)
            }

            // g. Check for item unlocks on level up
            val itemsUnlocked = if (leveledUp) {
                characterStateRepository.updateMaxSkillLevel(leveledSkill.level)
                checkAndUnlockItems(leveledSkill, itemRepository)
            } else {
                emptyList()
            }

            // g2. Check achievements (use efficient count query)
            val totalRecords = withTimeout(5000) {
                timeRecordRepository.getTotalRecordCount()
            }
            if (totalRecords == 1) {
                achievementRepository.unlockAchievement("first_skill")
            }
            if (leveledSkill.level >= 5) {
                achievementRepository.unlockAchievement("skill_level_5")
            }
            if (leveledSkill.level >= 10) {
                achievementRepository.unlockAchievement("skill_level_10")
            }

            // h. Return TimerResult with all gains
            TimerResult(
                expGained = expGained,
                goldGained = goldGained,
                leveledUp = leveledUp,
                newLevel = leveledSkill.level,
                itemsUnlocked = itemsUnlocked
            )
        } catch (e: Exception) {
            throw e
        }
    }

    fun checkLevelUp(skill: Skill): Skill {
        return try {
            var currentLevel = skill.level
            var changed = false
            var iterations = 0
            val maxIterations = 100

            while (iterations < maxIterations) {
                val nextLevel = currentLevel + 1
                val threshold = skill.customThresholds[nextLevel] ?: (nextLevel * 60)
                if (skill.totalMinutes >= threshold && nextLevel <= 5) {
                    currentLevel = nextLevel
                    changed = true
                } else {
                    break
                }
                iterations++
            }

            if (changed) {
                skill.copy(level = currentLevel, updatedAt = System.currentTimeMillis())
            } else {
                skill
            }
        } catch (e: Exception) {
            skill
        }
    }

    suspend fun calculateDailyState(
        date: String,
        dailyStateRepository: DailyStateRepository,
        todoRepository: TodoRepository,
        timeRecordRepository: TimeRecordRepository
    ): DailyState {
        return try {
            val existing = withTimeout(5000) {
                dailyStateRepository.getStateByDate(date).first()
            }
            if (existing != null) return existing

            val todosCompleted = todoRepository.getCompletedCountByDate(date)
            val investmentMinutes = timeRecordRepository.getInvestmentMinutesByDate(date)

            val previousStreak = dailyStateRepository.getLatestStreak() ?: 0

            DailyState(
                date = date,
                investmentMinutes = investmentMinutes,
                todosCompleted = todosCompleted,
                streakCount = previousStreak,
                lastUpdated = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            DailyState(date = date, lastUpdated = System.currentTimeMillis())
        }
    }

    private suspend fun checkAndUnlockItems(
        skill: Skill,
        itemRepository: ItemRepository
    ): List<Item> {
        return try {
            val unlockedItems = mutableListOf<Item>()
            val level = skill.level

            val itemsToCreate = when (level) {
                2 -> listOf(
                    Item(
                        name = "${skill.name}学徒徽章",
                        skillId = skill.id,
                        itemTier = ItemTier.COMMON,
                        attributeBonus = 1,
                        expBonusContribution = 0.02f,
                        slotType = SlotType.ACCESSORY,
                        isUnlocked = true
                    )
                )
                3 -> listOf(
                    Item(
                        name = "${skill.name}精通符文",
                        skillId = skill.id,
                        itemTier = ItemTier.FINE,
                        attributeBonus = 3,
                        expBonusContribution = 0.05f,
                        slotType = SlotType.ACCESSORY,
                        isUnlocked = true
                    )
                )
                4 -> listOf(
                    Item(
                        name = "${skill.name}大师之证",
                        skillId = skill.id,
                        itemTier = ItemTier.RARE,
                        attributeBonus = 6,
                        expBonusContribution = 0.08f,
                        slotType = SlotType.ACCESSORY,
                        isUnlocked = true
                    )
                )
                5 -> listOf(
                    Item(
                        name = "${skill.name}传说印记",
                        skillId = skill.id,
                        itemTier = ItemTier.EPIC,
                        attributeBonus = 10,
                        expBonusContribution = 0.12f,
                        slotType = SlotType.ACCESSORY,
                        isUnlocked = true
                    ),
                    Item(
                        name = "${skill.name}传奇圣物",
                        skillId = skill.id,
                        itemTier = ItemTier.LEGENDARY,
                        attributeBonus = 15,
                        expBonusContribution = 0.18f,
                        slotType = SlotType.ACCESSORY,
                        isUnlocked = true
                    )
                )
                else -> emptyList()
            }

            for (item in itemsToCreate) {
                itemRepository.insertItem(item)
                unlockedItems.add(item)
            }

            unlockedItems
        } catch (e: Exception) {
            emptyList()
        }
    }
}
