package com.fazli.vispar.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fazli.vispar.VideoPlayerActivity
import com.fazli.vispar.components.DownloadOptionsDialog
import com.fazli.vispar.components.QualityPickerDialog
import com.fazli.vispar.data.model.FavoriteItem
import com.fazli.vispar.data.model.Movie
import com.fazli.vispar.data.model.Source
import com.fazli.vispar.ui.glass.DialogAction
import com.fazli.vispar.ui.glass.DialogActionStyle
import com.fazli.vispar.ui.glass.DialogHeader
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassDialog
import com.fazli.vispar.ui.glass.GlassScaffold
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.IconTile
import com.fazli.vispar.ui.glass.LiquidButton
import com.fazli.vispar.ui.glass.LiquidIconButton
import com.fazli.vispar.ui.glass.LiquidLoader
import com.fazli.vispar.ui.glass.SectionHeader
import com.fazli.vispar.utils.DownloadUtils
import com.fazli.vispar.utils.StorageUtils
import com.kyant.shapes.RoundedRectangle

@Composable
fun SingleMovieScreen(
    movieId: Int,
    navController: NavController
) {
    val context = LocalContext.current
    var movie by remember { mutableStateOf<Movie?>(null) }
    var loaded by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    var optionsFor by remember { mutableStateOf<Source?>(null) }
    var downloadFor by remember { mutableStateOf<Source?>(null) }
    var pickQualityToPlay by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    LaunchedEffect(movieId) {
        movie = StorageUtils.loadMovieFromFile(context, movieId)
        isFavorite = StorageUtils.isFavorite(context, movieId, "movie")
        loaded = true
    }

    fun toggleFavorite() {
        if (isFavorite) {
            StorageUtils.removeFavorite(context, movieId, "movie")
            isFavorite = false
            showToast(context, "از موارد مورد علاقه حذف شد")
        } else {
            movie?.let {
                StorageUtils.saveFavorite(context, it.toFavorite())
                isFavorite = true
                showToast(context, "به موارد مورد علاقه اضافه شد")
            }
        }
    }

    CoverArtBackdrop(
        coverUrl = movie?.cover.orEmpty(),
        scrollOffset = { scroll.value.toFloat() }
    ) { backdrop ->
        GlassScaffold(
            title = movie?.title ?: "جزئیات فیلم",
            titleAlpha = { ((scroll.value - 260f) / 140f).coerceIn(0f, 1f) },
            onBack = { navController.popBackStack() },
            actions = {
                if (movie != null) FavoriteToggle(isFavorite, ::toggleFavorite)
            },
            background = backdrop
        ) { padding ->
            val current = movie
            when {
                current != null -> MovieDetails(
                    movie = current,
                    padding = padding,
                    scroll = scroll,
                    onPlay = { source -> VideoPlayerActivity.start(context, source.url) },
                    onPlayBest = {
                        when (current.sources.size) {
                            0 -> Unit
                            1 -> VideoPlayerActivity.start(context, current.sources.first().url)
                            else -> pickQualityToPlay = true
                        }
                    },
                    onSourceOptions = { optionsFor = it },
                    onDownload = { downloadFor = it }
                )

                loaded -> DetailNotFound(
                    title = "فیلم یافت نشد",
                    message = "متاسفانه اطلاعات فیلم مورد نظر در دسترس نیست",
                    padding = padding
                )

                else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LiquidLoader() }
            }
        }
    }

    optionsFor?.let { source ->
        SourceOptionsDialog(
            source = source,
            onDismiss = { optionsFor = null },
            onDownload = {
                optionsFor = null
                downloadFor = source
            },
            onPlay = {
                optionsFor = null
                VideoPlayerActivity.start(context, source.url)
            }
        )
    }

    downloadFor?.let { source -> DownloadOptions(context, source) { downloadFor = null } }

    if (pickQualityToPlay) {
        QualityPickerDialog(
            title = "انتخاب کیفیت",
            message = "کیفیت پخش را انتخاب کنید",
            sources = movie?.sources.orEmpty(),
            icon = Icons.Rounded.PlayArrow,
            onPick = { source ->
                pickQualityToPlay = false
                VideoPlayerActivity.start(context, source.url)
            },
            onDismiss = { pickQualityToPlay = false }
        )
    }
}

