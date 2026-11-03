package com.fazli.vispar.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fazli.vispar.BuildConfig
import com.fazli.vispar.R
import com.fazli.vispar.ui.glass.AmbientBackground
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.glass
import com.fazli.vispar.ui.glass.liveGlass
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.isRuntimeShaderSupported
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Geometry of the centred stack (logo, gap, wordmark), relative to the screen centre.
private val LogoSize = 92.dp
private val LogoCenterY = (-62).dp
private val WordCenterY = 44.dp

/**
 * Launch moment. The wordmark sits on a softly lit canvas; a drop of glass sweeps across it from
 * the reading-start side, magnifying each letter with a hint of chromatic aberration, then glides
 * up and settles as a lens around the logo, which surfaces underneath it.
 *
 * Without live glass (Android 12 and below, or "Opaque" glass) the sequence falls back to a calm
 * fade so nothing opaque ever covers the logo.
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val colors = Glass.colors
    val latestOnTimeout = rememberUpdatedState(onTimeout)
    val scene = rememberLayerBackdrop()
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val lensEnabled = colors.liveGlass && isRuntimeShaderSupported()

    // Lens: position (dp from centre) and size (dp).
    val lensX = remember { Animatable(0f) }
    val lensY = remember { Animatable(WordCenterY.value) }
    val lensW = remember { Animatable(210f) }
    val lensH = remember { Animatable(92f) }
    val lensAlpha = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val wordAlpha = remember { Animatable(0f) }
    val detailAlpha = remember { Animatable(0f) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val halfWidth = maxWidth.value / 2f
        val start = if (isLtr) -1f else 1f // the side reading begins from

        LaunchedEffect(Unit) {
            if (lensEnabled) {
                lensX.snapTo(start * (halfWidth + 140f))
                launch { wordAlpha.animateTo(1f, tween(500)) }
                launch { lensAlpha.animateTo(1f, tween(250)) }
                // 1. Sweep across the wordmark.
                lensX.animateTo(-start * 150f, tween(950, easing = FastOutSlowInEasing))
                // 2. Glide up and settle into a round lens over the logo.
                launch { lensX.animateTo(0f, spring(0.62f, 160f)) }
                launch { lensY.animateTo(LogoCenterY.value, spring(0.62f, 160f)) }
                launch { lensW.animateTo(140f, spring(0.55f, 180f)) }
                launch { lensH.animateTo(140f, spring(0.55f, 180f)) }
                delay(120)
                launch { logoAlpha.animateTo(1f, tween(450)) }
                delay(380)
                detailAlpha.animateTo(1f, tween(500))
                delay(650)
            } else {
                launch { wordAlpha.animateTo(1f, tween(600)) }
                launch { logoAlpha.animateTo(1f, tween(600)) }
                delay(300)
                detailAlpha.animateTo(1f, tween(500))
                delay(1100)
            }
            latestOnTimeout.value()
        }

        // The scene the lens refracts: lit canvas, logo and wordmark.
        Box(Modifier.fillMaxSize().layerBackdrop(scene)) {
            AmbientBackground(Modifier.fillMaxSize(), animated = true)
            Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.splash_logo),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(LogoSize)
                        .graphicsLayer { alpha = logoAlpha.value }
                )
                Spacer(Modifier.height(32.dp))
                Text(
                    stringResource(R.string.app_name),
                    style = GlassType.LargeTitle.copy(fontSize = 56.sp, lineHeight = 72.sp, fontWeight = FontWeight.Bold),
                    color = colors.content,
                    modifier = Modifier.graphicsLayer { alpha = wordAlpha.value }
                )
                Text(
                    stringResource(R.string.tagline),
                    style = GlassType.Callout,
                    color = colors.contentSecondary,
                    modifier = Modifier.graphicsLayer { alpha = detailAlpha.value }
                )
            }
        }

        if (lensEnabled) {
            val density = LocalDensity.current
            Box(
                Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        translationX = with(density) { lensX.value.dp.toPx() }
                        translationY = with(density) { lensY.value.dp.toPx() }
                        alpha = lensAlpha.value
                    }
                    .size(lensW.value.dp, lensH.value.dp)
                    .glass(
                        backdrop = scene,
                        shape = Capsule(),
                        colors = colors,
                        style = GlassStyle(
                            blur = 0.5.dp,
                            refractionHeight = 30.dp,
                            refractionAmount = 64.dp,
                            chromaticAberration = true,
                            depthEffect = true
                        ),
                        tint = Color.Transparent,
                        highlight = Highlight.Default,
                        shadow = Shadow(radius = 24.dp, color = Color.Black.copy(alpha = if (colors.isDark) 0.5f else 0.14f))
                    )
            )
        }

        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 36.dp)
                .graphicsLayer { alpha = detailAlpha.value },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(stringResource(R.string.developed_by), style = GlassType.Caption, color = colors.contentTertiary)
            Text(stringResource(R.string.version_x, BuildConfig.VERSION_NAME), style = GlassType.Caption, color = colors.contentTertiary)
        }
    }
}
