package com.lifeup.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val level: Int = 1,
    val maxLevel: Int = 20,
    val exp: Long = 0,
    val expToNext: Long = 78,                   // 60 * level * 1.3 (for Lv1)
    val icon: String = "⭐",
    val description: String = "",
    val parentSkillId: Long? = null,
    val parentLevelRequired: Int = 3,
    val unlocked: Boolean = true,
    val totalMinutesInvested: Long = 0,
    val attributeContribution: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