@Composable
private fun MovieDetails(
    movie: Movie,
    padding: PaddingValues,
    scroll: androidx.compose.foundation.ScrollState,
    onPlay: (Source) -> Unit,
    onPlayBest: () -> Unit,
    onSourceOptions: (Source) -> Unit,
    onDownload: (Source) -> Unit,
) {
    val colors = Glass.colors
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DetailHeader(
            title = movie.title,
            poster = movie.image,
            year = movie.year,
            countries = movie.country.map { it.title },
            rating = movie.imdb,
            duration = movie.duration
        )

        if (movie.sources.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = Glass.ScreenPadding),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidButton(
                    onClick = onPlayBest,
                    tint = colors.accent,
                    height = 54.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.PlayArrow, null, tint = colors.onAccent, modifier = Modifier.size(26.dp))
                    Text("پخش فیلم", style = GlassType.Headline, color = colors.onAccent)
                }
                LiquidIconButton(
                    onClick = { onDownload(movie.sources.first()) },
                    icon = Icons.Rounded.Download,
                    contentDescription = "دانلود",
                    size = 54.dp
                )
            }
        }

        GenreChips(movie.genres.map { it.title })
        DescriptionCard(movie.description)

        if (movie.sources.isNotEmpty()) {
            SectionHeader("کیفیت‌های موجود", trailing = "${movie.sources.size} کیفیت")
            Column(
                Modifier.padding(horizontal = Glass.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                movie.sources.forEach { source ->
                    SourceRow(
                        source = source,
                        onClick = { onSourceOptions(source) },
                        onPlay = { onPlay(source) },
                        onDownload = { onDownload(source) }
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun SourceRow(source: Source, onClick: () -> Unit, onPlay: () -> Unit, onDownload: () -> Unit) {
    val colors = Glass.colors
    GlassSurface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedRectangle(24.dp),
        onClick = onClick
    ) {
        Row(
            Modifier.padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconTile(Icons.Rounded.HighQuality, colors.accent)
            Column(Modifier.weight(1f)) {
                Text(source.quality, style = GlassType.Headline, color = colors.content)
                if (source.type.isNotBlank()) {
                    Text(source.type.uppercase(), style = GlassType.Caption, color = colors.contentTertiary)
                }
            }
            LiquidIconButton(onClick = onDownload, icon = Icons.Rounded.Download, contentDescription = "دانلود", size = 42.dp, iconSize = 20.dp)
            LiquidIconButton(
                onClick = onPlay,
                icon = Icons.Rounded.PlayArrow,
                contentDescription = "پخش",
                tint = colors.accent,
                size = 42.dp,
                iconSize = 22.dp
            )
        }
    }
}

@Composable
fun SourceOptionsDialog(
    source: Source,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onPlay: () -> Unit
) {
    GlassDialog(onDismissRequest = onDismiss) {
        DialogHeader(
            title = source.quality,
            message = "یک عمل برای این کیفیت ویدیو انتخاب کنید",
            icon = Icons.Rounded.HighQuality
        )
        DialogAction("پخش در برنامه", onPlay, DialogActionStyle.Primary, Icons.Rounded.PlayArrow)
        DialogAction("گزینه‌های دانلود", onDownload, DialogActionStyle.Secondary, Icons.Rounded.Download)
        DialogAction("انصراف", onDismiss, DialogActionStyle.Plain)
    }
}

/** [DownloadOptionsDialog] wired to [DownloadUtils]. */
@Composable
fun DownloadOptions(context: Context, source: Source, onDismiss: () -> Unit) {
    DownloadOptionsDialog(
        source = source,
        onDismiss = onDismiss,
        onCopyLink = { DownloadUtils.copyToClipboard(context, source.url) },
        onDownloadWithBrowser = { DownloadUtils.openUrl(context, source.url) },
        onDownloadWithADM = { DownloadUtils.openWithADM(context, source.url) },
        onOpenInVLC = { DownloadUtils.openWithVLC(context, source.url) },
        onOpenInMXPlayer = { DownloadUtils.openWithMXPlayer(context, source.url) },
        onOpenInKMPlayer = { DownloadUtils.openWithKMPlayer(context, source.url) }
    )
}

private fun Movie.toFavorite() = FavoriteItem(
    id = id,
    type = "movie",
    title = title,
    description = description,
    year = year,
    imdb = imdb,
    rating = rating,
    duration = duration,
    image = image,
    cover = cover,
    genres = genres,
    country = country,
    sources = sources
)
