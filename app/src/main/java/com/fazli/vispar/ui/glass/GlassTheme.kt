package com.fazli.vispar.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop

/**
 * Colour tokens for the Liquid Glass design language.
 *
 * Glass is never a flat colour: every surface is the blurred, refracted backdrop plus a thin
 * [glassTint] wash on top. The tokens below are tuned so that text stays legible on that wash in
 * both light and dark appearance.
 */
@Immutable
data class GlassColors(
    val isDark: Boolean,
    /** The user-selectable brand colour. */
    val accent: Color,
    /** Readable foreground drawn on top of [accent]. */
    val onAccent: Color,
    val content: Color,
    val contentSecondary: Color,
    val contentTertiary: Color,
    /** Wash painted over the blurred backdrop of every glass surface. */
    val glassTint: Color,
    /** Stronger wash for surfaces that hold a lot of text (dialogs, long descriptions). */
    val glassTintStrong: Color,
    /** Opaque-ish wash used where blur is unavailable (Android < 12). */
    val glassFallback: Color,
    /** Hairline used for separators inside glass. */
    val separator: Color,
    /** Base colours of the ambient background. */
    val backgroundTop: Color,
    val backgroundBottom: Color,
    /** Neutral fill behind images while they load. */
    val placeholder: Color,
    val danger: Color,
    val gold: Color,
    val heart: Color,
    val scrim: Color,
)

/**
 * Sentinel accent meaning "monochrome": white on dark, black on light, the way Apple's media apps
 * draw their primary buttons.
 */
val MonochromeAccent = Color(0xFF000000)

fun resolveAccent(accent: Color, isDark: Boolean): Color =
    if (accent.toArgb() == MonochromeAccent.toArgb()) {
        if (isDark) Color.White else Color(0xFF111111)
    } else {
        accent
    }

fun glassColors(isDark: Boolean, accent: Color): GlassColors {
    @Suppress("NAME_SHADOWING")
    val accent = resolveAccent(accent, isDark)
    val onAccent = if (accent.luminance() > 0.55f) Color(0xFF111111) else Color.White
    return if (isDark) {
        GlassColors(
            isDark = true,
            accent = accent,
            onAccent = onAccent,
            content = Color.White,
            contentSecondary = Color.White.copy(alpha = 0.72f),
            contentTertiary = Color.White.copy(alpha = 0.48f),
            glassTint = Color(0xFF1C1C1E).copy(alpha = 0.40f),
            glassTintStrong = Color(0xFF1C1C1E).copy(alpha = 0.62f),
            glassFallback = Color(0xFF1C1C1E).copy(alpha = 0.90f),
            separator = Color.White.copy(alpha = 0.10f),
            backgroundTop = Color.Black,
            backgroundBottom = Color.Black,
            placeholder = Color(0xFF1C1C1E),
            danger = Color(0xFFFF453A),
            gold = Color(0xFFFFD60A),
            heart = Color(0xFFFF375F),
            scrim = Color.Black.copy(alpha = 0.45f),
        )
    } else {
        GlassColors(
            isDark = false,
            accent = accent,
            onAccent = onAccent,
            content = Color.Black,
            contentSecondary = Color(0xFF3C3C43).copy(alpha = 0.75f),
            contentTertiary = Color(0xFF3C3C43).copy(alpha = 0.45f),
            glassTint = Color.White.copy(alpha = 0.55f),
            glassTintStrong = Color.White.copy(alpha = 0.72f),
            glassFallback = Color.White.copy(alpha = 0.92f),
            separator = Color(0xFF3C3C43).copy(alpha = 0.18f),
            backgroundTop = Color.White,
            backgroundBottom = Color(0xFFF2F2F7),
            placeholder = Color(0xFFE5E5EA),
            danger = Color(0xFFFF3B30),
            gold = Color(0xFFFF9F0A),
            heart = Color(0xFFFF2D55),
            scrim = Color.Black.copy(alpha = 0.20f),
        )
    }
}

val LocalGlassColors = staticCompositionLocalOf { glassColors(isDark = true, accent = MonochromeAccent) }

/**
 * The backdrop glass surfaces sample by default. The app shell provides the ambient background
 * here; screens with their own hero artwork provide a richer one.
 */
val LocalBackdrop = compositionLocalOf<Backdrop> { emptyBackdrop() }

/** Space a screen must leave at the bottom so content can scroll beneath the floating tab bar. */
val LocalBottomBarInset = compositionLocalOf { 0.dp }

object Glass {
    val colors: GlassColors
        @Composable @ReadOnlyComposable get() = LocalGlassColors.current

    val ScreenPadding: Dp = 16.dp
    val CardRadius: Dp = 28.dp
    val TileRadius: Dp = 22.dp
}
