package com.lifeup.app.domain.model

import com.lifeup.app.data.db.SkillCategory

/**
 * 12 只内置心魔 + 终章镜像的静态数据。
 *
 * 分类克制逻辑：
 *  - PROCRASTINATION 章（拖延）惧怕「谋生 / 心智」类训练（结构化、目标导向的练习）
 *  - DISTRACTION 章（分心）惧怕「体能 / 生活」类训练（需要切出 App 的活动）
 *  - RHYTHM 章（作息）惧怕「体能 / 生活」类训练（晨跑、整理皆为强干预）
 *  - PSYCHE 章（心理）惧怕「社交 / 艺术」类训练（表达型活动能重塑情绪）
 *
 * 终章镜像（MIRROR_OF_SELF）对所有分类中庸，不克制、亦不抵抗，
 * 纯由"完美完成一周七日循环"作为终极胜利条件。
 */
object DemonTemplate {

    val PROCRASTINATION_SERPENT = InnerDemon(
        id = DemonId.PROCRASTINATION_SERPENT,
        chapter = DemonChapter.PROCRASTINATION,
        displayName = "拖延之蛇",
        shortDescription = "它用'再等五分钟'缠住你的脚踝。",
        story = """
            它没有头颅，因为它的头就是那个"等下再说"的声音。
            每当你打开手机，它就从你拖延的缝隙里探出身体，
            用"先刷一会儿""还来得及"这类细密鳞片把你的决心一寸寸绞紧。
            击败它不需要勇气，只需要——开始。
        """.trimIndent(),
        colorHex = "#5B6B7A",
        accentColorHex = "#A6B5C4",
        emoji = "🐍",
        artShape = DemonArtShape.SERPENT,
        weakCategories = listOf(SkillCategory.LIVELIHOOD, SkillCategory.MENTAL),
        resistCategories = listOf(SkillCategory.SOCIAL),
        recommendedCategories = listOf(SkillCategory.LIVELIHOOD),
        basePartHps = listOf(120, 100, 140, 120, 100, 80, 100),
        minFocusMinutes = 25,
        unlock = DemonUnlockKey.INSTANT_START,
        rewardGold = 80,
        difficulty = 1,
        reflectionPrompt = "你通常会在开始前想些什么？那是真的顾虑，还是它在低语？"
    )

    val LAST_MINUTE_SHADOW = InnerDemon(
        id = DemonId.LAST_MINUTE_SHADOW,
        chapter = DemonChapter.PROCRASTINATION,
        displayName = "最后五分钟之影",
        shortDescription = "它让你相信 23:59 才开始也来得及。",
        story = """
            它比秒针更细，比夜里的暗更黑。
            它的拿手好戏是：把"还剩 5 分钟"放大成"还很充裕"。
            当你抬头看钟，已经是凌晨三点，而它正笑嘻嘻地收拾它的战绩。
        """.trimIndent(),
        colorHex = "#3B3F4E",
        accentColorHex = "#7B7E8C",
        emoji = "⏱",
        artShape = DemonArtShape.SHADOW,
        weakCategories = listOf(SkillCategory.MENTAL, SkillCategory.LIVELIHOOD),
        resistCategories = listOf(SkillCategory.PHYSICAL),
        recommendedCategories = listOf(SkillCategory.MENTAL),
        basePartHps = listOf(140, 120, 120, 140, 100, 100, 80),
        minFocusMinutes = 45,
        unlock = DemonUnlockKey.DEADLINE_REMINDER,
        rewardGold = 110,
        difficulty = 2,
        reflectionPrompt = "你上一次低估了任务所需的时间，是哪一次？"
    )

    val EXCUSE_FOG = InnerDemon(
        id = DemonId.EXCUSE_FOG,
        chapter = DemonChapter.PROCRASTINATION,
        displayName = "借口之雾",
        shortDescription = "它把每一个'今天不行'都包成棉花糖。",
        story = """
            它很温柔。它从不说"放弃"，只说"今天状态不好"。
            它会递给你一杯热饮，让你坐在沙发上，让浓雾把今天轻轻遮住。
            你醒来的时候，发现一周都泡在雾里。
        """.trimIndent(),
        colorHex = "#7E8F9C",
        accentColorHex = "#C2D0DC",
        emoji = "🌫",
        artShape = DemonArtShape.FOG,
        weakCategories = listOf(SkillCategory.LIVELIHOOD, SkillCategory.MENTAL),
        resistCategories = listOf(SkillCategory.SOCIAL),
        recommendedCategories = listOf(SkillCategory.LIVELIHOOD),
        basePartHps = listOf(100, 100, 120, 120, 140, 120, 100),
        minFocusMinutes = 25,
        unlock = DemonUnlockKey.CUSTOM_THEMES,
        rewardGold = 130,
        difficulty = 2,
        reflectionPrompt = "你常用的那一句'今天不行'，最常出现在什么场景？"
    )

