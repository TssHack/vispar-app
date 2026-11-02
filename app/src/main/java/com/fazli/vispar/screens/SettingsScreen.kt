package com.fazli.vispar.screens

import android.content.Intent
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.fazli.vispar.BuildConfig
import com.fazli.vispar.data.model.SubtitleSettings
import com.fazli.vispar.data.model.VideoPlayerSettings
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassAlertDialog
import com.fazli.vispar.ui.glass.GlassDivider
import com.fazli.vispar.ui.glass.GlassRow
import com.fazli.vispar.ui.glass.GlassScaffold
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.IconTile
import com.fazli.vispar.ui.glass.LargeTitle
import com.fazli.vispar.ui.glass.LiquidIconButton
import com.fazli.vispar.ui.glass.LiquidSlider
import com.fazli.vispar.ui.glass.LiquidTab
import com.fazli.vispar.ui.glass.LiquidTabs
import com.fazli.vispar.ui.glass.MonochromeAccent
import com.fazli.vispar.ui.theme.ThemeManager
import com.fazli.vispar.ui.theme.ThemeMode
import com.fazli.vispar.ui.theme.ThemeSettings
import com.fazli.vispar.ui.theme.colorOptions
import com.fazli.vispar.utils.StorageUtils
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val html_url: String
)

private val themeModes = listOf(
    Triple(ThemeMode.LIGHT, "روشن", Icons.Rounded.LightMode),
    Triple(ThemeMode.DARK, "تاریک", Icons.Rounded.DarkMode),
    Triple(ThemeMode.SYSTEM, "سیستم", Icons.Rounded.BrightnessAuto),
)

private val json = Json { ignoreUnknownKeys = true }

/** True when [latestVersion] (e.g. "v1.2.0") is newer than [currentVersion]. */
fun isVersionNewer(currentVersion: String, latestVersion: String): Boolean = try {
    val current = currentVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    val latest = latestVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    var result = false
    for (i in 0 until maxOf(current.size, latest.size)) {
        val c = current.getOrElse(i) { 0 }
        val l = latest.getOrElse(i) { 0 }
        if (l != c) {
            result = l > c
            break
        }
    }
    result
} catch (e: Exception) {
    Log.e("SettingsScreen", "Error comparing versions", e)
    false
}

