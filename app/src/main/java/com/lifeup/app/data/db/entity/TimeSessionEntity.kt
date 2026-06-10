package com.lifeup.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_sessions")
data class TimeSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val linkedSkillId: Long? = null,
    val startTime: Long,
    val endTime: Long? = null,
    val durationMinutes: Long = 0,
    val baseExp: Long = 0,
    val bonusExp: Long = 0,
    val totalExp: Long = 0,
    val goldEarned: Long = 0,
    val isInvestment: Boolean = true,
    val note: String = "",
    val date: String = ""
)
