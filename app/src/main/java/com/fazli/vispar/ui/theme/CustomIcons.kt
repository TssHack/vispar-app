package com.fazli.vispar.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.fazli.vispar.R

object CustomIcons {
    val Movie: Int = R.drawable.ic_movie
    val Series: Int = R.drawable.ic_series
    val Search: Int = R.drawable.ic_search
    val Settings: Int = R.drawable.ic_settings

    @Composable
    fun toPainter(resourceId: Int): Painter {
        return painterResource(id = resourceId)
    }
}
