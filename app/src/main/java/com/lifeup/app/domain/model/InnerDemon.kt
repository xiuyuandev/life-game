package com.lifeup.app.domain.model

import androidx.compose.ui.graphics.Color

/**
 * 战斗中伤害的分类克制系数。
 * 当玩家训练的 [com.lifeup.app.data.db.SkillCategory] 命中"克制"分类时，
 * 伤害乘以 [MULTIPLIER_WEAK]；命中"抵抗"分类时，乘以 [MULTIPLIER_RESIST]；否则为 1.0。
 */
object DemonTypeMultiplier {
    const val MULTIPLIER_WEAK: Float = 1.6f
    const val MULTIPLIER_RESIST: Float = 0.55f
    const val MULTIPLIER_NEUTRAL: Float = 1.0f
}

/**
 * 击败心魔后，玩家在真实应用中获得的"能力解锁"。
 * 每一个都对应一个真实落地的 Android 能力或界面（例如：1 秒极速启动）。
 *
 * `key` 是稳定的偏好键；具体实现落在 SettingsPrefs 与各 ViewModel 中。
 */
enum class DemonUnlockKey(
    val key: String,
    val title: String,
    val description: String,
    val emoji: String
) {
    INSTANT_START("unlock_instant_start", "1 秒极速启动", "点一下技能卡片直接开始计时，跳过确认。", "⚡"),
    DEADLINE_REMINDER("unlock_deadline_reminder", "死线提醒", "为技能设定死线；提前 24h/6h/1h 自动推送。", "⏰"),
    APP_USAGE_HUD("unlock_app_usage_hud", "应用使用叠层", "在计时进行中，悬浮显示今日已专注分钟数。", "📊"),
    FOCUS_LEDGER("unlock_focus_ledger", "时间账本", "解锁'账本'页面，支持自定义分类与日 / 周 / 月视图。", "📒"),
    NIGHT_MODE_DASHBOARD("unlock_night_dashboard", "夜间仪表盘", "降低 22:00 后界面亮度与饱和度，保护节律。", "🌙"),
    MORNING_BOOT("unlock_morning_boot", "清晨启动卡", "首次打开应用推送今日 3 个核心技能，5 秒开练。", "🌅"),
    WEEKLY_RECAP("unlock_weekly_recap", "周回顾", "每周日生成上 7 天的胜负表与心魔击杀数。", "📅"),
    CUSTOM_THEMES("unlock_custom_themes", "自定义主题", "解锁定制色板、强调色、字体大小。", "🎨"),
    COMBO_TEMPLATES("unlock_combo_templates", "组合模板", "把多个技能打包成'早间仪式''深度工作'一键启动。", "🧩"),
    HABIT_CHAINS("unlock_habit_chains", "习惯链", "把'技能 A → 技能 B → 技能 C'做成锁链，断一节即失败。", "🔗"),
    EXPORT_RAW("unlock_export_raw", "原始数据导出", "把时间记录、击杀、能耗导出为 JSON / CSV。", "📤"),
    DEEP_FOCUS_SHIELD("unlock_deep_focus_shield", "深度专注盾", "在长时段 (>60 分钟) 计时中关闭所有通知与快捷开关。", "🛡")
}

/**
 * 一只"心魔"的静态领域模型。
 *
 * 心魔是"拖延 / 分心 / 失律 / 心理"四章 + 终章镜像 中的一员。
 * 一只标准心魔 = 7 个身体部位（头 / 颈 / 胸 / 腹 / 背 / 尾 / 翼），
 * 每个身体部位对应一周中的一天，只能在该日被攻击。
 *
 * 该模型是不可变模板，运行时的"被攻击进度"由
 * [com.lifeup.app.data.db.entity.DemonProgressEntity] 承担。
 */
data class InnerDemon(
    val id: DemonId,
    val chapter: DemonChapter,
    /** 短标题 */
    val displayName: String,
    /** 单行描述（用于卡片副标题） */
    val shortDescription: String,
    /** 背景故事，2~3 段中文叙述 */
    val story: String,
    /** 主色（用于卡片背景 / 进度条） */
    val colorHex: String,
    /** 副色（用于强调 / 描边） */
    val accentColorHex: String,
    /** 卡片 emoji 角标 */
    val emoji: String,
    /** 心魔外形类别（用于 Compose 矢量绘制的图形选择） */
    val artShape: DemonArtShape,
    /** 弱点的技能分类列表：命中时伤害 ×1.6 */
    val weakCategories: List<com.lifeup.app.data.db.SkillCategory>,
    /** 抵抗的技能分类列表：命中时伤害 ×0.55 */
    val resistCategories: List<com.lifeup.app.data.db.SkillCategory>,
    /** 推荐技能分类（用于新手提示） */
    val recommendedCategories: List<com.lifeup.app.data.db.SkillCategory>,
    /** 7 个身体部位的基准 HP（头 / 颈 / 胸 / 腹 / 背 / 尾 / 翼） */
    val basePartHps: List<Int>,
    /** 攻击前置条件（最低专注时长，单位：分钟） */
    val minFocusMinutes: Int,
    /** 解锁内容（击败后真实生效） */
    val unlock: DemonUnlockKey,
    /** 击败后奖励金币（用于表现层计算） */
    val rewardGold: Int,
    /** 难度等级 1~5，影响伤害系数 */
    val difficulty: Int,
    /** 击败后反思引导（一句开放式问题） */
    val reflectionPrompt: String
) {
    val totalHp: Int get() = basePartHps.sum()

    val color: Color get() = parseColor(colorHex)
    val accent: Color get() = parseColor(accentColorHex)

    companion object {
        fun parseColor(hex: String): Color {
            val v = hex.removePrefix("#")
            val longVal = v.toLong(16)
            return when (v.length) {
                6 -> Color(0xFF000000 or longVal)
                8 -> Color(longVal)
                else -> Color(0xFF888888)
            }
        }
    }
}

/**
 * 心魔的矢量艺术形状类别。
 * 实际绘制时由 [com.lifeup.app.ui.demon.DemonPortrait] 根据该枚举选择对应的 path 组合。
 */
enum class DemonArtShape {
    SERPENT,        // 蛇形
    SHADOW,         // 抽象阴影
    FOG,            // 雾团
    HORDE,          // 多眼怪物
    TSUNAMI,        // 水波
    QUAGMIRE,       // 漩涡
    OWL,            // 夜枭
    MIST,           // 晨雾
    GRAVITY,        // 重力漩涡
    DRAGON,         // 巨龙
    CLOUD,          // 阴云
    NIHILISM,       // 虚无黑洞
    MIRROR          // 镜像
}