    val PHONE_HORDE = InnerDemon(
        id = DemonId.PHONE_HORDE,
        chapter = DemonChapter.DISTRACTION,
        displayName = "手机群怪",
        shortDescription = "它分裂成几十个应用图标，齐刷刷朝你眨眼。",
        story = """
            它由一个又一个图标堆叠而成：聊天、购物、短视频、小游戏……
            它们彼此挤压、彼此遮蔽，
            形成一座会眨眼的城市，把你的注意力切成碎片撒向虚空。
            拆解它，只需一座专注的孤岛。
        """.trimIndent(),
        colorHex = "#5468FF",
        accentColorHex = "#9AA7FF",
        emoji = "📱",
        artShape = DemonArtShape.HORDE,
        weakCategories = listOf(SkillCategory.PHYSICAL, SkillCategory.LIFE),
        resistCategories = listOf(SkillCategory.SOCIAL),
        recommendedCategories = listOf(SkillCategory.PHYSICAL),
        basePartHps = listOf(140, 120, 140, 120, 100, 100, 100),
        minFocusMinutes = 30,
        unlock = DemonUnlockKey.APP_USAGE_HUD,
        rewardGold = 150,
        difficulty = 3,
        reflectionPrompt = "你最常用的三个 App 是哪三个？它们替谁偷走了你的时间？"
    )

    val NOTIFICATION_TSUNAMI = InnerDemon(
        id = DemonId.NOTIFICATION_TSUNAMI,
        chapter = DemonChapter.DISTRACTION,
        displayName = "通知海啸",
        shortDescription = "它把每一次震动都变成打断你的理由。",
        story = """
            它没有形状，只有声音。
            叮，咚，嗡——
            每一个通知都是一根小钉，把你"当下正在做的事"钉在墙上。
            一千根钉子之后，你的"当下"就碎成屏幕上的玻璃渣。
        """.trimIndent(),
        colorHex = "#1E88E5",
        accentColorHex = "#7AB6F0",
        emoji = "🔔",
        artShape = DemonArtShape.TSUNAMI,
        weakCategories = listOf(SkillCategory.PHYSICAL, SkillCategory.LIFE),
        resistCategories = listOf(SkillCategory.SOCIAL),
        recommendedCategories = listOf(SkillCategory.LIFE),
        basePartHps = listOf(120, 100, 140, 120, 140, 100, 100),
        minFocusMinutes = 30,
        unlock = DemonUnlockKey.DEEP_FOCUS_SHIELD,
        rewardGold = 170,
        difficulty = 3,
        reflectionPrompt = "今天你点开了多少次通知？其中真正重要的有几次？"
    )

    val VIDEO_QUAGMIRE = InnerDemon(
        id = DemonId.VIDEO_QUAGMIRE,
        chapter = DemonChapter.DISTRACTION,
        displayName = "视频泥沼",
        shortDescription = "它让你滑过一个又一个'下一个'，直到世界变黑。",
        story = """
            它是一片会动的沼泽，每一条视频都是一脚泥。
            "再看一条"是它的咒语，让你越陷越深。
            你以为自己在休息，其实你在被它一片一片消化。
        """.trimIndent(),
        colorHex = "#7B1FA2",
        accentColorHex = "#B285D6",
        emoji = "🌀",
        artShape = DemonArtShape.QUAGMIRE,
        weakCategories = listOf(SkillCategory.PHYSICAL, SkillCategory.LIFE),
        resistCategories = listOf(SkillCategory.SOCIAL),
        recommendedCategories = listOf(SkillCategory.LIFE),
        basePartHps = listOf(120, 140, 120, 100, 140, 100, 100),
        minFocusMinutes = 45,
        unlock = DemonUnlockKey.WEEKLY_RECAP,
        rewardGold = 200,
        difficulty = 3,
        reflectionPrompt = "你最想戒掉哪个视频 App？它填补了你内心的哪一块空白？"
    )

    val NIGHT_OWL_SHADOW = InnerDemon(
        id = DemonId.NIGHT_OWL_SHADOW,
        chapter = DemonChapter.RHYTHM,
        displayName = "夜枭之影",
        shortDescription = "它守在你枕边，等你伸手点亮屏幕。",
        story = """
            它是凌晨两点的访客，羽毛无声，眼睛明亮。
            白天它藏起来，夜晚就停在你的床柱上，
            鼓动你"再刷一会儿"。
            它的战绩写在第二天你木然的脸上。
        """.trimIndent(),
        colorHex = "#283593",
        accentColorHex = "#5C6BC0",
        emoji = "🦉",
        artShape = DemonArtShape.OWL,
        weakCategories = listOf(SkillCategory.PHYSICAL, SkillCategory.LIFE),
        resistCategories = listOf(SkillCategory.MENTAL),
        recommendedCategories = listOf(SkillCategory.PHYSICAL),
        basePartHps = listOf(140, 120, 120, 140, 100, 100, 100),
        minFocusMinutes = 30,
        unlock = DemonUnlockKey.NIGHT_MODE_DASHBOARD,
        rewardGold = 220,
        difficulty = 3,
        reflectionPrompt = "你真正入睡前的最后一小时，是怎么度过的？"
    )

