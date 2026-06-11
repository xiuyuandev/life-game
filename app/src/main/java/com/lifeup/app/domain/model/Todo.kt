package com.lifeup.app.domain.model

import com.lifeup.app.data.db.Priority

data class Todo(
    val id: Long = 0,
    val title: String,
    val isHabit: Boolean = false,
    val priority: Priority = Priority.NONE,
    val linkedSkillId: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val date: String,
    val sortOrder: Int = 0
)
