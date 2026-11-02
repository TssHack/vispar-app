package com.fazli.vispar.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.fazli.vispar.BuildConfig
import com.fazli.vispar.R
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassDivider
import com.fazli.vispar.ui.glass.GlassRow
import com.fazli.vispar.ui.glass.GlassScaffold
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.SectionHeader
import com.fazli.vispar.ui.glass.shiftHue
import com.kyant.shapes.RoundedRectangle
import java.util.Calendar

@Composable
fun AboutScreen(navController: NavController?) {
    val context = LocalContext.current
    val colors = Glass.colors

    fun open(url: String, isEmail: Boolean = false) {
        runCatching {
            val intent = if (isEmail) Intent(Intent.ACTION_SENDTO, "mailto:$url".toUri())
            else Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        }
    }

    GlassScaffold(
        title = "درباره",
        onBack = { navController?.popBackStack() }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding())
                .padding(horizontal = Glass.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            GlassSurface(
                modifier = Modifier.size(128.dp),
                shape = RoundedRectangle(40.dp),
                style = GlassStyle(blur = 6.dp, refractionHeight = 28.dp, refractionAmount = 56.dp, chromaticAberration = true),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.splash_logo),
                    contentDescription = "لوگوی ویسپار",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(80.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ویسپار", style = GlassType.LargeTitle, color = colors.content)
                Text(
                    "دنیای فیلم و سریال، پشت یک لایه شیشه",
                    style = GlassType.Callout,
                    color = colors.contentSecondary,
                    textAlign = TextAlign.Center
                )
            }

            GlassSurface(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoStat(Icons.Rounded.NewReleases, BuildConfig.VERSION_NAME, "نسخه")
                    InfoStat(Icons.Rounded.Code, "${BuildConfig.VERSION_CODE}", "بیلد")
                    InfoStat(Icons.Rounded.Memory, getArchitecture(), "معماری")
                }
            }

            GlassSurface(Modifier.fillMaxWidth(), style = GlassStyle.Frosted) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("توسعه یافته توسط احسان فضلی", style = GlassType.Headline, color = colors.content)
                    Text("برنامه نویس فول استک", style = GlassType.Caption, color = colors.contentSecondary)
                }
            }

            SectionHeader("ارتباط با ما", Modifier.padding(horizontal = 0.dp))
            GlassSurface(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(vertical = 6.dp)) {
                    GlassRow(
                        icon = Icons.AutoMirrored.Rounded.Send,
                        title = "کانال تلگرام",
                        subtitle = "t.me/nexzoteam",
                        iconTint = colors.accent.shiftHue(-60f),
                        onClick = { open("https://t.me/nexzoteam") }
                    )
                    GlassDivider()
                    GlassRow(
                        icon = Icons.AutoMirrored.Rounded.Send,
                        title = "تلگرام توسعه‌دهنده",
                        subtitle = "t.me/Devehsan",
                        iconTint = colors.accent.shiftHue(-30f),
                        onClick = { open("https://t.me/Devehsan") }
                    )
                    GlassDivider()
                    GlassRow(
                        icon = Icons.Rounded.Email,
                        title = "ایمیل توسعه‌دهنده",
                        subtitle = "ehsanfazlinejad@gmail.com",
                        iconTint = colors.accent.shiftHue(30f),
                        onClick = { open("ehsanfazlinejad@gmail.com", isEmail = true) }
                    )
                }
            }

            Text(
                "© ${Calendar.getInstance().get(Calendar.YEAR)} ویسپار. تمامی حقوق محفوظ است.",
                style = GlassType.Caption,
                color = colors.contentTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun InfoStat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    val colors = Glass.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        androidx.compose.material3.Icon(icon, null, tint = colors.accent, modifier = Modifier.size(22.dp))
        Text(value, style = GlassType.Headline, color = colors.content)
        Text(label, style = GlassType.Caption, color = colors.contentTertiary)
    }
}

fun getArchitecture(): String = try {
    when (val abi = android.os.Build.SUPPORTED_ABIS.firstOrNull()) {
        "arm64-v8a" -> "ARM64"
        "armeabi-v7a" -> "ARM32"
        "x86_64" -> "x86_64"
        "x86" -> "x86"
        null -> "ناشناخته"
        else -> abi
    }
} catch (e: Exception) {
    "ناشناخته"
}
