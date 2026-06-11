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

    fun calculateExp(
        baseMinutes: Int,
        skillLevel: Int,
        equippedItems: List<Item>,
        activeCombos: List<Combo>,
        streakDays: Int,
        isFirstTimerToday: Boolean,
        dailyInvestmentMinutes: Int
    ): Long {
        val baseExp = baseMinutes * 10L

        val tierMultiplier = tierMultipliers[skillLevel.coerceIn(1, 5)] ?: 1.00

        val itemMultiplier = equippedItems.fold(1.0) { acc, item ->
            acc + item.expBonusContribution
        }

        val comboMultiplier = activeCombos.fold(1.0) { acc, combo ->
            acc * combo.expBonus
        }

        val streakMultiplier = 1.0 + min(streakDays, 30) * 0.02

        val firstHitMultiplier = if (isFirstTimerToday) 1.5 else 1.0

        val softCapMultiplier = calculateSoftCapMultiplier(dailyInvestmentMinutes)

        val totalExp = baseExp *
                tierMultiplier *
                itemMultiplier *
                comboMultiplier *
                streakMultiplier *
                firstHitMultiplier *
                softCapMultiplier

        return totalExp.toLong()
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
