package com.fazli.vispar.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoPlayerSettings(
    val seekTimeSeconds: Int = 10
) {
    companion object {
        val DEFAULT = VideoPlayerSettings()
    }
}