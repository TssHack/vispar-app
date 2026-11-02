package com.fazli.vispar

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fazli.vispar.navigation.AppNavigation
import com.fazli.vispar.navigation.AppScreens
import com.fazli.vispar.navigation.LiquidBottomBar
import com.fazli.vispar.navigation.SidebarNavigation
import com.fazli.vispar.ui.glass.AmbientBackground
import com.fazli.vispar.ui.glass.GlassOverlayHost
import com.fazli.vispar.ui.glass.GlassOverlayLayer
import com.fazli.vispar.ui.glass.LocalBackdrop
import com.fazli.vispar.ui.glass.LocalBottomBarInset
import com.fazli.vispar.ui.glass.LocalGlassOverlayHost
import com.fazli.vispar.ui.theme.ThemeManager
import com.fazli.vispar.ui.theme.ThemeSettings
import com.fazli.vispar.ui.theme.visparTheme
import com.fazli.vispar.utils.DeviceUtils
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Portrait on phones and tablets; TVs have a fixed orientation.
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
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    var themeSettings by remember { mutableStateOf(themeManager.loadThemeSettings()) }

    // The whole app is right-to-left (Persian).
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        visparTheme(themeSettings) {
            MainScreen(
                onThemeSettingsChanged = {
                    themeSettings = it
                    themeManager.saveThemeSettings(it)
                }
            )
        }
    }
}

/**
 * Root of the glass UI. The scene is built from three stacked layers:
 *
 * 1. the ambient aurora background (recorded as `backgroundLayer`),
 * 2. the navigation host with every screen (recorded as `screensLayer`),
 * 3. floating chrome — the liquid tab bar and dialogs — which refracts both layers below it.
 */
@Composable
fun MainScreen(onThemeSettingsChanged: (ThemeSettings) -> Unit = {}) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val isTv = remember { DeviceUtils.isTv(context) }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentScreen = AppScreens.fromRoute(currentRoute)
    val showTabBar = !isTv && currentScreen.showBottomBar && currentRoute != null &&
            currentRoute != AppScreens.Splash.route
    val showSidebar = isTv && currentScreen.showSidebar && currentRoute != null &&
            currentRoute != AppScreens.Splash.route

    val backgroundLayer = rememberLayerBackdrop()
    val screensLayer = rememberLayerBackdrop()
    val chromeBackdrop = rememberCombinedBackdrop(backgroundLayer, screensLayer)
    val overlayHost = remember { GlassOverlayHost() }

    CompositionLocalProvider(
        LocalGlassOverlayHost provides overlayHost,
        LocalBottomBarInset provides if (showTabBar) 84.dp else 0.dp,
    ) {
        Box(Modifier.fillMaxSize()) {
            AmbientBackground(Modifier.fillMaxSize().layerBackdrop(backgroundLayer))

            Box(Modifier.fillMaxSize().layerBackdrop(screensLayer)) {
                CompositionLocalProvider(LocalBackdrop provides backgroundLayer) {
                    if (isTv) {
                        Row(Modifier.fillMaxSize()) {
                            AnimatedVisibility(visible = showSidebar) {
                                SidebarNavigation(navController)
                            }
                            Box(Modifier.weight(1f)) {
                                AppNavigation(navController, onThemeSettingsChanged)
                            }
                        }
                    } else {
                        AppNavigation(navController, onThemeSettingsChanged)
                    }
                }
            }

            AnimatedVisibility(
                visible = showTabBar,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = fadeIn() + slideInVertically(spring(0.75f, 320f, IntOffset.VisibilityThreshold)) { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                CompositionLocalProvider(LocalBackdrop provides chromeBackdrop) {
                    LiquidBottomBar(
                        navController = navController,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                            .widthIn(max = 520.dp)
                            .fillMaxWidth()
                    )
                }
            }

            GlassOverlayLayer(overlayHost, chromeBackdrop)
        }
    }
}
