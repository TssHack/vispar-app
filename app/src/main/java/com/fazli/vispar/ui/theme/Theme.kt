package com.fazli.vispar.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.fazli.vispar.ui.glass.GlassColors
import com.fazli.vispar.ui.glass.LocalGlassColors
import com.fazli.vispar.ui.glass.glassColors

/**
 * App theme. The visual language is Liquid Glass ([GlassColors]); a matching Material colour
 * scheme is still provided so that the few platform primitives we use (Text, Icon) pick sensible
 * defaults.
 */
@Composable
fun visparTheme(
    themeSettings: ThemeSettings = ThemeSettings(),
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeSettings.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val glass = remember(darkTheme, themeSettings.primaryColor) {
        glassColors(isDark = darkTheme, accent = themeSettings.primaryColor)
    }
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = glass.accent,
            onPrimary = glass.onAccent,
            secondary = glass.accent,
            background = glass.backgroundTop,
            surface = glass.backgroundTop,
            onBackground = glass.content,
            onSurface = glass.content,
            onSurfaceVariant = glass.contentSecondary,
            error = glass.danger,
        )
    } else {
        lightColorScheme(
            primary = glass.accent,
            onPrimary = glass.onAccent,
            secondary = glass.accent,
            background = glass.backgroundTop,
            surface = glass.backgroundTop,
            onBackground = glass.content,
            onSurface = glass.content,
            onSurfaceVariant = glass.contentSecondary,
            error = glass.danger,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            // Edge-to-edge: bars are transparent, only the icon colour follows the theme.
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalGlassColors provides glass) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
