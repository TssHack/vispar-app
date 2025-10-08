package com.fazli.vispar.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.painter.Painter
import com.fazli.vispar.R

sealed class AppScreens(
    val route: String,
    @StringRes val resourceId: Int,
    @DrawableRes val iconRes: Int? = null,
    val showBottomBar: Boolean = true,
    val showSidebar: Boolean = true
) {
    data object Splash : AppScreens(
        route = "splash",
        resourceId = R.string.app_name
    )

    data object Movies : AppScreens(
        route = "movies",
        resourceId = R.string.movies,
        iconRes = R.drawable.ic_movie
    )

    data object Series : AppScreens(
        route = "series",
        resourceId = R.string.series,
        iconRes = R.drawable.ic_series
    )

    data object Search : AppScreens(
        route = "search",
        resourceId = R.string.search,
        iconRes = R.drawable.ic_search
    )

    data object Settings : AppScreens(
        route = "settings",
        resourceId = R.string.settings,
        iconRes = R.drawable.ic_settings
    )

    data object SingleMovie : AppScreens(
        route = "single_movie/{movieId}",
        resourceId = R.string.movie_details,
        iconRes = R.drawable.ic_movie,
        showBottomBar = false,
        showSidebar = false
    )

    data object SingleSeries : AppScreens(
        route = "single_series/{seriesId}",
        resourceId = R.string.series_details,
        iconRes = R.drawable.ic_series,
        showBottomBar = false,
        showSidebar = false
    )

    data object Favorites : AppScreens(
        route = "favorites",
        resourceId = R.string.favorites,
        showBottomBar = false,
        showSidebar = false
    )

    data object About : AppScreens(
        route = "about",
        resourceId = R.string.about,
        iconRes = R.drawable.ic_settings,
        showBottomBar = false,
        showSidebar = false
    )

    companion object {
        val screens = listOf(Movies, Series, Search, Settings)
    }
}

@Composable
fun AppScreens.getIcon(): Painter? {
    return iconRes?.let { painterResource(id = it) }
}
