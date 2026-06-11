package com.lifeup.app.domain.model

data class CharacterState(
    val characterLevel: Int,
    val totalExp: Long,
    val attributes: Map<String, Int>,
    val equippedItems: List<Item>,
    val title: String
)
