package com.lifeup.app.domain.calculator

import com.lifeup.app.data.db.entity.EquipmentEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipmentEffectCalculator @Inject constructor() {

    fun getActiveEffects(
        activeEquipment: List<EquipmentEntity>,
        category: String,
        durationMinutes: Long,
        isFirstSessionToday: Boolean,
        streakDays: Int
    ): List<EquipmentEffect> {
        val effects = mutableListOf<EquipmentEffect>()

        for (equip in activeEquipment) {
            when (equip.effectType) {
                "exp_multiplier" -> {
                    if (matchesTarget(equip.effectTarget, category)) {
                        effects.add(
                            EquipmentEffect(
                                name = equip.name,
                                type = EffectType.EXP_MULTIPLIER,
                                value = equip.effectValue,
                                description = "${equip.name}: +${(equip.effectValue * 100).toInt()}%经验"
                            )
                        )
                    }
                }
                "gold_multiplier" -> {
                    if (matchesTarget(equip.effectTarget, category)) {
                        effects.add(
                            EquipmentEffect(
                                name = equip.name,
                                type = EffectType.GOLD_MULTIPLIER,
                                value = equip.effectValue,
                                description = "${equip.name}: +${(equip.effectValue * 100).toInt()}%金币"
                            )
                        )
                    }
                }
                "first_daily_bonus" -> {
                    if (isFirstSessionToday && matchesTarget(equip.effectTarget, category)) {
                        effects.add(
                            EquipmentEffect(
                                name = equip.name,
                                type = EffectType.FIRST_DAILY_BONUS,
                                value = equip.effectValue,
                                description = "${equip.name}: 首次+${(equip.effectValue * 100).toInt()}%"
                            )
                        )
                    }
                }
                "long_session_bonus" -> {
                    val minMinutes = equip.effectTarget.toLongOrNull() ?: 45
                    if (durationMinutes >= minMinutes) {
                        effects.add(
                            EquipmentEffect(
                                name = equip.name,
                                type = EffectType.LONG_SESSION_BONUS,
                                value = equip.effectValue,
                                description = "${equip.name}: 长时段+${(equip.effectValue * 100).toInt()}%"
                            )
                        )
                    }
                }
                "streak_bonus" -> {
                    val interval = equip.effectTarget.toIntOrNull() ?: 7
                    val bonusCount = streakDays / interval
                    if (bonusCount > 0) {
                        val totalValue = equip.effectValue * bonusCount
                        effects.add(
                            EquipmentEffect(
                                name = equip.name,
                                type = EffectType.STREAK_BONUS,
                                value = totalValue,
                                description = "${equip.name}: 连续${streakDays}天+${(totalValue * 100).toInt()}%"
                            )
                        )
                    }
                }
                "sp_recovery" -> {
                    effects.add(
                        EquipmentEffect(
                            name = equip.name,
                            type = EffectType.SP_RECOVERY,
                            value = equip.effectValue,
                            description = "${equip.name}: 精力恢复+${equip.effectValue.toInt()}"
                        )
                    )
                }
            }
        }

        return effects
    }

    fun calculateExpMultiplier(effects: List<EquipmentEffect>): Double {
        var multiplier = 1.0
        effects.filter { it.type == EffectType.EXP_MULTIPLIER || it.type == EffectType.FIRST_DAILY_BONUS || it.type == EffectType.LONG_SESSION_BONUS || it.type == EffectType.STREAK_BONUS }
            .forEach { multiplier += it.value }
        return multiplier
    }

    fun calculateGoldMultiplier(effects: List<EquipmentEffect>): Double {
        var multiplier = 1.0
        effects.filter { it.type == EffectType.GOLD_MULTIPLIER }
            .forEach { multiplier += it.value }
        return multiplier
    }

    private fun matchesTarget(target: String, category: String): Boolean {
        return target == "all" || target == category
    }
}

data class EquipmentEffect(
    val name: String,
    val type: EffectType,
    val value: Double,
    val description: String
)

enum class EffectType {
    EXP_MULTIPLIER,
    GOLD_MULTIPLIER,
    FIRST_DAILY_BONUS,
    LONG_SESSION_BONUS,
    STREAK_BONUS,
    SP_RECOVERY
}
