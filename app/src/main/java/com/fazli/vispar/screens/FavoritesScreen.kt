package com.fazli.vispar.screens

import androidx.compose.ui.res.stringResource
import com.fazli.vispar.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fazli.vispar.data.model.FavoriteItem
import com.fazli.vispar.navigation.AppScreens
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassAlertDialog
import com.fazli.vispar.ui.glass.GlassChip
import com.fazli.vispar.ui.glass.GlassScaffold
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LargeTitle
import com.fazli.vispar.ui.glass.LiquidIconButton
import com.fazli.vispar.ui.glass.StatusView
import com.fazli.vispar.ui.glass.formatRating
import com.fazli.vispar.utils.StorageUtils
import com.kyant.shapes.RoundedRectangle

@Composable
fun FavoritesScreen(navController: NavController) {
    val context = LocalContext.current
    val colors = Glass.colors
    var favorites by remember { mutableStateOf<List<FavoriteItem>>(emptyList()) }
    var confirmClearAll by remember { mutableStateOf(false) }

    val syncRevision by com.fazli.vispar.account.SyncManager.revision.collectAsState()
    LaunchedEffect(syncRevision) {
        favorites = StorageUtils.loadAllFavorites(context)
    }

    GlassScaffold(
        title = stringResource(R.string.favorites),
        onBack = { navController.popBackStack() },
        actions = {
            if (favorites.isNotEmpty()) {
                LiquidIconButton(
                    onClick = { confirmClearAll = true },
                    icon = Icons.Rounded.DeleteSweep,
                    contentDescription = stringResource(R.string.delete_all),
                    iconTint = colors.danger
                )
            }
        }
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                StatusView(
                    icon = Icons.Rounded.FavoriteBorder,
                    title = stringResource(R.string.no_favorites),
                    message = stringResource(R.string.no_favorites_msg),
                    iconTint = colors.heart
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Glass.ScreenPadding,
                    end = Glass.ScreenPadding,
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "title") {
                    LargeTitle(stringResource(R.string.favorites), stringResource(R.string.n_saved, favorites.size), Modifier.padding(bottom = 4.dp))
                }
                items(favorites, key = { "${it.type}-${it.id}" }) { favorite ->
                    FavoriteCard(
                        favorite = favorite,
                        modifier = Modifier.animateItem(),
                        onClick = {
                            StorageUtils.saveFavoriteToDatabase(context, favorite)
                            when (favorite.type) {
                                "movie" -> navController.navigate(
                                    AppScreens.SingleMovie.route.replace("{movieId}", favorite.id.toString())
                                )
                                "series" -> navController.navigate(
                                    AppScreens.SingleSeries.route.replace("{seriesId}", favorite.id.toString())
                                )
                            }
                        },
                        onDelete = {
                            StorageUtils.removeFavorite(context, favorite.id, favorite.type)
                            favorites = StorageUtils.loadAllFavorites(context)
                            showToast(context, context.getString(R.string.removed_from_favorites))
                        }
                    )
                }
            }
        }
    }

    if (confirmClearAll) {
        GlassAlertDialog(
            title = stringResource(R.string.delete_all_favorites),
            message = stringResource(R.string.delete_all_favorites_msg),
            confirmText = stringResource(R.string.delete_all),
            onConfirm = {
                StorageUtils.clearAllFavorites(context)
                favorites = emptyList()
                confirmClearAll = false
                showToast(context, context.getString(R.string.all_favorites_deleted))
            },
            dismissText = stringResource(R.string.cancel),
            onDismiss = { confirmClearAll = false },
            icon = Icons.Rounded.DeleteSweep,
            destructive = true
        )
    }
}

@Composable
private fun FavoriteCard(
    favorite: FavoriteItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Glass.colors
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedRectangle(28.dp),
        onClick = onClick
    ) {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FramedPoster(favorite.image, favorite.title, Modifier.width(76.dp).height(112.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(favorite.title, style = GlassType.Headline, color = colors.content, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    GlassChip(
                        tint = colors.accent.copy(alpha = 0.25f),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(if (favorite.type == "movie") stringResource(R.string.movie) else stringResource(R.string.serie), style = GlassType.CaptionBold, color = colors.content)
                    }
                    if (favorite.year > 0) {
                        Text("${favorite.year}", style = GlassType.Caption, color = colors.contentSecondary)
                    }
                }
                if (favorite.imdb > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Star, null, tint = colors.gold, modifier = Modifier.size(16.dp))
                        Text(" ${formatRating(favorite.imdb)}", style = GlassType.CaptionBold, color = colors.content)
                    }
                }
            }
            LiquidIconButton(
                onClick = onDelete,
                icon = Icons.Rounded.DeleteOutline,
                contentDescription = stringResource(R.string.delete),
                iconTint = colors.danger,
                size = 42.dp,
                iconSize = 20.dp
            )
        }
    }
}
