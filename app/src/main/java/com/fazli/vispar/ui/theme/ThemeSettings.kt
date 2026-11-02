package com.fazli.vispar.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val primaryColor: Color = defaultPrimaryColor
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

/** Default accent: a vivid violet that refracts beautifully through glass. */
val defaultPrimaryColor = Color(0xFF7C5CFF)

/** Accent palette offered in Settings, tuned for saturation so the aurora background glows. */
val colorOptions = listOf(
    Color(0xFF7C5CFF), // Violet
    Color(0xFF0A84FF), // Blue
    Color(0xFF00C7BE), // Teal
    Color(0xFF30D158), // Green
    Color(0xFFFFB800), // Amber
    Color(0xFFFF6B35), // Orange
    Color(0xFFFF375F), // Pink
    Color(0xFFBF5AF2), // Purple
)

@Composable
fun rememberThemeSettings(): ThemeSettings {
    return remember { ThemeSettings() }
}
