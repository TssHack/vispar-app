package com.fazli.vispar.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
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
    val danger: Color,
    val gold: Color,
    val heart: Color,
    val scrim: Color,
)

fun glassColors(isDark: Boolean, accent: Color): GlassColors {
    val onAccent = if (accent.luminance() > 0.55f) Color(0xFF111114) else Color.White
    return if (isDark) {
        GlassColors(
            isDark = true,
            accent = lerp(accent, Color.White, 0.12f),
            onAccent = onAccent,
            content = Color.White,
            contentSecondary = Color.White.copy(alpha = 0.72f),
            contentTertiary = Color.White.copy(alpha = 0.48f),
            glassTint = Color(0xFF16161C).copy(alpha = 0.34f),
            glassTintStrong = Color(0xFF101016).copy(alpha = 0.55f),
            glassFallback = Color(0xFF1C1C24).copy(alpha = 0.86f),
            separator = Color.White.copy(alpha = 0.10f),
            backgroundTop = Color(0xFF07070C),
            backgroundBottom = lerp(Color(0xFF0B0B12), accent, 0.10f),
            danger = Color(0xFFFF6961),
            gold = Color(0xFFFFD60A),
            heart = Color(0xFFFF375F),
            scrim = Color.Black.copy(alpha = 0.45f),
        )
    } else {
        GlassColors(
            isDark = false,
            accent = accent,
            onAccent = onAccent,
            content = Color(0xFF0E0E12),
            contentSecondary = Color(0xFF0E0E12).copy(alpha = 0.66f),
            contentTertiary = Color(0xFF0E0E12).copy(alpha = 0.42f),
            glassTint = Color.White.copy(alpha = 0.42f),
            glassTintStrong = Color.White.copy(alpha = 0.62f),
            glassFallback = Color.White.copy(alpha = 0.88f),
            separator = Color.Black.copy(alpha = 0.08f),
            backgroundTop = Color(0xFFF6F5FA),
            backgroundBottom = lerp(Color(0xFFEDEBF4), accent, 0.10f).compositeOver(Color.White),
            danger = Color(0xFFE5383B),
            gold = Color(0xFFF5A300),
            heart = Color(0xFFFF2D55),
            scrim = Color(0xFF1B1B2A).copy(alpha = 0.22f),
        )
    }
}

val LocalGlassColors = staticCompositionLocalOf { glassColors(isDark = true, accent = Color(0xFF7C5CFF)) }

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
