package com.example.carzorrouserside.ui.theme

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
    primary = appPrimary,
    secondary = appPrimaryDark,
    tertiary = appPrimaryLight,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    error = DarkError,
    onError = Color.White,
    surfaceVariant = DarkSurface, // Use a dark surface for cards in dark mode
    onSurfaceVariant = DarkGray   // Use a light gray for muted text in dark mode
)

private val LightColorScheme = lightColorScheme(
    primary = appPrimary,
    secondary = appPrimaryDark,
    tertiary = appPrimaryLight,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    error = LightError,
    onError = Color.White,
    surfaceVariant = LightSurface, // Use a light surface for cards in light mode
    onSurfaceVariant = LightGray    // Use a darker gray for muted text in light mode
)

@Composable
fun CarZorroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to use our custom colors
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
            // Set status bar to transparent to enable edge-to-edge drawing
            window.statusBarColor = Color.Transparent.toArgb()
            // Set the status bar icons (time, wifi, battery) to be light in dark mode and dark in light mode
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

