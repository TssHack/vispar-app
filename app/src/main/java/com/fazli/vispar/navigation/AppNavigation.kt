package com.fazli.vispar.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fazli.vispar.screens.AboutScreen
import com.fazli.vispar.screens.MoviesScreen
import com.fazli.vispar.screens.SearchScreen
import com.fazli.vispar.screens.SeriesScreen
import com.fazli.vispar.screens.SettingsScreen
import com.fazli.vispar.screens.SingleMovieScreen
import com.fazli.vispar.screens.SingleSeriesScreen
import com.fazli.vispar.screens.SplashScreen
import com.fazli.vispar.screens.FavoritesScreen
import com.fazli.vispar.ui.theme.ThemeSettings
import com.fazli.vispar.ui.theme.ThemeManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppNavigation(
    navController: NavHostController,
    onThemeSettingsChanged: (ThemeSettings) -> Unit = {}
) {
    val context = LocalContext.current
    val themeManager = ThemeManager(context)
    val themeSettings = themeManager.loadThemeSettings()
    
    NavHost(
        navController = navController,
        startDestination = AppScreens.Splash.route
    ) {
        composable(route = AppScreens.Splash.route) {
            SplashScreen(
                onTimeout = {
                    navController.popBackStack()
                    navController.navigate(AppScreens.Movies.route)
                },
                backgroundColor = if (themeSettings.themeMode == com.fazli.vispar.ui.theme.ThemeMode.DARK) {
                    androidx.compose.ui.graphics.Color(0xFF121212)
                } else {
                    androidx.compose.ui.graphics.Color(0xFFFFFBFE)
                }
            )
        }
        
        composable(route = AppScreens.Movies.route) {
            MoviesScreen(navController = navController)
        }
        composable(route = AppScreens.Series.route) {
            SeriesScreen(navController = navController)
        }
        composable(route = AppScreens.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(route = AppScreens.Settings.route) {
            SettingsScreen(onThemeSettingsChanged, navController)
        }
        composable(route = AppScreens.Favorites.route) {
            FavoritesScreen(navController)
        }
        composable(route = AppScreens.About.route) {
            AboutScreen(navController)
        }
        composable(
            route = AppScreens.SingleMovie.route,
            arguments = listOf(navArgument("movieId") { defaultValue = "0" })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull() ?: 0
            SingleMovieScreen(movieId = movieId, navController = navController)
        }
        composable(
            route = AppScreens.SingleSeries.route,
            arguments = listOf(navArgument("seriesId") { defaultValue = "0" })
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getString("seriesId")?.toIntOrNull() ?: 0
            SingleSeriesScreen(seriesId = seriesId, navController = navController)
        }
    }
}