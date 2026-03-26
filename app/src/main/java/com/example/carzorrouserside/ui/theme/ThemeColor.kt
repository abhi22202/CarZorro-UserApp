package com.example.carzorrouserside.ui.theme



import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object CarZorroColors {
    val disabledButtonBackground: Color
        @Composable
        get() = if (isSystemInDarkTheme()) Color(0xFFF5F5F5) else lightGray // Light grey in dark mode, re-use existing in light.

    val disabledButtonText: Color
        @Composable
        get() = if (isSystemInDarkTheme()) Color.Black else lightGray // Black text in dark, same as before in light.

    val primary: Color
        @Composable
        get() = MaterialTheme.colorScheme.primary

    val background: Color
        @Composable
        get() = MaterialTheme.colorScheme.background

    val surface: Color
        @Composable
        get() = MaterialTheme.colorScheme.surface

    val onBackground: Color
        @Composable
        get() = MaterialTheme.colorScheme.onBackground

    val onSurface: Color
        @Composable
        get() = MaterialTheme.colorScheme.onSurface

    val gray: Color
        @Composable
        get() = if (isSystemInDarkTheme()) DarkGray else LightGray

    val lightGray: Color
        @Composable
        get() = if (isSystemInDarkTheme()) DarkLightGray else LightLightGray

    val border: Color
        @Composable
        get() = if (isSystemInDarkTheme()) DarkBorder else LightBorder

    val error: Color
        @Composable
        get() = MaterialTheme.colorScheme.error

    val success: Color
        @Composable
        get() = if (isSystemInDarkTheme()) DarkSuccess else LightSuccess

    val cardBackground: Color
        @Composable
        get() = if (isSystemInDarkTheme()) DarkSurface else Color.White

    val textPrimary: Color
        @Composable
        get() = MaterialTheme.colorScheme.onBackground

    val textSecondary: Color
        @Composable
        get() = if (isSystemInDarkTheme()) DarkGray else LightGray

    val divider: Color
        @Composable
        get() = if (isSystemInDarkTheme()) DarkBorder else Color(0xFFE0E0E0)
}