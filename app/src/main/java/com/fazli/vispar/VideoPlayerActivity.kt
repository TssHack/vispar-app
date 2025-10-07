package com.fazli.vispar

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.GestureDetector
import android.view.KeyEvent
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import com.fazli.vispar.data.model.SubtitleSettings
import com.fazli.vispar.data.model.VideoPlayerSettings
import com.fazli.vispar.ui.theme.VazirFontFamily
import com.fazli.vispar.utils.DeviceUtils
import com.fazli.vispar.utils.StorageUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

// Extension function to set subtitle text size on PlayerView
fun PlayerView.setSubtitleTextSize(spSize: Float) {
    val displayMetrics = context.resources.displayMetrics
    val pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spSize, displayMetrics)
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
            null
        )
    )
}

class VideoPlayerActivity : ComponentActivity() {
    companion object {
        const val EXTRA_VIDEO_URL = "video_url"
        const val EXTRA_AUTO_PLAY = "auto_play"
        
        fun start(context: Context, videoUrl: String, autoPlay: Boolean = true) {
            val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_URL, videoUrl)
                putExtra(EXTRA_AUTO_PLAY, autoPlay)
            }
            context.startActivity(intent)
        }
    }
    
    private var exoPlayer: ExoPlayer? = null
    private var videoUrl: String? = null
    private var autoPlay: Boolean = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableFullScreenMode()
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL)
        autoPlay = intent.getBooleanExtra(EXTRA_AUTO_PLAY, true)
        
        if (videoUrl != null) {
            setContent {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Rtl
                ) {
                    VideoPlayerScreen(
                        videoUrl = videoUrl!!,
                        autoPlay = autoPlay,
                        onBack = this::finish,
                        onPlayerReady = { player -> exoPlayer = player }
                    )
                }
            }
        } else {
            finish()
        }
    }
    
    // Handle TV remote control keys
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val isTv = DeviceUtils.isTv(this)
        
        if (isTv) {
            when (keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    exoPlayer?.let { player ->
                        if (player.isPlaying) {
                            player.pause()
                        } else {
                            player.play()
                        }
                    }
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                    exoPlayer?.play()
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                    exoPlayer?.pause()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER -> {
                    // Toggle play/pause on OK button
                    exoPlayer?.let { player ->
                        if (player.isPlaying) {
                            player.pause()
                        } else {
                            player.play()
                        }
                    }
                    return true
                }
            }
        }
        
        return super.onKeyDown(keyCode, event)
    }
    
    private fun enableFullScreenMode() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                window.insetsController?.let { controller ->
                    controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
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
        window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    
    override fun onResume() {
        super.onResume()
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
    autoPlay: Boolean,
    onBack: () -> Unit,
    onPlayerReady: (ExoPlayer) -> Unit
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(autoPlay) } // Start with autoPlay value
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isSeeking by remember { mutableStateOf(false) }
    var playerError by remember { mutableStateOf<String?>(null) }
    var showForwardIndicator by remember { mutableStateOf(false) }
    var showRewindIndicator by remember { mutableStateOf(false) }
    var wasPlayingBeforeSeek by remember { mutableStateOf(false) }
    var isScreenLocked by remember { mutableStateOf(false) }
    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val isTv = DeviceUtils.isTv(context)
    
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
                playWhenReady = autoPlay // Use the autoPlay parameter
                playbackSpeed = this@VideoPlayerScreen.playbackSpeed
            }
        } catch (e: Exception) {
            playerError = "خطا در راه‌اندازی پخش‌کننده: ${e.message}"
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
    
    // Update playback speed
    LaunchedEffect(playbackSpeed, exoPlayer) {
        try {
            exoPlayer?.setPlaybackSpeed(playbackSpeed)
        } catch (e: Exception) {
            // Ignore speed change errors
        }
    }
    
    // Auto-hide controls for TV
    LaunchedEffect(showControls, isPlaying, isScreenLocked, isTv) {
        try {
            if (showControls && isPlaying && !isScreenLocked && isTv) {
                delay(5000) // Hide controls after 5 seconds for TV
                showControls = false
            }
        } catch (e: Exception) {
            // Ignore delay errors
        }
    }
    
    // Listen to player events
    val playerListener = remember(exoPlayer) {
        object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                if (!isSeeking) {
                    isPlaying = playing
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                try {
                    if (playbackState == Player.STATE_READY) {
                        duration = exoPlayer?.duration ?: 0L
                        // Auto-play when ready if autoPlay is true
                        if (autoPlay && !isPlaying && !isSeeking) {
                            isPlaying = true
                            exoPlayer?.playWhenReady = true
                        }
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
    
    // Periodically update the current position
    LaunchedEffect(exoPlayer, isPlaying) {
        if (exoPlayer == null) return@LaunchedEffect
        
        try {
            while (true) {
                delay(100)
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
    LaunchedEffect(showControls, isPlaying, isScreenLocked) {
        try {
            if (showControls && isPlaying && !isScreenLocked) {
                delay(3000)
                showControls = false
            }
        } catch (e: Exception) {
            // Ignore delay errors
        }
    }
    
    // Hide forward/rewind indicators
    LaunchedEffect(showForwardIndicator) {
        if (showForwardIndicator) {
            delay(500)
            showForwardIndicator = false
        }
    }
    
    LaunchedEffect(showRewindIndicator) {
        if (showRewindIndicator) {
            delay(500)
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
                if (!isScreenLocked) {
                    detectTapGestures(
                        onDoubleTap = { offset ->
                            val screenWidth = size.width
                            val tapX = offset.x
                            
                            wasPlayingBeforeSeek = isPlaying
                            isSeeking = true
                            
                            if (tapX < screenWidth / 2) {
                                try {
                                    exoPlayer?.let { player ->
                                        val seekTimeMs = videoPlayerSettings.seekTimeSeconds * 1000L
                                        val newPosition = (player.currentPosition - seekTimeMs).coerceAtLeast(0L)
                                        player.seekTo(newPosition)
                                        currentPosition = newPosition
                                        showRewindIndicator = true
                                        if (wasPlayingBeforeSeek) {
                                            player.playWhenReady = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore seek errors
                                }
                            } else {
                                try {
                                    exoPlayer?.let { player ->
                                        val seekTimeMs = videoPlayerSettings.seekTimeSeconds * 1000L
                                        val newPosition = (player.currentPosition + seekTimeMs).coerceAtMost(player.duration)
                                        player.seekTo(newPosition)
                                        currentPosition = newPosition
                                        showForwardIndicator = true
                                        if (wasPlayingBeforeSeek) {
                                            player.playWhenReady = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore seek errors
                                }
                            }
                            
                            coroutineScope.launch {
                                delay(500)
                                isSeeking = false
                                try {
                                    exoPlayer?.playWhenReady = wasPlayingBeforeSeek
                                    isPlaying = wasPlayingBeforeSeek
                                } catch (e: Exception) {
                                    // Ignore errors
                                }
                            }
                        },
                        onTap = {
                            showControls = !showControls
                        }
                    )
                }
            }
    ) {
        // Error handling
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
                        text = playerError ?: "خطای ناشناخته رخ داده است",
                        color = Color.Red,
                        fontFamily = VazirFontFamily,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "بازگشت",
                            tint = Color.White
                        )
                    }
                }
            }
            return@Box
        }
        
        // Player initialization
        if (exoPlayer == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "در حال راه‌اندازی پخش‌کننده...",
                    color = Color.White,
                    fontFamily = VazirFontFamily,
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
        
        // Indicators
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
                        imageVector = Icons.Default.Replay10,
                        contentDescription = "عقب بردن ${videoPlayerSettings.seekTimeSeconds} ثانیه",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "${videoPlayerSettings.seekTimeSeconds} ثانیه",
                        color = Color.White,
                        fontFamily = VazirFontFamily,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
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
                        imageVector = Icons.Default.Forward10,
                        contentDescription = "جلو بردن ${videoPlayerSettings.seekTimeSeconds} ثانیه",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "${videoPlayerSettings.seekTimeSeconds} ثانیه",
                        color = Color.White,
                        fontFamily = VazirFontFamily,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
        
        // Screen lock indicator
        if (isScreenLocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "صفحه قفل شده",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "صفحه قفل شده",
                            color = Color.White,
                            fontFamily = VazirFontFamily,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
        
        // TV controls overlay (simplified for TV)
        if (isTv && showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large play/pause button for TV
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.7f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "توقف" else "پخش",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Time display
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontFamily = VazirFontFamily
                        )
                        
                        Text(
                            text = " / ",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontFamily = VazirFontFamily
                        )
                        
                        Text(
                            text = formatTime(duration),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontFamily = VazirFontFamily
                        )
                    }
                }
            }
        }
        
        // Mobile controls overlay
        if (!isTv && showControls && !isScreenLocked) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                // Top bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت",
                                tint = Color.White
                            )
                        }
                        
                        IconButton(
                            onClick = { isScreenLocked = !isScreenLocked },
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = if (isScreenLocked) 
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) 
                                    else 
                                        Color.Black.copy(alpha = 0.7f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isScreenLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = if (isScreenLocked) "باز کردن قفل" else "قفل کردن",
                                tint = Color.White
                            )
                        }
                    }
                }
                
                // Middle controls
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        IconButton(
                            onClick = {
                                wasPlayingBeforeSeek = isPlaying
                                isSeeking = true
                                try {
                                    exoPlayer?.let { player ->
                                        val seekTimeMs = 10000L
                                        val newPosition = (player.currentPosition - seekTimeMs).coerceAtLeast(0L)
                                        player.seekTo(newPosition)
                                        currentPosition = newPosition
                                        showRewindIndicator = true
                                        if (wasPlayingBeforeSeek) {
                                            player.playWhenReady = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore seek errors
                                }
                                
                                coroutineScope.launch {
                                    delay(500)
                                    isSeeking = false
                                    try {
                                        exoPlayer?.playWhenReady = wasPlayingBeforeSeek
                                        isPlaying = wasPlayingBeforeSeek
                                    } catch (e: Exception) {
                                        // Ignore errors
                                    }
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay10,
                                contentDescription = "عقب بردن 10 ثانیه",
                                tint = Color.White
                            )
                        }
                        
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.7f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "توقف" else "پخش",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = {
                                wasPlayingBeforeSeek = isPlaying
                                isSeeking = true
                                try {
                                    exoPlayer?.let { player ->
                                        val seekTimeMs = 10000L
                                        val newPosition = (player.currentPosition + seekTimeMs).coerceAtMost(player.duration)
                                        player.seekTo(newPosition)
                                        currentPosition = newPosition
                                        showForwardIndicator = true
                                        if (wasPlayingBeforeSeek) {
                                            player.playWhenReady = true
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Ignore seek errors
                                }
                                
                                coroutineScope.launch {
                                    delay(500)
                                    isSeeking = false
                                    try {
                                        exoPlayer?.playWhenReady = wasPlayingBeforeSeek
                                        isPlaying = wasPlayingBeforeSeek
                                    } catch (e: Exception) {
                                        // Ignore errors
                                    }
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward10,
                                contentDescription = "جلو بردن 10 ثانیه",
                                tint = Color.White
                            )
                        }
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
                            if (!isSeeking) {
                                wasPlayingBeforeSeek = isPlaying
                            }
                            isSeeking = true
                            val newPosition = (progress * duration).toLong()
                            try {
                                exoPlayer?.seekTo(newPosition)
                                currentPosition = newPosition
                                if (wasPlayingBeforeSeek) {
                                    exoPlayer?.playWhenReady = true
                                }
                            } catch (e: Exception) {
                                // Ignore seek errors
                            }
                        },
                        onValueChangeFinished = {
                            isSeeking = false
                            try {
                                exoPlayer?.playWhenReady = wasPlayingBeforeSeek
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
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = VazirFontFamily
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Row {
                            // Speed control button
                            Box {
                                IconButton(
                                    onClick = { showSpeedMenu = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "سرعت پخش",
                                        tint = Color.White
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = showSpeedMenu,
                                    onDismissRequest = { showSpeedMenu = false }
                                ) {
                                    listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "${speed}x",
                                                    fontFamily = VazirFontFamily
                                                )
                                            },
                                            onClick = {
                                                playbackSpeed = speed
                                                showSpeedMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "توقف" else "پخش",
                                    tint = Color.White
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        Text(
                            text = formatTime(duration),
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = VazirFontFamily
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
        String.format(Locale.US, "%02d:%02d:%02d", hours, remainingMinutes, remainingSeconds)
    } else {
        String.format(Locale.US, "%02d:%02d", remainingMinutes, remainingSeconds)
    }
}