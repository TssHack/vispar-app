package com.fazli.vispar.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.LayersClear
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.fazli.vispar.VideoPlayerActivity
import com.fazli.vispar.components.QualityPickerDialog
import com.fazli.vispar.data.model.Episode
import com.fazli.vispar.data.model.FavoriteItem
import com.fazli.vispar.data.model.Season
import com.fazli.vispar.data.model.Series
import com.fazli.vispar.data.model.Source
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassChip
import com.fazli.vispar.ui.glass.GlassScaffold
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LiquidButton
import com.fazli.vispar.ui.glass.LiquidIconButton
import com.fazli.vispar.ui.glass.LiquidLoader
import com.fazli.vispar.ui.glass.LiquidTab
import com.fazli.vispar.ui.glass.LiquidTabs
import com.fazli.vispar.ui.glass.SectionHeader
import com.fazli.vispar.ui.glass.StatusView
import com.fazli.vispar.ui.series.SeasonsViewModel
import com.fazli.vispar.utils.StorageUtils
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.RoundedRectangle

@Composable
fun SingleSeriesScreen(
    seriesId: Int,
    navController: NavController
) {
    val context = LocalContext.current
    val seasonsViewModel: SeasonsViewModel = viewModel()
    var series by remember { mutableStateOf<Series?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    var playPickerFor by remember { mutableStateOf<Episode?>(null) }
    var downloadPickerFor by remember { mutableStateOf<Episode?>(null) }
    var downloadFor by remember { mutableStateOf<Source?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(seriesId) {
        series = StorageUtils.loadSeriesFromFile(context, seriesId)
        isFavorite = StorageUtils.isFavorite(context, seriesId, "series")
        loaded = true
        seasonsViewModel.loadSeasons(seriesId)
    }

    fun toggleFavorite() {
        if (isFavorite) {
            StorageUtils.removeFavorite(context, seriesId, "series")
            isFavorite = false
            showToast(context, "از موارد مورد علاقه حذف شد")
        } else {
            series?.let {
                StorageUtils.saveFavorite(context, it.toFavorite())
                isFavorite = true
                showToast(context, "به موارد مورد علاقه اضافه شد")
            }
        }
    }

    fun play(episode: Episode) {
        when (episode.sources.size) {
            0 -> Unit
            1 -> VideoPlayerActivity.start(context, episode.sources.first().url)
            else -> playPickerFor = episode
        }
    }

    fun download(episode: Episode) {
        when (episode.sources.size) {
            0 -> Unit
            1 -> downloadFor = episode.sources.first()
            else -> downloadPickerFor = episode
        }
    }

    CoverArtBackdrop(
        coverUrl = series?.cover.orEmpty(),
        scrollOffset = { listState.scrollOffsetPx() }
    ) { backdrop ->
        GlassScaffold(
            title = series?.title ?: "جزئیات سریال",
            titleAlpha = { ((listState.scrollOffsetPx() - 260f) / 140f).coerceIn(0f, 1f) },
            onBack = { navController.popBackStack() },
            actions = {
                if (series != null) FavoriteToggle(isFavorite, ::toggleFavorite)
            },
            background = backdrop
        ) { padding ->
            val current = series
            when {
                current != null -> SeriesDetails(
                    series = current,
                    viewModel = seasonsViewModel,
                    listState = listState,
                    padding = padding,
                    onPlay = ::play,
                    onDownload = ::download
                )

                loaded -> DetailNotFound(
                    title = "سریال یافت نشد",
                    message = "متاسفانه اطلاعات سریال مورد نظر در دسترس نیست",
                    padding = padding
                )

                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LiquidLoader() }
            }
        }
    }

    playPickerFor?.let { episode ->
        QualityPickerDialog(
            title = episode.title,
            message = "کیفیت را برای پخش انتخاب کنید",
            sources = episode.sources,
            icon = Icons.Rounded.PlayArrow,
            onPick = { source ->
                playPickerFor = null
                VideoPlayerActivity.start(context, source.url)
            },
            onDismiss = { playPickerFor = null }
        )
    }

    downloadPickerFor?.let { episode ->
        QualityPickerDialog(
            title = "انتخاب کیفیت برای دانلود",
            message = "یک گزینه کیفیت برای دانلود انتخاب کنید",
            sources = episode.sources,
            icon = Icons.Rounded.Download,
            primary = false,
            onPick = { source ->
                downloadPickerFor = null
                downloadFor = source
            },
            onDismiss = { downloadPickerFor = null }
        )
    }

    downloadFor?.let { source -> DownloadOptions(context, source) { downloadFor = null } }
}

/** Scroll distance usable for parallax; large once the header has scrolled away. */
private fun LazyListState.scrollOffsetPx(): Float =
    if (firstVisibleItemIndex == 0) firstVisibleItemScrollOffset.toFloat() else 2000f

@Composable
private fun SeriesDetails(
    series: Series,
    viewModel: SeasonsViewModel,
    listState: LazyListState,
    padding: PaddingValues,
    onPlay: (Episode) -> Unit,
    onDownload: (Episode) -> Unit,
) {
    val colors = Glass.colors
    val seasons = viewModel.seasons
    var selectedSeason by rememberSaveable { mutableIntStateOf(0) }
    val season: Season? = seasons.getOrNull(selectedSeason.coerceIn(0, (seasons.size - 1).coerceAtLeast(0)))

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "header") {
            DetailHeader(
                title = series.title,
                poster = series.image,
                year = series.year,
                countries = series.country.map { it.title },
                rating = series.imdb,
                duration = series.duration
            )
        }
        val firstEpisode = seasons.firstOrNull()?.episodes?.firstOrNull()
        if (firstEpisode != null) {
            item(key = "play") {
                LiquidButton(
                    onClick = { onPlay(season?.episodes?.firstOrNull() ?: firstEpisode) },
                    tint = colors.accent,
                    height = 54.dp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Glass.ScreenPadding)
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = colors.onAccent, modifier = Modifier.size(26.dp))
                    Text(
                        "پخش ${season?.title ?: "قسمت اول"}",
                        style = GlassType.Headline,
                        color = colors.onAccent,
                        maxLines = 1
                    )
                }
            }
        }
        item(key = "genres") { GenreChips(series.genres.map { it.title }) }
        item(key = "description") { DescriptionCard(series.description) }

        when {
            viewModel.isLoading -> item(key = "seasons-loading") {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    LiquidLoader()
                    Text("در حال بارگذاری فصل‌ها...", style = GlassType.Callout, color = colors.contentSecondary, modifier = Modifier.padding(top = 12.dp))
                }
            }

            viewModel.errorMessage != null -> item(key = "seasons-error") {
                StatusView(
                    icon = Icons.Rounded.CloudOff,
                    title = "خطا در بارگذاری فصل‌ها",
                    message = viewModel.errorMessage,
                    iconTint = colors.danger,
                    actionText = "تلاش مجدد",
                    actionIcon = Icons.Rounded.Refresh,
                    onAction = { viewModel.loadSeasons(series.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            seasons.isEmpty() -> item(key = "seasons-empty") {
                StatusView(icon = Icons.Rounded.LayersClear, title = "فصلی موجود نیست", modifier = Modifier.fillMaxWidth())
            }

            else -> {
                item(key = "seasons-title") {
                    SectionHeader("فصل‌ها", trailing = "${seasons.size} فصل")
                }
                item(key = "seasons-picker") {
                    SeasonPicker(seasons, selectedSeason) { selectedSeason = it }
                }
                if (season != null) {
                    item(key = "episodes-title") {
                        SectionHeader(season.title, trailing = "${season.episodes.size} قسمت")
                    }
                    itemsIndexed(season.episodes, key = { _, e -> "e${season.id}-${e.id}" }) { index, episode ->
                        EpisodeCard(
                            number = index + 1,
                            episode = episode,
                            onPlay = { onPlay(episode) },
                            onDownload = { onDownload(episode) }
                        )
                    }
                }
            }
        }
    }
}

/** Liquid segmented control for a handful of seasons, a scrolling row of liquid pills otherwise. */
@Composable
private fun SeasonPicker(seasons: List<Season>, selected: Int, onSelect: (Int) -> Unit) {
    val colors = Glass.colors
    if (seasons.size in 2..4) {
        LiquidTabs(
            selectedIndex = selected,
            onTabSelected = onSelect,
            tabsCount = seasons.size,
            height = 50.dp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Glass.ScreenPadding)
        ) {
            seasons.forEachIndexed { index, s ->
                LiquidTab(onClick = { onSelect(index) }) {
                    Text(s.title, style = GlassType.Headline, color = colors.content, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = Glass.ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(seasons) { index, s ->
                val isSelected = index == selected
                LiquidButton(
                    onClick = { onSelect(index) },
                    tint = if (isSelected) colors.accent else Color.Unspecified,
                    height = 44.dp
                ) {
                    Text(s.title, style = GlassType.Headline, color = if (isSelected) colors.onAccent else colors.content)
                }
            }
        }
    }
}

@Composable
private fun EpisodeCard(number: Int, episode: Episode, onPlay: () -> Unit, onDownload: () -> Unit) {
    val colors = Glass.colors
    val thumbLayer = rememberLayerBackdrop()
    GlassSurface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Glass.ScreenPadding),
        shape = RoundedRectangle(26.dp),
        onClick = onPlay
    ) {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier
                    .width(124.dp)
                    .height(72.dp)
                    .clip(RoundedRectangle(18.dp))
            ) {
                Box(Modifier.fillMaxSize().layerBackdrop(thumbLayer).background(colors.accent.copy(alpha = 0.3f))) {
                    if (episode.image.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current).data(episode.image).crossfade(true).build()
                            ),
                            contentDescription = episode.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                GlassChip(
                    modifier = Modifier.align(Alignment.Center),
                    backdrop = thumbLayer,
                    tint = Color.Black.copy(alpha = 0.2f),
                    contentPadding = PaddingValues(6.dp)
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                GlassChip(
                    modifier = Modifier.align(Alignment.TopStart).padding(5.dp),
                    backdrop = thumbLayer,
                    tint = Color.Black.copy(alpha = 0.25f),
                    contentPadding = PaddingValues(horizontal = 7.dp, vertical = 1.dp)
                ) {
                    Text("$number", style = GlassType.CaptionBold, color = Color.White)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(episode.title, style = GlassType.Headline, color = colors.content, maxLines = 2, overflow = TextOverflow.Ellipsis)
                val meta = listOfNotNull(
                    episode.duration?.takeIf { it.isNotBlank() },
                    episode.sources.size.takeIf { it > 1 }?.let { "$it کیفیت" }
                ).joinToString("  •  ")
                if (meta.isNotEmpty()) {
                    Text(meta, style = GlassType.Caption, color = colors.contentSecondary)
                }
            }
            if (episode.sources.isNotEmpty()) {
                LiquidIconButton(
                    onClick = onDownload,
                    icon = Icons.Rounded.Download,
                    contentDescription = "دانلود",
                    size = 40.dp,
                    iconSize = 20.dp
                )
            }
        }
    }
}

private fun Series.toFavorite() = FavoriteItem(
    id = id,
    type = "series",
    title = title,
    description = description,
    year = year,
    imdb = imdb,
    rating = rating,
    duration = duration,
    image = image,
    cover = cover,
    genres = genres,
    country = country
)
