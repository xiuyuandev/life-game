package com.lifeup.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.lifeup.app.ui.achievement.AchievementScreen
import com.lifeup.app.ui.about.AboutScreen
import com.lifeup.app.ui.about.PrivacyPolicyScreen
import com.lifeup.app.ui.backup.BackupScreen
import com.lifeup.app.ui.character.CharacterScreen
import com.lifeup.app.ui.combo.ComboScreen
import com.lifeup.app.ui.ledger.LedgerScreen
import com.lifeup.app.ui.onboarding.OnboardingScreen
import com.lifeup.app.ui.profile.ProfileScreen
import com.lifeup.app.ui.retroactive.RetroactiveScreen
import com.lifeup.app.ui.review.ReviewScreen
import com.lifeup.app.ui.shop.ShopScreen
import com.lifeup.app.ui.showcase.ShowcaseScreen
import com.lifeup.app.ui.skills.CreateSkillScreen
import com.lifeup.app.ui.skills.SkillDetailScreen
import com.lifeup.app.ui.skills.SkillsScreen
import com.lifeup.app.ui.stats.StatsScreen
import com.lifeup.app.ui.timer.TimerScreen
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
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(
            Screen.Today.route,
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) },
            popEnterTransition = { fadeIn(animationSpec = tween(250)) },
            popExitTransition = { fadeOut(animationSpec = tween(250)) }
        ) {
            TodayScreen(
                onNavigateToTimer = { skillId ->
                    navController.navigate(Screen.Timer.createRoute(skillId))
                },
                onNavigateToCreateSkill = {
                    navController.navigate(Screen.CreateSkill.route)
                },
                onNavigateToRetroactive = {
                    navController.navigate(Screen.Retroactive.route)
                }
            )
        }

        composable(
            Screen.Skills.route,
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) },
            popEnterTransition = { fadeIn(animationSpec = tween(250)) },
            popExitTransition = { fadeOut(animationSpec = tween(250)) }
        ) {
            SkillsScreen(
                onNavigateToDetail = { skillId ->
                    navController.navigate(Screen.SkillDetail.createRoute(skillId))
                },
                onNavigateToCreateSkill = {
                    navController.navigate(Screen.CreateSkill.route)
                },
                onNavigateToTimer = { skillId ->
                    navController.navigate(Screen.Timer.createRoute(skillId))
                },
                onNavigateToCombo = {
                    navController.navigate(Screen.Combo.route)
                },
                onNavigateToShowcase = {
                    navController.navigate(Screen.Showcase.route)
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

        composable(
            Screen.Character.route,
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) },
            popEnterTransition = { fadeIn(animationSpec = tween(250)) },
            popExitTransition = { fadeOut(animationSpec = tween(250)) }
        ) {
            CharacterScreen(
                onNavigateToAchievement = {
                    navController.navigate(Screen.Achievement.route)
                },
                onNavigateToShop = {
                    navController.navigate(Screen.Shop.route)
                }
            )
        }

        composable(
            Screen.Profile.route,
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) },
            popEnterTransition = { fadeIn(animationSpec = tween(250)) },
            popExitTransition = { fadeOut(animationSpec = tween(250)) }
        ) {
            ProfileScreen(
                onNavigateToReview = { navController.navigate(Screen.Review.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToLedger = { navController.navigate(Screen.Ledger.route) },
                onNavigateToSettings = { /* placeholder */ },
                onNavigateToBackup = { navController.navigate(Screen.Backup.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.Review.route) {
            ReviewScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Ledger.route) {
            LedgerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
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
            TimerScreen(
                skillId = skillId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Achievement.route) {
            AchievementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Retroactive.route) {
            RetroactiveScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Shop.route) {
            ShopScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Combo.route) {
            ComboScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Showcase.route) {
            ShowcaseScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { skillId ->
                    navController.navigate(Screen.SkillDetail.createRoute(skillId))
                },
                onNavigateToCreateSkill = {
                    navController.navigate(Screen.CreateSkill.route)
                }
            )
        }

        composable(Screen.Backup.route) {
            BackupScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPrivacyPolicy = { navController.navigate(Screen.PrivacyPolicy.route) }
            )
        }

        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
