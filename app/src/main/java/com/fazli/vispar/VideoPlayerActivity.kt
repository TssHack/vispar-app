package com.fazli.vispar

import android.view.LayoutInflater
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LiquidIconButton
import com.fazli.vispar.ui.glass.LiquidLoader
import com.fazli.vispar.ui.glass.LiquidSlider
import com.fazli.vispar.ui.glass.LocalBackdrop
import com.fazli.vispar.ui.glass.LocalGlassColors
import com.fazli.vispar.ui.glass.StatusView
import com.fazli.vispar.ui.glass.glass
import com.fazli.vispar.ui.glass.glassColors
import com.fazli.vispar.ui.theme.ThemeManager
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import com.fazli.vispar.data.model.SubtitleSettings
import com.fazli.vispar.data.model.VideoPlayerSettings
import com.fazli.vispar.utils.StorageUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Extension function to set subtitle text size on PlayerView
fun PlayerView.setSubtitleTextSize(spSize: Float) {
    // Convert sp to pixels
    val displayMetrics = context.resources.displayMetrics
    val pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spSize, displayMetrics)
    
    // Set the subtitle text size
    subtitleView?.setFixedTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, pixels)
}

// Extension function to set subtitle colors
fun PlayerView.setSubtitleColors(settings: SubtitleSettings) {
    subtitleView?.setStyle(
        CaptionStyleCompat(
            settings.textColor,
            settings.backgroundColor,
            settings.borderColor,
            CaptionStyleCompat.EDGE_TYPE_OUTLINE,
            settings.borderColor,
            null // typeface
        )
    )
}

class VideoPlayerActivity : ComponentActivity() {
    companion object {
        const val EXTRA_VIDEO_URL = "video_url"
        
        fun start(context: Context, videoUrl: String) {
            val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_URL, videoUrl)
            }
            context.startActivity(intent)
        }
    }
    
    private var exoPlayer: ExoPlayer? = null
    private var videoUrl: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set fullscreen landscape mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        // Enable immersive full-screen mode
        enableFullScreenMode()
        
        // Keep screen on while in video player
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL)
        
        if (videoUrl != null) {
            val accent = ThemeManager(this).loadThemeSettings().primaryColor
            setContent {
                // The player is always dark: glass is tuned for video content.
                CompositionLocalProvider(
                    LocalGlassColors provides glassColors(isDark = true, accent = accent),
                    LocalLayoutDirection provides LayoutDirection.Rtl
                ) {
                    VideoPlayerScreen(videoUrl!!, this::finish) { player ->
                        exoPlayer = player
                    }
                }
            }
        } else {
            finish()
        }
    }
    
    private fun enableFullScreenMode() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                // For Android 11 and above
                window.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                // For older Android versions
                @Suppress("DEPRECATION")
                window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                )
            }
        } catch (e: Exception) {
            // Fallback to basic fullscreen if there's an issue
            @Suppress("DEPRECATION")
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            exoPlayer?.release()
        } catch (e: Exception) {
            // Ignore any exceptions during release
        }
        exoPlayer = null
        
        // Remove keep screen on flag to conserve battery
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    
    override fun onResume() {
        super.onResume()
        // Re-enable full-screen mode when resuming
        try {
            enableFullScreenMode()
        } catch (e: Exception) {
            // Ignore fullscreen errors
        }
    }
}

