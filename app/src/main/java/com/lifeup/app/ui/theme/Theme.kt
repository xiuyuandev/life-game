package com.lifeup.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PixelColors.Primary,
    onPrimary = PixelColors.TextPrimary,
    primaryContainer = PixelColors.PrimaryVariant,
    onPrimaryContainer = PixelColors.TextPrimary,
    secondary = PixelColors.Secondary,
    onSecondary = PixelColors.TextPrimary,
    secondaryContainer = PixelColors.SecondaryVariant,
    onSecondaryContainer = PixelColors.TextPrimary,
    tertiary = PixelColors.Tertiary,
    onTertiary = PixelColors.TextPrimary,
    tertiaryContainer = PixelColors.TertiaryVariant,
    onTertiaryContainer = PixelColors.TextPrimary,
    background = PixelColors.Background,
    onBackground = PixelColors.TextPrimary,
    surface = PixelColors.Surface,
    onSurface = PixelColors.TextPrimary,
    surfaceVariant = PixelColors.SurfaceVariant,
    onSurfaceVariant = PixelColors.TextSecondary,
    error = PixelColors.AccentRed,
    onError = PixelColors.TextPrimary,
    outline = PixelColors.Border,
    outlineVariant = PixelColors.BorderStrong,
    scrim = PixelColors.DeepSpace.copy(alpha = 0.8f)
)

@Composable
fun LifeUpTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = PixelColors.DeepSpace.toArgb()
            window.navigationBarColor = PixelColors.DeepSpace.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
