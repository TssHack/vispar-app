package com.fazli.vispar.ui.glass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawPlainBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.runtimeShaderEffect
import com.kyant.backdrop.isRuntimeShaderSupported
import com.kyant.shapes.Capsule

val TopBarHeight: Dp = 56.dp

/**
 * Screen frame for the glass UI.
 *
 * Content is drawn full-bleed and scrolls *under* a floating glass top bar. The bar and the
 * scroll-edge blur sample both the background and the content, so posters visibly refract through
 * the chrome as they pass beneath it.
 *
 * @param titleAlpha lets screens with a large in-content title fade the compact title in as the
 *        large one scrolls away.
 */
@Composable
fun GlassScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleAlpha: () -> Float = { 1f },
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    background: Backdrop = LocalBackdrop.current,
    content: @Composable (PaddingValues) -> Unit
) {
    val contentLayer = rememberLayerBackdrop()
    val chromeBackdrop = rememberCombinedBackdrop(background, contentLayer)
    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBar = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomInset = LocalBottomBarInset.current
    val hasTopBar = title != null || onBack != null

    val contentPadding = PaddingValues(
        top = statusBar + if (hasTopBar) TopBarHeight + 8.dp else 8.dp,
        bottom = navBar + bottomInset + 24.dp
    )

    Box(modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().layerBackdrop(contentLayer)) {
            CompositionLocalProvider(LocalBackdrop provides background) {
                content(contentPadding)
            }
        }

        ScrollEdgeEffect(
            backdrop = chromeBackdrop,
            height = statusBar + if (hasTopBar) TopBarHeight + 20.dp else 24.dp
        )

        if (hasTopBar) {
            CompositionLocalProvider(LocalBackdrop provides chromeBackdrop) {
                GlassTopBar(
                    title = title,
                    titleAlpha = titleAlpha,
                    onBack = onBack,
                    actions = actions,
                    modifier = Modifier.padding(top = statusBar)
                )
            }
        }
    }
}

@Composable
fun GlassTopBar(
    title: String?,
    modifier: Modifier = Modifier,
    titleAlpha: () -> Float = { 1f },
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = Glass.colors
    Box(
        modifier
            .fillMaxWidth()
            .height(TopBarHeight)
            .padding(horizontal = Glass.ScreenPadding),
        contentAlignment = Alignment.Center
    ) {
        if (title != null) {
            Box(
                Modifier
                    .graphicsLayer { alpha = titleAlpha() }
                    .widthIn(max = 220.dp)
                    .glass(
                        backdrop = LocalBackdrop.current,
                        shape = Capsule(),
                        colors = colors,
                        style = GlassStyle(blur = 8.dp, refractionHeight = 10.dp, refractionAmount = 20.dp),
                        shadow = null
                    )
                    .padding(horizontal = 18.dp, vertical = 9.dp)
            ) {
                Text(
                    title,
                    style = GlassType.Headline,
                    color = colors.content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                // The app is right-to-left, so "back" points to the right.
                LiquidIconButton(
                    onClick = onBack,
                    icon = Icons.AutoMirrored.Rounded.ArrowBackIos,
                    contentDescription = "بازگشت",
                    iconSize = 18.dp
                )
            }
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                actions()
            }
        }
    }
}

/**
 * Progressive blur under the status bar: content dissolves into frosted glass as it scrolls up,
 * instead of colliding with the clock and the floating buttons.
 */
@Composable
fun ScrollEdgeEffect(backdrop: Backdrop, height: Dp, modifier: Modifier = Modifier) {
    val colors = Glass.colors
    val tint = colors.backgroundTop
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .then(
                if (isRuntimeShaderSupported()) {
                    Modifier.drawPlainBackdrop(
                        backdrop = backdrop,
                        shape = { RectangleShape },
                        effects = {
                            blur(6.dp.toPx())
                            runtimeShaderEffect(
                                "VisparScrollEdge",
                                """
uniform shader content;
uniform float2 size;
layout(color) uniform half4 tint;
uniform float tintIntensity;

half4 main(float2 coord) {
    float a = smoothstep(size.y, size.y * 0.35, coord.y);
    return mix(content.eval(coord) * a, tint * a, tintIntensity);
}""",
                                "content"
                            ) {
                                setFloatUniform("size", size.width, size.height)
                                setColorUniform("tint", tint)
                                setFloatUniform("tintIntensity", if (colors.isDark) 0.45f else 0.55f)
                            }
                        }
                    )
                } else {
                    Modifier.drawPlainBackdrop(
                        backdrop = backdrop,
                        shape = { RectangleShape },
                        effects = {},
                        onDrawBackdrop = { },
                        onDrawSurface = {
                            drawRect(Brush.verticalGradient(listOf(tint.copy(alpha = 0.92f), Color.Transparent)))
                        }
                    )
                }
            )
    )
}

/** A large title that sits at the top of a tab's scroll content, iOS style. */
@Composable
fun LargeTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    val colors = Glass.colors
    androidx.compose.foundation.layout.Column(modifier.padding(horizontal = Glass.ScreenPadding, vertical = 4.dp)) {
        Text(title, style = GlassType.LargeTitle, color = colors.content)
        if (subtitle != null) {
            Text(subtitle, style = GlassType.Callout, color = colors.contentSecondary)
        }
    }
}
