package com.fazli.vispar.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.fazli.vispar.R

object CustomIcons {
    @Composable
    fun Movie(): Painter = painterResource(R.drawable.ic_movie)
    @Composable
    fun Series(): Painter = painterResource(R.drawable.ic_series)
    @Composable
    fun Search(): Painter = painterResource(R.drawable.ic_search)
    @Composable
    fun Settings(): Painter = painterResource(R.drawable.ic_settings)
}
