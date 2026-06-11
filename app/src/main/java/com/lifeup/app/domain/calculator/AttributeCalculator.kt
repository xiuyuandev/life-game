package com.lifeup.app.domain.calculator

import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sqrt

object AttributeCalculator {

    fun calculateEffectiveValue(rawValue: Int, baseValue: Int = 10): Int {
        val overflow = rawValue - baseValue
        if (overflow <= 0) return baseValue
        return baseValue + sqrt(overflow.toDouble()).toInt()
    }

    fun calculateEndurance(maxStreak: Int, activeSkillCount: Int): Int {
        return floor(maxStreak * 0.5).toInt() + activeSkillCount
    }

    /**
     * Character level is calculated as floor(sqrt(totalExp / 1000)).
     * Level 1: 0-999 EXP
     * Level 2: 1,000-3,999 EXP
     * Level 3: 4,000-8,999 EXP
     * Level N: requires (N-1)² × 1000 EXP
     */
    fun calculateCharacterLevel(totalExp: Long): Int {
        if (totalExp <= 0) return 1
        return floor(sqrt(totalExp / 1000.0)).toInt() + 1
    }

    /**
     * Total EXP threshold to reach the given character level.
     * Level 1 = 0 EXP, Level 2 = 1000 EXP, Level 3 = 4000 EXP, ...
     * Formula: (level - 1)² × 1000
     */
    fun getLevelThreshold(level: Int): Long {
        if (level <= 1) return 0L
        val delta = (level - 1).toLong()
        return delta * delta * 1000L
    }

    fun getEnergyCap(baseCap: Int, characterLevel: Int): Int {
        val bonus = characterLevel * 2
        return baseCap + bonus.coerceAtMost(50)
    }
}
