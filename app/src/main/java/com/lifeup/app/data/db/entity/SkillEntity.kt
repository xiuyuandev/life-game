package com.lifeup.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "skills")
data class SkillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val category: String,

    val boundAttribute: String,

    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Long = 0L,

    val level: Int = 1,

    @ColumnInfo(name = "mastery_stars")
    val masteryStars: Int = 0,

    @ColumnInfo(name = "custom_thresholds")
    val customThresholds: String = "{}",

    @ColumnInfo(name = "icon_key")
    val iconKey: String? = null,

    val color: String? = null,

    val status: String = "ACTIVE",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,

    @ColumnInfo(name = "display_in_showcase")
    val displayInShowcase: Boolean = true
)
