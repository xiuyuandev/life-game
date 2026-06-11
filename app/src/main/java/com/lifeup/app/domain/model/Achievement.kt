package com.lifeup.app.domain.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val target: Int = 1
)

enum class AchievementCategory {
    SKILL, HABIT, STREAK, COLLECTION, COMBO
}
