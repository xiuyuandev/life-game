package com.lifeup.app.ui.navigation

sealed class Screen(val route: String, val title: String) {
    data object Today : Screen("today", "今日")
    data object Skills : Screen("skills", "技能")
    data object SkillDetail : Screen("skill/{skillId}", "技能详情") {
        fun createRoute(skillId: Long): String = "skill/$skillId"
    }
    data object CreateSkill : Screen("create_skill", "创建技能")
    data object Character : Screen("character", "角色馆")
    data object Profile : Screen("profile", "我的")
    data object Review : Screen("review", "回顾")
    data object Splash : Screen("splash", "启动页")
    data object Onboarding : Screen("onboarding", "引导")
    data object Achievement : Screen("achievement", "成就墙")
    data object Retroactive : Screen("retroactive", "补录时间")
    data object Timer : Screen("timer/{skillId}", "计时器") {
        fun createRoute(skillId: Long): String = "timer/$skillId"
    }
    data object Shop : Screen("shop", "金币商店")
    data object Combo : Screen("combo", "技能组合")
    data object Showcase : Screen("showcase", "技能图鉴")
    data object Stats : Screen("stats", "技能统计")
    data object Ledger : Screen("ledger", "时间账本")
    data object Backup : Screen("backup", "数据管理")
    data object About : Screen("about", "关于")
    data object Settings : Screen("settings", "偏好设置")
    data object PrivacyPolicy : Screen("privacy_policy", "隐私政策")
}
