package com.fazli.vispar.ui.glass

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import java.util.Locale

fun formatRating(value: Double): String = String.format(Locale.US, "%.1f", value)

/**
 * Poster tile: the artwork fills a continuous-corner card and the caption floats on a pane of
 * glass that refracts the poster itself. The rating sits in a small glass capsule on top.
 */
@Composable
fun PosterCard(
    imageUrl: String,
    title: String,
    subtitle: String?,
    rating: Double?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    val colors = Glass.colors
    val posterLayer = rememberLayerBackdrop()
    val scope = rememberCoroutineScope()
    val highlightFx = remember(scope) { InteractiveHighlight(scope) }
    var focused by remember { mutableStateOf(false) }
    val focusScale by animateFloatAsState(if (focused) 1.05f else 1f, spring(0.6f, 400f), label = "focus")
    val shape = RoundedRectangle(26.dp)

    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(2f / 3f)
            .graphicsLayer {
                val press = 1f - 0.04f * highlightFx.pressProgress
                scaleX = press * focusScale
                scaleY = press * focusScale
            }
            .drawBackdrop(
                backdrop = emptyBackdrop(),
                shape = { shape },
                effects = {},
                highlight = { Highlight.Default.copy(alpha = 0.9f) },
                shadow = { Shadow(radius = 18.dp, color = Color.Black.copy(alpha = if (colors.isDark) 0.45f else 0.18f)) }
            )
            .then(
                if (focused) Modifier.border(2.dp, colors.accent, shape) else Modifier
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable(interactionSource = null, indication = null, role = Role.Button, onClick = onClick)
            .then(highlightFx.modifier)
            .then(highlightFx.gestureModifier)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .layerBackdrop(posterLayer)
                .background(
                    Brush.linearGradient(
                        listOf(colors.accent.copy(alpha = 0.35f), colors.accent.shiftHue(50f).copy(alpha = 0.18f))
                    )
                )
        ) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build()
                ),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (rating != null && rating > 0) {
                GlassChip(
                    backdrop = posterLayer,
                    tint = Color.Black.copy(alpha = 0.18f),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Rounded.Star, null, tint = colors.gold, modifier = Modifier.size(14.dp))
                    Text(formatRating(rating), style = GlassType.CaptionBold, color = Color.White)
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }
            if (badge != null) {
                GlassChip(
                    backdrop = posterLayer,
                    tint = colors.accent.copy(alpha = 0.55f),
                    contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(badge, style = GlassType.CaptionBold, color = Color.White)
                }
            }
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(6.dp)
                .fillMaxWidth()
                .glass(
                    backdrop = posterLayer,
                    shape = RoundedRectangle(20.dp),
                    colors = colors,
                    style = GlassStyle(blur = 12.dp, refractionHeight = 12.dp, refractionAmount = 24.dp),
                    tint = Color.Black.copy(alpha = 0.28f),
                    shadow = null
                )
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Text(
                title,
                style = GlassType.Headline,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    style = GlassType.Caption,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Placeholder for [PosterCard] while content loads: a glass tile with a sheen sweeping across. */
@Composable
fun PosterSkeleton(modifier: Modifier = Modifier) {
    val colors = Glass.colors
    val transition = rememberInfiniteTransition(label = "sheen")
    val sweep by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing)),
        label = "sweep"
    )
    GlassSurface(
        modifier = modifier.fillMaxWidth().aspectRatio(2f / 3f),
        shape = RoundedRectangle(26.dp),
        style = GlassStyle.Frosted,
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val x = size.width * sweep
            drawRect(
                Brush.linearGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = if (colors.isDark) 0.10f else 0.35f), Color.Transparent),
                    start = Offset(x - size.width * 0.6f, 0f),
                    end = Offset(x, size.height)
                )
            )
        }
        Column(
            Modifier.align(Alignment.BottomCenter).padding(14.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.fillMaxWidth(0.8f).height(14.dp).clip(Capsule()).background(colors.content.copy(alpha = 0.10f)))
            Box(Modifier.fillMaxWidth(0.45f).height(10.dp).clip(Capsule()).background(colors.content.copy(alpha = 0.08f)))
        }
    }
}

