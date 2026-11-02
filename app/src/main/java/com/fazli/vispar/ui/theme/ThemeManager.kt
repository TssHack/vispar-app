package com.fazli.vispar.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit
import com.fazli.vispar.ui.glass.GlassMaterial
import com.fazli.vispar.utils.DeviceUtils

class ThemeManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    fun saveThemeSettings(settings: ThemeSettings) {
        prefs.edit {
            putString("theme_mode", settings.themeMode.name)
            putInt("primary_color", settings.primaryColor.toArgb())
            putString("glass_material", settings.glassMaterial.name)
            putBoolean("compact_grid", settings.compactGrid)
            putBoolean("haptics", settings.haptics)
        }
    }
    
    fun loadThemeSettings(): ThemeSettings {
        val themeModeStr = prefs.getString("theme_mode", ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeModeStr)
        } catch (e: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
        
        val primaryColor = prefs.getInt("primary_color", defaultPrimaryColor.toArgb())
            .let { if (it in legacyAccents) defaultPrimaryColor.toArgb() else it }
        
        // TV boxes have weak GPUs and are viewed from afar: default to opaque glass there.
        val defaultMaterial = if (DeviceUtils.isTv(context)) GlassMaterial.Solid else GlassMaterial.Regular
        val material = prefs.getString("glass_material", null)
            ?.let { name -> GlassMaterial.entries.firstOrNull { it.name == name } }
            ?: defaultMaterial

        return ThemeSettings(
            themeMode = themeMode,
            primaryColor = Color(primaryColor),
            glassMaterial = material,
            compactGrid = prefs.getBoolean("compact_grid", false),
            haptics = prefs.getBoolean("haptics", true),
        )
    }
}

@Composable
fun rememberThemeManager(): ThemeManager {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember { ThemeManager(context) }
}

@Composable
fun rememberAppThemeSettings(): MutableState<ThemeSettings> {
    val themeManager = rememberThemeManager()
    val settings = remember { mutableStateOf(themeManager.loadThemeSettings()) }
    return settings
}