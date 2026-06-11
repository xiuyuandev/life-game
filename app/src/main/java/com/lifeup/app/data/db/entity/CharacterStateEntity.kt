package com.lifeup.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "character_state")
data class CharacterStateEntity(
    @PrimaryKey
    val id: Int = 1, // Single row table
    val totalExp: Long = 0,
    val characterLevel: Int = 1,
    val title: String = "初学者",
    val totalTimeMinutes: Long = 0,
    val skillCount: Int = 0,
    val maxSkillLevel: Int = 1,
    val achievementsUnlocked: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)
