package com.fazli.vispar.ui.glass

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.RoundedRectangle

/**
 * Glass cannot sample across windows, so dialogs are not platform dialogs: they are rendered in
 * an overlay layer at the root of the app, above the tab bar, where they can refract the whole
 * screen. Screens declare them where they need them with [GlassDialog].
 */
@Stable
class GlassOverlayHost {
    internal val entries = mutableStateListOf<OverlayEntry>()
}

internal class OverlayEntry(
    val content: State<@Composable ColumnScope.() -> Unit>,
    val onDismiss: State<() -> Unit>,
) {
    val visibleState = MutableTransitionState(false).apply { targetState = true }
}

val LocalGlassOverlayHost = staticCompositionLocalOf<GlassOverlayHost?> { null }

/** Renders every open dialog. Place it last inside the app's root box. */
@Composable
fun GlassOverlayLayer(host: GlassOverlayHost, backdrop: Backdrop) {
    val colors = Glass.colors
    host.entries.toList().forEach { entry ->
        key(entry) {
            val state = entry.visibleState
            LaunchedEffect(state.isIdle, state.currentState, state.targetState) {
                if (state.isIdle && !state.currentState && !state.targetState) host.entries.remove(entry)
            }
            BackHandler(enabled = state.targetState) { entry.onDismiss.value() }

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AnimatedVisibility(
                    visibleState = state,
                    enter = fadeIn(tween(220)),
                    exit = fadeOut(tween(180))
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(colors.scrim)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                enabled = state.targetState
                            ) { entry.onDismiss.value() }
                    )
                }
                AnimatedVisibility(
                    visibleState = state,
                    enter = fadeIn(tween(160)) + scaleIn(spring(0.62f, 380f), initialScale = 0.82f),
                    exit = fadeOut(tween(140)) + scaleOut(tween(160), targetScale = 0.92f)
                ) {
                    val cardLayer = rememberLayerBackdrop()
                    CompositionLocalProvider(LocalBackdrop provides cardLayer) {
                        Column(
                            Modifier
                                .padding(horizontal = 28.dp, vertical = 48.dp)
                                .widthIn(max = 420.dp)
                                .fillMaxWidth()
                                .glass(
                                    backdrop = backdrop,
                                    shape = RoundedRectangle(36.dp),
                                    colors = colors,
                                    style = GlassStyle.Floating,
                                    tint = colors.glassTintStrong,
                                    highlight = Highlight.Plain,
                                    shadow = Shadow(radius = 40.dp, color = Color.Black.copy(alpha = 0.25f)),
                                    exportedBackdrop = cardLayer
                                )
                                // Swallow taps so they don't reach the scrim.
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {}
                                )
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            entry.content.value(this)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shows a liquid glass dialog for as long as this composable is in the composition.
 *
 * Note: the content keeps rendering during the exit animation after the caller removed it, so it
 * must not dereference state that the caller clears on dismiss (avoid `!!` on nullable state).
 */
@Composable
fun GlassDialog(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val host = LocalGlassOverlayHost.current ?: return
    val latestContent = rememberUpdatedState(content)
    val latestDismiss = rememberUpdatedState(onDismissRequest)
    val entry = remember { OverlayEntry(latestContent, latestDismiss) }
    DisposableEffect(entry) {
        host.entries.add(entry)
        onDispose { entry.visibleState.targetState = false }
    }
}

/** Title + optional message block for dialogs. */
@Composable
fun ColumnScope.DialogHeader(title: String, message: String? = null, icon: ImageVector? = null, iconTint: Color = Glass.colors.accent) {
    val colors = Glass.colors
    if (icon != null) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .size(56.dp)
                .glass(
                    backdrop = LocalBackdrop.current,
                    shape = RoundedRectangle(20.dp),
                    colors = colors,
                    style = GlassStyle.Clear,
                    tint = iconTint.copy(alpha = 0.22f),
                    shadow = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(28.dp))
        }
    }
    Text(
        title,
        style = GlassType.Title2,
        color = colors.content,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    if (message != null) {
        Text(
            message,
            style = GlassType.Callout,
            color = colors.contentSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
    Spacer(Modifier.height(4.dp))
}

enum class DialogActionStyle { Primary, Secondary, Destructive, Plain }

/** Full width liquid button for dialog actions. */
@Composable
fun DialogAction(
    text: String,
    onClick: () -> Unit,
    style: DialogActionStyle = DialogActionStyle.Secondary,
    icon: ImageVector? = null,
) {
    val colors = Glass.colors
    val tint = when (style) {
        DialogActionStyle.Primary -> colors.accent
        DialogActionStyle.Destructive -> colors.danger
        else -> Color.Unspecified
    }
    val contentColor = when (style) {
        DialogActionStyle.Primary -> colors.onAccent
        DialogActionStyle.Destructive -> Color.White
        DialogActionStyle.Plain -> colors.contentSecondary
        DialogActionStyle.Secondary -> colors.content
    }
    LiquidButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        tint = tint,
        height = 52.dp,
        surfaceColor = if (style == DialogActionStyle.Plain) Color.Transparent else Color.Unspecified,
    ) {
        if (icon != null) Icon(icon, null, tint = contentColor, modifier = Modifier.size(20.dp))
        Text(text, style = GlassType.Headline, color = contentColor)
    }
}

/** Convenience confirmation dialog. */
@Composable
fun GlassAlertDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    onDismiss: () -> Unit,
    icon: ImageVector? = null,
    destructive: Boolean = false,
) {
    GlassDialog(onDismissRequest = onDismiss) {
        DialogHeader(
            title = title,
            message = message,
            icon = icon,
            iconTint = if (destructive) Glass.colors.danger else Glass.colors.accent
        )
        DialogAction(
            text = confirmText,
            onClick = onConfirm,
            style = if (destructive) DialogActionStyle.Destructive else DialogActionStyle.Primary
        )
        DialogAction(text = dismissText, onClick = onDismiss, style = DialogActionStyle.Plain)
    }
}
