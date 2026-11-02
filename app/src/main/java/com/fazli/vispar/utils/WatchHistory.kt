package com.fazli.vispar.utils

import android.content.Context
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/** One entry of "continue watching". Keyed by the stream URL. */
@Serializable
data class WatchEntry(
    val url: String,
    val title: String,
    val subtitle: String? = null,
    val image: String = "",
    val contentId: Int = 0,
    /** "movie" or "series". */
    val type: String = "movie",
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val updatedAt: Long = 0,
) {
    val progress: Float get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f

    /** Worth resuming: watched for a bit, not finished. */
    val isResumable: Boolean get() = positionMs > 10_000 && (durationMs <= 0 || positionMs < durationMs - 30_000)
}

/** Small JSON-file store of playback positions, most recent first. */
object WatchHistory {
    private const val TAG = "WatchHistory"
    private const val FILE = "watch_history.json"
    private const val MAX_ENTRIES = 30
    private val json = Json { ignoreUnknownKeys = true }

    @Synchronized
    fun all(context: Context): List<WatchEntry> = try {
        val file = File(context.filesDir, FILE)
        if (file.exists()) json.decodeFromString<List<WatchEntry>>(file.readText()) else emptyList()
    } catch (e: Exception) {
        Log.e(TAG, "Failed to read history", e)
        emptyList()
    }

    fun get(context: Context, url: String): WatchEntry? = all(context).firstOrNull { it.url == url }

    /** Entries that can be resumed, newest first, optionally filtered by type. */
    fun continueWatching(context: Context, type: String? = null): List<WatchEntry> =
        all(context).filter { it.isResumable && (type == null || it.type == type) }

    @Synchronized
    fun save(context: Context, entry: WatchEntry) {
        val updated = listOf(entry.copy(updatedAt = System.currentTimeMillis())) +
                all(context).filterNot { it.url == entry.url }
        write(context, updated.take(MAX_ENTRIES))
    }

    @Synchronized
    fun remove(context: Context, url: String) = write(context, all(context).filterNot { it.url == url })

    @Synchronized
    fun clear(context: Context) = write(context, emptyList())

    private fun write(context: Context, entries: List<WatchEntry>) {
        try {
            File(context.filesDir, FILE).writeText(json.encodeToString(entries))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write history", e)
        }
    }
}
