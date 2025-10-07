package com.fazli.vispar.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fazli.vispar.ui.theme.CustomIcons
import com.fazli.vispar.ui.theme.VazirFontFamily

@Composable
fun SidebarNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // تنظیم راست‌چینی برای نوار کناری
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(100.dp) // Increased width for better TV experience
                .padding(top = 8.dp, bottom = 8.dp, end = 8.dp), // Add padding
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
            shadowElevation = 8.dp
        ) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = Color.Transparent,
                header = {
                    // Optional header content
                    Spacer(modifier = Modifier.height(16.dp))
                }
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(28.dp) // Increased spacing between items
                    ) {
                        AppScreens.screens.filter { it.showSidebar }.forEach { screen ->
                            val isSelected = currentRoute == screen.route
                            val scale by animateFloatAsState(
                                targetValue = if (isSelected) 1.15f else 1f,
                                animationSpec = tween(durationMillis = 300),
                                label = "scale"
                            )
                            
                            val iconColor by animateColorAsState(
                                targetValue = if (isSelected) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                animationSpec = tween(durationMillis = 300),
                                label = "iconColor"
                            )
                            
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                animationSpec = tween(durationMillis = 300),
                                label = "textColor"
                            )

                            NavigationRailItem(
                                icon = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 16.dp) // Add horizontal padding
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp) // Increased size for better TV experience
                                                .scale(scale),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Surface(
                                                    modifier = Modifier.size(48.dp), // Increased size
                                                    shape = CircleShape,
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                ) {}
                                            }
                                            // تغییر اصلی در این بخش
                                            val icon = screen.getIcon() ?: CustomIcons.toImageVector(CustomIcons.Movie)
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = stringResource(screen.resourceId),
                                                tint = iconColor,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(screen.resourceId),
                                            color = textColor,
                                            fontFamily = VazirFontFamily, // Use Vazir font
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                    }
                                },
                                label = null, // We're using custom label in icon
                                selected = isSelected,
                                onClick = {
                                    if (currentRoute != screen.route) {
                                        navController.navigate(screen.route) {
                                            // Pop up to the start destination of the graph to
                                            // avoid building up a large stack of destinations
                                            // on the back stack as users select items
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            // Avoid multiple copies of the same destination when
                                            // reselecting the same item
                                            launchSingleTop = true
                                            // Restore state when reselecting a previously selected item
                                            restoreState = true
                                        }
                                    }
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                    unselectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                    
                    // Optional footer content like settings
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

