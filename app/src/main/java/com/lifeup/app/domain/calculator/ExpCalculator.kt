package com.lifeup.app.domain.calculator

import com.lifeup.app.data.db.entity.EquipmentEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpCalculator @Inject constructor(
    private val equipmentEffectCalculator: EquipmentEffectCalculator
) {

    fun calculate(
        durationMinutes: Long,
        isInvestment: Boolean,
        skillLevel: Int,
        category: String,
        isFirstSessionToday: Boolean,
        streakDays: Int,
        activeEquipment: List<EquipmentEntity>
    ): ExpBreakdown {
        // 基础经验: 每分钟 2 exp
        var baseExp = durationMinutes * 2

        // 消耗性活动系数 0.3x
        if (!isInvestment) {
            baseExp = (baseExp * 0.3).toLong()
        }

        // 技能等级加成: 每5级增加10%
        val skillLevelBonus = 1.0 + (skillLevel / 5) * 0.1
        val skillBonus = (baseExp * (skillLevelBonus - 1.0)).toLong()

        // 装备加成
        val effects = equipmentEffectCalculator.getActiveEffects(
            activeEquipment, category, durationMinutes, isFirstSessionToday, streakDays
        )
        val equipmentMultiplier = equipmentEffectCalculator.calculateExpMultiplier(effects)
        val equipmentBonus = (baseExp * (equipmentMultiplier - 1.0)).toLong()

        // 总经验
        val total = baseExp + skillBonus + equipmentBonus

        return ExpBreakdown(
            base = baseExp,
            skillBonus = skillBonus,
            equipmentBonus = equipmentBonus,
            total = total,
            equipmentDetails = effects.map { it.description }
        )
    }

    fun calculateCharacterExpToNext(level: Int): Long = (100 * level * 1.4).toLong()

    fun calculateSkillExpToNext(level: Int): Long = (60 * level * 1.3).toLong()
}

data class ExpBreakdown(
    val base: Long,
    val skillBonus: Long,
    val equipmentBonus: Long,
    val total: Long,
    val equipmentDetails: List<String>
)
