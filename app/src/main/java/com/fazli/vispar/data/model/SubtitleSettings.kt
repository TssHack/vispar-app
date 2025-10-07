package com.fazli.vispar.data.model

import android.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color as ComposeColor
import kotlinx.serialization.Serializable

@Serializable
data class SubtitleSettings(
    val backgroundColor: Int = Color.BLACK,
    val textColor: Int = Color.YELLOW,
    val borderColor: Int = Color.TRANSPARENT,
    val textSize: Float = 16f
) {
    companion object {
        // Default subtitle settings
        val DEFAULT = SubtitleSettings()
        
        // Convert Android Color int to Compose Color
        fun Int.toComposeColor(): ComposeColor = ComposeColor(this)
        
        // Convert Compose Color to Android Color int
        fun ComposeColor.toInt(): Int = this.toArgb()
    }
}