@Composable
fun VideoPlayerScreen(
    videoUrl: String,
    onBack: () -> Unit,
    onPlayerReady: (ExoPlayer) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isSeeking by remember { mutableStateOf(false) }
    var playerError by remember { mutableStateOf<String?>(null) }
    var showForwardIndicator by remember { mutableStateOf(false) }
    var showRewindIndicator by remember { mutableStateOf(false) }
    var wasPlayingBeforeSeek by remember { mutableStateOf(false) } // Track if player was playing before seeking
    
    // Load video player settings
    val videoPlayerSettings = remember(context) {
        StorageUtils.loadVideoPlayerSettings(context)
    }
    
    // Load subtitle settings
    val subtitleSettings = remember(context) {
        StorageUtils.loadSubtitleSettings(context)
    }
    
    val exoPlayer = remember(context) {
        try {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
                prepare()
                playWhenReady = isPlaying
            }
        } catch (e: Exception) {
            playerError = "Failed to initialize player: ${e.message}"
            null
        }
    }
    
    // Notify activity of player reference
    LaunchedEffect(Unit) {
        try {
            exoPlayer?.let { onPlayerReady(it) }
        } catch (e: Exception) {
            // Ignore callback errors
        }
    }
    
    // Update player state
    LaunchedEffect(isPlaying, exoPlayer) {
        try {
            exoPlayer?.playWhenReady = isPlaying
        } catch (e: Exception) {
            // Ignore player state errors
        }
    }
    
    // Listen to player events and handle cleanup
    val playerListener = remember(exoPlayer) {
        object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                // Only update isPlaying if we're not currently seeking
                if (!isSeeking) {
                    isPlaying = playing
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                try {
                    if (playbackState == Player.STATE_READY) {
                        duration = exoPlayer?.duration ?: 0L
                    }
                } catch (e: Exception) {
                    // Ignore duration errors
                }
            }
            
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                try {
                    if (!isSeeking) {
                        currentPosition = exoPlayer?.currentPosition ?: 0L
                    }
                } catch (e: Exception) {
                    // Ignore position errors
                }
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                playerError = error.message
            }
        }
    }
    
    LaunchedEffect(exoPlayer) {
        if (exoPlayer == null) return@LaunchedEffect
        
        exoPlayer.addListener(playerListener)
    }
    
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.removeListener(playerListener)
        }
    }
    
    // Periodically update the current position for real-time progress tracking
    LaunchedEffect(exoPlayer, isPlaying) {
        if (exoPlayer == null) return@LaunchedEffect
        
        try {
            while (true) {
                delay(100) // Update every 100ms for smooth progress tracking
                if (isPlaying && !isSeeking) {
                    try {
                        exoPlayer?.let { player ->
                            if (player.isPlaying) {
                                currentPosition = player.currentPosition
                                duration = player.duration
                            }
                        }
                    } catch (e: Exception) {
                        // Ignore position/duration errors
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore coroutine errors
        }
    }
    
    // Hide controls after a delay
    LaunchedEffect(showControls, isPlaying) {
        try {
            if (showControls && isPlaying) {
                delay(3000) // Hide controls after 3 seconds
                showControls = false
            }
        } catch (e: Exception) {
            // Ignore delay errors
        }
    }
    
    // Hide forward/rewind indicators after a delay
    LaunchedEffect(showForwardIndicator) {
        if (showForwardIndicator) {
            delay(500) // Hide after 500ms
            showForwardIndicator = false
        }
    }
    
    LaunchedEffect(showRewindIndicator) {
        if (showRewindIndicator) {
            delay(500) // Hide after 500ms
            showRewindIndicator = false
        }
    }
    
    // Clean up player
    DisposableEffect(exoPlayer) {
        onDispose {
            try {
                exoPlayer?.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
    }
    
    fun seekBy(deltaMs: Long) {
        val player = exoPlayer ?: return
        wasPlayingBeforeSeek = isPlaying
        isSeeking = true
        try {
            val target = (player.currentPosition + deltaMs)
                .coerceAtLeast(0L)
                .let { if (player.duration > 0) it.coerceAtMost(player.duration) else it }
            player.seekTo(target)
            currentPosition = target
            if (deltaMs < 0) showRewindIndicator = true else showForwardIndicator = true
            if (wasPlayingBeforeSeek) player.playWhenReady = true
        } catch (e: Exception) {
            // Ignore seek errors
        }
        scope.launch {
            delay(500)
            isSeeking = false
            try {
                player.playWhenReady = wasPlayingBeforeSeek
                isPlaying = wasPlayingBeforeSeek
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }

    val seekMs = videoPlayerSettings.seekTimeSeconds * 1000L
    val videoLayer = rememberLayerBackdrop()
    val glassColors = Glass.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        // Left half rewinds, right half skips forward.
                        if (offset.x < size.width / 2) seekBy(-seekMs) else seekBy(seekMs)
                    },
                    onTap = { showControls = !showControls }
                )
            }
    ) {
        if (playerError != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                StatusView(
                    icon = Icons.Rounded.ErrorOutline,
                    title = "پخش ویدیو ممکن نیست",
                    message = playerError ?: "خطای نامشخص",
                    iconTint = glassColors.danger,
                    actionText = "بازگشت",
                    onAction = onBack
                )
            }
            return@Box
        }

        if (exoPlayer == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LiquidLoader() }
            return@Box
        }

        // The video, recorded so the controls can refract it.
        Box(Modifier.fillMaxSize().layerBackdrop(videoLayer)) {
            AndroidView(
                factory = { ctx ->
                    (LayoutInflater.from(ctx).inflate(R.layout.vispar_player_view, null, false) as PlayerView).apply {
                        player = exoPlayer
                        useController = false
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        setSubtitleTextSize(subtitleSettings.textSize)
                        setSubtitleColors(subtitleSettings)
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { playerView ->
                    playerView.setSubtitleTextSize(subtitleSettings.textSize)
                    playerView.setSubtitleColors(subtitleSettings)
                }
            )
        }

        CompositionLocalProvider(LocalBackdrop provides videoLayer) {
            // Double-tap feedback: a glass bubble on the side that was tapped.
            SeekIndicator(
                visible = showRewindIndicator,
                icon = Icons.Rounded.FastRewind,
                label = "${videoPlayerSettings.seekTimeSeconds} ثانیه",
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 64.dp)
            )
            SeekIndicator(
                visible = showForwardIndicator,
                icon = Icons.Rounded.FastForward,
                label = "${videoPlayerSettings.seekTimeSeconds} ثانیه",
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 64.dp)
            )

            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(tween(220)),
                exit = fadeOut(tween(300)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(Modifier.fillMaxSize()) {
                    // Soft scrims keep the chrome legible without dimming the whole picture.
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Black.copy(alpha = 0.45f),
                                    0.25f to Color.Transparent,
                                    0.7f to Color.Transparent,
                                    1f to Color.Black.copy(alpha = 0.55f)
                                )
                            )
                    )

                    LiquidIconButton(
                        onClick = onBack,
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "بازگشت",
                        iconTint = Color.White,
                        modifier = Modifier.align(Alignment.TopStart).padding(24.dp)
                    )

                    // Transport cluster
                    Row(
                        Modifier.align(Alignment.Center),
                        horizontalArrangement = Arrangement.spacedBy(28.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LiquidIconButton(
                            onClick = { seekBy(-seekMs) },
                            icon = Icons.Rounded.Replay10,
                            contentDescription = "عقب",
                            iconTint = Color.White,
                            size = 56.dp,
                            iconSize = 28.dp
                        )
                        LiquidIconButton(
                            onClick = { isPlaying = !isPlaying },
                            icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "توقف" else "پخش",
                            iconTint = Color.White,
                            size = 80.dp,
                            iconSize = 42.dp
                        )
                        LiquidIconButton(
                            onClick = { seekBy(seekMs) },
                            icon = Icons.Rounded.Forward10,
                            contentDescription = "جلو",
                            iconTint = Color.White,
                            size = 56.dp,
                            iconSize = 28.dp
                        )
                    }

                    // Timeline on a pane of glass
                    Row(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                            .fillMaxWidth()
                            .glass(
                                backdrop = videoLayer,
                                shape = RoundedRectangle(28.dp),
                                colors = glassColors,
                                style = GlassStyle(blur = 16.dp, refractionHeight = 16.dp, refractionAmount = 32.dp),
                                tint = Color.Black.copy(alpha = 0.25f)
                            )
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Media time reads left-to-right regardless of the app's language.
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(formatTime(currentPosition), style = GlassType.CaptionBold, color = Color.White)
                            LiquidSlider(
                                value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                                onValueChange = { progress ->
                                    if (!isSeeking) wasPlayingBeforeSeek = isPlaying
                                    isSeeking = true
                                    val newPosition = (progress * duration).toLong()
                                    try {
                                        exoPlayer.seekTo(newPosition)
                                        currentPosition = newPosition
                                        if (wasPlayingBeforeSeek) exoPlayer.playWhenReady = true
                                    } catch (e: Exception) {
                                        // Ignore seek errors
                                    }
                                },
                                onValueChangeFinished = {
                                    isSeeking = false
                                    try {
                                        exoPlayer.playWhenReady = wasPlayingBeforeSeek
                                        isPlaying = wasPlayingBeforeSeek
                                    } catch (e: Exception) {
                                        // Ignore errors
                                    }
                                },
                                valueRange = 0f..1f,
                                accentColor = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            Text(formatTime(duration), style = GlassType.CaptionBold, color = Color.White.copy(alpha = 0.75f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeekIndicator(
    visible: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(120)) + scaleIn(initialScale = 0.7f),
        exit = fadeOut(tween(300)) + scaleOut(targetScale = 1.1f),
        modifier = modifier
    ) {
        GlassSurface(
            modifier = Modifier.size(96.dp),
            shape = Capsule(),
            style = GlassStyle(blur = 8.dp, refractionHeight = 24.dp, refractionAmount = 48.dp, chromaticAberration = true),
            tint = Color.Black.copy(alpha = 0.15f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(36.dp))
                Text(label, style = GlassType.Caption, color = Color.White)
            }
        }
    }
}

fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1000).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    val hours = minutes / 60
    val remainingMinutes = minutes % 60
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, remainingMinutes, remainingSeconds)
    } else {
        String.format("%02d:%02d", remainingMinutes, remainingSeconds)
    }
}
