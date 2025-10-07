package com.fazli.vispar

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.fazli.vispar.navigation.AppNavigation
import com.fazli.vispar.navigation.AppScreens
import com.fazli.vispar.navigation.BottomNavigationBar
import com.fazli.vispar.navigation.SidebarNavigation
import com.fazli.vispar.ui.theme.visparTheme
import com.fazli.vispar.ui.theme.ThemeManager
import com.fazli.vispar.ui.theme.ThemeSettings
import com.fazli.vispar.utils.DeviceUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set default orientation to portrait for mobile/tablet
        // For TV, we don't set orientation as it's typically fixed
        if (!DeviceUtils.isTv(this)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        enableEdgeToEdge()
        setContent {
            MainApp()
        }
    }
}

@Composable
fun MainApp() {
    val themeManager = ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    var themeSettings by remember { mutableStateOf(themeManager.loadThemeSettings()) }
    
    // Save theme settings when they change
    LaunchedEffect(themeSettings) {
        themeManager.saveThemeSettings(themeSettings)
    }
    
    visparTheme(themeSettings) {
        MainScreen(onThemeSettingsChanged = { themeSettings = it })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onThemeSettingsChanged: (ThemeSettings) -> Unit = {}) {
    val navController = rememberNavController()
    val themeManager = ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    val themeSettings = themeManager.loadThemeSettings()
    val configuration = LocalConfiguration.current
    val isTv = DeviceUtils.isTv(androidx.compose.ui.platform.LocalContext.current)
    
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    // Find the current screen, including the SingleMovie and SingleSeries screens
    val currentScreen = when {
        currentRoute?.startsWith("single_movie") == true -> AppScreens.SingleMovie
        currentRoute?.startsWith("single_series") == true -> AppScreens.SingleSeries
        currentRoute == AppScreens.Favorites.route -> AppScreens.Favorites
        currentRoute == AppScreens.About.route -> AppScreens.About
        else -> AppScreens.screens.find { it.route == currentRoute } ?: AppScreens.Movies
    }
    
    // System UI controller for edge-to-edge support
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = themeSettings.themeMode != com.fazli.vispar.ui.theme.ThemeMode.DARK
    
    LaunchedEffect(themeSettings) {
        systemUiController.setStatusBarColor(
            color = androidx.compose.ui.graphics.Color.Transparent,
            darkIcons = useDarkIcons
        )
        systemUiController.setNavigationBarColor(
            color = androidx.compose.ui.graphics.Color.Transparent,
            darkIcons = useDarkIcons
        )
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show bottom bar if the current screen requires it and we're not on splash
            // and we're not on TV (TV uses sidebar instead)
            if (!isTv && currentScreen.showBottomBar && currentRoute != AppScreens.Splash.route) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        if (isTv) {
            // TV layout with sidebar navigation
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Sidebar navigation for TV
                SidebarNavigation(navController)
                
                // Main content area with padding to separate from sidebar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, end = 24.dp, top = 24.dp, bottom = 24.dp)
                ) {
                    // Pass theme settings callback to navigation
                    AppNavigation(navController, onThemeSettingsChanged)
                }
            }
        } else {
            // Mobile/tablet layout with bottom navigation
            Box(modifier = Modifier.padding(innerPadding)) {
                // Pass theme settings callback to navigation
                AppNavigation(navController, onThemeSettingsChanged)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val themeManager = ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    val themeSettings = themeManager.loadThemeSettings()
    
    visparTheme(themeSettings) {
        MainScreen()
    }
}