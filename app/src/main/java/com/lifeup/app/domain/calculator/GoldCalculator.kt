package com.lifeup.app.domain.calculator

object GoldCalculator {

    fun calculateGold(
        minutes: Int,
        isInvestment: Boolean,
        isFirstTimerToday: Boolean,
        skillLevel: Int
    ): Int {
        val baseRate = if (isInvestment) 1 else 0.2
        var gold = (minutes * baseRate).toInt()

        if (isFirstTimerToday) {
            gold += 50
        }

        val levelBonus = when {
            skillLevel >= 5 -> 5
            skillLevel >= 4 -> 3
            skillLevel >= 3 -> 2
            skillLevel >= 2 -> 1
            else -> 0
        }
        gold += levelBonus

        return gold
    }
}
