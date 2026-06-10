package com.lifeup.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_assets")
data class TimeAssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val totalMinutes: Long = 0,
    val investedMinutes: Long = 0,
    val consumedMinutes: Long = 0,
    val investmentRatio: Double = 0.0,
    val topSkillId: Long? = null,
    val topSkillMinutes: Long = 0,
    val sessionsCount: Int = 0,
    val expEarned: Long = 0,
    val goldEarned: Long = 0
)
