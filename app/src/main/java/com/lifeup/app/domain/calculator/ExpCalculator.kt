package com.lifeup.app.domain.calculator

import com.lifeup.app.domain.model.Combo
import com.lifeup.app.domain.model.Item
import kotlin.math.min

object ExpCalculator {

    private val tierMultipliers = mapOf(
        1 to 1.00,
        2 to 1.05,
        3 to 1.10,
        4 to 1.18,
        5 to 1.25
    )

    private const val MAX_EXP = Long.MAX_VALUE / 2

    fun calculateExp(
        baseMinutes: Int,
        skillLevel: Int,
        equippedItems: List<Item>,
        activeCombos: List<Combo>,
        streakDays: Int,
        isFirstTimerToday: Boolean,
        dailyInvestmentMinutes: Int
    ): Long {
        val safeBaseMinutes = baseMinutes.coerceAtLeast(0)
        val safeSkillLevel = skillLevel.coerceAtLeast(1)
        val safeStreakDays = streakDays.coerceAtLeast(0)
        val safeDailyInvestmentMinutes = dailyInvestmentMinutes.coerceAtLeast(0)

        val baseExp = safeBaseMinutes * 10L

        val tierMultiplier = tierMultipliers[safeSkillLevel.coerceIn(1, 5)] ?: 1.00

        val itemMultiplier = equippedItems.fold(1.0) { acc, item ->
            acc + (item.expBonusContribution.coerceAtLeast(0f))
        }

        val comboMultiplier = activeCombos.fold(1.0) { acc, combo ->
            acc * (combo.expBonus.coerceAtLeast(0.0))
        }

        val streakMultiplier = 1.0 + min(safeStreakDays, 30) * 0.02

        val firstHitMultiplier = if (isFirstTimerToday) 1.5 else 1.0

        val softCapMultiplier = calculateSoftCapMultiplier(safeDailyInvestmentMinutes)

        val totalExp = baseExp *
                tierMultiplier *
                itemMultiplier *
                comboMultiplier *
                streakMultiplier *
                firstHitMultiplier *
                softCapMultiplier

        val result = totalExp.toLong()
        return if (result < 0 || result > MAX_EXP) MAX_EXP else result
    }

    private fun calculateSoftCapMultiplier(dailyInvestmentMinutes: Int): Double {
        return when {
            dailyInvestmentMinutes <= 120 -> 1.0
            dailyInvestmentMinutes <= 240 -> 0.8
            dailyInvestmentMinutes <= 360 -> 0.6
            else -> 0.4
        }
    }
}
