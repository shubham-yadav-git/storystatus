package com.sky.storystatus.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * StatusWala Theme Configuration
 *
 * Uses a custom editorial color scheme rather than Material dynamic colors
 * to maintain the brand identity consistently across all devices.
 */

private val StatusWalaLightScheme = lightColorScheme(
    primary = StatusWalaColors.Primary,
    onPrimary = Color.White,
    primaryContainer = StatusWalaColors.PrimaryContainer,
    onPrimaryContainer = StatusWalaColors.OnPrimaryContainer,

    secondary = StatusWalaColors.Secondary,
    onSecondary = Color.White,
    secondaryContainer = StatusWalaColors.SecondaryContainer,
    onSecondaryContainer = StatusWalaColors.OnSecondaryContainer,

    tertiary = StatusWalaColors.Tertiary,
    onTertiary = Color.White,
    tertiaryContainer = StatusWalaColors.TertiaryContainer,
    onTertiaryContainer = StatusWalaColors.OnTertiaryContainer,

    background = StatusWalaColors.BackgroundLight,
    onBackground = StatusWalaColors.OnSurfaceLight,

    surface = StatusWalaColors.SurfaceLight,
    onSurface = StatusWalaColors.OnSurfaceLight,
    surfaceVariant = StatusWalaColors.SurfaceVariantLight,
    onSurfaceVariant = StatusWalaColors.OnSurfaceVariantLight,

    error = StatusWalaColors.Error,
    onError = Color.White,
    errorContainer = StatusWalaColors.ErrorContainer,
    onErrorContainer = Color(0xFF93000A),

    outline = Color(0xFFD6CFC8),
    outlineVariant = Color(0xFFEDE6DF),
    inverseSurface = StatusWalaColors.OnSurfaceLight,
    inverseOnSurface = StatusWalaColors.SurfaceLight,
    inversePrimary = StatusWalaColors.PrimaryLight,
    surfaceTint = StatusWalaColors.Primary
)

private val StatusWalaDarkScheme = darkColorScheme(
    primary = StatusWalaColors.PrimaryLight,
    onPrimary = Color(0xFF1D0066),
    primaryContainer = StatusWalaColors.PrimaryDark,
    onPrimaryContainer = Color(0xFFEDE7FF),

    secondary = StatusWalaColors.SecondaryLight,
    onSecondary = Color(0xFF3D2300),
    secondaryContainer = Color(0xFF5A3800),
    onSecondaryContainer = Color(0xFFFFF0DE),

    tertiary = Color(0xFFFFB1C8),
    onTertiary = Color(0xFF3E0021),
    tertiaryContainer = Color(0xFF7A2954),
    onTertiaryContainer = Color(0xFFFFD9E7),

    background = StatusWalaColors.BackgroundDark,
    onBackground = StatusWalaColors.OnSurfaceDark,

    surface = StatusWalaColors.SurfaceDark,
    onSurface = StatusWalaColors.OnSurfaceDark,
    surfaceVariant = StatusWalaColors.SurfaceVariantDark,
    onSurfaceVariant = StatusWalaColors.OnSurfaceVariantDark,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFF5C4E56),
    outlineVariant = Color(0xFF3D3138),
    inverseSurface = StatusWalaColors.OnSurfaceDark,
    inverseOnSurface = StatusWalaColors.SurfaceDark,
    inversePrimary = StatusWalaColors.Primary,
    surfaceTint = StatusWalaColors.PrimaryLight
)

@Composable
fun StatuswalaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StatusWalaDarkScheme else StatusWalaLightScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StatusWalaTypography,
        content = content
    )
}
