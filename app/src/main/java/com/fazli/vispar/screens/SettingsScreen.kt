package com.fazli.vispar.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Brightness1
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fazli.vispar.BuildConfig
import com.fazli.vispar.R
import com.fazli.vispar.data.model.SubtitleSettings
import com.fazli.vispar.data.model.VideoPlayerSettings
import com.fazli.vispar.ui.theme.ThemeMode
import com.fazli.vispar.ui.theme.ThemeSettings
import com.fazli.vispar.ui.theme.ThemeManager
import com.fazli.vispar.ui.theme.colorOptions
import com.fazli.vispar.ui.theme.defaultPrimaryColor
import com.fazli.vispar.utils.StorageUtils
import androidx.compose.foundation.layout.Arrangement.SpaceBetween
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.net.URL

// تعریف فونت وزیری
private val VazirFontFamily = FontFamily(
    Font(R.font.vazir_regular, FontWeight.Normal),
    Font(R.font.vazir_bold, FontWeight.Bold)
)

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val html_url: String
)

@Composable
fun SettingsScreen(
    onThemeSettingsChanged: (ThemeSettings) -> Unit = {},
    navController: NavController? = null
) {
    val themeManager = ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    var themeSettings by remember { mutableStateOf(themeManager.loadThemeSettings()) }
    val context = LocalContext.current
    var subtitleSettings by remember { mutableStateOf(StorageUtils.loadSubtitleSettings(context)) }
    var videoPlayerSettings by remember { mutableStateOf(StorageUtils.loadVideoPlayerSettings(context)) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var latestVersionUrl by remember { mutableStateOf("") }
    var isCheckingUpdate by remember { mutableStateOf(false) }
    
    // Configure JSON to ignore unknown keys
    val json = Json { ignoreUnknownKeys = true }
    
    // Update parent when settings change
    fun updateThemeSettings(newSettings: ThemeSettings) {
        themeSettings = newSettings
        onThemeSettingsChanged(newSettings)
        themeManager.saveThemeSettings(newSettings)
    }
    
    // Update subtitle settings
    fun updateSubtitleSettings(newSettings: SubtitleSettings) {
        subtitleSettings = newSettings
        StorageUtils.saveSubtitleSettings(context, newSettings)
    }
    
    // Update video player settings
    fun updateVideoPlayerSettings(newSettings: VideoPlayerSettings) {
        videoPlayerSettings = newSettings
        StorageUtils.saveVideoPlayerSettings(context, newSettings)
    }
    
    // Reset all settings to defaults
    fun resetToDefaults() {
        val defaultSettings = ThemeSettings()
        updateThemeSettings(defaultSettings)
        // Reset subtitle settings to default as well
        val defaultSubtitleSettings = SubtitleSettings.DEFAULT
        updateSubtitleSettings(defaultSubtitleSettings)
        // Reset video player settings to default as well
        val defaultVideoPlayerSettings = VideoPlayerSettings.DEFAULT
        updateVideoPlayerSettings(defaultVideoPlayerSettings)
    }
    
    // Compare version strings
    fun isVersionNewer(currentVersion: String, latestVersion: String): Boolean {
        try {
            // Remove 'v' prefix if present
            val current = currentVersion.removePrefix("v")
            val latest = latestVersion.removePrefix("v")
            
            // Split version numbers
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }
            val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
            
            // Compare each part
            for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
                val currentPart = if (i < currentParts.size) currentParts[i] else 0
                val latestPart = if (i < latestParts.size) latestParts[i] else 0
                
                if (latestPart > currentPart) return true
                if (latestPart < currentPart) return false
            }
            
            return false // Versions are equal
        } catch (e: Exception) {
            Log.e("SettingsScreen", "Error comparing versions", e)
            return false
        }
    }
    
    // Check for updates
    fun checkForUpdates() {
        isCheckingUpdate = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://api.github.com/repos/tsshack/vispar/releases")
                val connection = url.openConnection()
                connection.setRequestProperty("User-Agent", "vispar-App")
                val response = connection.getInputStream().bufferedReader().use { it.readText() }
                
                val releases = json.decodeFromString<List<GitHubRelease>>(response)
                if (releases.isNotEmpty()) {
                    val latestRelease = releases.first()
                    val latestVersion = latestRelease.tag_name
                    val currentVersion = "v${BuildConfig.VERSION_NAME}"
                    
                    withContext(Dispatchers.Main) {
                        isCheckingUpdate = false
                        if (isVersionNewer(currentVersion, latestVersion)) {
                            latestVersionUrl = latestRelease.html_url
                            showUpdateDialog = true
                        } else {
                            Toast.makeText(context, "برنامه به‌روز است", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        isCheckingUpdate = false
                        Toast.makeText(context, "خطا در بررسی به‌روزرسانی", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("SettingsScreen", "Error checking for updates", e)
                withContext(Dispatchers.Main) {
                    isCheckingUpdate = false
                    Toast.makeText(context, "خطا در بررسی به‌روزرسانی", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300)) + slideOutVertically(animationSpec = tween(300))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.weight(1f),
                        fontFamily = VazirFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Add like icon button that navigates to favorites
                    navController?.let {
                        IconButton(
                            onClick = { navController.navigate("favorites") },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "علاقه‌مندی‌ها",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        
        // Theme Settings Card
        item {
            var isExpanded by remember { mutableStateOf(false) }
            
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(400)) + slideInVertically(animationSpec = tween(400, delayMillis = 100)),
                exit = fadeOut(animationSpec = tween(400)) + slideOutVertically(animationSpec = tween(400))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatColorFill,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "تنظیمات ظاهر",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .weight(1f),
                                fontFamily = VazirFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = if (isExpanded) "بستن" else "باز کردن",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Theme Mode Section
                                Text(
                                    text = "حالت ظاهر",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    fontFamily = VazirFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                ThemeModeOption(
                                    mode = ThemeMode.LIGHT,
                                    label = "روشن",
                                    isSelected = themeSettings.themeMode == ThemeMode.LIGHT,
                                    onSelect = { mode ->
                                        val newSettings = themeSettings.copy(themeMode = mode)
                                        updateThemeSettings(newSettings)
                                    }
                                )
                                
                                ThemeModeOption(
                                    mode = ThemeMode.DARK,
                                    label = "تاریک",
                                    isSelected = themeSettings.themeMode == ThemeMode.DARK,
                                    onSelect = { mode ->
                                        val newSettings = themeSettings.copy(themeMode = mode)
                                        updateThemeSettings(newSettings)
                                    }
                                )
                                
                                ThemeModeOption(
                                    mode = ThemeMode.SYSTEM,
                                    label = "پیش‌فرض سیستم",
                                    isSelected = themeSettings.themeMode == ThemeMode.SYSTEM,
                                    onSelect = { mode ->
                                        val newSettings = themeSettings.copy(themeMode = mode)
                                        updateThemeSettings(newSettings)
                                    }
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Primary Color Section
                                Text(
                                    text = "رنگ اصلی",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 12.dp),
                                    fontFamily = VazirFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                // Display color options in rows of 4
                                for (rowColors in colorOptions.chunked(4)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        rowColors.forEach { color ->
                                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                                ColorOption(
                                                    color = color,
                                                    isSelected = themeSettings.primaryColor == color,
                                                    onSelect = { selectedColor ->
                                                        val newSettings = themeSettings.copy(primaryColor = selectedColor)
                                                        updateThemeSettings(newSettings)
                                                    }
                                                )
                                            }
                                        }
                                        // Fill remaining spaces if less than 4 items
                                        repeat(4 - rowColors.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                                
                                // Add default color option
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    val defaultColor = defaultPrimaryColor
                                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                        ColorOption(
                                            color = defaultColor,
                                            isSelected = themeSettings.primaryColor == defaultColor,
                                            onSelect = { selectedColor ->
                                                val newSettings = themeSettings.copy(primaryColor = selectedColor)
                                                updateThemeSettings(newSettings)
                                            },
                                            label = "پیش‌فرض"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(500)) + slideInVertically(animationSpec = tween(500, delayMillis = 200)),
                exit = fadeOut(animationSpec = tween(500)) + slideOutVertically(animationSpec = tween(500))
            ) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Video Player Settings Card
        item {
            var isExpanded by remember { mutableStateOf(false) }
            
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(600)) + slideInVertically(animationSpec = tween(600, delayMillis = 300)),
                exit = fadeOut(animationSpec = tween(600)) + slideOutVertically(animationSpec = tween(600))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TextFields,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "تنظیمات پخش‌کننده ویدیو",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .weight(1f),
                                fontFamily = VazirFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = if (isExpanded) "بستن" else "باز کردن",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "زمان جستجو: ${videoPlayerSettings.seekTimeSeconds} ثانیه",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    fontFamily = VazirFontFamily
                                )
                                
                                Slider(
                                    value = videoPlayerSettings.seekTimeSeconds.toFloat(),
                                    onValueChange = { seconds ->
                                        updateVideoPlayerSettings(videoPlayerSettings.copy(seekTimeSeconds = seconds.toInt()))
                                    },
                                    valueRange = 5f..30f,
                                    steps = 24, // Allow values from 5 to 30 in 1-second increments,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Subtitle Settings Section
                                Text(
                                    text = "تنظیمات زیرنویس",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 12.dp),
                                    fontFamily = VazirFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                // Background color setting
                                SubtitleColorSetting(
                                    title = "رنگ پس‌زمینه",
                                    currentColor = Color(subtitleSettings.backgroundColor),
                                    onColorSelected = { color ->
                                        updateSubtitleSettings(subtitleSettings.copy(backgroundColor = color.toArgb()))
                                    },
                                    noColorOption = true
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Text color setting
                                SubtitleColorSetting(
                                    title = "رنگ متن",
                                    currentColor = Color(subtitleSettings.textColor),
                                    onColorSelected = { color ->
                                        updateSubtitleSettings(subtitleSettings.copy(textColor = color.toArgb()))
                                    },
                                    defaultColor = Color.Yellow
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Border color setting
                                SubtitleColorSetting(
                                    title = "رنگ حاشیه",
                                    currentColor = Color(subtitleSettings.borderColor),
                                    onColorSelected = { color ->
                                        updateSubtitleSettings(subtitleSettings.copy(borderColor = color.toArgb()))
                                    },
                                    noColorOption = true
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Text size setting
                                Text(
                                    text = "اندازه متن: ${subtitleSettings.textSize.toInt()}sp",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    fontFamily = VazirFontFamily
                                )
                                
                                Slider(
                                    value = subtitleSettings.textSize,
                                    onValueChange = { size ->
                                        updateSubtitleSettings(subtitleSettings.copy(textSize = size))
                                    },
                                    valueRange = 10f..30f,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(700)) + slideInVertically(animationSpec = tween(700, delayMillis = 400)),
                exit = fadeOut(animationSpec = tween(700)) + slideOutVertically(animationSpec = tween(700))
            ) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Favorites Card
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(animationSpec = tween(800, delayMillis = 500)),
                exit = fadeOut(animationSpec = tween(800)) + slideOutVertically(animationSpec = tween(800))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController?.navigate("favorites") },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "علاقه‌مندی‌ها",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .weight(1f),
                                fontFamily = VazirFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = "مشاهده و مدیریت فیلم‌ها و سریال‌های مورد علاقه خود",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = VazirFontFamily
                        )
                    }
                }
            }
        }
        
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(900)) + slideInVertically(animationSpec = tween(900, delayMillis = 600)),
                exit = fadeOut(animationSpec = tween(900)) + slideOutVertically(animationSpec = tween(900))
            ) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // About Card
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(animationSpec = tween(1000, delayMillis = 700)),
                exit = fadeOut(animationSpec = tween(1000)) + slideOutVertically(animationSpec = tween(1000))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController?.navigate("about") },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "درباره",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .weight(1f),
                                fontFamily = VazirFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = "اطلاعات بیشتر درباره ویسپار و توسعه‌دهنده آن",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = VazirFontFamily
                        )
                    }
                }
            }
        }
        
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1100)) + slideInVertically(animationSpec = tween(1100, delayMillis = 800)),
                exit = fadeOut(animationSpec = tween(1100)) + slideOutVertically(animationSpec = tween(1100))
            ) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Check for Updates Card
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1200)) + slideInVertically(animationSpec = tween(1200, delayMillis = 900)),
                exit = fadeOut(animationSpec = tween(1200)) + slideOutVertically(animationSpec = tween(1200))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            if (!isCheckingUpdate) {
                                checkForUpdates()
                            }
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "بررسی به‌روزرسانی",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .weight(1f),
                                fontFamily = VazirFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                            if (isCheckingUpdate) {
                                // Show loading indicator
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "در حال بررسی",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(2.dp)
                                )
                            }
                        }
                        
                        Text(
                            text = "بررسی آخرین نسخه برنامه",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = VazirFontFamily
                        )
                    }
                }
            }
        }
        
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1300)) + slideInVertically(animationSpec = tween(1300, delayMillis = 1000)),
                exit = fadeOut(animationSpec = tween(1300)) + slideOutVertically(animationSpec = tween(1300))
            ) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        
        // Reset to Defaults Card
        item {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(1400)) + slideInVertically(animationSpec = tween(1400, delayMillis = 1100)),
                exit = fadeOut(animationSpec = tween(1400)) + slideOutVertically(animationSpec = tween(1400))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showResetDialog = true },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Brightness1,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "بازنشانی به پیش‌فرض",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .weight(1f),
                                fontFamily = VazirFontFamily,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = "برای بازنشانی تمام تنظیمات به مقادیر پیش‌فرض، اینجا ضربه بزنید",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = VazirFontFamily
                        )
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    
    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "بازنشانی تنظیمات",
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "آیا مطمئن هستید که می‌خواهید تمام تنظیمات را به مقادیر پیش‌فرض بازنشانی کنید؟",
                    fontFamily = VazirFontFamily,
                    textAlign = TextAlign.Right
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        resetToDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text(
                        text = "بازنشانی",
                        fontFamily = VazirFontFamily,
                        color = Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetDialog = false }
                ) {
                    Text(
                        text = "انصراف",
                        fontFamily = VazirFontFamily
                    )
                }
            }
        )
    }
    
    // Update available dialog
    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = {
                Text(
                    text = "به‌روزرسانی موجود است",
                    fontFamily = VazirFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "نسخه جدیدی از برنامه موجود است. آیا مایلید اکنون آن را دانلود کنید؟",
                    fontFamily = VazirFontFamily,
                    textAlign = TextAlign.Right
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(latestVersionUrl))
                        context.startActivity(intent)
                        showUpdateDialog = false
                    }
                ) {
                    Text(
                        text = "دانلود",
                        fontFamily = VazirFontFamily
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUpdateDialog = false }
                ) {
                    Text(
                        text = "بعداً",
                        fontFamily = VazirFontFamily
                    )
                }
            }
        )
    }
}

