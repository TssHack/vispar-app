package com.fazli.vispar.ui.glass

import com.fazli.vispar.R

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.isRenderEffectSupported
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
    /** How much the glass blurs and refracts; user preference. */
    val material: GlassMaterial = GlassMaterial.Regular,
)

/**
 * Glass "material" presets, from crystal clear to fully opaque. [Solid] is the equivalent of iOS'
 * "Reduce Transparency": no blur or refraction at all, which is also far cheaper to render and is
 * the default on Android TV boxes.
 */
enum class GlassMaterial(@param:androidx.annotation.StringRes val label: Int, val blurScale: Float, val tintScale: Float) {
    Clear(R.string.glass_clear, 0.35f, 0.55f),
    Regular(R.string.glass_regular, 1f, 1f),
    Frosted(R.string.glass_frosted, 1.9f, 1.4f),
    Solid(R.string.glass_solid, 0f, 0f),
}

/** True when the user picked the monochrome (black/white) accent. */
val GlassColors.isMonochromeAccent: Boolean
    get() = accent.toArgb() == resolveAccent(MonochromeAccent, isDark).toArgb()

/** True when glass is actually rendered live (blur available and transparency not reduced). */
val GlassColors.liveGlass: Boolean
    get() = material != GlassMaterial.Solid && isRenderEffectSupported()

/** The wash to paint over a glass surface for the current material. */
fun GlassColors.surfaceTint(tint: Color): Color = when {
    liveGlass -> if (tint.isSpecified) tint.copy(alpha = (tint.alpha * material.tintScale).coerceIn(0f, 1f)) else tint
    !tint.isSpecified || tint == glassTint || tint == glassTintStrong -> glassFallback
    else -> tint.copy(alpha = (tint.alpha * 2.6f).coerceIn(0.6f, 0.94f))
}

/**
 * Standard glass effect chain (vibrancy, blur, lens) scaled for the current [GlassMaterial].
 * Does nothing for [GlassMaterial.Solid].
 */
fun BackdropEffectScope.glassEffects(
    colors: GlassColors,
    blurRadius: Float,
    refractionHeight: Float,
    refractionAmount: Float,
    depthEffect: Boolean = false,
    chromaticAberration: Boolean = false,
) {
    if (colors.material == GlassMaterial.Solid) return
    if (colors.isDark) vibrancy() else colorControls(brightness = 0.08f, saturation = 1.6f)
    blur(blurRadius * colors.material.blurScale)
    lens(refractionHeight, refractionAmount, depthEffect = depthEffect, chromaticAberration = chromaticAberration)
}

/** For [GlassMaterial.Solid] the backdrop is not drawn at all — only the opaque wash. */
val GlassColors.backdropDrawer: DrawScope.(drawBackdrop: DrawScope.() -> Unit) -> Unit
    get() = if (material == GlassMaterial.Solid) NoBackdrop else DrawBackdrop

private val NoBackdrop: DrawScope.(DrawScope.() -> Unit) -> Unit = { }
private val DrawBackdrop: DrawScope.(DrawScope.() -> Unit) -> Unit = { it() }

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

fun glassColors(isDark: Boolean, accent: Color, material: GlassMaterial = GlassMaterial.Regular): GlassColors {
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
            material = material,
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
            material = material,
        )
    }
}

val LocalGlassColors = staticCompositionLocalOf { glassColors(isDark = true, accent = MonochromeAccent) }

/**
 * The backdrop glass surfaces sample by default. The app shell provides the ambient background
 * here; screens with their own hero artwork provide a richer one.
 */
val LocalBackdrop = compositionLocalOf<Backdrop> { emptyBackdrop() }

/** Whether liquid controls give haptic feedback. */
val LocalHapticsEnabled = staticCompositionLocalOf { true }

/** Number of poster columns for grids, decided by device and the user's density preference. */
val LocalPosterColumns = compositionLocalOf { 2 }

/** True on Android TV: controls show a focus ring and grow when focused by the D-pad. */
val LocalIsTv = staticCompositionLocalOf { false }

/** Space a screen must leave at the bottom so content can scroll beneath the floating tab bar. */
val LocalBottomBarInset = compositionLocalOf { 0.dp }

object Glass {
    val colors: GlassColors
        @Composable @ReadOnlyComposable get() = LocalGlassColors.current

    val ScreenPadding: Dp = 16.dp
    val CardRadius: Dp = 28.dp
    val TileRadius: Dp = 22.dp
}
