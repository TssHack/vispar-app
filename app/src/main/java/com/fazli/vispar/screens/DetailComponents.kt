package com.fazli.vispar.screens

import androidx.compose.ui.res.stringResource
import com.fazli.vispar.R

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassChip
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LiquidIconButton
import com.fazli.vispar.ui.glass.LocalBackdrop
import com.fazli.vispar.ui.glass.StatusView
import com.fazli.vispar.ui.glass.formatRating
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.RoundedRectangle

/**
 * Immersive backdrop for detail screens: the title's cover art fills the top of the screen,
 * drifting slower than the content (parallax) and melting into the ambient background. Everything
 * glass on the screen refracts it.
 *
 * @param scrollOffset current scroll distance in px, used for the parallax.
 */
@Composable
fun CoverArtBackdrop(
    coverUrl: String,
    scrollOffset: () -> Float,
    content: @Composable BoxScope.(backdrop: Backdrop) -> Unit
) {
    val colors = Glass.colors
    val artLayer = rememberLayerBackdrop()
    val backdrop = rememberCombinedBackdrop(LocalBackdrop.current, artLayer)

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(560.dp)
                .layerBackdrop(artLayer)
                .graphicsLayer {
                    val offset = scrollOffset()
                    translationY = -offset * 0.45f
                    val zoom = 1f + (offset / 4000f).coerceIn(0f, 0.15f)
                    scaleX = zoom
                    scaleY = zoom
                    alpha = (1f - offset / 1600f).coerceIn(0.35f, 1f)
                }
        ) {
            if (coverUrl.isNotBlank()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current).data(coverUrl).crossfade(true).build()
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to colors.backgroundTop.copy(alpha = 0.35f),
                            0.45f to Color.Transparent,
                            0.82f to colors.backgroundTop.copy(alpha = 0.75f),
                            1f to colors.backgroundTop
                        )
                    )
            )
        }
        CompositionLocalProvider(LocalBackdrop provides backdrop) {
            content(backdrop)
        }
    }
}

/** Poster with a specular rim and soft drop shadow. */
@Composable
fun FramedPoster(imageUrl: String, contentDescription: String, modifier: Modifier = Modifier) {
    val colors = Glass.colors
    val shape = RoundedRectangle(22.dp)
    Box(
        modifier
            .drawBackdrop(
                backdrop = emptyBackdrop(),
                shape = { shape },
                effects = {},
                highlight = { Highlight.Default },
                shadow = { Shadow(radius = 28.dp, color = Color.Black.copy(alpha = if (colors.isDark) 0.55f else 0.25f)) }
            )
            .background(colors.placeholder)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current).data(imageUrl).crossfade(true).build()
            ),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/** Poster, title and key facts. */
@Composable
fun DetailHeader(
    title: String,
    poster: String,
    year: Int,
    countries: List<String>,
    rating: Double,
    duration: String?,
    modifier: Modifier = Modifier,
) {
    val colors = Glass.colors
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = Glass.ScreenPadding)
            // Leave room for the cover art to breathe above the poster.
            .padding(top = 110.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FramedPoster(poster, title, Modifier.width(128.dp).height(192.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                title,
                style = GlassType.Title,
                color = colors.content,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            val meta = listOfNotNull(
                countries.takeIf { it.isNotEmpty() }?.joinToString(stringResource(R.string.list_separator)),
                year.takeIf { it > 0 }?.toString()
            ).joinToString("  •  ")
            if (meta.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Public, null, tint = colors.contentSecondary, modifier = Modifier.size(14.dp))
                    Text(" $meta", style = GlassType.Caption, color = colors.contentSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (rating > 0) {
                    GlassChip {
                        Icon(Icons.Rounded.Star, null, tint = colors.gold, modifier = Modifier.size(16.dp))
                        Text(formatRating(rating), style = GlassType.Headline, color = colors.content)
                        Text("IMDb", style = GlassType.Caption, color = colors.contentTertiary)
                    }
                }
                if (!duration.isNullOrBlank()) {
                    GlassChip {
                        Icon(Icons.Rounded.Schedule, null, tint = colors.contentSecondary, modifier = Modifier.size(14.dp))
                        Text(duration, style = GlassType.CaptionBold, color = colors.content, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun GenreChips(genres: List<String>, modifier: Modifier = Modifier) {
    if (genres.isEmpty()) return
    val colors = Glass.colors
    LazyRow(
        modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = Glass.ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(genres) { genre ->
            GlassChip(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                Text(genre, style = GlassType.CaptionBold, color = colors.content)
            }
        }
    }
}

/** Synopsis on frosted glass; tap to expand. */
@Composable
fun DescriptionCard(text: String, modifier: Modifier = Modifier) {
    if (text.isBlank()) return
    val colors = Glass.colors
    var expanded by rememberSaveable { mutableStateOf(false) }
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Glass.ScreenPadding),
        style = GlassStyle.Frosted,
        tint = colors.glassTintStrong,
        onClick = { expanded = !expanded }
    ) {
        Column(
            Modifier
                .animateContentSize(spring(0.85f, 300f))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(stringResource(R.string.synopsis), style = GlassType.Headline, color = colors.content)
            Text(
                text,
                style = GlassType.Body,
                color = colors.contentSecondary,
                textAlign = TextAlign.Justify,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                if (expanded) stringResource(R.string.show_less) else stringResource(R.string.show_more),
                style = GlassType.CaptionBold,
                color = colors.accent
            )
        }
    }
}

/** Heart toggle for the top bar. Fills with a springy pop when added. */
@Composable
fun FavoriteToggle(isFavorite: Boolean, onToggle: () -> Unit) {
    val colors = Glass.colors
    val pop by animateFloatAsState(if (isFavorite) 1f else 0f, spring(0.4f, 400f), label = "fav")
    Box(Modifier.graphicsLayer { scaleX = 1f + 0.12f * pop; scaleY = 1f + 0.12f * pop }) {
        LiquidIconButton(
            onClick = onToggle,
            icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = stringResource(R.string.favorite),
            iconTint = if (isFavorite) colors.heart else colors.content
        )
    }
}

fun showToast(context: android.content.Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

/** Centered "not found" state for a detail screen. */
@Composable
fun DetailNotFound(title: String, message: String, padding: PaddingValues) {
    Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        StatusView(
            icon = Icons.Rounded.SearchOff,
            title = title,
            message = message,
            iconTint = Glass.colors.danger
        )
    }
}
