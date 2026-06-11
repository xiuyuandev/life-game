package com.lifeup.app.ui.splash

import androidx.compose.runtime.Composable

/**
 * Splash entry point used by the navigation graph.
 *
 * The previous text-only splash has been replaced by the full-featured
 * [BrandSplashScreen] which renders a mesh gradient background, an
 * animated brand mark with rotating rays, a sweeping gradient wordmark
 * and animated progress dots.
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    BrandSplashScreen(onReady = onSplashComplete)
}
