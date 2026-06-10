package com.lifeup.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.lifeup.app.ui.adventure.AdventureScreen
import com.lifeup.app.ui.ledger.LedgerScreen
import com.lifeup.app.ui.settings.AchievementsScreen
import com.lifeup.app.ui.settings.CharacterCreateScreen
import com.lifeup.app.ui.settings.EquipmentScreen
import com.lifeup.app.ui.settings.SessionHistoryScreen
import com.lifeup.app.ui.settings.SettingsScreen
import com.lifeup.app.ui.skill.SkillDetailScreen
import com.lifeup.app.ui.skill.SkillMapScreen
import com.lifeup.app.ui.theme.PixelColors

@Composable
fun LifeUpNavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem.Adventure,
        BottomNavItem.SkillMap,
        BottomNavItem.Ledger,
        BottomNavItem.Settings
    )

    val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            ambientColor = PixelColors.DeepSpace,
                            spotColor = PixelColors.DeepSpace
                        )
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    containerColor = PixelColors.Surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PixelColors.AccentGold,
                                selectedTextColor = PixelColors.AccentGold,
                                unselectedIconColor = PixelColors.TextMuted,
                                unselectedTextColor = PixelColors.TextMuted,
                                indicatorColor = PixelColors.AccentGold.copy(alpha = 0.12f)
                            ),
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.CharacterCreate.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.CharacterCreate.route) {
                CharacterCreateScreen(
                    onCharacterCreated = {
                        navController.navigate(Screen.Adventure.route) {
                            popUpTo(Screen.CharacterCreate.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Adventure.route) {
                AdventureScreen(
                    onNavigateToSkill = { skillId ->
                        navController.navigate(Screen.SkillDetail.createRoute(skillId))
                    }
                )
            }

            composable(Screen.SkillMap.route) {
                SkillMapScreen(
                    onSkillClick = { skillId ->
                        navController.navigate(Screen.SkillDetail.createRoute(skillId))
                    }
                )
            }

            composable(
                route = Screen.SkillDetail.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val skillId = backStackEntry.arguments?.getLong("id") ?: 0L
                SkillDetailScreen(
                    skillId = skillId,
                    onBack = { navController.popBackStack() },
                    onStartSession = {
                        navController.navigate(Screen.Adventure.route) {
                            popUpTo(Screen.SkillMap.route) { inclusive = false }
                        }
                    }
                )
            }

            composable(Screen.Ledger.route) {
                LedgerScreen()
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToEquipment = {
                        navController.navigate(Screen.Equipment.route)
                    },
                    onNavigateToAchievements = {
                        navController.navigate(Screen.Achievements.route)
                    },
                    onNavigateToHistory = {
                        navController.navigate(Screen.SessionHistory.route)
                    },
                    onResetCharacter = {
                        navController.navigate(Screen.CharacterCreate.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Equipment.route) {
                EquipmentScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Achievements.route) {
                AchievementsScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.SessionHistory.route) {
                SessionHistoryScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    data object Adventure : BottomNavItem(Screen.Adventure.route, Icons.Default.Explore, "冒险")
    data object SkillMap : BottomNavItem(Screen.SkillMap.route, Icons.Default.Map, "技能")
    data object Ledger : BottomNavItem(Screen.Ledger.route, Icons.Default.BarChart, "账本")
    data object Settings : BottomNavItem(Screen.Settings.route, Icons.Default.Settings, "设置")
}