@Composable
fun ThemeModeOption(
    mode: ThemeMode,
    label: String,
    isSelected: Boolean,
    onSelect: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(mode) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onSelect(mode) }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
            fontFamily = VazirFontFamily
        )
    }
}

@Composable
fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onSelect: (Color) -> Unit,
    label: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .clickable { onSelect(color) }
                .then(
                    if (isSelected) {
                        Modifier.padding(4.dp)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "انتخاب شده",
                    tint = if (color == Color.White || color == Color.Yellow) Color.Black else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
                fontFamily = VazirFontFamily
            )
        }
    }
}

@Composable
fun SubtitleColorSetting(
    title: String,
    currentColor: Color,
    onColorSelected: (Color) -> Unit,
    noColorOption: Boolean = false,
    defaultColor: Color? = null
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp),
            fontFamily = VazirFontFamily
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Color options
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // No color option (transparent)
                if (noColorOption) {
                    ColorOptionButton(
                        color = Color.Transparent,
                        isSelected = currentColor == Color.Transparent,
                        onClick = { onColorSelected(Color.Transparent) },
                        showBorder = true
                    )
                }
                
                // Default color option
                if (defaultColor != null) {
                    ColorOptionButton(
                        color = defaultColor,
                        isSelected = currentColor == defaultColor,
                        onClick = { onColorSelected(defaultColor) }
                    )
                }
                
                // Standard color options
                listOf(Color.White, Color.Black, Color.Red, Color.Blue, Color.Green).forEach { color ->
                    ColorOptionButton(
                        color = color,
                        isSelected = currentColor == color,
                        onClick = { onColorSelected(color) }
                    )
                }
            }
            
            // Current color preview
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(currentColor)
                    .then(
                        if (currentColor == Color.Transparent) {
                            Modifier.background(Color.Gray.copy(alpha = 0.3f))
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

@Composable
fun ColorOptionButton(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    showBorder: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (color == Color.Transparent && showBorder) {
                    color
                } else {
                    color
                }
            )
            .then(
                if (isSelected) {
                    Modifier.background(
                        color = color,
                        shape = RoundedCornerShape(4.dp)
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "انتخاب شده",
                tint = if (color == Color.White || color == Color.Yellow) Color.Black else Color.White,
                modifier = Modifier.size(16.dp)
            )
        } else if (color == Color.Transparent && showBorder) {
            Icon(
                imageVector = Icons.Default.Brightness1,
                contentDescription = "بدون رنگ",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}