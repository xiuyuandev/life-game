package com.lifeup.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Records a real-world reflection the player wrote after defeating
 * (or attempting to fight) an inner demon.
 *
 * The diary is opt-in: the prompt only appears after a defeat, and the
 * player can skip it.
 */
@Entity(tableName = "demon_diary")
data class DemonDiaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val demonId: String,
    /** ISO date the reflection was written. */
    val date: String,
    /** Free-form text written by the player. */
    val content: String,
    /** Optional one-line takeaway. */
    val takeaway: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
