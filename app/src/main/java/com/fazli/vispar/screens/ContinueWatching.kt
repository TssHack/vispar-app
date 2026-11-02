package com.fazli.vispar.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fazli.vispar.PlayerMeta
import com.fazli.vispar.VideoPlayerActivity
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LiquidIconButton
import com.fazli.vispar.ui.glass.SectionHeader
import com.fazli.vispar.ui.glass.glass
import com.fazli.vispar.utils.WatchEntry
import com.fazli.vispar.utils.WatchHistory
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

/** Resumable items of [type], refreshed every time the screen comes back (e.g. from the player). */
@Composable
fun rememberContinueWatching(type: String): Pair<List<WatchEntry>, (String) -> Unit> {
    val context = LocalContext.current
    var entries by remember { mutableStateOf(emptyList<WatchEntry>()) }
    LifecycleResumeEffect(type) {
        entries = WatchHistory.continueWatching(context, type)
        onPauseOrDispose { }
    }
    return entries to { url: String ->
        WatchHistory.remove(context, url)
        entries = entries.filterNot { it.url == url }
    }
}

fun resume(context: Context, entry: WatchEntry) {
    VideoPlayerActivity.start(
        context,
        PlayerMeta(
            url = entry.url,
            title = entry.title,
            subtitle = entry.subtitle,
            image = entry.image,
            contentId = entry.contentId,
            type = entry.type
        )
    )
}

/** Horizontal shelf of "continue watching" cards with glass progress panes. */
@Composable
fun ContinueWatchingShelf(entries: List<WatchEntry>, onRemove: (String) -> Unit) {
    if (entries.isEmpty()) return
    val context = LocalContext.current
    Column {
        SectionHeader("ادامه تماشا", Modifier.padding(horizontal = 0.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries, key = { it.url }) { entry ->
                ContinueCard(entry, onClick = { resume(context, entry) }, onRemove = { onRemove(entry.url) })
            }
        }
    }
}

@Composable
private fun ContinueCard(entry: WatchEntry, onClick: () -> Unit, onRemove: () -> Unit) {
    val colors = Glass.colors
    val art = rememberLayerBackdrop()
    GlassSurface(
        modifier = Modifier.width(264.dp).aspectRatio(16f / 9f),
        shape = RoundedRectangle(24.dp),
        style = GlassStyle.Clear,
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize().clip(RoundedRectangle(24.dp))) {
            Box(Modifier.fillMaxSize().layerBackdrop(art).background(colors.placeholder)) {
                if (entry.image.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current).data(entry.image).crossfade(true).build()
                        ),
                        contentDescription = entry.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            LiquidIconButton(
                onClick = onRemove,
                icon = Icons.Rounded.Close,
                contentDescription = "حذف از ادامه تماشا",
                iconTint = Color.White,
                backdrop = art,
                size = 32.dp,
                iconSize = 16.dp,
                modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
            )
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(6.dp)
                    .fillMaxWidth()
                    .glass(
                        backdrop = art,
                        shape = RoundedRectangle(18.dp),
                        colors = colors,
                        style = GlassStyle(blur = 12.dp, refractionHeight = 10.dp, refractionAmount = 20.dp),
                        tint = Color.Black.copy(alpha = 0.3f),
                        shadow = null
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Text(
                        entry.title,
                        style = GlassType.CaptionBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    val remaining = ((entry.durationMs - entry.positionMs) / 60_000).coerceAtLeast(0)
                    if (entry.durationMs > 0) {
                        Text("$remaining دقیقه مانده", style = GlassType.Caption, color = Color.White.copy(alpha = 0.75f))
                    }
                }
                entry.subtitle?.let {
                    Text(it, style = GlassType.Caption, color = Color.White.copy(alpha = 0.75f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(Modifier.fillMaxWidth().height(4.dp).clip(Capsule()).background(Color.White.copy(alpha = 0.25f))) {
                    Box(Modifier.fillMaxWidth(entry.progress).height(4.dp).clip(Capsule()).background(Color.White))
                }
            }
        }
    }
}
