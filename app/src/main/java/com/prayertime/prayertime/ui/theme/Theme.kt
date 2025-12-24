package com.prayertime.prayertime.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Theme colors data class for easy access
data class PrayerTimeColors(
    val background: Color,
    val surfaceCard: Color,
    val surfaceCardElevated: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val goldBright: Color,
    val goldWarm: Color,
    val goldMuted: Color,
    val accentViolet: Color,
    val borderColor: Color,
    val isDark: Boolean
)

val LocalPrayerTimeColors = compositionLocalOf {
    PrayerTimeColors(
        background = MidnightDeep,
        surfaceCard = DarkSurfaceCard,
        surfaceCardElevated = DarkSurfaceCardElevated,
        textPrimary = DarkTextPrimary,
        textSecondary = DarkTextSecondary,
        textMuted = DarkTextMuted,
        goldBright = GoldBright,
        goldWarm = GoldWarm,
        goldMuted = GoldMuted,
        accentViolet = CelestialViolet,
        borderColor = MidnightLight,
        isDark = true
    )
}

private val DarkColors = PrayerTimeColors(
    background = MidnightDeep,
    surfaceCard = DarkSurfaceCard,
    surfaceCardElevated = DarkSurfaceCardElevated,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    goldBright = GoldBright,
    goldWarm = GoldWarm,
    goldMuted = GoldMuted,
    accentViolet = CelestialViolet,
    borderColor = MidnightLight,
    isDark = true
)

private val LightColors = PrayerTimeColors(
    background = IvoryDeep,
    surfaceCard = LightSurfaceCard,
    surfaceCardElevated = LightSurfaceCardElevated,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textMuted = LightTextMuted,
    goldBright = LightGoldBright,
    goldWarm = LightGoldWarm,
    goldMuted = LightGoldMuted,
    accentViolet = LightCelestialViolet,
    borderColor = IvorySoft,
    isDark = false
)

// Dark Material color scheme
private val DarkColorScheme = darkColorScheme(
    primary = GoldBright,
    onPrimary = MidnightDeep,
    primaryContainer = GoldMuted,
    onPrimaryContainer = DarkTextPrimary,
    secondary = CelestialViolet,
    onSecondary = DarkTextPrimary,
    secondaryContainer = CelestialPurple,
    onSecondaryContainer = CelestialLight,
    tertiary = FajrAccent,
    onTertiary = MidnightDeep,
    background = MidnightDeep,
    onBackground = DarkTextPrimary,
    surface = MidnightBase,
    onSurface = DarkTextPrimary,
    surfaceVariant = MidnightLight,
    onSurfaceVariant = DarkTextSecondary,
    outline = MidnightSoft,
    outlineVariant = GoldSubtle
)

// Light Material color scheme
private val LightColorScheme = lightColorScheme(
    primary = LightGoldBright,
    onPrimary = IvoryLight,
    primaryContainer = LightGoldWarm,
    onPrimaryContainer = LightTextPrimary,
    secondary = LightCelestialViolet,
    onSecondary = IvoryLight,
    secondaryContainer = CelestialLight,
    onSecondaryContainer = LightTextPrimary,
    tertiary = LightFajrAccent,
    onTertiary = IvoryLight,
    background = IvoryDeep,
    onBackground = LightTextPrimary,
    surface = IvoryBase,
    onSurface = LightTextPrimary,
    surfaceVariant = IvorySoft,
    onSurfaceVariant = LightTextSecondary,
    outline = IvorySoft,
    outlineVariant = LightGoldWarm
)

@Composable
fun PrayerTimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val prayerTimeColors = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = prayerTimeColors.background.toArgb()
            window.navigationBarColor = prayerTimeColors.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalPrayerTimeColors provides prayerTimeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
