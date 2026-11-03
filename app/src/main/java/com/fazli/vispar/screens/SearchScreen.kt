package com.fazli.vispar.screens

import androidx.compose.ui.res.stringResource
import com.fazli.vispar.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fazli.vispar.data.model.Poster
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassScaffold
import com.fazli.vispar.ui.glass.GlassSearchField
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LargeTitle
import com.fazli.vispar.ui.glass.LiquidLoader
import com.fazli.vispar.ui.glass.LiquidTab
import com.fazli.vispar.ui.glass.LiquidTabs
import com.fazli.vispar.ui.glass.PosterCard
import com.fazli.vispar.ui.glass.StatusView
import com.fazli.vispar.ui.search.SearchViewModel
import com.fazli.vispar.ui.glass.LocalPosterColumns
import com.fazli.vispar.utils.StorageUtils

private val searchFilters = listOf(R.string.all, R.string.movie, R.string.serie)

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    navController: NavController? = null
) {
    val context = LocalContext.current
    val colors = Glass.colors
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val gridState = rememberLazyGridState()
    val density = LocalDensity.current
    val columns = LocalPosterColumns.current
    var filter by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        if (viewModel.searchQuery.isEmpty()) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    val results = viewModel.searchResults.filter {
        when (filter) {
            1 -> it.isMovie()
            2 -> it.isSeries()
            else -> true
        }
    }

    fun open(poster: Poster) {
        if (poster.isMovie()) {
            StorageUtils.saveMovieToFile(context, poster.toMovie())
            navController?.navigate("single_movie/${poster.id}")
        } else {
            StorageUtils.saveSeriesToFile(context, poster.toSeries())
            navController?.navigate("single_series/${poster.id}")
        }
    }

    GlassScaffold(
        title = stringResource(R.string.search),
        titleAlpha = { compactTitleAlpha(gridState, with(density) { 56.dp.toPx() }) },
    ) { padding ->
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Glass.ScreenPadding,
                end = Glass.ScreenPadding,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding()
            ),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "title") {
                LargeTitle(stringResource(R.string.search), stringResource(R.string.search_subtitle))
            }
            item(span = { GridItemSpan(maxLineSpan) }, key = "field") {
                GlassSearchField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = stringResource(R.string.search_placeholder),
                    onClear = { viewModel.clearSearch() },
                    onSearch = { focusManager.clearFocus() },
                    focusRequester = focusRequester
                )
            }
            if (viewModel.searchResults.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "filters") {
                    LiquidTabs(
                        selectedIndex = filter,
                        onTabSelected = { filter = it },
                        tabsCount = searchFilters.size,
                        height = 48.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        searchFilters.forEachIndexed { index, label ->
                            LiquidTab(onClick = { filter = index }) {
                                Text(stringResource(label), style = GlassType.Headline, color = colors.content)
                            }
                        }
                    }
                }
            }

            when {
                viewModel.isLoading -> item(span = { GridItemSpan(maxLineSpan) }, key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(top = 64.dp), contentAlignment = Alignment.Center) {
                        LiquidLoader()
                    }
                }

                viewModel.errorMessage != null -> item(span = { GridItemSpan(maxLineSpan) }, key = "error") {
                    StatusView(
                        icon = Icons.Rounded.CloudOff,
                        title = stringResource(R.string.search_error),
                        message = viewModel.errorMessage ?: stringResource(R.string.unknown_error),
                        iconTint = colors.danger,
                        actionText = stringResource(R.string.retry),
                        actionIcon = Icons.Rounded.Refresh,
                        onAction = { viewModel.updateSearchQuery(viewModel.searchQuery) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                results.isNotEmpty() -> items(results, key = { "r${it.type}${it.id}" }) { poster ->
                    PosterCard(
                        imageUrl = poster.image,
                        title = poster.title,
                        subtitle = listOfNotNull(
                            poster.year.takeIf { it > 0 }?.toString(),
                            poster.genres.firstOrNull()?.title
                        ).joinToString(" • "),
                        rating = poster.imdb,
                        badge = if (poster.isMovie()) stringResource(R.string.movie) else stringResource(R.string.serie),
                        onClick = { open(poster) }
                    )
                }

                viewModel.searchQuery.isNotEmpty() -> item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                    StatusView(
                        icon = Icons.Rounded.SearchOff,
                        title = stringResource(R.string.no_results),
                        message = stringResource(R.string.no_results_msg),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                else -> item(span = { GridItemSpan(maxLineSpan) }, key = "hint") {
                    StatusView(
                        icon = Icons.Rounded.TravelExplore,
                        title = stringResource(R.string.search_hint_title),
                        message = stringResource(R.string.search_hint_msg),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
