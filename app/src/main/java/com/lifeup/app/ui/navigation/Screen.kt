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
    data object Onboarding : Screen("onboarding", "引导")
    data object Timer : Screen("timer/{skillId}", "计时器") {
        fun createRoute(skillId: Long): String = "timer/$skillId"
    }
}
