package com.lifeup.app.domain.game

import com.lifeup.app.data.db.ItemTier
import com.lifeup.app.domain.model.Combo
import com.lifeup.app.domain.model.Item
import com.lifeup.app.domain.model.Skill

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

enum class AchievementCategory {
    SKILL, STREAK, COLLECTION, COMBO, TIME, GOLD
}

object AchievementChecker {

    private val predefinedAchievements = listOf(
        // SKILL achievements
        Achievement("skill_1", "初出茅庐", "拥有1个技能", AchievementCategory.SKILL),
        Achievement("skill_5", "技能达人", "拥有5个技能", AchievementCategory.SKILL),
        Achievement("skill_10", "技能大师", "拥有10个技能", AchievementCategory.SKILL),
        Achievement("skill_lv5", "传说降临", "拥有1个LV5技能", AchievementCategory.SKILL),

        // STREAK achievements
        Achievement("streak_7", "坚持7天", "连续打卡7天", AchievementCategory.STREAK),
        Achievement("streak_30", "毅力30天", "连续打卡30天", AchievementCategory.STREAK),
        Achievement("streak_100", "自律100天", "连续打卡100天", AchievementCategory.STREAK),
        Achievement("streak_365", "传奇365天", "连续打卡365天", AchievementCategory.STREAK),

        // COLLECTION achievements
        Achievement("collection_10", "收藏家", "解锁10个物品", AchievementCategory.COLLECTION),
        Achievement("collection_rare_5", "鉴赏家", "解锁5个稀有或以上品质物品", AchievementCategory.COLLECTION),
        Achievement("collection_legendary_1", "传说猎人", "解锁1个传说品质物品", AchievementCategory.COLLECTION),

        // COMBO achievements
        Achievement("combo_1", "跨界思维", "激活1个连击", AchievementCategory.COMBO),
        Achievement("combo_3", "多面手", "激活3个连击", AchievementCategory.COMBO),
        Achievement("combo_5", "全能达人", "激活5个连击", AchievementCategory.COMBO),

        // TIME achievements
        Achievement("time_100", "百日筑基", "累计投资100小时", AchievementCategory.TIME),
        Achievement("time_500", "千锤百炼", "累计投资500小时", AchievementCategory.TIME),
        Achievement("time_1000", "万小时定律", "累计投资1000小时", AchievementCategory.TIME),

        // GOLD achievements
        Achievement("gold_1000", "小有积蓄", "累计获得1000金币", AchievementCategory.GOLD),
        Achievement("gold_10000", "财富自由", "累计获得10000金币", AchievementCategory.GOLD)
    )

    fun checkAchievements(
        skills: List<Skill>,
        streakCount: Int,
        unlockedItems: List<Item>,
        activeCombos: List<Combo>,
        totalMinutes: Int = 0,
        totalGold: Int = 0
    ): List<Achievement> {
        val now = System.currentTimeMillis()
        val newlyUnlocked = mutableListOf<Achievement>()

        for (achievement in predefinedAchievements) {
            val conditionMet = when (achievement.category) {
                AchievementCategory.SKILL -> checkSkillAchievement(achievement, skills)
                AchievementCategory.STREAK -> checkStreakAchievement(achievement, streakCount)
                AchievementCategory.COLLECTION -> checkCollectionAchievement(achievement, unlockedItems)
                AchievementCategory.COMBO -> checkComboAchievement(achievement, activeCombos)
                AchievementCategory.TIME -> checkTimeAchievement(achievement, totalMinutes)
                AchievementCategory.GOLD -> checkGoldAchievement(achievement, totalGold)
            }

            if (conditionMet) {
                newlyUnlocked.add(achievement.copy(isUnlocked = true, unlockedAt = now))
            }
        }

        return newlyUnlocked
    }

    private fun checkSkillAchievement(achievement: Achievement, skills: List<Skill>): Boolean {
        return when (achievement.id) {
            "skill_1" -> skills.size >= 1
            "skill_5" -> skills.size >= 5
            "skill_10" -> skills.size >= 10
            "skill_lv5" -> skills.any { it.level >= 5 }
            else -> false
        }
    }

    private fun checkStreakAchievement(achievement: Achievement, streakCount: Int): Boolean {
        return when (achievement.id) {
            "streak_7" -> streakCount >= 7
            "streak_30" -> streakCount >= 30
            "streak_100" -> streakCount >= 100
            "streak_365" -> streakCount >= 365
            else -> false
        }
    }

    private fun checkCollectionAchievement(achievement: Achievement, unlockedItems: List<Item>): Boolean {
        return when (achievement.id) {
            "collection_10" -> unlockedItems.size >= 10
            "collection_rare_5" -> unlockedItems.count {
                it.itemTier == ItemTier.RARE || it.itemTier == ItemTier.EPIC || it.itemTier == ItemTier.LEGENDARY
            } >= 5
            "collection_legendary_1" -> unlockedItems.any { it.itemTier == ItemTier.LEGENDARY }
            else -> false
        }
    }

    private fun checkComboAchievement(achievement: Achievement, activeCombos: List<Combo>): Boolean {
        return when (achievement.id) {
            "combo_1" -> activeCombos.size >= 1
            "combo_3" -> activeCombos.size >= 3
            "combo_5" -> activeCombos.size >= 5
            else -> false
        }
    }

    private fun checkTimeAchievement(achievement: Achievement, totalMinutes: Int): Boolean {
        return when (achievement.id) {
            "time_100" -> totalMinutes >= 100 * 60
            "time_500" -> totalMinutes >= 500 * 60
            "time_1000" -> totalMinutes >= 1000 * 60
            else -> false
        }
    }

    private fun checkGoldAchievement(achievement: Achievement, totalGold: Int): Boolean {
        return when (achievement.id) {
            "gold_1000" -> totalGold >= 1000
            "gold_10000" -> totalGold >= 10000
            else -> false
        }
    }
}
