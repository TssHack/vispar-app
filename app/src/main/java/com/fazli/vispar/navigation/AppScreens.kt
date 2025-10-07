package com.fazli.vispar.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.fazli.vispar.R
import com.fazli.vispar.ui.theme.CustomIcons

sealed class AppScreens(
    val route: String,
    @StringRes val resourceId: Int,
    val iconResource: Int? = null, // تغییر به Int? به جای ImageVector?
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
        iconResource = CustomIcons.Movie
    )

    data object Series : AppScreens(
        route = "series",
        resourceId = R.string.series,
        iconResource = CustomIcons.Series
    )

    data object Search : AppScreens(
        route = "search",
        resourceId = R.string.search,
        iconResource = CustomIcons.Search
    )

    data object Settings : AppScreens(
        route = "settings",
        resourceId = R.string.settings,
        iconResource = CustomIcons.Settings
    )

    data object SingleMovie : AppScreens(
        route = "single_movie/{movieId}",
        resourceId = R.string.movie_details,
        iconResource = CustomIcons.Movie,
        showBottomBar = false,
        showSidebar = false
    )
    
    data object SingleSeries : AppScreens(
        route = "single_series/{seriesId}",
        resourceId = R.string.series_details,
        iconResource = CustomIcons.Series,
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
        iconResource = CustomIcons.Settings,
        showBottomBar = false,
        showSidebar = false
    )

    companion object {
        val screens = listOf(Movies, Series, Search, Settings)
        
        // تابع کمکی برای تبدیل منبع به ImageVector
        @Composable
        fun AppScreens.getIcon(): ImageVector? {
            return iconResource?.let { CustomIcons.toImageVector(it) }
        }
    }
}