    val MORNING_FOG = InnerDemon(
        id = DemonId.MORNING_FOG,
        chapter = DemonChapter.RHYTHM,
        displayName = "晨雾",
        shortDescription = "它把早晨的每一束光都磨成棉花。",
        story = """
            太阳起床的时候，它就坐在窗台上。
            它把"再睡五分钟"包装成柔软的云，让你一直闭眼，
            直到世界错过你，你也错过世界。
        """.trimIndent(),
        colorHex = "#90A4AE",
        accentColorHex = "#CFD8DC",
        emoji = "🌅",
        artShape = DemonArtShape.MIST,
        weakCategories = listOf(SkillCategory.PHYSICAL, SkillCategory.LIFE),
        resistCategories = listOf(SkillCategory.SOCIAL),
        recommendedCategories = listOf(SkillCategory.PHYSICAL),
        basePartHps = listOf(100, 100, 120, 140, 120, 140, 100),
        minFocusMinutes = 25,
        unlock = DemonUnlockKey.MORNING_BOOT,
        rewardGold = 200,
        difficulty = 2,
        reflectionPrompt = "如果你必须在起床后 10 分钟内做一件具体的事，你会选什么？"
    )

    val COUCH_GRAVITY = InnerDemon(
        id = DemonId.COUCH_GRAVITY,
        chapter = DemonChapter.RHYTHM,
        displayName = "沙发重力",
        shortDescription = "它把你按进沙发，让你连抬手的力气都失去。",
        story = """
            它是看不见的、温柔的、黏糊糊的力量。
            它说："休息一下吧。"
            它说："你已经够努力了。"
            它说："运动是明天的事。"
            它把你按进沙发，一按就是几年。
        """.trimIndent(),
        colorHex = "#6D4C41",
        accentColorHex = "#A1887F",
        emoji = "🛋",
        artShape = DemonArtShape.GRAVITY,
        weakCategories = listOf(SkillCategory.PHYSICAL, SkillCategory.LIFE),
        resistCategories = listOf(SkillCategory.MENTAL),
        recommendedCategories = listOf(SkillCategory.PHYSICAL),
        basePartHps = listOf(100, 140, 100, 120, 120, 140, 100),
        minFocusMinutes = 45,
        unlock = DemonUnlockKey.COMBO_TEMPLATES,
        rewardGold = 240,
        difficulty = 3,
        reflectionPrompt = "让你离开沙发的那股力量，通常是什么？"
    )

    val PERFECTION_DRAGON = InnerDemon(
        id = DemonId.PERFECTION_DRAGON,
        chapter = DemonChapter.PSYCHE,
        displayName = "完美巨龙",
        shortDescription = "它的鳞片是'还不够好'，它的吐息是'重做'。",
        story = """
            它盘踞在每一个"未完成"的稿件上方，
            它的鳞片由"还不够好""再改一版""这点小瑕疵"组成。
            它从不让你提交。
            它说：完美是一种姿态。
        """.trimIndent(),
        colorHex = "#C62828",
        accentColorHex = "#EF5350",
        emoji = "🐉",
        artShape = DemonArtShape.DRAGON,
        weakCategories = listOf(SkillCategory.SOCIAL, SkillCategory.ART),
        resistCategories = listOf(SkillCategory.MENTAL),
        recommendedCategories = listOf(SkillCategory.ART),
        basePartHps = listOf(160, 140, 140, 120, 120, 100, 140),
        minFocusMinutes = 60,
        unlock = DemonUnlockKey.EXPORT_RAW,
        rewardGold = 320,
        difficulty = 4,
        reflectionPrompt = "如果只能交出 60 分的成果，你会害怕发生什么？"
    )

