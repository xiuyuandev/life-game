package com.lifeup.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Damage persisted per (demon, part) pair.
 *
 * There are 7 attackable parts per demon (one per day-of-week). Each
 * part can only be attacked on its day, so we store a per-part damage
 * total plus a per-day count.
 */
@Entity(tableName = "demon_part_damage")
data class DemonPartDamageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val demonId: String,
    /** Day of week 1..7 (Monday..Sunday). */
    val dayOfWeek: Int,
    /** Max HP for this part. */
    val maxHp: Int,
    /** Current HP for this part. */
    val currentHp: Int = maxHp,
    /** Total damage dealt to this part across the whole campaign. */
    val totalDamage: Int = 0,
    /** How many attacks landed. */
    val hitCount: Int = 0,
    val lastHitAt: Long? = null,
    /** Whether this part has been broken (HP reached 0). */
    val isBroken: Boolean = false
)
