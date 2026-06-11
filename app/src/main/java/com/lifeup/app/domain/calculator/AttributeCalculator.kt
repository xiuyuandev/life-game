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

    fun calculateCharacterLevel(totalExp: Long): Int {
        return floor(sqrt(totalExp / 1000.0)).toInt()
    }

    fun getEnergyCap(baseCap: Int, characterLevel: Int): Int {
        val bonus = characterLevel * 2
        return baseCap + bonus.coerceAtMost(50)
    }
}
