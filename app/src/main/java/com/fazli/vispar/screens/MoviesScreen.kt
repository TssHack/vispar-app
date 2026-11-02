package com.fazli.vispar.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fazli.vispar.data.model.Movie
import com.fazli.vispar.ui.movies.MoviesViewModel
import com.fazli.vispar.utils.StorageUtils

fun Movie.toCatalogItem() = CatalogItem(
    id = id,
    title = title,
    year = year,
    genres = genres.map { it.title },
    rating = imdb,
    image = image,
    cover = cover
)

@Composable
fun MoviesScreen(
    viewModel: MoviesViewModel = viewModel(),
    navController: NavController? = null
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (viewModel.movies.isEmpty()) viewModel.loadMovies()
    }
    val movies = viewModel.movies
    val items = remember(movies) { movies.map { it.toCatalogItem() } }

    val (continueWatching, removeContinue) = rememberContinueWatching("movie")

    CatalogScreen(
        title = "فیلم‌ها",
        subtitle = "تازه‌ترین فیلم‌های جهان، یک لمس فاصله",
        items = items,
        isLoading = viewModel.isLoading,
        isLoadingMore = viewModel.isLoadingMore,
        errorMessage = viewModel.errorMessage,
        errorTitle = "خطا در بارگذاری فیلم‌ها",
        onRetry = { viewModel.retry() },
        onRefresh = { viewModel.refresh() },
        onLoadMore = { viewModel.loadMoreMovies() },
        continueWatching = continueWatching,
        onRemoveContinue = removeContinue,
        onItemClick = { item ->
            movies.firstOrNull { it.id == item.id }?.let { StorageUtils.saveMovieToFile(context, it) }
            navController?.navigate("single_movie/${item.id}")
        }
    )
}
