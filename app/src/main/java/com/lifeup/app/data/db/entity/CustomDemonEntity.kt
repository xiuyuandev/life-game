package com.lifeup.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A demon that the player created themselves (P2 feature).
 *
 * Custom demons live alongside the 12 built-in ones but use a different
 * id prefix so the engine can route them to the right renderer.
 */
@Entity(tableName = "custom_demons")
data class CustomDemonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val displayName: String,
    /** Description shown in the demon detail page. */
    val description: String,
    /** Color hex like "#FF8A50". */
    val colorHex: String,
    /** Comma-separated list of categories the demon is weak to. */
    val weakCategories: String,
    /** Comma-separated list of categories the demon resists. */
    val resistCategories: String,
    /** Per-day part HPs, stored as "mon:120,tue:120,...". */
    val partHpMap: String,
    /** Total damage dealt across the whole campaign. */
    val currentDamage: Int = 0,
    val maxHp: Int,
    val isDefeated: Boolean = false,
    val defeatedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
