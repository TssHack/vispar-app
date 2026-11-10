package com.fazli.vispar.account

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.fazli.vispar.BuildConfig
import com.fazli.vispar.R
import com.fazli.vispar.data.api.Api
import com.fazli.vispar.ui.glass.DialogAction
import com.fazli.vispar.ui.glass.DialogActionStyle
import com.fazli.vispar.ui.glass.DialogHeader
import com.fazli.vispar.ui.glass.GlassDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.Request

@Serializable
data class Announcement(val id: String = "", val fa: String = "", val en: String = "")

/** Served by /v1/config and edited from the admin panel. */
@Serializable
data class RemoteConfig(
    @SerialName("min_version_code") val minVersionCode: Int = 0,
    @SerialName("latest_version_name") val latestVersionName: String = "",
    @SerialName("update_url") val updateUrl: String = "",
    val announcement: Announcement? = null,
)

private suspend fun fetchConfig(): RemoteConfig? = withContext(Dispatchers.IO) {
    runCatching {
        Api.client.newCall(Request.Builder().url(Api.BASE + "/config").build()).execute().use { res ->
            if (!res.isSuccessful) null else Api.json.decodeFromString(RemoteConfig.serializer(), res.body!!.string())
        }
    }.getOrNull()
}

/**
 * Shows, once per launch, a forced-update prompt when this build is older than the minimum the
 * server accepts, otherwise any new announcement (each one only once).
 */
@Composable
fun AppNotices() {
    val context = LocalContext.current
    val language = LocalConfiguration.current.locales[0].language
    var config by remember { mutableStateOf<RemoteConfig?>(null) }
    var dismissedAnnouncement by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { config = fetchConfig() }
    val cfg = config ?: return
    val prefs = remember { context.getSharedPreferences("vispar_notices", Context.MODE_PRIVATE) }

    if (cfg.minVersionCode > BuildConfig.VERSION_CODE && cfg.updateUrl.isNotBlank()) {
        GlassDialog(onDismissRequest = {}) {
            DialogHeader(
                stringResource(R.string.update_required),
                stringResource(R.string.update_required_msg, cfg.latestVersionName.ifBlank { "" }),
                Icons.Rounded.SystemUpdate
            )
            DialogAction(stringResource(R.string.update_now), onClick = {
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(cfg.updateUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }, style = DialogActionStyle.Primary)
        }
        return
    }

    val ann = cfg.announcement ?: return
    val text = (if (language == "fa") ann.fa else ann.en).ifBlank { ann.fa.ifBlank { ann.en } }
    if (dismissedAnnouncement || ann.id.isBlank() || text.isBlank() || prefs.getString("seen", null) == ann.id) return
    val dismiss = {
        prefs.edit().putString("seen", ann.id).apply()
        dismissedAnnouncement = true
    }
    GlassDialog(onDismissRequest = dismiss) {
        DialogHeader(stringResource(R.string.announcement), text, Icons.Rounded.Campaign)
        DialogAction(stringResource(R.string.got_it), onClick = dismiss, style = DialogActionStyle.Primary)
    }
}
