package com.fazli.vispar.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fazli.vispar.BuildConfig
import com.fazli.vispar.R
import kotlinx.coroutines.delay

// تعریف فونت وزیری
private val VazirFontFamily = FontFamily(
    Font(R.font.vazir_regular, FontWeight.Normal),
    Font(R.font.vazir_bold, FontWeight.Bold)
)

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    backgroundColor: Color
) {
    // تنظیم جهت‌گیری راست‌چین برای کل صفحه
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor
        ) {
            val scale = remember { Animatable(0f) }
            val alpha = remember { Animatable(0f) }
            val contentAlpha = remember { Animatable(0f) }
            
            LaunchedEffect(Unit) {
                // Animate the logo scale
                scale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 1000,
                        easing = {
                            OvershootInterpolator(2f).getInterpolation(it)
                        }
                    )
                )
                
                // Animate the alpha for logo
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 800
                    )
                )
                
                // Animate the content alpha
                contentAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 1000,
                        delayMillis = 300
                    )
                )
                
                // Wait for some time before navigating
                delay(2000)
                onTimeout()
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                backgroundColor,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(contentAlpha.value),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logo container with animation
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .scale(scale.value)
                            .alpha(alpha.value),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.splash_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(100.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // App name with animation
                    Text(
                        text = "ویسپار",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        fontFamily = VazirFontFamily
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Version with animation
                    Text(
                        text = "نسخه ${BuildConfig.VERSION_NAME ?: "1.0"}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        fontFamily = VazirFontFamily
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Developer with animation
                    Text(
                        text = "توسعه یافته توسط احسان فضلی",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        fontFamily = VazirFontFamily
                    )
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    // Loading indicator with animation
                    androidx.compose.animation.core.Animatable(0f).apply {
                        LaunchedEffect(Unit) {
                            animateTo(
                                targetValue = 360f,
                                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                    animation = tween(
                                        durationMillis = 1500,
                                        easing = androidx.compose.animation.core.LinearEasing
                                    )
                                )
                            )
                        }
                    }
                    
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            trackColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}