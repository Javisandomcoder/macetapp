 package com.example.pruebaandroid.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.pruebaandroid.ui.Theme

// ============================================================================
// THEME CONFIGURATIONS
// ============================================================================

// 1. FOREST THEME
private val ForestColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = ForestContainer,
    onPrimaryContainer = ForestOnContainer,
    secondary = ForestSecondary,
    onSecondary = ForestOnSecondary,
    tertiary = ForestTertiary,
    background = ForestBackground,
    surface = ForestSurface,
    onBackground = Neutral900,
    onSurface = Neutral900
)

// 2. OCEAN THEME
private val OceanColorScheme = lightColorScheme(
    primary = OceanPrimary,
    onPrimary = OceanOnPrimary,
    primaryContainer = OceanContainer,
    onPrimaryContainer = OceanOnContainer,
    secondary = OceanSecondary,
    onSecondary = OceanOnSecondary,
    tertiary = OceanTertiary,
    background = OceanBackground,
    surface = OceanSurface,
    onBackground = Neutral900,
    onSurface = Neutral900
)

// 3. SUNSET THEME
private val SunsetColorScheme = lightColorScheme(
    primary = SunsetPrimary,
    onPrimary = SunsetOnPrimary,
    primaryContainer = SunsetContainer,
    onPrimaryContainer = SunsetOnContainer,
    secondary = SunsetSecondary,
    onSecondary = SunsetOnSecondary,
    tertiary = SunsetTertiary,
    background = SunsetBackground,
    surface = SunsetSurface,
    onBackground = Neutral900,
    onSurface = Neutral900
)

// 4. LAVENDER THEME
private val LavenderColorScheme = lightColorScheme(
    primary = LavenderPrimary,
    onPrimary = LavenderOnPrimary,
    primaryContainer = LavenderContainer,
    onPrimaryContainer = LavenderOnContainer,
    secondary = LavenderSecondary,
    onSecondary = LavenderOnSecondary,
    tertiary = LavenderTertiary,
    background = LavenderBackground,
    surface = LavenderSurface,
    onBackground = Neutral900,
    onSurface = Neutral900
)

// 5. MINT THEME
private val MintColorScheme = lightColorScheme(
    primary = MintPrimary,
    onPrimary = MintOnPrimary,
    primaryContainer = MintContainer,
    onPrimaryContainer = MintOnContainer,
    secondary = MintSecondary,
    onSecondary = MintOnSecondary,
    tertiary = MintTertiary,
    background = MintBackground,
    surface = MintSurface,
    onBackground = Neutral900,
    onSurface = Neutral900
)

// 6. CORAL THEME
private val CoralColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = CoralOnPrimary,
    primaryContainer = CoralContainer,
    onPrimaryContainer = CoralOnContainer,
    secondary = CoralSecondary,
    onSecondary = CoralOnSecondary,
    tertiary = CoralTertiary,
    background = CoralBackground,
    surface = CoralSurface,
    onBackground = Neutral900,
    onSurface = Neutral900
)

// 7. ROYAL THEME
private val RoyalColorScheme = lightColorScheme(
    primary = RoyalPrimary,
    onPrimary = RoyalOnPrimary,
    primaryContainer = RoyalContainer,
    onPrimaryContainer = RoyalOnContainer,
    secondary = RoyalSecondary,
    onSecondary = RoyalOnSecondary,
    tertiary = RoyalTertiary,
    background = RoyalBackground,
    surface = RoyalSurface,
    onBackground = Neutral900,
    onSurface = Neutral900
)

// 8. MIDNIGHT THEME (Dark)
private val MidnightColorScheme = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = MidnightOnPrimary,
    primaryContainer = MidnightContainer,
    onPrimaryContainer = MidnightOnContainer,
    secondary = MidnightSecondary,
    onSecondary = MidnightOnSecondary,
    background = MidnightBackground,
    surface = MidnightSurface,
    onBackground = Neutral200,
    onSurface = Neutral200
)

// DEFAULT DARK THEME
private val DarkColorScheme = darkColorScheme(
    primary = MidnightPrimary,
    onPrimary = MidnightOnPrimary,
    primaryContainer = MidnightContainer,
    onPrimaryContainer = MidnightOnContainer,
    secondary = MidnightSecondary,
    onSecondary = MidnightOnSecondary,
    background = MidnightBackground,
    surface = MidnightSurface,
    onBackground = Neutral200,
    onSurface = Neutral200
)

// DEFAULT LIGHT THEME
private val LightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = ForestContainer,
    onPrimaryContainer = ForestOnContainer,
    secondary = ForestSecondary,
    onSecondary = ForestOnSecondary,
    background = ForestBackground,
    surface = ForestSurface,
    onBackground = Neutral900,
    onSurface = Neutral900
)

@Composable
fun PruebaAndroidTheme(
    theme: Theme = Theme.SYSTEM,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        (theme == Theme.SYSTEM || theme == Theme.LIGHT || theme == Theme.DARK) -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        theme == Theme.MIDNIGHT -> MidnightColorScheme
        theme == Theme.OCEAN -> OceanColorScheme
        theme == Theme.SUNSET -> SunsetColorScheme
        theme == Theme.FOREST -> ForestColorScheme
        theme == Theme.LAVENDER -> LavenderColorScheme
        theme == Theme.CORAL -> CoralColorScheme
        theme == Theme.ROYAL -> RoyalColorScheme
        theme == Theme.MINT -> MintColorScheme

        theme == Theme.DARK -> DarkColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
