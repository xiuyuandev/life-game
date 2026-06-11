package com.lifeup.app.domain.model

import com.lifeup.app.data.db.BoundAttribute
import com.lifeup.app.data.db.SkillCategory
import com.lifeup.app.data.db.SkillStatus

data class Skill(
    val id: Long = 0,
    val name: String,
    val category: SkillCategory,
    val boundAttribute: BoundAttribute,
    val totalMinutes: Long = 0L,
    val level: Int = 1,
    val masteryStars: Int = 0,
    val customThresholds: Map<Int, Int> = emptyMap(),
    val iconKey: String? = null,
    val color: String? = null,
    val status: SkillStatus = SkillStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    val displayInShowcase: Boolean = true
) {
    fun getProgressToNextLevel(): Float {
        if (level <= 0) return 0f
        val currentThreshold = customThresholds[level] ?: (level * 60)
        val nextThreshold = customThresholds[level + 1] ?: ((level + 1) * 60)
        val range = nextThreshold - currentThreshold
        if (range <= 0) return 1f
        val progressInLevel = totalMinutes - currentThreshold
        return (progressInLevel.toFloat() / range).coerceIn(0f, 1f)
    }

    fun getAttributeBonus(): Int {
        return when {
            masteryStars >= 3 -> 23
            masteryStars == 2 -> 20
            masteryStars == 1 -> 18
            else -> when (level) {
                1 -> 1
                2 -> 3
                3 -> 6
                4 -> 10
                else -> 15 // level >= 5
            }
        }
    }
}
