package com.fazli.vispar.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.fazli.vispar.R
import com.fazli.vispar.ui.theme.CustomIcons

sealed class AppScreens(
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector? = null,
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
        icon = CustomIcons.Movie
    )

    data object Series : AppScreens(
        route = "series",
        resourceId = R.string.series,
        icon = CustomIcons.Series
    )

    data object Search : AppScreens(
        route = "search",
        resourceId = R.string.search,
        icon = CustomIcons.Search
    )

    data object Settings : AppScreens(
        route = "settings",
        resourceId = R.string.settings,
        icon = CustomIcons.Settings
    )

    data object SingleMovie : AppScreens(
        route = "single_movie/{movieId}",
        resourceId = R.string.movie_details,
        icon = CustomIcons.Movie,
        showBottomBar = false,
        showSidebar = false
    )
    
    data object SingleSeries : AppScreens(
        route = "single_series/{seriesId}",
        resourceId = R.string.series_details,
        icon = CustomIcons.Series,
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
        icon = CustomIcons.Settings,
        showBottomBar = false,
        showSidebar = false
    )

    companion object {
        val screens = listOf(Movies, Series, Search, Settings)
    }
}