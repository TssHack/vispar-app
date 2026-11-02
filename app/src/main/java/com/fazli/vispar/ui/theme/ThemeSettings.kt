package com.fazli.vispar.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.fazli.vispar.ui.glass.MonochromeAccent

data class ThemeSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val primaryColor: Color = defaultPrimaryColor
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

/** Default accent: monochrome — white on dark, black on light. */
val defaultPrimaryColor = MonochromeAccent

/** Accent palette offered in Settings: monochrome plus Apple's system colours. */
val colorOptions = listOf(
    MonochromeAccent,
    Color(0xFF007AFF), // Blue
    Color(0xFF30B0C7), // Teal
    Color(0xFF34C759), // Green
    Color(0xFFFF9500), // Orange
    Color(0xFFFF3B30), // Red
    Color(0xFFFF2D55), // Pink
    Color(0xFF8E8E93), // Graphite
)

/** Purple accents shipped by earlier versions; they now fall back to the default. */
val legacyAccents = setOf(0xFF6650A4.toInt(), 0xFF7C5CFF.toInt(), 0xFFBF5AF2.toInt())

@Composable
fun rememberThemeSettings(): ThemeSettings {
    return remember { ThemeSettings() }
}