    val ANXIETY_CLOUD = InnerDemon(
        id = DemonId.ANXIETY_CLOUD,
        chapter = DemonChapter.PSYCHE,
        displayName = "焦虑之云",
        shortDescription = "它没有脚，却能压住你的胸膛。",
        story = """
            它不需要理由。
            它只是在你的胸口住下，让你每一次呼吸都带着"万一"的尾音。
            它的雨点就是无数个"如果"。
            你的每一滴专注都在它的雨里被打湿。
        """.trimIndent(),
        colorHex = "#5E35B1",
        accentColorHex = "#9575CD",
        emoji = "☁",
        artShape = DemonArtShape.CLOUD,
        weakCategories = listOf(SkillCategory.SOCIAL, SkillCategory.ART),
        resistCategories = listOf(SkillCategory.MENTAL),
        recommendedCategories = listOf(SkillCategory.SOCIAL),
        basePartHps = listOf(140, 140, 120, 120, 140, 100, 100),
        minFocusMinutes = 30,
        unlock = DemonUnlockKey.HABIT_CHAINS,
        rewardGold = 320,
        difficulty = 4,
        reflectionPrompt = "你胸口的那片云，通常被什么吹散？"
    )

    val NIHILISM_SHADOW = InnerDemon(
        id = DemonId.NIHILISM_SHADOW,
        chapter = DemonChapter.PSYCHE,
        displayName = "虚无之影",
        shortDescription = "它说'这一切都没有意义'。",
        story = """
            它最安静，也最难缠。
            它从不吵闹，只在你耳语："又一天。"
            它让一切颜色褪成灰。
            它不杀你，它只是让你不再相信"做点什么"是有用的。
        """.trimIndent(),
        colorHex = "#212121",
        accentColorHex = "#616161",
        emoji = "🕳",
        artShape = DemonArtShape.NIHILISM,
        weakCategories = listOf(SkillCategory.SOCIAL, SkillCategory.ART),
        resistCategories = listOf(SkillCategory.LIVELIHOOD),
        recommendedCategories = listOf(SkillCategory.ART),
        basePartHps = listOf(140, 120, 140, 120, 120, 100, 100),
        minFocusMinutes = 60,
        unlock = DemonUnlockKey.FOCUS_LEDGER,
        rewardGold = 360,
        difficulty = 5,
        reflectionPrompt = "当你怀疑"这一切没有意义"时，是哪些事被盖上阴影？"
    )

    val MIRROR_OF_SELF = InnerDemon(
        id = DemonId.MIRROR_OF_SELF,
        chapter = DemonChapter.FINAL,
        displayName = "自我之镜",
        shortDescription = "你凝视深渊，深渊也在凝视你。",
        story = """
            终章。
            当你击败了所有心魔，你会发现最后一个敌人，
            不是别人。
            它是你的"另一个版本"：
            那个想放弃、想偷懒、想逃避、想完美的你。
            它没有招式，你也没有。
            这一战，只需要你愿意继续活下去。
        """.trimIndent(),
        colorHex = "#FFFFFF",
        accentColorHex = "#CFD8DC",
        emoji = "🪞",
        artShape = DemonArtShape.MIRROR,
        weakCategories = emptyList(),
        resistCategories = emptyList(),
        recommendedCategories = listOf(SkillCategory.MENTAL),
        basePartHps = listOf(200, 200, 200, 200, 200, 200, 200),
        minFocusMinutes = 90,
        unlock = DemonUnlockKey.DEEP_FOCUS_SHIELD,
        rewardGold = 0,
        difficulty = 5,
        reflectionPrompt = "写下你想成为的那个人，最具体的一个动作。"
    )

    /** 全部 12 + 1 个心魔 */
    val ALL: List<InnerDemon> = listOf(
        PROCRASTINATION_SERPENT,
        LAST_MINUTE_SHADOW,
        EXCUSE_FOG,
        PHONE_HORDE,
        NOTIFICATION_TSUNAMI,
        VIDEO_QUAGMIRE,
        NIGHT_OWL_SHADOW,
        MORNING_FOG,
        COUCH_GRAVITY,
        PERFECTION_DRAGON,
        ANXIETY_CLOUD,
        NIHILISM_SHADOW,
        MIRROR_OF_SELF
    )

    /** 只包含 12 只"标准"心魔（不含终章镜像） */
    val STANDARD: List<InnerDemon> = ALL.filter { it.id != DemonId.MIRROR_OF_SELF }

    fun byId(id: DemonId): InnerDemon =
        ALL.firstOrNull { it.id == id } ?: error("Unknown demon id: $id")

    fun byKey(key: String): InnerDemon? {
        val parsed = DemonId.fromKey(key) ?: return null
        return ALL.firstOrNull { it.id == parsed }
    }

    fun byChapter(chapter: DemonChapter): List<InnerDemon> =
        ALL.filter { it.chapter == chapter }

    /** 把 7 个 part 索引（周一~周日）映射到身体部位名称 */
    val PART_NAMES: List<String> = listOf("头", "颈", "胸", "腹", "背", "尾", "翼")

    /** 击败全部 12 个标准心魔后才能解锁镜像 */
    fun isMirrorUnlocked(defeatedIds: Set<DemonId>): Boolean =
        STANDARD.all { it.id in defeatedIds }
}
