package com.fazli.vispar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fazli.vispar.R
import com.fazli.vispar.data.model.Source

// تعریف فونت وزیری
private val VazirFontFamily = FontFamily(
    Font(R.font.vazir_regular, FontWeight.Normal),
    Font(R.font.vazir_bold, FontWeight.Bold)
)

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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "گزینه‌های دانلود",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = VazirFontFamily,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                text = "نحوه دانلود این ویدیو را انتخاب کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = VazirFontFamily,
                fontSize = 16.sp
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onCopyLink()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = "کپی لینک",
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp
                    )
                }
                
                Button(
                    onClick = {
                        onDownloadWithBrowser()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = "دانلود با مرورگر",
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp
                    )
                }
                
                Button(
                    onClick = {
                        onDownloadWithADM()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = "دانلود با ADM",
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp
                    )
                }
                
                Button(
                    onClick = {
                        onOpenInVLC()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = "باز کردن در VLC Player",
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp
                    )
                }
                
                Button(
                    onClick = {
                        onOpenInMXPlayer()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = "باز کردن در MX Player",
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp
                    )
                }
                
                Button(
                    onClick = {
                        onOpenInKMPlayer()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = "باز کردن در KM Player",
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp
                    )
                }
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "انصراف",
                        fontFamily = VazirFontFamily,
                        fontSize = 16.sp
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 6.dp
    )
}
