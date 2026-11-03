package com.fazli.vispar.account

import android.content.Context
import android.util.Log
import com.fazli.vispar.data.api.Api
import com.fazli.vispar.data.model.FavoriteItem
import com.fazli.vispar.utils.StorageUtils
import com.fazli.vispar.utils.WatchEntry
import com.fazli.vispar.utils.WatchHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Serializable
private data class RemoteFavorite(
    val kind: String,
    @SerialName("content_id") val contentId: Long,
    val title: String = "",
    val image: String = "",
    val payload: JsonElement? = null,
    val deleted: Boolean = false,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
private data class RemoteProgress(
    @SerialName("stream_key") val streamKey: String,
    val kind: String = "movie",
    @SerialName("content_id") val contentId: Long = 0,
    val title: String = "",
    val subtitle: String = "",
    val image: String = "",
    @SerialName("position_ms") val positionMs: Long = 0,
    @SerialName("duration_ms") val durationMs: Long = 0,
    val deleted: Boolean = false,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
private data class SyncBody(
    val favorites: List<RemoteFavorite> = emptyList(),
    val progress: List<RemoteProgress> = emptyList(),
    @SerialName("server_time") val serverTime: String? = null,
)

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Done(val atMs: Long) : SyncStatus
    data class Failed(val message: String) : SyncStatus
}

/**
 * Keeps favourites and "continue watching" in step across the user's devices (phone ⇄ TV).
 *
 * Local stores stay the source of truth for the UI; every change is stamped with a time and
 * deletions leave a tombstone. Sync pushes what changed since the last push and pulls what changed
 * on the server since the last pull; for each item the newest change wins on both sides.
 */
object SyncManager {
    private const val TAG = "Sync"
    private const val PREFS = "vispar_sync"
    private const val TOMBSTONES = "sync_tombstones.json"
    private const val BATCH = 500

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private var pending: Job? = null

    private val _revision = MutableStateFlow(0)
    /** Bumped whenever a pull changed local data, so screens can reload. */
    val revision: StateFlow<Int> = _revision.asStateFlow()

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    // ---- Hooks called by the local stores -----------------------------------------------------

    fun favoriteChanged(context: Context, type: String, id: Int, deleted: Boolean) {
        tombstone(context, favKey(type, id), if (deleted) now() else null)
        schedule(context, 3_000)
    }

    fun progressChanged(context: Context, url: String, deleted: Boolean) {
        tombstone(context, watchKey(url), if (deleted) now() else null)
        // Positions are saved often while playing; batch them.
        schedule(context, if (deleted) 3_000 else 15_000)
    }

    // ---- Triggers ----------------------------------------------------------------------------

    /** Sync soon (debounced). Does nothing while signed out. */
    fun schedule(context: Context, delayMs: Long = 0) {
        if (!AccountManager.isSignedIn) return
        val app = context.applicationContext
        pending?.cancel()
        pending = scope.launch {
            delay(delayMs)
            syncNow(app)
        }
    }

    /** First sync after signing in merges everything this device has with the account. */
    fun onSignedIn(context: Context) {
        prefs(context).edit().remove("last_pull").putLong("last_push", 0).apply()
        schedule(context)
    }

    /** Forgets sync position (after deleting the account); local data is kept. */
    fun forget(context: Context) {
        prefs(context).edit().clear().apply()
        File(context.filesDir, TOMBSTONES).delete()
    }

    suspend fun syncNow(context: Context): Boolean = mutex.withLock {
        if (!AccountManager.isSignedIn) return false
        _status.value = SyncStatus.Syncing
        return try {
            push(context)
            val changed = pull(context)
            if (changed) _revision.value++
            _status.value = SyncStatus.Done(System.currentTimeMillis())
            prefs(context).edit().putLong("last_sync", System.currentTimeMillis()).apply()
            true
        } catch (e: Exception) {
            Log.w(TAG, "sync failed: ${e.message}")
            _status.value = SyncStatus.Failed(e.message ?: "")
            false
        }
    }

    fun lastSyncMs(context: Context): Long = prefs(context).getLong("last_sync", 0)

    // ---- Push --------------------------------------------------------------------------------

    private suspend fun push(context: Context) {
        val prefs = prefs(context)
        val since = prefs.getLong("last_push", 0)
        val startedAt = now()
        val tombs = tombstones(context)

        val favorites = StorageUtils.loadAllFavorites(context)
            .filter { it.updatedAt > since || since == 0L }
            .map {
                RemoteFavorite(
                    kind = it.type, contentId = it.id.toLong(), title = it.title, image = it.image,
                    payload = Api.json.encodeToJsonElement(FavoriteItem.serializer(), it),
                    updatedAt = iso(it.updatedAt.takeIf { t -> t > 0 } ?: startedAt)
                )
            } + tombs.filter { it.key.startsWith("fav:") && it.value > since }.mapNotNull { (key, time) ->
                val parts = key.split(":")
                val id = parts.getOrNull(2)?.toLongOrNull() ?: return@mapNotNull null
                RemoteFavorite(kind = parts[1], contentId = id, deleted = true, updatedAt = iso(time))
            }

        val progress = WatchHistory.all(context)
            .filter { it.updatedAt > since || since == 0L }
            .map {
                RemoteProgress(
                    streamKey = it.url, kind = it.type, contentId = it.contentId.toLong(), title = it.title,
                    subtitle = it.subtitle.orEmpty(), image = it.image, positionMs = it.positionMs,
                    durationMs = it.durationMs, updatedAt = iso(it.updatedAt.takeIf { t -> t > 0 } ?: startedAt)
                )
            } + tombs.filter { it.key.startsWith("watch:") && it.value > since }.map { (key, time) ->
                RemoteProgress(streamKey = key.removePrefix("watch:"), deleted = true, updatedAt = iso(time))
            }

        val favChunks = favorites.filter { it.kind == "movie" || it.kind == "series" }.chunked(BATCH)
        val progChunks = progress.chunked(BATCH)
        for (i in 0 until maxOf(favChunks.size, progChunks.size)) {
            val body = SyncBody(favChunks.getOrElse(i) { emptyList() }, progChunks.getOrElse(i) { emptyList() })
            val text = Api.json.encodeToString(SyncBody.serializer(), body)
            AccountManager.authed(
                Request.Builder().url(Api.BASE + "/sync").post(text.toRequestBody("application/json".toMediaType()))
            ).close()
        }
        prefs.edit().putLong("last_push", startedAt).apply()
        // Tombstones older than a month have reached every device that syncs at all.
        pruneTombstones(context, startedAt - 30L * 24 * 3600 * 1000)
    }

    // ---- Pull --------------------------------------------------------------------------------

    private suspend fun pull(context: Context): Boolean {
        val prefs = prefs(context)
        val since = prefs.getString("last_pull", null)
        val url = Api.BASE + "/sync" + (since?.let { "?since=" + URLEncoder.encode(it, "UTF-8") } ?: "")
        val json: JsonObject = AccountManager.authedJson(Request.Builder().url(url).get())
        val body = Api.json.decodeFromJsonElement(SyncBody.serializer(), json)
        var changed = false

        if (body.favorites.isNotEmpty()) {
            val tombs = tombstones(context).toMutableMap()
            val local = StorageUtils.loadAllFavorites(context).associateBy { favKey(it.type, it.id) }.toMutableMap()
            for (f in body.favorites) {
                val key = favKey(f.kind, f.contentId.toInt())
                val remoteTime = parseIso(f.updatedAt) ?: continue
                val localTime = maxOf(local[key]?.updatedAt ?: 0, tombs[key] ?: 0)
                if (remoteTime <= localTime) continue
                if (f.deleted) {
                    if (local.remove(key) != null) changed = true
                    tombs[key] = remoteTime
                } else {
                    val item = f.payload?.let {
                        runCatching { Api.json.decodeFromJsonElement(FavoriteItem.serializer(), it) }.getOrNull()
                    } ?: continue
                    local[key] = item.copy(updatedAt = remoteTime)
                    tombs.remove(key)
                    changed = true
                }
            }
            StorageUtils.writeFavorites(context, local.values.sortedByDescending { it.updatedAt })
            writeTombstones(context, tombs)
        }

        if (body.progress.isNotEmpty()) {
            val tombs = tombstones(context).toMutableMap()
            val local = WatchHistory.all(context).associateBy { it.url }.toMutableMap()
            for (p in body.progress) {
                val key = watchKey(p.streamKey)
                val remoteTime = parseIso(p.updatedAt) ?: continue
                val localTime = maxOf(local[p.streamKey]?.updatedAt ?: 0, tombs[key] ?: 0)
                if (remoteTime <= localTime) continue
                if (p.deleted) {
                    if (local.remove(p.streamKey) != null) changed = true
                    tombs[key] = remoteTime
                } else {
                    local[p.streamKey] = WatchEntry(
                        url = p.streamKey, title = p.title, subtitle = p.subtitle.ifEmpty { null }, image = p.image,
                        contentId = p.contentId.toInt(), type = p.kind, positionMs = p.positionMs,
                        durationMs = p.durationMs, updatedAt = remoteTime
                    )
                    tombs.remove(key)
                    changed = true
                }
            }
            WatchHistory.replaceAll(context, local.values.toList())
            writeTombstones(context, tombs)
        }

        body.serverTime?.let { prefs.edit().putString("last_pull", it).apply() }
        return changed
    }

    // ---- Tombstones --------------------------------------------------------------------------

    private fun favKey(type: String, id: Int) = "fav:$type:$id"
    private fun watchKey(url: String) = "watch:$url"

    @Synchronized
    private fun tombstones(context: Context): Map<String, Long> = try {
        val f = File(context.filesDir, TOMBSTONES)
        if (f.exists()) Api.json.decodeFromString<Map<String, Long>>(f.readText()) else emptyMap()
    } catch (e: Exception) {
        emptyMap()
    }

    @Synchronized
    private fun writeTombstones(context: Context, map: Map<String, Long>) {
        runCatching { File(context.filesDir, TOMBSTONES).writeText(Api.json.encodeToString(map)) }
    }

    /** Sets (time != null) or clears a tombstone. */
    @Synchronized
    private fun tombstone(context: Context, key: String, time: Long?) {
        val map = tombstones(context).toMutableMap()
        val changed = if (time != null) map.put(key, time) != time else map.remove(key) != null
        if (changed) writeTombstones(context, map)
    }

    @Synchronized
    private fun pruneTombstones(context: Context, olderThan: Long) {
        val map = tombstones(context)
        val kept = map.filterValues { it >= olderThan }
        if (kept.size != map.size) writeTombstones(context, kept)
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun now() = System.currentTimeMillis()

    // ---- Time (minSdk 24: no java.time) ------------------------------------------------------

    private fun iso(ms: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(ms)

    private val isoPattern =
        Regex("""(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2})(?:\.(\d+))?(Z|[+-]\d{2}:\d{2})""")

    /** Parses RFC 3339 times as Go writes them (any fraction, Z or ±hh:mm). */
    internal fun parseIso(s: String): Long? {
        val m = isoPattern.matchEntire(s.trim()) ?: return null
        val g = m.groupValues
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"), Locale.US).apply {
            clear()
            set(g[1].toInt(), g[2].toInt() - 1, g[3].toInt(), g[4].toInt(), g[5].toInt(), g[6].toInt())
        }
        val millis = g[7].takeIf { it.isNotEmpty() }?.padEnd(3, '0')?.take(3)?.toInt() ?: 0
        var t = cal.timeInMillis + millis
        val zone = g[8]
        if (zone != "Z") {
            val sign = if (zone[0] == '-') -1 else 1
            val (hh, mm) = zone.substring(1).split(":").map { it.toInt() }
            t -= sign * (hh * 3_600_000L + mm * 60_000L)
        }
        return t
    }
}
