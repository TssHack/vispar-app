package com.fazli.vispar.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.fazli.vispar.R

object CustomIcons {
    val Movie: Int = R.drawable.ic_movie
    val Series: Int = R.drawable.ic_series
    val Search: Int = R.drawable.ic_search
    val Settings: Int = R.drawable.ic_settings

    @Composable
    fun toImageVector(resourceId: Int): ImageVector {
        return ImageVector.vectorResource(resourceId)
    }
}
