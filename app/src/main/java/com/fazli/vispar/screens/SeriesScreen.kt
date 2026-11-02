package com.fazli.vispar.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fazli.vispar.data.model.Series
import com.fazli.vispar.ui.series.SeriesViewModel
import com.fazli.vispar.utils.StorageUtils

fun Series.toCatalogItem() = CatalogItem(
    id = id,
    title = title,
    year = year,
    genres = genres.map { it.title },
    rating = imdb,
    image = image,
    cover = cover
)

@Composable
fun SeriesScreen(
    viewModel: SeriesViewModel = viewModel(),
    navController: NavController? = null
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (viewModel.series.isEmpty()) viewModel.loadSeries()
    }
    val series = viewModel.series
    val items = remember(series) { series.map { it.toCatalogItem() } }

    val (continueWatching, removeContinue) = rememberContinueWatching("series")

    CatalogScreen(
        title = "سریال‌ها",
        subtitle = "فصل به فصل، قسمت به قسمت",
        items = items,
        isLoading = viewModel.isLoading,
        isLoadingMore = viewModel.isLoadingMore,
        errorMessage = viewModel.errorMessage,
        errorTitle = "خطا در بارگذاری سریال‌ها",
        onRetry = { viewModel.retry() },
        onRefresh = { viewModel.refresh() },
        onLoadMore = { viewModel.loadMoreSeries() },
        continueWatching = continueWatching,
        onRemoveContinue = removeContinue,
        onItemClick = { item ->
            series.firstOrNull { it.id == item.id }?.let { StorageUtils.saveSeriesToFile(context, it) }
            navController?.navigate("single_series/${item.id}")
        }
    )
}
