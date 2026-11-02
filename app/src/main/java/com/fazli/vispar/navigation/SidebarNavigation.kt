package com.fazli.vispar.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LocalBackdrop
import com.fazli.vispar.ui.glass.glass
import com.kyant.shapes.RoundedRectangle

/**
 * Vertical glass rail for Android TV. Items are D-pad focusable; focus swells the item and fills
 * it with frosted accent glass.
 */
@Composable
fun SidebarNavigation(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val colors = Glass.colors

    Column(
        Modifier
            .fillMaxHeight()
            .padding(16.dp)
            .width(104.dp)
            .glass(
                backdrop = LocalBackdrop.current,
                shape = RoundedRectangle(36.dp),
                colors = colors,
                style = GlassStyle.Regular
            )
            .padding(vertical = 20.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ویسپار", style = GlassType.Headline, color = colors.accent)
        Spacer(Modifier.height(12.dp))
        AppScreens.screens.filter { it.showSidebar }.forEach { screen ->
            val selected = currentRoute == screen.route
            var focused by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(if (focused) 1.08f else 1f, spring(0.6f, 400f), label = "scale")
            val container by animateColorAsState(
                when {
                    focused -> colors.accent.copy(alpha = 0.55f)
                    selected -> colors.accent.copy(alpha = 0.22f)
                    else -> Color.Transparent
                },
                label = "container"
            )
            val content = if (selected || focused) colors.content else colors.contentSecondary

            Column(
                Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .fillMaxWidth()
                    .clip(RoundedRectangle(24.dp))
                    .background(container)
                    .onFocusChanged { focused = it.isFocused }
                    .clickable { navController.navigateToTab(screen.route) }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    screen.iconRes?.let {
                        Icon(painterResource(it), null, tint = content, modifier = Modifier.size(28.dp))
                    }
                }
                Text(stringResource(screen.resourceId), style = GlassType.CaptionBold, color = content, maxLines = 1)
            }
        }
    }
}
