package com.fazli.vispar.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassChip
import com.fazli.vispar.ui.glass.GlassScaffold
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LargeTitle
import com.fazli.vispar.ui.glass.LiquidButton
import com.fazli.vispar.ui.glass.LiquidIconButton
import com.fazli.vispar.ui.glass.LiquidLoader
import com.fazli.vispar.ui.glass.PosterCard
import com.fazli.vispar.ui.glass.PosterSkeleton
import com.fazli.vispar.ui.glass.StatusView
import com.fazli.vispar.ui.glass.formatRating
import com.fazli.vispar.ui.glass.glass
import com.fazli.vispar.utils.DeviceUtils
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.RoundedRectangle

/** UI model shared by the movie and series catalogues. */
data class CatalogItem(
    val id: Int,
    val title: String,
    val year: Int,
    val genres: List<String>,
    val rating: Double,
    val image: String,
    val cover: String,
)

/**
 * Shared catalogue layout: a large title, a spotlight hero for the first title and an endless
 * grid of glass-captioned posters.
 */
@Composable
fun CatalogScreen(
    title: String,
    subtitle: String,
    items: List<CatalogItem>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    errorMessage: String?,
    errorTitle: String,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onItemClick: (CatalogItem) -> Unit,
) {
    val colors = Glass.colors
    val gridState = rememberLazyGridState()
    val columns = DeviceUtils.getGridColumns(LocalContext.current.resources)
    val density = LocalDensity.current

    GlassScaffold(
        title = title,
        titleAlpha = { compactTitleAlpha(gridState, with(density) { 56.dp.toPx() }) },
        actions = {
            LiquidIconButton(onClick = onRefresh, icon = Icons.Rounded.Refresh, contentDescription = "بارگذاری مجدد")
        }
    ) { padding ->
        when {
            isLoading && items.isEmpty() -> SkeletonGrid(columns, padding, title, subtitle)

            errorMessage != null && items.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                StatusView(
                    icon = Icons.Rounded.CloudOff,
                    title = errorTitle,
                    message = errorMessage,
                    iconTint = colors.danger,
                    actionText = "تلاش مجدد",
                    actionIcon = Icons.Rounded.Refresh,
                    onAction = onRetry
                )
            }

            else -> {
                val shouldLoadMore by remember(items.size) {
                    derivedStateOf {
                        val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        items.isNotEmpty() && last >= gridState.layoutInfo.totalItemsCount - 6
                    }
                }
                LaunchedEffect(shouldLoadMore, items.size) {
                    if (shouldLoadMore) onLoadMore()
                }

                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Glass.ScreenPadding,
                        end = Glass.ScreenPadding,
                        top = padding.calculateTopPadding(),
                        bottom = padding.calculateBottomPadding()
                    ),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "title") {
                        LargeTitle(title, subtitle, Modifier.padding(bottom = 4.dp))
                    }
                    items.firstOrNull()?.let { hero ->
                        item(span = { GridItemSpan(maxLineSpan) }, key = "hero") {
                            SpotlightCard(hero, onClick = { onItemClick(hero) })
                        }
                    }
                    itemsIndexed(items.drop(1), key = { _, item -> "p${item.id}" }) { _, item ->
                        PosterCard(
                            imageUrl = item.image,
                            title = item.title,
                            subtitle = listOfNotNull(
                                item.year.takeIf { it > 0 }?.toString(),
                                item.genres.firstOrNull()
                            ).joinToString(" • "),
                            rating = item.rating,
                            onClick = { onItemClick(item) }
                        )
                    }
                    if (isLoadingMore) {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "loading") {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                LiquidLoader()
                            }
                        }
                    }
                    if (errorMessage != null && items.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }, key = "error") {
                            InlineError(message = errorMessage, onRetry = onRetry)
                        }
                    }
                }
            }
        }
    }
}

/** 0 while the large title is visible, fading to 1 once it scrolled under the bar. */
fun compactTitleAlpha(state: LazyGridState, distancePx: Float): Float =
    if (state.firstVisibleItemIndex > 0) 1f
    else (state.firstVisibleItemScrollOffset / distancePx).coerceIn(0f, 1f)

