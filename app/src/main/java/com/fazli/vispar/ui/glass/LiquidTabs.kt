/*
 * Liquid tab bar / segmented control.
 *
 * Adapted from the Backdrop catalog's LiquidBottomTabs by Kyant
 * (https://github.com/Kyant0/AndroidLiquidGlass), licensed under the Apache License, Version 2.0.
 */
package com.fazli.vispar.ui.glass

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.isRenderEffectSupported
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

private val LocalLiquidTabScale = staticCompositionLocalOf { { 1f } }

/**
 * A capsule of tabs with a liquid lens that marks the selection. Tap a tab or grab the lens and
 * drag it: it stretches with velocity, magnifies what is under it and snaps to the nearest tab.
 *
 * [content] is composed twice — once normally and once tinted with the accent colour — and the
 * lens reveals the tinted copy, which is how the selected tab lights up "through" the glass.
 */
@Composable
fun LiquidTabs(
    selectedIndex: Int,
    onTabSelected: (index: Int) -> Unit,
    tabsCount: Int,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = LocalBackdrop.current,
    height: Dp = 64.dp,
    accentColor: Color = Glass.colors.accent,
    content: @Composable RowScope.() -> Unit
) {
    val colors = Glass.colors
    val containerColor = if (isRenderEffectSupported()) colors.glassTint else colors.glassFallback
    val tabsBackdrop = rememberLayerBackdrop()
    val innerPadding = 4.dp
    val lensHeight = height - innerPadding * 2
    val latestOnTabSelected by rememberUpdatedState(onTabSelected)

    BoxWithConstraints(modifier, contentAlignment = Alignment.CenterStart) {
        val density = LocalDensity.current
        val maxWidthPx = constraints.maxWidth.toFloat()
        val tabWidth = with(density) { (maxWidthPx - (innerPadding * 2).toPx()) / tabsCount }

        val offsetAnimation = remember { Animatable(0f) }
        val panelOffset by remember(density, maxWidthPx) {
            derivedStateOf {
                val fraction = (offsetAnimation.value / maxWidthPx).fastCoerceIn(-1f, 1f)
                with(density) { 4.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction)) }
            }
        }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        val dampedDragAnimation = remember(animationScope, tabsCount, tabWidth) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = selectedIndex.toFloat().coerceIn(0f, (tabsCount - 1).toFloat()),
                valueRange = 0f..(tabsCount - 1).toFloat(),
                visibilityThreshold = 0.001f,
                initialScale = 1f,
                pressedScale = 78f / 56f,
                onDragStarted = {},
                onDragStopped = {
                    val target = targetValue.fastRoundToInt().fastCoerceIn(0, tabsCount - 1)
                    animateToValue(target.toFloat())
                    latestOnTabSelected(target)
                    animationScope.launch { offsetAnimation.animateTo(0f, spring(1f, 300f, 0.5f)) }
                },
                onDrag = { _, dragAmount ->
                    updateValue(
                        (targetValue + dragAmount.x / tabWidth * if (isLtr) 1f else -1f)
                            .fastCoerceIn(0f, (tabsCount - 1).toFloat())
                    )
                    animationScope.launch { offsetAnimation.snapTo(offsetAnimation.value + dragAmount.x) }
                }
            )
        }

        LaunchedEffect(selectedIndex, dampedDragAnimation) {
            if (selectedIndex in 0 until tabsCount &&
                dampedDragAnimation.targetValue.fastRoundToInt() != selectedIndex
            ) {
                dampedDragAnimation.animateToValue(selectedIndex.toFloat())
            }
        }

        val interactiveHighlight = remember(animationScope, dampedDragAnimation) {
            InteractiveHighlight(
                animationScope = animationScope,
                position = { size, _ ->
                    Offset(
                        if (isLtr) (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset
                        else size.width - (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset,
                        size.height / 2f
                    )
                }
            )
        }

        val glassEffects: com.kyant.backdrop.BackdropEffectScope.() -> Unit = {
            if (colors.isDark) vibrancy() else colorControls(brightness = 0.08f, saturation = 1.6f)
            blur(8.dp.toPx())
            lens(24.dp.toPx(), 24.dp.toPx())
        }

        // 1. The visible bar.
        Row(
            Modifier
                .graphicsLayer { translationX = panelOffset }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = glassEffects,
                    shadow = { Shadow(radius = 24.dp, color = Color.Black.copy(alpha = 0.16f)) },
                    layerBlock = {
                        val progress = dampedDragAnimation.pressProgress
                        val scale = lerp(1f, 1f + 16.dp.toPx() / size.width, progress)
                        scaleX = scale
                        scaleY = scale
                    },
                    onDrawSurface = { drawRect(containerColor) }
                )
                .then(interactiveHighlight.modifier)
                .height(height)
                .fillMaxWidth()
                .padding(innerPadding),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )

        // 2. An invisible, accent-tinted twin that only the lens reveals.
        CompositionLocalProvider(
            LocalLiquidTabScale provides { lerp(1f, 1.2f, dampedDragAnimation.pressProgress) }
        ) {
            Row(
                Modifier
                    .clearAndSetSemantics {}
                    .alpha(0f)
                    .layerBackdrop(tabsBackdrop)
                    .graphicsLayer { translationX = panelOffset }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { Capsule() },
                        effects = {
                            val progress = dampedDragAnimation.pressProgress
                            if (colors.isDark) vibrancy() else colorControls(brightness = 0.08f, saturation = 1.6f)
                            blur(8.dp.toPx())
                            lens(24.dp.toPx() * progress, 24.dp.toPx() * progress)
                        },
                        highlight = {
                            Highlight.Default.copy(alpha = dampedDragAnimation.pressProgress)
                        },
                        onDrawSurface = { drawRect(containerColor) }
                    )
                    .then(interactiveHighlight.modifier)
                    .height(lensHeight)
                    .fillMaxWidth()
                    .padding(horizontal = innerPadding)
                    .graphicsLayer(colorFilter = ColorFilter.tint(accentColor)),
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }

        // 3. The lens.
        Box(
            Modifier
                .padding(horizontal = innerPadding)
                .graphicsLayer {
                    translationX =
                        if (isLtr) dampedDragAnimation.value * tabWidth + panelOffset
                        else size.width - (dampedDragAnimation.value + 1f) * tabWidth + panelOffset
                }
                .then(interactiveHighlight.gestureModifier)
                .then(dampedDragAnimation.modifier)
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(backdrop, tabsBackdrop),
                    shape = { Capsule() },
                    effects = {
                        val progress = dampedDragAnimation.pressProgress
                        lens(
                            10.dp.toPx() * progress,
                            14.dp.toPx() * progress,
                            chromaticAberration = true
                        )
                    },
                    highlight = { Highlight.Default.copy(alpha = dampedDragAnimation.pressProgress) },
                    shadow = { Shadow(alpha = dampedDragAnimation.pressProgress) },
                    innerShadow = {
                        val progress = dampedDragAnimation.pressProgress
                        InnerShadow(radius = 8.dp * progress, alpha = progress)
                    },
                    layerBlock = {
                        scaleX = dampedDragAnimation.scaleX
                        scaleY = dampedDragAnimation.scaleY
                        val velocity = dampedDragAnimation.velocity / 10f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                        val progress = dampedDragAnimation.pressProgress
                        drawRect(
                            if (colors.isDark) Color.White.copy(0.12f) else Color.Black.copy(0.08f),
                            alpha = 1f - progress
                        )
                        if (!isRenderEffectSupported()) {
                            // No lens below Android 13: tint the pill so the selection stays obvious.
                            drawRect(accentColor.copy(alpha = 0.22f))
                        }
                        drawRect(Color.Black.copy(alpha = 0.03f * progress))
                    }
                )
                .height(lensHeight)
                .fillMaxWidth(1f / tabsCount)
        )
    }
}

/** One tab inside [LiquidTabs]. */
@Composable
fun RowScope.LiquidTab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scale = LocalLiquidTabScale.current
    Column(
        modifier
            .clip(Capsule())
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .fillMaxHeight()
            .weight(1f)
            .graphicsLayer {
                val s = scale()
                scaleX = s
                scaleY = s
            },
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}
