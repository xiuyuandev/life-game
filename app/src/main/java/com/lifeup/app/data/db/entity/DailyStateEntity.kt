package com.lifeup.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_states")
data class DailyStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val date: String,

    val energy: Float = 100f,

    @ColumnInfo(name = "energy_cap")
    val energyCap: Float = 100f,

    @ColumnInfo(name = "investment_minutes")
    val investmentMinutes: Int = 0,

    @ColumnInfo(name = "consumption_minutes")
    val consumptionMinutes: Int = 0,

    @ColumnInfo(name = "streak_count")
    val streakCount: Int = 0,

    @ColumnInfo(name = "is_first_timer_used")
    val isFirstTimerUsed: Boolean = false,

    @ColumnInfo(name = "todos_completed")
    val todosCompleted: Int = 0,

    @ColumnInfo(name = "habits_completed")
    val habitsCompleted: Int = 0,

    @ColumnInfo(name = "gold_earned")
    val goldEarned: Int = 0
)
