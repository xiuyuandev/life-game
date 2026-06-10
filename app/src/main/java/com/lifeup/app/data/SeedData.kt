package com.lifeup.app.data

import com.lifeup.app.data.db.entity.AchievementEntity
import com.lifeup.app.data.db.entity.EquipmentEntity
import com.lifeup.app.data.db.entity.SkillEntity

object SeedData {

    val defaultSkills = listOf(
        // 职业技能
        SkillEntity(
            name = "编程",
            category = "professional",
            icon = "💻",
            description = "软件开发与编程能力",
            attributeContribution = "{\"intelligence\":2}"
        ),
        SkillEntity(
            name = "英语",
            category = "language",
            icon = "🗣️",
            description = "英语听说读写",
            attributeContribution = "{\"intelligence\":1,\"charm\":1}"
        ),
        SkillEntity(
            name = "数据分析",
            category = "professional",
            icon = "📊",
            description = "数据思维与分析能力",
            parentSkillId = 1,
            parentLevelRequired = 3,
            unlocked = false,
            attributeContribution = "{\"intelligence\":2}"
        ),
        SkillEntity(
            name = "Web开发",
            category = "professional",
            icon = "🌐",
            description = "全栈Web开发",
            parentSkillId = 1,
            parentLevelRequired = 5,
            unlocked = false,
            attributeContribution = "{\"intelligence\":2}"
        ),
        // 运动技能
        SkillEntity(
            name = "跑步",
            category = "sport",
            icon = "🏃",
            description = "跑步耐力与速度",
            attributeContribution = "{\"strength\":2}"
        ),
        SkillEntity(
            name = "力量训练",
            category = "sport",
            icon = "💪",
            description = "力量与肌肉训练",
            attributeContribution = "{\"strength\":2}"
        ),
        // 艺术技能
        SkillEntity(
            name = "写作",
            category = "art",
            icon = "✍️",
            description = "文字表达与创作",
            attributeContribution = "{\"charm\":2}"
        ),
        // 生活技能
        SkillEntity(
            name = "冥想",
            category = "life",
            icon = "🧘",
            description = "正念冥想与内心平静",
            attributeContribution = "{\"constitution\":1,\"intelligence\":1}"
        ),
        SkillEntity(
            name = "烹饪",
            category = "life",
            icon = "🍳",
            description = "烹饪与健康饮食",
            attributeContribution = "{\"constitution\":1,\"charm\":1}"
        )
    )

    val defaultEquipment = listOf(
        // 习惯槽
        EquipmentEntity(
            name = "早起仪式",
            description = "每日首次计时经验+20%",
            icon = "🌅",
            slot = "habit",
            effectType = "first_daily_bonus",
            effectValue = 0.2,
            effectTarget = "all",
            maxDurability = 30,
            currentDurability = 30,
            maintenanceActivity = "planning",
            source = "shop",
            price = 300
        ),
        EquipmentEntity(
            name = "运动习惯",
            description = "运动类活动经验+15%",
            icon = "🏃",
            slot = "habit",
            effectType = "exp_multiplier",
            effectValue = 0.15,
            effectTarget = "sport",
            maxDurability = 30,
            currentDurability = 30,
            maintenanceActivity = "exercise",
            source = "skill_unlock",
            price = 0
        ),
        // 工具槽
        EquipmentEntity(
            name = "深度专注",
            description = "连续计时>45分钟额外+10%",
            icon = "🎯",
            slot = "tool",
            effectType = "long_session_bonus",
            effectValue = 0.1,
            effectTarget = "45",
            maxDurability = 30,
            currentDurability = 30,
            maintenanceActivity = "study",
            source = "shop",
            price = 500
        ),
        EquipmentEntity(
            name = "番茄工作法",
            description = "每完成一个25分钟时段额外+5%经验",
            icon = "🍅",
            slot = "tool",
            effectType = "exp_multiplier",
            effectValue = 0.05,
            effectTarget = "all",
            maxDurability = 30,
            currentDurability = 30,
            maintenanceActivity = "coding",
            source = "shop",
            price = 200
        ),
        // 心态槽
        EquipmentEntity(
            name = "成长思维",
            description = "所有活动经验+8%",
            icon = "🌱",
            slot = "mindset",
            effectType = "exp_multiplier",
            effectValue = 0.08,
            effectTarget = "all",
            maxDurability = 60,
            currentDurability = 60,
            maintenanceActivity = "reading",
            source = "achievement_reward",
            price = 0
        ),
        EquipmentEntity(
            name = "坚韧意志",
            description = "连续天数每7天+3%经验",
            icon = "💎",
            slot = "mindset",
            effectType = "streak_bonus",
            effectValue = 0.03,
            effectTarget = "7",
            maxDurability = 60,
            currentDurability = 60,
            maintenanceActivity = "meditation",
            source = "skill_unlock",
            price = 0
        ),
        // 环境槽
        EquipmentEntity(
            name = "整洁空间",
            description = "每日精力恢复+2",
            icon = "✨",
            slot = "environment",
            effectType = "sp_recovery",
            effectValue = 2.0,
            effectTarget = "",
            maxDurability = 14,
            currentDurability = 14,
            maintenanceActivity = "cooking",
            source = "shop",
            price = 150
        )
    )

