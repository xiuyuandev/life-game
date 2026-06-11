package com.lifeup.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "combos",
    foreignKeys = [
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["primary_skill_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["secondary_skill_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ComboEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    @ColumnInfo(name = "primary_skill_id", index = true)
    val primarySkillId: Long,

    @ColumnInfo(name = "secondary_skill_id", index = true)
    val secondarySkillId: Long,

    @ColumnInfo(name = "required_level")
    val requiredLevel: Int = 2,

    @ColumnInfo(name = "exp_bonus")
    val expBonus: Float = 1.05f,

    val suggestion: String? = null,

    @ColumnInfo(name = "connection_color")
    val connectionColor: String? = null,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
