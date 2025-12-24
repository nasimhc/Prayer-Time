package com.prayertime.prayertime.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Celestial Serenity - Dark immersive theme
private val CelestialColorScheme = darkColorScheme(
    primary = GoldBright,
    onPrimary = MidnightDeep,
    primaryContainer = GoldMuted,
    onPrimaryContainer = TextPrimary,

    secondary = CelestialViolet,
    onSecondary = TextPrimary,
    secondaryContainer = CelestialPurple,
    onSecondaryContainer = CelestialLight,

    tertiary = FajrAccent,
    onTertiary = MidnightDeep,
    tertiaryContainer = FajrColor,
    onTertiaryContainer = TextPrimary,

    background = MidnightDeep,
    onBackground = TextPrimary,

    surface = MidnightBase,
    onSurface = TextPrimary,
    surfaceVariant = MidnightLight,
    onSurfaceVariant = TextSecondary,

    outline = MidnightSoft,
    outlineVariant = GoldSubtle,

    inverseSurface = TextPrimary,
    inverseOnSurface = MidnightDeep,
    inversePrimary = GoldMuted
)

@Composable
fun PrayerTimeTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = CelestialColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = MidnightDeep.toArgb()
            window.navigationBarColor = MidnightDeep.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
