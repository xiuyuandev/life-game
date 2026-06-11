package com.lifeup.app.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lifeup.app.ui.character.CharacterScreen
import com.lifeup.app.ui.onboarding.OnboardingScreen
import com.lifeup.app.ui.profile.ProfileScreen
import com.lifeup.app.ui.skills.CreateSkillScreen
import com.lifeup.app.ui.skills.SkillDetailScreen
import com.lifeup.app.ui.skills.SkillsScreen
import com.lifeup.app.ui.today.TodayScreen

@Composable
fun LifeUpNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Today.route) {
            TodayScreen(
                onNavigateToTimer = { skillId ->
                    navController.navigate(Screen.Timer.createRoute(skillId))
                },
                onNavigateToCreateSkill = {
                    navController.navigate(Screen.CreateSkill.route)
                }
            )
        }

        composable(Screen.Skills.route) {
            SkillsScreen(
                onNavigateToDetail = { skillId ->
                    navController.navigate(Screen.SkillDetail.createRoute(skillId))
                },
                onNavigateToCreateSkill = {
                    navController.navigate(Screen.CreateSkill.route)
                },
                onNavigateToTimer = { skillId ->
                    navController.navigate(Screen.Timer.createRoute(skillId))
                }
            )
        }

        composable(
            route = Screen.SkillDetail.route,
            arguments = listOf(
                navArgument("skillId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val skillId = backStackEntry.arguments?.getLong("skillId") ?: 0L
            SkillDetailScreen(
                skillId = skillId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToTimer = { id ->
                    navController.navigate(Screen.Timer.createRoute(id))
                }
            )
        }

        composable(Screen.CreateSkill.route) {
            CreateSkillScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Character.route) {
            CharacterScreen()
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToReview = { navController.navigate(Screen.Review.route) },
                onNavigateToSettings = { /* placeholder */ }
            )
        }

        composable(Screen.Review.route) {
            Text("回顾")
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Today.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                },
                onNavigateToTimer = { skillId ->
                    navController.navigate(Screen.Timer.createRoute(skillId))
                }
            )
        }

        composable(
            route = Screen.Timer.route,
            arguments = listOf(
                navArgument("skillId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val skillId = backStackEntry.arguments?.getLong("skillId") ?: 0L
            Text("计时器 - 技能ID: $skillId")
        }
    }
}
