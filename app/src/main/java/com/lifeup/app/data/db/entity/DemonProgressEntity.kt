package com.lifeup.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted progress for a single inner demon.
 *
 * Each of the 12 inner demons is identified by a stable [demonId]
 * (e.g. "PROCRASTINATION_SERPENT"). One row per demon.
 *
 * The 7 attackable parts are stored separately in [DemonPartDamageEntity]
 * so the database can be queried per-part efficiently.
 */
@Entity(tableName = "demon_progress")
data class DemonProgressEntity(
    @PrimaryKey
    val demonId: String,
    /** Total HP for the demon (sum of all 7 part HPs). */
    val totalHp: Int,
    /** Current HP (sum of current damage across all 7 parts). */
    val currentHp: Int = totalHp,
    /** Whether the demon has been defeated. */
    val isDefeated: Boolean = false,
    /** When the demon was first encountered. */
    val discoveredAt: Long = System.currentTimeMillis(),
    /** When the demon was defeated (null until then). */
    val defeatedAt: Long? = null,
    /** How many times the player has attempted this demon. */
    val attemptCount: Int = 0,
    /** Whether the player opted to fight this demon. */
    val isActive: Boolean = false,
    /** A 0..1 progress value cached for list rendering. */
    val progressFraction: Float = 0f,
    /** A monotonic counter that increments on every damage event. */
    val lastUpdated: Long = System.currentTimeMillis()
)
