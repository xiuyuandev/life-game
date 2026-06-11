package com.lifeup.app.domain.model

data class DailyState(
    val id: Long = 0,
    val date: String,
    val energy: Float = 100f,
    val energyCap: Float = 100f,
    val investmentMinutes: Int = 0,
    val consumptionMinutes: Int = 0,
    val streakCount: Int = 0,
    val isFirstTimerUsed: Boolean = false,
    val todosCompleted: Int = 0,
    val habitsCompleted: Int = 0,
    val goldEarned: Int = 0,
    val goldSpent: Int = 0
)
