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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary80,
    onPrimary = Color(0xFF003300),
    primaryContainer = DarkContainer,
    onPrimaryContainer = Color(0xFFB2DFDB),

    secondary = GreenSecondary80,
    onSecondary = Color(0xFF1B5E20),
    secondaryContainer = Color(0xFF2E4A2E),
    onSecondaryContainer = Color(0xFFC8E6C9),

    tertiary = EarthTertiary80,
    onTertiary = Color(0xFF3E2723),
    tertiaryContainer = Color(0xFF4E3A35),
    onTertiaryContainer = Color(0xFFD7CCC8),

    background = DarkBackground,
    onBackground = Color(0xFFE8F5E9),
    surface = DarkSurface,
    onSurface = Color(0xFFE8F5E9),
    surfaceVariant = Color(0xFF3A4A3A),
    onSurfaceVariant = Color(0xFFC8E6C9),

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF5D1F1A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = LightContainer,
    onPrimaryContainer = Color(0xFF1B5E20),

    secondary = GreenSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8F5E9),
    onSecondaryContainer = Color(0xFF2E7D32),

    tertiary = EarthTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFEFEBE9),
    onTertiaryContainer = Color(0xFF4E342E),

    background = LightBackground,
    onBackground = Color(0xFF1B5E20),
    surface = LightSurface,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF424242),

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun PruebaAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}