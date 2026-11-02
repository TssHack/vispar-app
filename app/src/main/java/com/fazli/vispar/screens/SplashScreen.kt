package com.fazli.vispar.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fazli.vispar.BuildConfig
import com.fazli.vispar.R
import com.fazli.vispar.ui.glass.AmbientBackground
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LiquidLoader
import com.fazli.vispar.ui.glass.LocalBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Launch moment: a drifting aurora with a lens of glass that drops in and wobbles into place over
 * the logo, then the whole scene zooms through to the catalogue.
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val colors = Glass.colors
    val latestOnTimeout = rememberUpdatedState(onTimeout)
    val auroraLayer = rememberLayerBackdrop()

    val orbScale = remember { Animatable(0.4f) }
    val orbAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(24f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { orbAlpha.animateTo(1f, tween(400)) }
        launch { orbScale.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = 180f)) }
        delay(350)
        launch { textAlpha.animateTo(1f, tween(600)) }
        launch { textOffset.animateTo(0f, spring(0.8f, 200f)) }
        delay(1700)
        latestOnTimeout.value()
    }

    Box(Modifier.fillMaxSize()) {
        AmbientBackground(Modifier.fillMaxSize().layerBackdrop(auroraLayer), animated = true)

        CompositionLocalProvider(LocalBackdrop provides auroraLayer) {
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                GlassSurface(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = orbScale.value
                            scaleY = orbScale.value
                            alpha = orbAlpha.value
                        }
                        .size(168.dp),
                    shape = Capsule(),
                    style = GlassStyle(blur = 4.dp, refractionHeight = 40.dp, refractionAmount = 80.dp, chromaticAberration = true, depthEffect = true),
                    tint = colors.glassTint.copy(alpha = colors.glassTint.alpha * 0.5f),
                    highlight = Highlight.Default,
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.splash_logo),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(96.dp)
                    )
                }

                Spacer(Modifier.height(32.dp))

                Column(
                    Modifier.graphicsLayer {
                        alpha = textAlpha.value
                        translationY = textOffset.value.dp.toPx()
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("ویسپار", style = GlassType.LargeTitle.copy(fontSize = GlassType.LargeTitle.fontSize * 1.25f), color = colors.content)
                    Text("نسخه ${BuildConfig.VERSION_NAME}", style = GlassType.Callout, color = colors.contentSecondary)
                }
            }

            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 40.dp)
                    .graphicsLayer { alpha = textAlpha.value },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LiquidLoader(size = 52.dp)
                Text("توسعه یافته توسط احسان فضلی", style = GlassType.Caption, color = colors.contentTertiary)
            }
        }
    }
}
