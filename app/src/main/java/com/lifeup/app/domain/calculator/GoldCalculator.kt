package com.lifeup.app.domain.calculator

object GoldCalculator {

    private const val MAX_GOLD = Int.MAX_VALUE / 2

    fun calculateGold(
        minutes: Int,
        isInvestment: Boolean,
        isFirstTimerToday: Boolean,
        skillLevel: Int
    ): Int {
        val safeMinutes = minutes.coerceAtLeast(0)
        val safeSkillLevel = skillLevel.coerceAtLeast(0)

        val baseRate = if (isInvestment) 1.0 else 0.2
        var gold = (safeMinutes * baseRate).toInt()

        if (isFirstTimerToday) {
            gold += 50
        }

        val levelBonus = when {
            safeSkillLevel >= 5 -> 5
            safeSkillLevel >= 4 -> 3
            safeSkillLevel >= 3 -> 2
            safeSkillLevel >= 2 -> 1
            else -> 0
        }
        gold += levelBonus

        return if (gold < 0 || gold > MAX_GOLD) MAX_GOLD else gold
    }
}
