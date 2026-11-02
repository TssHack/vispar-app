package com.fazli.vispar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.OndemandVideo
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.SmartDisplay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.fazli.vispar.data.model.Source
import com.fazli.vispar.ui.glass.DialogAction
import com.fazli.vispar.ui.glass.DialogActionStyle
import com.fazli.vispar.ui.glass.DialogHeader
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassDivider
import com.fazli.vispar.ui.glass.GlassDialog
import com.fazli.vispar.ui.glass.GlassRow
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.shiftHue
import com.kyant.shapes.RoundedRectangle

/** Every way to take a video out of the app, as a grouped glass list. */
@Composable
fun DownloadOptionsDialog(
    source: Source,
    onDismiss: () -> Unit,
    onCopyLink: () -> Unit,
    onDownloadWithBrowser: () -> Unit,
    onDownloadWithADM: () -> Unit,
    onOpenInVLC: () -> Unit,
    onOpenInMXPlayer: () -> Unit,
    onOpenInKMPlayer: () -> Unit
) {
    val accent = Glass.colors.accent
    val options: List<Triple<ImageVector, String, () -> Unit>> = listOf(
        Triple(Icons.Rounded.ContentCopy, "کپی لینک", onCopyLink),
        Triple(Icons.Rounded.Language, "دانلود با مرورگر", onDownloadWithBrowser),
        Triple(Icons.Rounded.Download, "دانلود با ADM", onDownloadWithADM),
        Triple(Icons.Rounded.PlayCircle, "باز کردن در VLC Player", onOpenInVLC),
        Triple(Icons.Rounded.SmartDisplay, "باز کردن در MX Player", onOpenInMXPlayer),
        Triple(Icons.Rounded.OndemandVideo, "باز کردن در KM Player", onOpenInKMPlayer),
    )

    GlassDialog(onDismissRequest = onDismiss) {
        DialogHeader(
            title = "گزینه‌های دانلود",
            message = "نحوه دانلود ویدیوی ${source.quality} را انتخاب کنید",
            icon = Icons.Rounded.Download
        )
        GlassSurface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedRectangle(24.dp),
            style = GlassStyle.Clear,
            shadow = null
        ) {
            Column(Modifier.padding(vertical = 4.dp)) {
                options.forEachIndexed { index, (icon, label, action) ->
                    GlassRow(
                        icon = icon,
                        title = label,
                        iconTint = accent.shiftHue(index * 22f),
                        onClick = {
                            action()
                            onDismiss()
                        }
                    )
                    if (index != options.lastIndex) GlassDivider()
                }
            }
        }
        DialogAction(text = "انصراف", onClick = onDismiss, style = DialogActionStyle.Plain)
    }
}

/** Lets the user pick a quality, then hands it to [onPick]. */
@Composable
fun QualityPickerDialog(
    title: String,
    message: String,
    sources: List<Source>,
    icon: ImageVector,
    onPick: (Source) -> Unit,
    onDismiss: () -> Unit,
    primary: Boolean = true,
) {
    GlassDialog(onDismissRequest = onDismiss) {
        DialogHeader(title = title, message = message, icon = icon)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            sources.forEachIndexed { index, source ->
                DialogAction(
                    text = source.quality,
                    icon = icon,
                    onClick = { onPick(source) },
                    style = if (primary && index == 0) DialogActionStyle.Primary else DialogActionStyle.Secondary
                )
            }
        }
        DialogAction(text = "انصراف", onClick = onDismiss, style = DialogActionStyle.Plain)
    }
}