    val defaultAchievements = listOf(
        // 时间投资
        AchievementEntity(
            title = "时间新手",
            description = "累计投资10小时",
            icon = "⏰",
            category = "time_invested",
            conditionType = "TOTAL_INVESTED_HOURS",
            conditionValue = 10,
            rewardExp = 50,
            rewardGold = 20
        ),
        AchievementEntity(
            title = "时间行者",
            description = "累计投资100小时",
            icon = "⏳",
            category = "time_invested",
            conditionType = "TOTAL_INVESTED_HOURS",
            conditionValue = 100,
            rewardExp = 500,
            rewardGold = 200,
            isMilestone = true
        ),
        AchievementEntity(
            title = "时间大师",
            description = "累计投资1000小时",
            icon = "⌛",
            category = "time_invested",
            conditionType = "TOTAL_INVESTED_HOURS",
            conditionValue = 1000,
            rewardExp = 5000,
            rewardGold = 2000,
            isMilestone = true
        ),
        // 技能精通
        AchievementEntity(
            title = "初窥门径",
            description = "任意技能达到Lv5",
            icon = "📖",
            category = "skill_mastery",
            conditionType = "ANY_SKILL_LEVEL",
            conditionValue = 5,
            rewardExp = 100,
            rewardGold = 50
        ),
        AchievementEntity(
            title = "登堂入室",
            description = "任意技能达到Lv10",
            icon = "📚",
            category = "skill_mastery",
            conditionType = "ANY_SKILL_LEVEL",
            conditionValue = 10,
            rewardExp = 500,
            rewardGold = 200,
            isMilestone = true
        ),
        AchievementEntity(
            title = "融会贯通",
            description = "任意技能达到Lv20",
            icon = "🎓",
            category = "skill_mastery",
            conditionType = "ANY_SKILL_LEVEL",
            conditionValue = 20,
            rewardExp = 5000,
            rewardGold = 2000,
            isMilestone = true
        ),
        // 连续
        AchievementEntity(
            title = "七日之约",
            description = "连续7天投资时间",
            icon = "🔥",
            category = "streak",
            conditionType = "STREAK_DAYS",
            conditionValue = 7,
            rewardExp = 200,
            rewardGold = 100
        ),
        AchievementEntity(
            title = "月度坚持",
            description = "连续30天投资时间",
            icon = "🌟",
            category = "streak",
            conditionType = "STREAK_DAYS",
            conditionValue = 30,
            rewardExp = 1000,
            rewardGold = 500,
            isMilestone = true
        ),
        AchievementEntity(
            title = "百日筑基",
            description = "连续100天投资时间",
            icon = "💎",
            category = "streak",
            conditionType = "STREAK_DAYS",
            conditionValue = 100,
            rewardExp = 10000,
            rewardGold = 5000,
            isMilestone = true
        ),
        // 多样性
        AchievementEntity(
            title = "多面手",
            description = "3个不同技能达到Lv3",
            icon = "🎭",
            category = "diversity",
            conditionType = "SKILL_COUNT_AT_LEVEL",
            conditionValue = 3,
            rewardExp = 300,
            rewardGold = 150
        ),
        // 投资比率
        AchievementEntity(
            title = "时间管家",
            description = "投资比率>70%持续7天",
            icon = "📊",
            category = "time_invested",
            conditionType = "INVESTMENT_RATIO",
            conditionValue = 7,
            rewardExp = 200,
            rewardGold = 100
        ),
        // 角色
        AchievementEntity(
            title = "初出茅庐",
            description = "角色达到Lv10",
            icon = "⚔️",
            category = "life_event",
            conditionType = "CHARACTER_LEVEL",
            conditionValue = 10,
            rewardExp = 500,
            rewardGold = 200
        ),
        AchievementEntity(
            title = "小有所成",
            description = "角色达到Lv20",
            icon = "🛡️",
            category = "life_event",
            conditionType = "CHARACTER_LEVEL",
            conditionValue = 20,
            rewardExp = 2000,
            rewardGold = 1000,
            isMilestone = true
        )
    )

    val activityToSkillCategory = mapOf(
        "coding" to "professional",
        "study" to "professional",
        "reading" to "professional",
        "language" to "language",
        "exercise" to "sport",
        "art" to "art",
        "social" to "social",
        "cooking" to "life",
        "meditation" to "life",
        "planning" to "life",
        "other_invest" to "general"
    )

    val investmentActivities = setOf(
        "coding", "study", "reading", "language", "exercise",
        "art", "social", "cooking", "meditation", "planning", "other_invest"
    )

    val consumableActivities = setOf(
        "scrolling", "gaming", "idle", "commute", "other_consume"
    )

    val activityNames = mapOf(
        "coding" to "💻 编程",
        "study" to "📚 学习",
        "reading" to "📖 阅读",
        "language" to "🗣️ 语言",
        "exercise" to "🏃 运动",
        "art" to "🎨 艺术",
        "social" to "🤝 社交",
        "cooking" to "🍳 烹饪",
        "meditation" to "🧘 冥想",
        "planning" to "📝 规划",
        "scrolling" to "📱 刷手机",
        "gaming" to "🎮 玩游戏",
        "idle" to "💤 发呆",
        "commute" to "🚌 通勤",
        "other_invest" to "✨ 其他(投资)",
        "other_consume" to "💨 其他(消耗)"
    )

    val categoryNames = mapOf(
        "professional" to "职业",
        "language" to "语言",
        "sport" to "运动",
        "art" to "艺术",
        "life" to "生活",
        "social" to "社交",
        "general" to "通用"
    )
}
