package com.lifeup.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val icon: String = "🏆",
    val category: String,
    val conditionType: String,
    val conditionValue: Long,
    val rewardExp: Long = 0,
    val rewardGold: Long = 0,
    val rewardEquipmentId: Long? = null,
    val unlocked: Boolean = false,
    val unlockedAt: Long = 0,
    val isMilestone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