@Composable
fun SettingsScreen(
    onThemeSettingsChanged: (ThemeSettings) -> Unit = {},
    navController: NavController? = null
) {
    val context = LocalContext.current
    val colors = Glass.colors
    val scope = rememberCoroutineScope()
    val themeManager = remember { ThemeManager(context) }
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    var themeSettings by remember { mutableStateOf(themeManager.loadThemeSettings()) }
    var subtitleSettings by remember { mutableStateOf(StorageUtils.loadSubtitleSettings(context)) }
    var playerSettings by remember { mutableStateOf(StorageUtils.loadVideoPlayerSettings(context)) }
    var showResetDialog by remember { mutableStateOf(false) }
    var latestVersionUrl by remember { mutableStateOf<String?>(null) }
    var isCheckingUpdate by remember { mutableStateOf(false) }

    fun updateTheme(newSettings: ThemeSettings) {
        themeSettings = newSettings
        onThemeSettingsChanged(newSettings)
        themeManager.saveThemeSettings(newSettings)
    }

    fun updateSubtitles(newSettings: SubtitleSettings) {
        subtitleSettings = newSettings
        StorageUtils.saveSubtitleSettings(context, newSettings)
    }

    fun updatePlayer(newSettings: VideoPlayerSettings) {
        playerSettings = newSettings
        StorageUtils.saveVideoPlayerSettings(context, newSettings)
    }

    fun checkForUpdates() {
        if (isCheckingUpdate) return
        isCheckingUpdate = true
        scope.launch {
            val newer: String? = try {
                withContext(Dispatchers.IO) {
                    val connection = URL("https://api.github.com/repos/tsshack/vispar/releases").openConnection()
                    connection.setRequestProperty("User-Agent", "vispar-App")
                    val response = connection.getInputStream().bufferedReader().use { it.readText() }
                    val latest = json.decodeFromString<List<GitHubRelease>>(response).firstOrNull()
                        ?: throw IllegalStateException("No releases")
                    if (isVersionNewer("v${BuildConfig.VERSION_NAME}", latest.tag_name)) latest.html_url else ""
                }
            } catch (e: Exception) {
                Log.e("SettingsScreen", "Error checking for updates", e)
                null
            }
            isCheckingUpdate = false
            when {
                newer == null -> showToast(context, "خطا در بررسی به‌روزرسانی")
                newer.isEmpty() -> showToast(context, "برنامه به‌روز است")
                else -> latestVersionUrl = newer
            }
        }
    }

    GlassScaffold(
        title = "تنظیمات",
        titleAlpha = {
            if (listState.firstVisibleItemIndex > 0) 1f
            else (listState.firstVisibleItemScrollOffset / with(density) { 56.dp.toPx() }).coerceIn(0f, 1f)
        },
        actions = {
            navController?.let {
                LiquidIconButton(
                    onClick = { it.navigate("favorites") },
                    icon = Icons.Rounded.Favorite,
                    contentDescription = "علاقه‌مندی‌ها",
                    iconTint = colors.heart
                )
            }
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Glass.ScreenPadding,
                end = Glass.ScreenPadding,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { LargeTitle("تنظیمات", "ظاهر و پخش را به سلیقه خودت تنظیم کن") }

            // Appearance
            item {
                SettingsCard(icon = Icons.Rounded.Palette, title = "ظاهر") {
                    Label("حالت نمایش")
                    LiquidTabs(
                        selectedIndex = themeModes.indexOfFirst { it.first == themeSettings.themeMode }.coerceAtLeast(0),
                        onTabSelected = { updateTheme(themeSettings.copy(themeMode = themeModes[it].first)) },
                        tabsCount = themeModes.size,
                        height = 56.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        themeModes.forEach { (mode, label, icon) ->
                            LiquidTab(onClick = { updateTheme(themeSettings.copy(themeMode = mode)) }) {
                                Icon(icon, null, tint = colors.content, modifier = Modifier.size(20.dp))
                                Text(label, style = GlassType.CaptionBold.copy(fontSize = 11.sp), color = colors.content)
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Label("رنگ اصلی")
                    AccentPalette(
                        selected = themeSettings.primaryColor,
                        onSelect = { updateTheme(themeSettings.copy(primaryColor = it)) }
                    )
                }
            }

            // Player & subtitles
            item {
                SettingsCard(icon = Icons.Rounded.Subtitles, title = "پخش‌کننده و زیرنویس") {
                    SubtitlePreview(subtitleSettings)
                    ValueLabel("زمان جلو/عقب رفتن", "${playerSettings.seekTimeSeconds} ثانیه")
                    LiquidSlider(
                        value = playerSettings.seekTimeSeconds.toFloat(),
                        onValueChange = { v ->
                            val seconds = v.toInt().coerceIn(5, 30)
                            if (seconds != playerSettings.seekTimeSeconds) updatePlayer(playerSettings.copy(seekTimeSeconds = seconds))
                        },
                        valueRange = 5f..30f,
                        steps = 24
                    )
                    ValueLabel("اندازه متن زیرنویس", "${subtitleSettings.textSize.toInt()}sp")
                    LiquidSlider(
                        value = subtitleSettings.textSize,
                        onValueChange = { v ->
                            val size = v.coerceIn(10f, 30f)
                            if (size.toInt() != subtitleSettings.textSize.toInt()) updateSubtitles(subtitleSettings.copy(textSize = size))
                        },
                        valueRange = 10f..30f,
                        steps = 19
                    )
                    SubtitleColorSetting(
                        title = "رنگ متن",
                        current = Color(subtitleSettings.textColor),
                        options = listOf(Color.Yellow, Color.White, Color(0xFF64D2FF), Color(0xFF30D158), Color(0xFFFF9F0A)),
                        onSelect = { updateSubtitles(subtitleSettings.copy(textColor = it.toArgb())) }
                    )
                    SubtitleColorSetting(
                        title = "رنگ پس‌زمینه",
                        current = Color(subtitleSettings.backgroundColor),
                        options = listOf(Color.Transparent, Color.Black, Color.Black.copy(alpha = 0.5f), Color(0xFF1C1C1E), Color.White),
                        onSelect = { updateSubtitles(subtitleSettings.copy(backgroundColor = it.toArgb())) }
                    )
                    SubtitleColorSetting(
                        title = "رنگ حاشیه",
                        current = Color(subtitleSettings.borderColor),
                        options = listOf(Color.Transparent, Color.Black, Color.White, Color.Red, Color.Blue),
                        onSelect = { updateSubtitles(subtitleSettings.copy(borderColor = it.toArgb())) }
                    )
                }
            }

            // Links & maintenance
            item {
                GlassSurface(Modifier.fillMaxWidth(), shape = RoundedRectangle(Glass.CardRadius)) {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        GlassRow(
                            icon = Icons.Rounded.Favorite,
                            title = "علاقه‌مندی‌ها",
                            subtitle = "فیلم‌ها و سریال‌های ذخیره شده",
                            iconTint = colors.heart,
                            onClick = { navController?.navigate("favorites") }
                        )
                        GlassDivider()
                        GlassRow(
                            icon = Icons.Rounded.Info,
                            title = "درباره ویسپار",
                            subtitle = "نسخه ${BuildConfig.VERSION_NAME}",
                            iconTint = Color(0xFF8E8E93),
                            onClick = { navController?.navigate("about") }
                        )
                        GlassDivider()
                        GlassRow(
                            icon = Icons.Rounded.SystemUpdate,
                            title = "بررسی به‌روزرسانی",
                            subtitle = "دریافت آخرین نسخه از گیت‌هاب",
                            iconTint = Color(0xFF34C759),
                            onClick = { checkForUpdates() },
                            trailing = if (isCheckingUpdate) {
                                { SpinningIcon() }
                            } else {
                                null
                            }
                        )
                        GlassDivider()
                        GlassRow(
                            icon = Icons.Rounded.RestartAlt,
                            title = "بازنشانی تنظیمات",
                            subtitle = "بازگشت همه گزینه‌ها به حالت پیش‌فرض",
                            iconTint = colors.danger,
                            onClick = { showResetDialog = true },
                            trailing = null
                        )
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        GlassAlertDialog(
            title = "بازنشانی تنظیمات",
            message = "آیا مطمئن هستید که می‌خواهید تمام تنظیمات را به مقادیر پیش‌فرض بازنشانی کنید؟",
            confirmText = "بازنشانی",
            onConfirm = {
                updateTheme(ThemeSettings())
                updateSubtitles(SubtitleSettings.DEFAULT)
                updatePlayer(VideoPlayerSettings.DEFAULT)
                showResetDialog = false
            },
            dismissText = "انصراف",
            onDismiss = { showResetDialog = false },
            icon = Icons.Rounded.RestartAlt,
            destructive = true
        )
    }

    latestVersionUrl?.let { url ->
        GlassAlertDialog(
            title = "به‌روزرسانی موجود است",
            message = "نسخه جدیدی از برنامه موجود است. آیا مایلید اکنون آن را دانلود کنید؟",
            confirmText = "دانلود",
            onConfirm = {
                latestVersionUrl = null
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
            },
            dismissText = "بعداً",
            onDismiss = { latestVersionUrl = null },
            icon = Icons.Rounded.SystemUpdate
        )
    }
}

@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassSurface(Modifier.fillMaxWidth(), shape = RoundedRectangle(Glass.CardRadius)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconTile(icon)
                Text(title, style = GlassType.Title2, color = Glass.colors.content)
            }
            content()
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = GlassType.Headline, color = Glass.colors.contentSecondary)
}

@Composable
private fun ValueLabel(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = GlassType.Headline, color = Glass.colors.contentSecondary, modifier = Modifier.weight(1f))
        Text(value, style = GlassType.Headline, color = Glass.colors.accent)
    }
}

/** Accent colours as glossy glass marbles; the chosen one grows and gets a check mark. */
@Composable
private fun AccentPalette(selected: Color, onSelect: (Color) -> Unit) {
    val colors = Glass.colors
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        colorOptions.forEach { color ->
            val isSelected = color.toArgb() == selected.toArgb()
            val scale by animateFloatAsState(if (isSelected) 1.12f else 1f, spring(0.5f, 400f), label = "swatch")
            Box(
                Modifier
                    .size(34.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .drawBackdrop(
                        backdrop = emptyBackdrop(),
                        shape = { Capsule() },
                        effects = {},
                        highlight = { Highlight.Default },
                        shadow = { Shadow(radius = 8.dp, color = color.copy(alpha = 0.45f)) }
                    )
                    .background(
                        if (color.toArgb() == MonochromeAccent.toArgb()) {
                            // Half black, half white: the monochrome option.
                            Brush.linearGradient(0.5f to Color.White, 0.5f to Color.Black)
                        } else {
                            androidx.compose.ui.graphics.SolidColor(color)
                        }
                    )
                    .then(if (isSelected) Modifier.border(2.dp, colors.content.copy(alpha = 0.85f), Capsule()) else Modifier.border(0.5.dp, colors.separator, Capsule()))
                    .clickable { onSelect(color) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Rounded.Check, "انتخاب شده",
                        tint = if (color.toArgb() == MonochromeAccent.toArgb()) colors.accent else Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .background(if (color.toArgb() == MonochromeAccent.toArgb()) colors.onAccent else Color.Transparent, Capsule())
                            .padding(2.dp)
                    )
                }
            }
        }
    }
}

/** A mock video frame that shows the subtitle exactly as the player will render it. */
@Composable
private fun SubtitlePreview(settings: SubtitleSettings) {
    val colors = Glass.colors
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(21f / 9f)
            .clip(RoundedRectangle(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF3A3A3C), Color(0xFF1C1C1E), Color.Black)
                )
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        val border = Color(settings.borderColor)
        Text(
            "این یک نمونه زیرنویس است",
            color = Color(settings.textColor),
            fontSize = settings.textSize.sp,
            fontWeight = FontWeight.Bold,
            style = GlassType.Body.copy(
                shadow = if (border.alpha > 0f) androidx.compose.ui.graphics.Shadow(border, blurRadius = 6f) else null
            ),
            modifier = Modifier
                .padding(bottom = 14.dp)
                .background(Color(settings.backgroundColor), RoundedRectangle(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun SubtitleColorSetting(
    title: String,
    current: Color,
    options: List<Color>,
    onSelect: (Color) -> Unit
) {
    val colors = Glass.colors
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = GlassType.Callout, color = colors.contentSecondary, modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val isSelected = option.toArgb() == current.toArgb()
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(RoundedRectangle(9.dp))
                        .background(if (option.alpha == 0f) colors.content.copy(alpha = 0.06f) else option)
                        .border(
                            width = if (isSelected) 2.dp else 0.5.dp,
                            color = if (isSelected) colors.accent else colors.content.copy(alpha = 0.2f),
                            shape = RoundedRectangle(9.dp)
                        )
                        .clickable { onSelect(option) },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isSelected -> Icon(
                            Icons.Rounded.Check, null,
                            tint = if (option.alpha == 0f || option.toArgb() == Color.White.toArgb() || option.toArgb() == Color.Yellow.toArgb()) colors.accent else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        option.alpha == 0f -> Icon(Icons.Rounded.Block, "بدون رنگ", tint = colors.contentTertiary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SpinningIcon() {
    val transition = rememberInfiniteTransition(label = "spin")
    val rotation by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing)), label = "r")
    Icon(
        Icons.Rounded.Sync,
        "در حال بررسی",
        tint = Glass.colors.accent,
        modifier = Modifier.size(22.dp).graphicsLayer { rotationZ = rotation }
    )
}
