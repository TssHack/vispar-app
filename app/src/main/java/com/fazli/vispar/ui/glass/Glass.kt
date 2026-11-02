package com.fazli.vispar.ui.glass

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.painter.Painter
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.isRenderEffectSupported
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

/** Visual recipe for a glass surface. */
data class GlassStyle(
    val blur: Dp = 10.dp,
    val refractionHeight: Dp = 14.dp,
    val refractionAmount: Dp = 28.dp,
    val chromaticAberration: Boolean = false,
    val depthEffect: Boolean = false,
) {
    companion object {
        /** Large panels: cards, sheets, the tab bar. */
        val Regular = GlassStyle()

        /** Text-heavy panels: stronger frost, gentler lens so text stays calm. */
        val Frosted = GlassStyle(blur = 18.dp, refractionHeight = 12.dp, refractionAmount = 20.dp)

        /** Small controls: buttons, chips. Clearer glass with a pronounced lens. */
        val Clear = GlassStyle(blur = 2.dp, refractionHeight = 12.dp, refractionAmount = 24.dp)

        /** Dialogs float above everything and get the full 3D treatment. */
        val Floating = GlassStyle(blur = 14.dp, refractionHeight = 24.dp, refractionAmount = 48.dp, depthEffect = true)
    }
}

private val DefaultGlassShadow = Shadow(radius = 20.dp, color = Color.Black.copy(alpha = 0.12f))

/**
 * Turns the receiver into a liquid glass surface that samples [backdrop].
 *
 * The [shape] must be a rounded rectangular shape ([RoundedRectangle], [Capsule] or a
 * corner based shape) because the lens shader refracts along its signed distance field.
 *
 * On Android 12+ the backdrop is blurred, saturated and refracted. On older versions the library
 * draws the backdrop as-is, so a denser frosted wash is painted instead to keep text legible.
 */
fun Modifier.glass(
    backdrop: Backdrop,
    shape: Shape,
    colors: GlassColors,
    style: GlassStyle = GlassStyle.Regular,
    tint: Color = colors.glassTint,
    highlight: Highlight? = Highlight.Default,
    shadow: Shadow? = DefaultGlassShadow,
    layerBlock: (GraphicsLayerScope.() -> Unit)? = null,
    exportedBackdrop: LayerBackdrop? = null,
): Modifier = this.drawBackdrop(
    backdrop = backdrop,
    shape = { shape },
    effects = {
        glassEffects(
            colors,
            style.blur.toPx(),
            style.refractionHeight.toPx(),
            style.refractionAmount.toPx(),
            depthEffect = style.depthEffect,
            chromaticAberration = style.chromaticAberration
        )
    },
    highlight = { highlight },
    shadow = { shadow },
    layerBlock = layerBlock,
    exportedBackdrop = exportedBackdrop,
    onDrawBackdrop = colors.backdropDrawer,
    onDrawSurface = {
        val wash = colors.surfaceTint(tint)
        if (wash.isSpecified) drawRect(wash)
    }
)

/**
 * D-pad focus treatment shared by every glass control: the element grows slightly and gets a
 * bright ring, the way tvOS lifts the focused item.
 */
@Composable
fun Modifier.tvFocusable(shape: Shape, focused: Boolean, onFocus: (Boolean) -> Unit): Modifier {
    val colors = Glass.colors
    val scale by animateFloatAsState(if (focused) 1.06f else 1f, spring(0.6f, 420f), label = "focus")
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .onFocusChanged { onFocus(it.isFocused) }
        .then(if (focused) Modifier.border(2.5.dp, colors.content.copy(alpha = 0.9f), shape) else Modifier)
}

/** Click handler that adds a light haptic tick when haptics are enabled. */
@Composable
fun rememberHapticClick(onClick: () -> Unit): () -> Unit {
    val haptics = LocalHapticFeedback.current
    val enabled = LocalHapticsEnabled.current
    val latest by rememberUpdatedState(onClick)
    return remember(haptics, enabled) {
        {
            if (enabled) haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
            latest()
        }
    }
}

/**
 * A rounded glass panel. When [onClick] is set the panel becomes a liquid control: it swells under
 * the finger, follows it a little like a drop of water and lights up where it is touched.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedRectangle(Glass.CardRadius),
    backdrop: Backdrop = LocalBackdrop.current,
    style: GlassStyle = GlassStyle.Regular,
    tint: Color = Glass.colors.glassTint,
    highlight: Highlight? = Highlight.Default,
    shadow: Shadow? = DefaultGlassShadow,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = Glass.colors
    val scope = rememberCoroutineScope()
    val interactive = onClick != null && enabled
    val highlightFx = remember(scope) { InteractiveHighlight(scope) }
    val surfaceLayer = rememberLayerBackdrop()
    var focused by remember { mutableStateOf(false) }
    val click = rememberHapticClick(onClick ?: {})

    Box(
        modifier = modifier
            .then(if (interactive) Modifier.tvFocusable(shape, focused) { focused = it } else Modifier)
            .glass(
                backdrop = backdrop,
                shape = shape,
                colors = colors,
                style = style,
                tint = tint,
                highlight = highlight,
                shadow = shadow,
                layerBlock = if (interactive) {
                    {
                        // A gentle swell rather than the button wobble: panels are big.
                        val progress = highlightFx.pressProgress
                        val s = lerp(1f, 1f - 6.dp.toPx() / size.width.coerceAtLeast(1f), progress)
                        scaleX = s
                        scaleY = s
                    }
                } else {
                    null
                },
                exportedBackdrop = surfaceLayer
            )
            .then(
                if (interactive) {
                    Modifier
                        .clickable(
                            interactionSource = null,
                            indication = null,
                            role = Role.Button,
                            onClick = click
                        )
                        .then(highlightFx.modifier)
                        .then(highlightFx.gestureModifier)
                } else {
                    Modifier
                }
            ),
        contentAlignment = contentAlignment
    ) {
        // Controls nested in this panel refract the panel itself, not the screen behind it.
        CompositionLocalProvider(LocalBackdrop provides surfaceLayer) { content() }
    }
}

/**
 * Capsule shaped liquid button with the signature "drop of water" response: it scales up when
 * pressed and is dragged along with the finger before springing back.
 *
 * @param tint when specified the glass is tinted with this colour (a filled, primary button).
 */
