package com.fazli.vispar.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.fazli.vispar.R

object CustomIcons {
    @DrawableRes val Movie: Int = R.drawable.ic_movie
    @DrawableRes val Series: Int = R.drawable.ic_series
    @DrawableRes val Search: Int = R.drawable.ic_search
    @DrawableRes val Settings: Int = R.drawable.ic_settings

    @Composable
    fun toPainter(@DrawableRes resourceId: Int): Painter {
        return painterResource(id = resourceId)
    }
}
