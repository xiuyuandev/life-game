package com.lifeup.app.ui.navigation

sealed class Screen(val route: String) {
    // 主导航
    data object Adventure : Screen("adventure")
    data object SkillMap : Screen("skill_map")
    data object Ledger : Screen("ledger")
    data object Settings : Screen("settings")

    // 详情/子页面（隐藏底部导航）
    data object SkillDetail : Screen("skill/{id}") {
        fun createRoute(id: Long) = "skill/$id"
    }
    data object SessionDetail : Screen("session/{id}") {
        fun createRoute(id: Long) = "session/$id"
    }
    data object Equipment : Screen("equipment")
    data object Achievements : Screen("achievements")
    data object CharacterCreate : Screen("character_create")
    data object SessionHistory : Screen("session_history")
}
