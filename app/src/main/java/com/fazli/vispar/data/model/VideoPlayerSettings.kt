package com.fazli.vispar.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoPlayerSettings(
    val seekTimeSeconds: Int = 10,
    /** Start playing as soon as the player opens. */
    val autoplay: Boolean = true,
    /** Continue from where the user left off. */
    val resumePlayback: Boolean = true,
    /** Default playback speed. */
    val playbackSpeed: Float = 1f,
) {
    companion object {
        val DEFAULT = VideoPlayerSettings()
        val SPEEDS = listOf(0.75f, 1f, 1.25f, 1.5f, 2f)
    }
}
