package com.lifeup.app.domain.calculator

import com.lifeup.app.data.db.entity.SkillEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttributeCalculator @Inject constructor() {

    fun calculateAttributes(
        skills: List<SkillEntity>,
        streakDays: Int,
        unlockedAchievementCount: Int
    ): Attributes {
        // 体质 = 1 + 运动类技能等级之和 / 3
        val strength = 1 + (skills.filter { it.category == "sport" }.sumOf { it.level.toLong() } / 3).toInt()

        // 智力 = 1 + 职业类技能等级之和 / 3
        val intelligence = 1 + (skills.filter { it.category == "professional" }.sumOf { it.level.toLong() } / 3).toInt()

        // 魅力 = 1 + (社交类 + 艺术类)技能等级之和 / 3
        val charm = 1 + ((
            skills.filter { it.category == "social" }.sumOf { it.level.toLong() } +
            skills.filter { it.category == "art" }.sumOf { it.level.toLong() }
        ) / 3).toInt()

        // 耐力 = 1 + 连续天数 / 7
        val constitution = 1 + (streakDays / 7)

        // 敏捷 = 1 + 不同类别技能达到Lv3的数量
        val categoryCount = skills.filter { it.level >= 3 }.map { it.category }.distinct().size
        val agility = 1 + categoryCount

        // 运气 = 1 + 成就解锁数 / 5
        val luck = 1 + (unlockedAchievementCount / 5)

        return Attributes(strength, intelligence, charm, constitution, agility, luck)
    }
}

data class Attributes(
    val strength: Int,
    val intelligence: Int,
    val charm: Int,
    val constitution: Int,
    val agility: Int,
    val luck: Int
)
