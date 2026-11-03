package com.fazli.vispar.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fazli.vispar.R
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LiquidButton
import com.fazli.vispar.ui.glass.LiquidTab
import com.fazli.vispar.ui.glass.LiquidTabs
import com.fazli.vispar.ui.glass.isMonochromeAccent

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

private val BarHeight = 64.dp

/** iOS system blue, used for the search button when the accent is monochrome. */
private val SearchBlue = Color(0xFF0088FF)

/**
 * The floating liquid glass tab bar, iOS 26 style: the main tabs share one glass capsule with a
 * draggable lens, and search sits beside it as its own tinted glass circle.
 */
@Composable
fun LiquidBottomBar(navController: NavController, modifier: Modifier = Modifier) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val tabs = AppScreens.screens.filter { it != AppScreens.Search }
    val tabIndex = tabs.indexOfFirst { it.route == currentRoute }
    val colors = Glass.colors

    Row(
        modifier.height(BarHeight),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiquidTabs(
            selectedIndex = tabIndex.coerceAtLeast(0),
            onTabSelected = { index -> navController.navigateToTab(tabs[index].route) },
            tabsCount = tabs.size,
            height = BarHeight,
            showLens = tabIndex >= 0,
            modifier = Modifier.weight(1f),
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

        // Tinted glass search button: hue-tinted refraction plus a 75% colour wash.
        val tint = if (colors.isMonochromeAccent) SearchBlue else colors.accent
        LiquidButton(
            onClick = { navController.navigateToTab(AppScreens.Search.route) },
            modifier = Modifier.size(BarHeight),
            tint = tint,
            shape = CircleShape,
            height = BarHeight,
            contentPadding = PaddingValues(0.dp),
            blurRadius = 4.dp,
            refractionHeight = 16.dp,
            refractionAmount = 32.dp,
            tintAlpha = 0.75f,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = stringResource(R.string.search),
                tint = if (tint.luminance() > 0.6f) Color.Black else Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
