package com.lifeup.app.domain.model

data class Combo(
    val id: Long = 0,
    val name: String,
    val primarySkillId: Long,
    val secondarySkillId: Long,
    val requiredLevel: Int = 2,
    val expBonus: Float = 1.05f,
    val suggestion: String? = null,
    val connectionColor: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
