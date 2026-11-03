package com.fazli.vispar.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fazli.vispar.screens.AboutScreen
import com.fazli.vispar.screens.AccountScreen
import com.fazli.vispar.screens.FavoritesScreen
import com.fazli.vispar.screens.MoviesScreen
import com.fazli.vispar.screens.SearchScreen
import com.fazli.vispar.screens.SeriesScreen
import com.fazli.vispar.screens.SettingsScreen
import com.fazli.vispar.screens.SingleMovieScreen
import com.fazli.vispar.screens.SingleSeriesScreen
import com.fazli.vispar.screens.SplashScreen
import com.fazli.vispar.ui.theme.ThemeSettings

private val tabRoutes = AppScreens.screens.map { it.route }.toSet()

private fun NavBackStackEntry.isTab() = destination.route in tabRoutes

private const val Duration = 380

/** Tabs cross-fade with a subtle zoom; pushed screens slide in from the reading direction. */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.enter(): EnterTransition =
    if (initialState.isTab() && targetState.isTab()) {
        fadeIn(tween(260)) + scaleIn(tween(320, easing = FastOutSlowInEasing), initialScale = 0.97f)
    } else {
        // RTL: new content enters from the left edge.
        fadeIn(tween(Duration)) + slideInHorizontally(tween(Duration, easing = FastOutSlowInEasing)) { -it / 3 }
    }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.exit(): ExitTransition =
    if (initialState.isTab() && targetState.isTab()) {
        fadeOut(tween(200))
    } else {
        fadeOut(tween(Duration)) + scaleOut(tween(Duration), targetScale = 0.94f)
    }

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnter(): EnterTransition =
    fadeIn(tween(Duration)) + scaleIn(tween(Duration, easing = FastOutSlowInEasing), initialScale = 0.94f)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExit(): ExitTransition =
    fadeOut(tween(Duration)) + slideOutHorizontally(tween(Duration, easing = FastOutSlowInEasing)) { -it / 3 }

@Composable
fun AppNavigation(
    navController: NavHostController,
    onThemeSettingsChanged: (ThemeSettings) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = AppScreens.Splash.route,
        enterTransition = { enter() },
        exitTransition = { exit() },
        popEnterTransition = { popEnter() },
        popExitTransition = { popExit() },
    ) {
        composable(
            route = AppScreens.Splash.route,
            exitTransition = { fadeOut(tween(500)) + scaleOut(tween(500), targetScale = 1.08f) }
        ) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(AppScreens.Movies.route) {
                        popUpTo(AppScreens.Splash.route) { inclusive = true }
                    }
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
        composable(route = AppScreens.Account.route) {
            AccountScreen(navController)
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
