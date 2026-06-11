package com.lifeup.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "todos",
    foreignKeys = [
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["linked_skill_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TodoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,

    @ColumnInfo(name = "is_habit")
    val isHabit: Boolean = false,

    val priority: String = "NONE",

    @ColumnInfo(name = "linked_skill_id")
    val linkedSkillId: Long? = null,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    val date: String,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0
)
