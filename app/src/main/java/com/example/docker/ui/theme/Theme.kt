package com.example.docker.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = DockerBlue,
    onPrimary = TextLight,
    primaryContainer = DockerNavy,
    onPrimaryContainer = DockerBlueLight,
    secondary = AccentCyan,
    onSecondary = TextDark,
    secondaryContainer = DockerNavyLight,
    onSecondaryContainer = AccentCyan,
    tertiary = AccentOrange,
    onTertiary = TextDark,
    background = DarkBackground,
    onBackground = TextLight,
    surface = DarkSurface,
    onSurface = TextLight,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    error = StatusError,
    onError = TextLight
)

private val LightColorScheme = lightColorScheme(
    primary = DockerBlue,
    onPrimary = TextLight,
    primaryContainer = DockerBlueLight,
    onPrimaryContainer = DockerNavy,
    secondary = AccentCyan,
    onSecondary = TextDark,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = DockerNavy,
    tertiary = AccentOrange,
    onTertiary = TextLight,
    background = LightBackground,
    onBackground = TextDark,
    surface = LightSurface,
    onSurface = TextDark,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextSecondary,
    error = StatusError,
    onError = TextLight
)

@Composable
fun DockerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic color to use our Docker theme
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}