@Composable
private fun SkeletonGrid(columns: Int, padding: PaddingValues, title: String, subtitle: String) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false,
        contentPadding = PaddingValues(
            start = Glass.ScreenPadding,
            end = Glass.ScreenPadding,
            top = padding.calculateTopPadding(),
            bottom = padding.calculateBottomPadding()
        ),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) { LargeTitle(title, subtitle) }
        item(span = { GridItemSpan(maxLineSpan) }) {
            GlassSurface(Modifier.fillMaxWidth().aspectRatio(16f / 10f), shape = RoundedRectangle(32.dp), style = GlassStyle.Frosted) {}
        }
        items(columns * 2) { PosterSkeleton() }
    }
}

/**
 * Wide hero for the newest title: its backdrop art fills the card and the details float on glass
 * that refracts the artwork.
 */
@Composable
fun SpotlightCard(item: CatalogItem, onClick: () -> Unit) {
    val colors = Glass.colors
    val artLayer = rememberLayerBackdrop()
    val shape = RoundedRectangle(32.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 10f)
            .drawBackdrop(
                backdrop = com.kyant.backdrop.backdrops.emptyBackdrop(),
                shape = { shape },
                effects = {},
                highlight = { Highlight.Default },
                shadow = { Shadow(radius = 24.dp, color = Color.Black.copy(alpha = if (colors.isDark) 0.5f else 0.2f)) }
            )
    ) {
        Box(Modifier.fillMaxSize().layerBackdrop(artLayer).background(colors.accent.copy(alpha = 0.3f))) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(item.cover.ifBlank { item.image })
                        .crossfade(true)
                        .build()
                ),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))))
            )
        }

        GlassChip(
            modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            backdrop = artLayer,
            tint = colors.accent.copy(alpha = 0.55f),
        ) {
            Text("تازه‌ترین", style = GlassType.CaptionBold, color = Color.White)
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(10.dp)
                .fillMaxWidth()
                .glass(
                    backdrop = artLayer,
                    shape = RoundedRectangle(24.dp),
                    colors = colors,
                    style = GlassStyle(blur = 14.dp, refractionHeight = 16.dp, refractionAmount = 32.dp),
                    tint = Color.Black.copy(alpha = 0.25f),
                    shadow = null
                )
                .padding(start = 16.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(item.title, style = GlassType.Title2, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.rating > 0) {
                        Icon(Icons.Rounded.Star, null, tint = colors.gold, modifier = Modifier.size(14.dp))
                        Text(" ${formatRating(item.rating)}  •  ", style = GlassType.Caption, color = Color.White.copy(alpha = 0.85f))
                    }
                    Text(
                        listOfNotNull(item.year.takeIf { it > 0 }?.toString(), item.genres.take(2).joinToString("، ").ifBlank { null })
                            .joinToString("  •  "),
                        style = GlassType.Caption,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            LiquidButton(onClick = onClick, backdrop = artLayer, tint = colors.accent, height = 44.dp) {
                Icon(Icons.Rounded.PlayArrow, null, tint = colors.onAccent, modifier = Modifier.size(20.dp))
                Text("مشاهده", style = GlassType.Headline, color = colors.onAccent)
            }
        }
    }
}

@Composable
fun InlineError(message: String, onRetry: () -> Unit) {
    val colors = Glass.colors
    GlassSurface(Modifier.fillMaxWidth(), style = GlassStyle.Frosted, tint = colors.danger.copy(alpha = 0.14f)) {
        Row(
            Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Rounded.CloudOff, null, tint = colors.danger)
            Column(Modifier.weight(1f)) {
                Text("خطا در بارگذاری موارد بیشتر", style = GlassType.Headline, color = colors.content)
                Text(message, style = GlassType.Caption, color = colors.contentSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            LiquidButton(onClick = onRetry, height = 40.dp, contentPadding = PaddingValues(horizontal = 14.dp)) {
                Text("تلاش مجدد", style = GlassType.CaptionBold, color = colors.content)
            }
        }
    }
}