@Composable
fun LiquidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    shape: Shape = Capsule(),
    height: Dp = 48.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp),
    enabled: Boolean = true,
    isInteractive: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val colors = Glass.colors
    val scope = rememberCoroutineScope()
    val highlightFx = remember(scope) { InteractiveHighlight(scope) }
    val interactive = isInteractive && enabled
    var focused by remember { mutableStateOf(false) }
    val click = rememberHapticClick(onClick)

    Row(
        modifier
            .tvFocusable(shape, focused) { focused = it }
            .drawBackdrop(
                backdrop = backdrop,
                shape = { shape },
                effects = { glassEffects(colors, 2.dp.toPx(), 12.dp.toPx(), 24.dp.toPx()) },
                onDrawBackdrop = colors.backdropDrawer,
                shadow = { Shadow(radius = 12.dp, color = Color.Black.copy(alpha = 0.10f)) },
                layerBlock = if (interactive) {
                    {
                        val width = size.width
                        val h = size.height
                        val progress = highlightFx.pressProgress
                        val scale = lerp(1f, 1f + 4.dp.toPx() / h, progress)

                        val maxOffset = size.minDimension
                        val initialDerivative = 0.05f
                        val offset = highlightFx.offset
                        translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                        translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                        val maxDragScale = 4.dp.toPx() / h
                        val angle = atan2(offset.y, offset.x)
                        scaleX = scale + maxDragScale * abs(cos(angle) * offset.x / size.maxDimension) *
                                (width / h).fastCoerceAtMost(1f)
                        scaleY = scale + maxDragScale * abs(sin(angle) * offset.y / size.maxDimension) *
                                (h / width).fastCoerceAtMost(1f)
                    }
                } else {
                    null
                },
                onDrawSurface = {
                    if (tint.isSpecified) {
                        if (colors.liveGlass) drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = if (colors.liveGlass) 0.78f else 1f))
                    } else if (!colors.liveGlass) {
                        drawRect(colors.glassFallback)
                    } else {
                        drawRect(colors.glassTint.copy(alpha = colors.glassTint.alpha * 0.6f * colors.material.tintScale))
                    }
                    if (surfaceColor.isSpecified) drawRect(surfaceColor)
                    if (!enabled) drawRect(Color.Gray.copy(alpha = 0.35f))
                }
            )
            .clickable(
                enabled = enabled,
                interactionSource = null,
                indication = if (interactive) null else LocalIndication.current,
                role = Role.Button,
                onClick = click
            )
            .then(
                if (interactive) {
                    Modifier
                        .then(highlightFx.modifier)
                        .then(highlightFx.gestureModifier)
                } else {
                    Modifier
                }
            )
            .defaultMinSize(minWidth = height)
            .height(height)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/** Circular liquid button holding a single icon. */
@Composable
fun LiquidIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current,
    tint: Color = Color.Unspecified,
    iconTint: Color = if (tint.isSpecified) Glass.colors.onAccent else Glass.colors.content,
    size: Dp = 46.dp,
    iconSize: Dp = 22.dp,
    enabled: Boolean = true,
) {
    LiquidButton(
        onClick = onClick,
        modifier = modifier.size(size),
        backdrop = backdrop,
        tint = tint,
        height = size,
        contentPadding = PaddingValues(0.dp),
        enabled = enabled,
    ) {
        Icon(icon, contentDescription, tint = iconTint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun LiquidIconButton(
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current,
    tint: Color = Color.Unspecified,
    iconTint: Color = if (tint.isSpecified) Glass.colors.onAccent else Glass.colors.content,
    size: Dp = 46.dp,
    iconSize: Dp = 22.dp,
) {
    LiquidButton(
        onClick = onClick,
        modifier = modifier.size(size),
        backdrop = backdrop,
        tint = tint,
        height = size,
        contentPadding = PaddingValues(0.dp),
    ) {
        Icon(painter, contentDescription, tint = iconTint, modifier = Modifier.size(iconSize))
    }
}

/** Small non-interactive glass capsule, used for ratings, genres and badges. */
@Composable
fun GlassChip(
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current,
    tint: Color = Glass.colors.glassTint,
    contentPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier
            .glass(
                backdrop = backdrop,
                shape = Capsule(),
                colors = Glass.colors,
                style = GlassStyle(blur = 6.dp, refractionHeight = 6.dp, refractionAmount = 12.dp),
                tint = tint,
                shadow = null,
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
