package com.lifeup.app.domain.game

import com.lifeup.app.data.db.FocusType
import com.lifeup.app.data.db.ItemTier
import com.lifeup.app.data.db.RecordType
import com.lifeup.app.data.db.SlotType
import com.lifeup.app.domain.calculator.ExpCalculator
import com.lifeup.app.domain.calculator.GoldCalculator
import com.lifeup.app.domain.model.Combo
import com.lifeup.app.domain.model.DailyState
import com.lifeup.app.domain.model.Item
import com.lifeup.app.domain.model.Skill
import com.lifeup.app.domain.model.TimeRecord
import com.lifeup.app.domain.repository.ComboRepository
import com.lifeup.app.domain.repository.DailyStateRepository
import com.lifeup.app.domain.repository.ItemRepository
import com.lifeup.app.domain.repository.SkillRepository
import com.lifeup.app.domain.repository.TimeRecordRepository
import com.lifeup.app.domain.repository.TodoRepository
import kotlinx.coroutines.flow.first
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
        itemRepository: ItemRepository
    ): TimerResult {
        val now = System.currentTimeMillis()
        val skill = skillRepository.getSkillById(skillId)
            ?: throw IllegalArgumentException("Skill not found: $skillId")

        // a. Create a TimeRecord
        val record = TimeRecord(
            skillId = skillId,
            startTime = now - durationMinutes * 60_000L,
            endTime = now,
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

        // d. Calculate exp using ExpCalculator
        val today = LocalDate.now().format(dateFormatter)
        val dailyState = dailyStateRepository.getStateByDate(today).first()
        val equippedItems = itemRepository.getEquippedItems().first()
        val activeCombos = comboRepository.getActiveCombos().first()
        val streakDays = dailyState?.streakCount ?: 0
        val isFirstTimerToday = dailyState?.isFirstTimerUsed ?: false
        val dailyInvestmentMinutes = dailyState?.investmentMinutes ?: 0

        val expGained = ExpCalculator.calculateExp(
            baseMinutes = durationMinutes,
            skillLevel = leveledSkill.level,
            equippedItems = equippedItems,
            activeCombos = activeCombos,
            streakDays = streakDays,
            isFirstTimerToday = isFirstTimerToday,
            dailyInvestmentMinutes = dailyInvestmentMinutes
        )

        // e. Calculate gold using GoldCalculator
        val goldGained = GoldCalculator.calculateGold(
            minutes = durationMinutes,
            isInvestment = recordType == RecordType.INVESTMENT,
            isFirstTimerToday = isFirstTimerToday,
            skillLevel = leveledSkill.level
        )

        // f. Update daily state (investment/consumption minutes)
        val currentDailyState = dailyState ?: DailyState(date = today)
        val updatedDailyState = when (recordType) {
            RecordType.INVESTMENT -> currentDailyState.copy(
                investmentMinutes = currentDailyState.investmentMinutes + durationMinutes,
                goldEarned = currentDailyState.goldEarned + goldGained
            )
            RecordType.CONSUMPTION -> currentDailyState.copy(
                consumptionMinutes = currentDailyState.consumptionMinutes + durationMinutes,
                goldEarned = currentDailyState.goldEarned + goldGained
            )
        }
        dailyStateRepository.insertOrUpdateState(updatedDailyState)

        // g. Check for item unlocks on level up
        val itemsUnlocked = if (leveledUp) {
            checkAndUnlockItems(leveledSkill, itemRepository)
        } else {
            emptyList()
        }

        // h. Return TimerResult with all gains
        return TimerResult(
            expGained = expGained,
            goldGained = goldGained,
            leveledUp = leveledUp,
            newLevel = leveledSkill.level,
            itemsUnlocked = itemsUnlocked
        )
    }

    fun checkLevelUp(skill: Skill): Skill {
        var currentLevel = skill.level
        var changed = false

        while (true) {
            val nextLevel = currentLevel + 1
            val threshold = skill.customThresholds[nextLevel] ?: (nextLevel * 60)
            if (skill.totalMinutes >= threshold && nextLevel <= 5) {
                currentLevel = nextLevel
                changed = true
            } else {
                break
            }
        }

        return if (changed) {
            skill.copy(level = currentLevel, updatedAt = System.currentTimeMillis())
        } else {
            skill
        }
    }

    suspend fun calculateDailyState(
        date: String,
        dailyStateRepository: DailyStateRepository,
        todoRepository: TodoRepository,
        timeRecordRepository: TimeRecordRepository
    ): DailyState {
        val existing = dailyStateRepository.getStateByDate(date).first()
        if (existing != null) return existing

        val todosCompleted = todoRepository.getCompletedCountByDate(date)
        val investmentMinutes = timeRecordRepository.getInvestmentMinutesByDate(date)

        val previousStreak = dailyStateRepository.getLatestStreak() ?: 0

        return DailyState(
            date = date,
            investmentMinutes = investmentMinutes,
            todosCompleted = todosCompleted,
            streakCount = previousStreak
        )
    }

    private suspend fun checkAndUnlockItems(
        skill: Skill,
        itemRepository: ItemRepository
    ): List<Item> {
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

        return unlockedItems
    }
}