/** Spinning gradient ring set in a glass bead. */
@Composable
fun LiquidLoader(modifier: Modifier = Modifier, size: Dp = 64.dp) {
    val colors = Glass.colors
    val transition = rememberInfiniteTransition(label = "loader")
    val rotation by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(1100, easing = LinearEasing)), label = "rot")
    GlassSurface(
        modifier = modifier.size(size),
        shape = Capsule(),
        style = GlassStyle.Clear,
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(size * 0.5f).graphicsLayer { rotationZ = rotation }) {
            val stroke = 3.5.dp.toPx()
            drawArc(
                brush = Brush.sweepGradient(listOf(Color.Transparent, colors.accent, colors.accent.shiftHue(40f))),
                startAngle = 0f,
                sweepAngle = 300f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
    }
}

/** Centered empty / error / hint state. */
@Composable
fun StatusView(
    icon: ImageVector,
    title: String,
    message: String? = null,
    modifier: Modifier = Modifier,
    iconTint: Color = Glass.colors.accent,
    actionText: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = Glass.colors
    Column(
        modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GlassSurface(
            modifier = Modifier.size(96.dp),
            shape = RoundedRectangle(32.dp),
            style = GlassStyle.Clear,
            tint = iconTint.copy(alpha = 0.16f),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(title, style = GlassType.Title2, color = colors.content, textAlign = TextAlign.Center)
        if (message != null) {
            Text(message, style = GlassType.Callout, color = colors.contentSecondary, textAlign = TextAlign.Center)
        }
        if (actionText != null && onAction != null) {
            Spacer(Modifier.height(4.dp))
            LiquidButton(onClick = onAction, tint = colors.accent) {
                if (actionIcon != null) Icon(actionIcon, null, tint = colors.onAccent, modifier = Modifier.size(20.dp))
                Text(actionText, style = GlassType.Headline, color = colors.onAccent)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, trailing: String? = null) {
    val colors = Glass.colors
    Row(
        modifier.fillMaxWidth().padding(horizontal = Glass.ScreenPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = GlassType.Title2, color = colors.content, modifier = Modifier.weight(1f))
        if (trailing != null) Text(trailing, style = GlassType.Caption, color = colors.contentTertiary)
    }
}

/** Search field in a glass capsule. */
@Composable
fun GlassSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    onClear: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
) {
    val colors = Glass.colors
    Row(
        modifier
            .fillMaxWidth()
            .height(54.dp)
            .glass(
                backdrop = LocalBackdrop.current,
                shape = Capsule(),
                colors = colors,
                style = GlassStyle(blur = 10.dp, refractionHeight = 14.dp, refractionAmount = 28.dp)
            )
            .padding(start = 18.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Rounded.Search, null, tint = colors.contentSecondary, modifier = Modifier.size(22.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(placeholder, style = GlassType.Body, color = colors.contentTertiary, maxLines = 1)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = GlassType.Body.copy(color = colors.content),
                cursorBrush = SolidColor(colors.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )
        }
        if (value.isNotEmpty()) {
            LiquidIconButton(
                onClick = onClear,
                icon = Icons.Rounded.Close,
                contentDescription = "پاک کردن",
                size = 38.dp,
                iconSize = 18.dp
            )
        }
    }
}

/** Rounded, accent tinted icon tile used at the start of list rows. */
@Composable
fun IconTile(icon: ImageVector, tint: Color = Glass.colors.accent, size: Dp = 40.dp) {
    Box(
        Modifier
            .size(size)
            .clip(RoundedRectangle(size * 0.32f))
            .background(Brush.linearGradient(listOf(tint, tint.shiftHue(28f)))),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(size * 0.55f))
    }
}

/** A tappable row inside a glass group (settings style). */
@Composable
fun GlassRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconTint: Color = Glass.colors.accent,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = {
        Icon(
            Icons.AutoMirrored.Rounded.ArrowForwardIos,
            null,
            tint = Glass.colors.contentTertiary,
            modifier = Modifier.size(14.dp)
        )
    },
) {
    val colors = Glass.colors
    Row(
        modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        IconTile(icon, iconTint)
        Column(Modifier.weight(1f)) {
            Text(title, style = GlassType.Headline, color = colors.content)
            if (subtitle != null) Text(subtitle, style = GlassType.Caption, color = colors.contentSecondary)
        }
        trailing?.invoke()
    }
}

@Composable
fun GlassDivider(modifier: Modifier = Modifier, startIndent: Dp = 70.dp) {
    Box(
        modifier
            .padding(start = startIndent)
            .fillMaxWidth()
            .height(0.5.dp)
            .background(Glass.colors.separator)
    )
}
