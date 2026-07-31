package com.voctext.app.ui.theme

import android.app.Activity
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

private val LightColorScheme = lightColorScheme(
    background = LightSurfacePage,
    surface = LightSurfaceCard,
    surfaceVariant = LightSurfaceElevated,
    primary = Accent,
    onPrimary = Color.White,
    secondary = LightSurfaceCard,
    onSecondary = LightTextPrimary,
    tertiary = AccentLight,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = Error,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    background = DarkSurfacePage,
    surface = DarkSurfaceCard,
    surfaceVariant = DarkSurfaceElevated,
    primary = AccentLight,
    onPrimary = Color.White,
    secondary = DarkSurfaceCard,
    onSecondary = DarkTextPrimary,
    tertiary = AccentHoverLight,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = Error,
    onError = Color.White,
)

@Composable
fun VoctextTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VoctextTypography,
        content = content,
    )
}