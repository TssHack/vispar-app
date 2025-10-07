package com.fazli.vispar

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
            setContent {
                VideoPlayerScreen(videoUrl!!, this::finish) { player ->
                    exoPlayer = player
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        // Calculate if the tap is on the left or right side
                        val screenWidth = size.width
                        val tapX = offset.x
                        
                        // Store the playing state before seeking
                        wasPlayingBeforeSeek = isPlaying
                        isSeeking = true
                        
                        if (tapX < screenWidth / 2) {
                            // Left side - rewind specified seconds
                            try {
                                exoPlayer?.let { player ->
                                    val seekTimeMs = videoPlayerSettings.seekTimeSeconds * 1000L
                                    val newPosition = (player.currentPosition - seekTimeMs).coerceAtLeast(0L)
                                    player.seekTo(newPosition)
                                    currentPosition = newPosition
                                    showRewindIndicator = true
                                    // Keep the player playing during seeking if it was playing before
                                    if (wasPlayingBeforeSeek) {
                                        player.playWhenReady = true
                                    }
                                }
                            } catch (e: Exception) {
                                // Ignore seek errors
                            }
                        } else {
                            // Right side - forward specified seconds
                            try {
                                exoPlayer?.let { player ->
                                    val seekTimeMs = videoPlayerSettings.seekTimeSeconds * 1000L
                                    val newPosition = (player.currentPosition + seekTimeMs).coerceAtMost(player.duration)
                                    player.seekTo(newPosition)
                                    currentPosition = newPosition
                                    showForwardIndicator = true
                                    // Keep the player playing during seeking if it was playing before
                                    if (wasPlayingBeforeSeek) {
                                        player.playWhenReady = true
                                    }
                                }
                            } catch (e: Exception) {
                                // Ignore seek errors
                            }
                        }
                        
                        // Reset seeking state after a short delay using a coroutine scope
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(500) // Reset after 500ms
                            isSeeking = false
                            // Restore the playing state after seeking is finished
                            try {
                                exoPlayer?.playWhenReady = wasPlayingBeforeSeek
                                // Update isPlaying state to match the player's actual state
                                isPlaying = wasPlayingBeforeSeek
                            } catch (e: Exception) {
                                // Ignore errors
                            }
                        }
                    },
                    onTap = {
                        showControls = !showControls
                        // Reset the auto-hide timer when controls are shown
                        if (showControls && isPlaying) {
                            // The LaunchedEffect above will handle the auto-hide
                        }
                    }
                )
            }
    ) {
        // Show error message if player failed to initialize
        if (playerError != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = playerError ?: "Unknown error occurred",
                        color = Color.Red,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            }
            return@Box
        }
        
        // Check if player is initialized
        if (exoPlayer == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Initializing player...",
                    color = Color.White,
                    modifier = Modifier.padding(16.dp)
                )
            }
            return@Box
        }
        
        // Video player
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // We're using our own controls
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // Make the player view fill the entire screen
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    
                    // Apply subtitle settings to the player view
                    // Set subtitle styling
                    setSubtitleTextSize(subtitleSettings.textSize)
                    setSubtitleColors(subtitleSettings)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { playerView ->
                // Update the player view when subtitle settings change
                // Update subtitle styling when settings change
                playerView.setSubtitleTextSize(subtitleSettings.textSize)
                playerView.setSubtitleColors(subtitleSettings)
            }
        )
        
        // Rewind indicator
        if (showRewindIndicator) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Rewind ${videoPlayerSettings.seekTimeSeconds} seconds",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "${videoPlayerSettings.seekTimeSeconds}s",
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
        // Forward indicator
        if (showForwardIndicator) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward,
                        contentDescription = "Forward ${videoPlayerSettings.seekTimeSeconds} seconds",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "${videoPlayerSettings.seekTimeSeconds}s",
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
        // Custom controls overlay
        if (showControls) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Top bar with back button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
                
                // Middle play/pause button
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                // Bottom controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(16.dp)
                ) {
                    // Progress slider
                    Slider(
                        value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                        onValueChange = { progress ->
                            // Store the playing state before seeking
                            if (!isSeeking) {
                                wasPlayingBeforeSeek = isPlaying
                            }
                            isSeeking = true
                            val newPosition = (progress * duration).toLong()
                            try {
                                exoPlayer?.seekTo(newPosition)
                                currentPosition = newPosition
                                // Keep the player playing during seeking if it was playing before
                                if (wasPlayingBeforeSeek) {
                                    exoPlayer?.playWhenReady = true
                                }
                            } catch (e: Exception) {
                                // Ignore seek errors
                            }
                        },
                        onValueChangeFinished = {
                            isSeeking = false
                            // Restore the playing state after seeking is finished
                            try {
                                exoPlayer?.playWhenReady = wasPlayingBeforeSeek
                                // Update isPlaying state to match the player's actual state
                                isPlaying = wasPlayingBeforeSeek
                            } catch (e: Exception) {
                                // Ignore errors
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Time and controls row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Text(
                            text = formatTime(duration),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
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