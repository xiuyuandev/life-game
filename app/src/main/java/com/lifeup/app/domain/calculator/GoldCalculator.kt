package com.lifeup.app.domain.calculator

import com.lifeup.app.data.db.entity.EquipmentEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoldCalculator @Inject constructor(
    private val equipmentEffectCalculator: EquipmentEffectCalculator
) {

    fun calculate(
        durationMinutes: Long,
        isInvestment: Boolean,
        category: String,
        activeEquipment: List<EquipmentEntity>
    ): Long {
        if (!isInvestment) return 0

        // 投资性时间每15分钟 = 1金币
        var gold = (durationMinutes / 15).toLong()
        if (gold < 1) gold = 1 // 最少1金币

        // 装备金币加成
        val effects = equipmentEffectCalculator.getActiveEffects(
            activeEquipment, category, durationMinutes, false, 0
        )
        val multiplier = equipmentEffectCalculator.calculateGoldMultiplier(effects)
        gold = (gold * multiplier).toLong()

        return gold
    }
}
