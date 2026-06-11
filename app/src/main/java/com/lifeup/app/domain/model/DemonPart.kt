package com.lifeup.app.domain.model

import java.time.DayOfWeek

/**
 * One of the seven attackable parts of an inner demon. Each part
 * corresponds to a day of the week — only sessions completed on that
 * day can damage the part.
 */
data class DemonPart(
    val dayOfWeek: Int,
    val bodyPartName: String,
    val maxHp: Int,
    val currentHp: Int = maxHp,
    val totalDamage: Int = 0,
    val hitCount: Int = 0,
    val isBroken: Boolean = false
) {
    val day: DayOfWeek get() = DayOfWeek.of(dayOfWeek)
    val isAlive: Boolean get() = currentHp > 0
    val progressFraction: Float
        get() = if (maxHp <= 0) 0f else 1f - (currentHp.toFloat() / maxHp)

    companion object {
        val ALL: List<DemonPart> = listOf(
            DemonPart(1, "头", 0, 0),  // HP injected at construction time
            DemonPart(2, "颈", 0, 0),
            DemonPart(3, "胸", 0, 0),
            DemonPart(4, "腹", 0, 0),
            DemonPart(5, "背", 0, 0),
            DemonPart(6, "尾", 0, 0),
            DemonPart(7, "翼", 0, 0)
        )
    }
}
