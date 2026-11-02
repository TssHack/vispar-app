package com.fazli.vispar.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LiquidTab
import com.fazli.vispar.ui.glass.LiquidTabs

/** Navigates to a top level tab, keeping one copy of each tab and restoring its state. */
fun NavController.navigateToTab(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        // Movies is the root once the splash is gone; keep one copy of each tab above it.
        popUpTo(AppScreens.Movies.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * The floating liquid glass tab bar. The selection is a lens of glass that can be dragged between
 * tabs; it magnifies and tints the icon under it.
 */
@Composable
fun LiquidBottomBar(navController: NavController, modifier: Modifier = Modifier) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val tabs = AppScreens.screens
    val selectedIndex = tabs.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val colors = Glass.colors

    LiquidTabs(
        selectedIndex = selectedIndex,
        onTabSelected = { index -> navController.navigateToTab(tabs[index].route) },
        tabsCount = tabs.size,
        modifier = modifier,
    ) {
        tabs.forEach { screen ->
            LiquidTab(onClick = { navController.navigateToTab(screen.route) }) {
                screen.iconRes?.let {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = null,
                        tint = colors.content,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = stringResource(screen.resourceId),
                    style = GlassType.CaptionBold.copy(fontSize = 11.sp, lineHeight = 14.sp),
                    color = colors.content,
                    maxLines = 1
                )
            }
        }
    }
}
