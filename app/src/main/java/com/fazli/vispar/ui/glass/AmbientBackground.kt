package com.fazli.vispar.ui.glass

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Rotates the hue of [color] by [degrees] keeping saturation and value. */
fun Color.shiftHue(degrees: Float, saturation: Float? = null, value: Float? = null): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
    hsv[0] = ((hsv[0] + degrees) % 360f + 360f) % 360f
    if (saturation != null) hsv[1] = saturation
    if (value != null) hsv[2] = value
    return Color(android.graphics.Color.HSVToColor(hsv))
}

/**
 * The "aurora" canvas every glass surface floats on: a deep base gradient with a few large, soft
 * colour fields derived from the accent colour. Glass needs something colourful behind it to
 * refract, otherwise it reads as flat grey.
 *
 * The background is static by default — an always-animating backdrop would force every glass
 * surface on screen to re-render each frame. Set [animated] for hero moments (splash, empty
 * states) where nothing else is on screen.
 */
@Composable
fun AmbientBackground(
    modifier: Modifier = Modifier,
    animated: Boolean = false,
    colors: GlassColors = Glass.colors,
) {
    val phase: State<Float> = if (animated) {
        val transition = rememberInfiniteTransition(label = "aurora")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(24_000, easing = LinearEasing), RepeatMode.Restart),
            label = "aurora-phase"
        )
    } else {
        remember { mutableFloatStateOf(0.18f) }
    }

    val accent = colors.accent
    val fields = remember(accent, colors.isDark) {
        val alpha = if (colors.isDark) 1f else 0.85f
        listOf(
            AuroraField(accent.copy(alpha = 0.55f * alpha), 0.18f, 0.10f, 0.75f, 0.0f),
            AuroraField(accent.shiftHue(48f).copy(alpha = 0.42f * alpha), 0.90f, 0.32f, 0.65f, 0.33f),
            AuroraField(accent.shiftHue(-62f).copy(alpha = 0.38f * alpha), 0.30f, 0.72f, 0.70f, 0.66f),
            AuroraField(accent.shiftHue(160f).copy(alpha = 0.22f * alpha), 0.85f, 0.95f, 0.60f, 0.5f),
        )
    }

    Canvas(modifier.fillMaxSize()) {
        drawRect(Brush.verticalGradient(listOf(colors.backgroundTop, colors.backgroundBottom)))
        val t = phase.value
        fields.forEach { drawField(it, t) }
        // A faint vignette grounds the composition and helps the floating chrome stand out.
        drawRect(
            Brush.radialGradient(
                colors = listOf(Color.Transparent, colors.backgroundTop.copy(alpha = if (colors.isDark) 0.55f else 0.25f)),
                center = Offset(size.width / 2f, size.height * 0.45f),
                radius = size.maxDimension * 0.85f
            )
        )
    }
}

private data class AuroraField(
    val color: Color,
    val x: Float,
    val y: Float,
    val radius: Float,
    val phaseOffset: Float,
)

private fun DrawScope.drawField(field: AuroraField, t: Float) {
    val angle = 2f * PI.toFloat() * (t + field.phaseOffset)
    val cx = size.width * (field.x + 0.10f * cos(angle))
    val cy = size.height * (field.y + 0.06f * sin(angle * 2f))
    val radius = size.maxDimension * field.radius * (0.92f + 0.08f * sin(angle))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(field.color, field.color.copy(alpha = field.color.alpha * 0.35f), Color.Transparent),
            center = Offset(cx, cy),
            radius = radius
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
}
