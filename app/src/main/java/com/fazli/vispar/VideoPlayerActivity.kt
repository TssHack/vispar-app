package com.fazli.vispar

import androidx.compose.ui.res.stringResource

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import com.fazli.vispar.data.model.SubtitleSettings
import com.fazli.vispar.data.model.VideoPlayerSettings
import com.fazli.vispar.ui.glass.DialogAction
import com.fazli.vispar.ui.glass.DialogActionStyle
import com.fazli.vispar.ui.glass.DialogHeader
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassDialog
import com.fazli.vispar.ui.glass.GlassOverlayHost
import com.fazli.vispar.ui.glass.GlassOverlayLayer
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LiquidButton
import com.fazli.vispar.ui.glass.LiquidIconButton
import com.fazli.vispar.ui.glass.LiquidLoader
import com.fazli.vispar.ui.glass.LiquidSlider
import com.fazli.vispar.ui.glass.LocalBackdrop
import com.fazli.vispar.ui.glass.LocalGlassColors
import com.fazli.vispar.ui.glass.LocalGlassOverlayHost
import com.fazli.vispar.ui.glass.LocalIsTv
import com.fazli.vispar.ui.glass.StatusView
import com.fazli.vispar.ui.glass.glass
import com.fazli.vispar.ui.glass.glassColors
import com.fazli.vispar.ui.theme.ThemeManager
import com.fazli.vispar.utils.DeviceUtils
import com.fazli.vispar.utils.StorageUtils
import com.fazli.vispar.utils.WatchEntry
import com.fazli.vispar.utils.WatchHistory
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.delay
import java.util.Locale

fun PlayerView.setSubtitleTextSize(spSize: Float) {
    val pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spSize, context.resources.displayMetrics)
    subtitleView?.setFixedTextSize(TypedValue.COMPLEX_UNIT_PX, pixels)
}

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

/** What is being played; used for the title bar and the "continue watching" history. */
data class PlayerMeta(
    val url: String,
    val title: String = "",
    val subtitle: String? = null,
    val image: String = "",
    val contentId: Int = 0,
    val type: String = "movie",
)

class VideoPlayerActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_VIDEO_URL = "video_url"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_SUBTITLE = "subtitle"
        private const val EXTRA_IMAGE = "image"
        private const val EXTRA_ID = "content_id"
        private const val EXTRA_TYPE = "type"

        fun start(context: Context, videoUrl: String) = start(context, PlayerMeta(url = videoUrl))

        fun start(context: Context, meta: PlayerMeta) {
            val intent = Intent(context, VideoPlayerActivity::class.java).apply {
                putExtra(EXTRA_VIDEO_URL, meta.url)
                putExtra(EXTRA_TITLE, meta.title)
                putExtra(EXTRA_SUBTITLE, meta.subtitle)
                putExtra(EXTRA_IMAGE, meta.image)
                putExtra(EXTRA_ID, meta.contentId)
                putExtra(EXTRA_TYPE, meta.type)
            }
            context.startActivity(intent)
        }
    }

    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.fazli.vispar.data.api.Api.init(this)
        com.fazli.vispar.account.AccountManager.init(this)
        if (!DeviceUtils.isTv(this)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        enableFullScreenMode()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val url = intent.getStringExtra(EXTRA_VIDEO_URL)
        if (url == null) {
            finish()
            return
        }
        val meta = PlayerMeta(
            url = url,
            title = intent.getStringExtra(EXTRA_TITLE).orEmpty(),
            subtitle = intent.getStringExtra(EXTRA_SUBTITLE),
            image = intent.getStringExtra(EXTRA_IMAGE).orEmpty(),
            contentId = intent.getIntExtra(EXTRA_ID, 0),
            type = intent.getStringExtra(EXTRA_TYPE) ?: "movie",
        )
        val theme = ThemeManager(this).loadThemeSettings()
        val isTv = DeviceUtils.isTv(this)

        setContent {
            // The player is always dark: glass is tuned for video content.
            CompositionLocalProvider(
                LocalGlassColors provides glassColors(isDark = true, accent = theme.primaryColor, material = theme.glassMaterial),
                LocalIsTv provides isTv,
            ) {
                VideoPlayerScreen(meta, onBack = ::finish) { exoPlayer = it }
            }
        }
    }

    private fun enableFullScreenMode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

    override fun onPause() {
        super.onPause()
        // Don't keep playing (and burning data) in the background.
        exoPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        runCatching { enableFullScreenMode() }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { exoPlayer?.release() }
        exoPlayer = null
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
fun VideoPlayerScreen(
    meta: PlayerMeta,
    onBack: () -> Unit,
    onPlayerReady: (ExoPlayer) -> Unit
) {
    val context = LocalContext.current
    val colors = Glass.colors
    val isTv = LocalIsTv.current
    val settings = remember { StorageUtils.loadVideoPlayerSettings(context) }
    val subtitleSettings = remember { StorageUtils.loadSubtitleSettings(context) }
    val seekMs = settings.seekTimeSeconds * 1000L

    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var position by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var scrubFraction by remember { mutableStateOf<Float?>(null) }
    var speed by remember { mutableFloatStateOf(settings.playbackSpeed) }
    var showControls by remember { mutableStateOf(true) }
    var controlsTick by remember { mutableIntStateOf(0) }
    var showSpeedPicker by remember { mutableStateOf(false) }
    var playerError by remember { mutableStateOf<String?>(null) }
    var seekFeedback by remember { mutableIntStateOf(0) } // -1 rewind, 1 forward
    var seekFeedbackTick by remember { mutableIntStateOf(0) }
    var rootFocused by remember { mutableStateOf(false) }
    val rootFocus = remember { FocusRequester() }
    val playFocus = remember { FocusRequester() }

    val resumeFrom = remember {
        if (settings.resumePlayback) WatchHistory.get(context, meta.url)?.takeIf { it.isResumable }?.positionMs else null
    }

    val exoPlayer = remember {
        try {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(meta.url.toUri()))
                setPlaybackSpeed(settings.playbackSpeed)
                if (resumeFrom != null) seekTo(resumeFrom)
                prepare()
                playWhenReady = settings.autoplay
            }
        } catch (e: Exception) {
            playerError = e.message ?: context.getString(R.string.player_init_error)
            null
        }
    }

    fun poke() {
        showControls = true
        controlsTick++
    }

    fun togglePlay() {
        exoPlayer ?: return
        if (exoPlayer.playbackState == Player.STATE_ENDED) exoPlayer.seekTo(0)
        exoPlayer.playWhenReady = !exoPlayer.playWhenReady
        poke()
    }

    fun seekBy(deltaMs: Long) {
        exoPlayer ?: return
        val end = exoPlayer.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
        val target = (exoPlayer.currentPosition + deltaMs).coerceIn(0L, end)
        exoPlayer.seekTo(target)
        position = target
        seekFeedback = if (deltaMs < 0) -1 else 1
        seekFeedbackTick++
    }

    fun saveProgress() {
        exoPlayer ?: return
        val pos = exoPlayer.currentPosition
        val dur = exoPlayer.duration.takeIf { it > 0 } ?: 0L
        if (pos <= 0) return
        WatchHistory.save(
            context,
            WatchEntry(
                url = meta.url,
                title = meta.title.ifBlank { context.getString(R.string.video) },
                subtitle = meta.subtitle,
                image = meta.image,
                contentId = meta.contentId,
                type = meta.type,
                positionMs = pos,
                durationMs = dur,
            )
        )
    }

    // Player events
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
                if (state == Player.STATE_READY) duration = exoPlayer?.duration?.coerceAtLeast(0) ?: 0
                if (state == Player.STATE_ENDED) showControls = true
            }

            override fun onPlayerError(error: PlaybackException) {
                playerError = error.message ?: context.getString(R.string.playback_error)
            }
        }
        exoPlayer?.addListener(listener)
        exoPlayer?.let(onPlayerReady)
        onDispose {
            saveProgress()
            exoPlayer?.removeListener(listener)
            runCatching { exoPlayer?.release() }
        }
    }

    // Progress polling and periodic history save
    LaunchedEffect(exoPlayer) {
        exoPlayer ?: return@LaunchedEffect
        var ticks = 0
        while (true) {
            delay(250)
            if (scrubFraction == null) position = exoPlayer.currentPosition
            if (exoPlayer.duration > 0) duration = exoPlayer.duration
            if (++ticks % 20 == 0 && exoPlayer.isPlaying) saveProgress()
        }
    }

    // Auto-hide the chrome while playing
    LaunchedEffect(showControls, isPlaying, controlsTick, showSpeedPicker) {
        if (showControls && isPlaying && !showSpeedPicker) {
            delay(3500)
            showControls = false
        }
    }
    LaunchedEffect(seekFeedbackTick) {
        if (seekFeedbackTick > 0) {
            delay(600)
            seekFeedback = 0
        }
    }
    // When the chrome hides on TV, focus returns to the video so the remote keeps working.
    LaunchedEffect(showControls) {
        if (!showControls || !isTv) runCatching { rootFocus.requestFocus() }
        else runCatching { playFocus.requestFocus() }
    }

    BackHandler(enabled = showControls && isPlaying) { showControls = false }

    val videoLayer = rememberLayerBackdrop()
    val overlayHost = remember { GlassOverlayHost() }

    CompositionLocalProvider(LocalGlassOverlayHost provides overlayHost) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .focusRequester(rootFocus)
                .onFocusChanged { rootFocused = it.isFocused }
                .onKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                    when (event.key) {
                        Key.MediaPlayPause, Key.MediaPlay, Key.MediaPause, Key.Spacebar -> {
                            togglePlay(); true
                        }
                        Key.MediaFastForward -> {
                            seekBy(seekMs); true
                        }
                        Key.MediaRewind -> {
                            seekBy(-seekMs); true
                        }
                        // Arrows/OK act on the video only while the chrome isn't being navigated.
                        Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> if (rootFocused) {
                            togglePlay(); true
                        } else false
                        Key.DirectionLeft -> if (rootFocused) {
                            // Remote arrows follow the screen, not the reading direction.
                            seekBy(-seekMs); poke(); true
                        } else false
                        Key.DirectionRight -> if (rootFocused) {
                            seekBy(seekMs); poke(); true
                        } else false
                        Key.DirectionUp, Key.DirectionDown -> if (rootFocused) {
                            poke()
                            runCatching { playFocus.requestFocus() }
                            true
                        } else false
                        else -> false
                    }
                }
                .focusable()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { offset -> if (offset.x < size.width / 2) seekBy(-seekMs) else seekBy(seekMs) },
                        onTap = {
                            if (showControls) showControls = false else poke()
                        }
                    )
                }
        ) {
            if (playerError != null) {
                StatusView(
                    icon = Icons.Rounded.ErrorOutline,
                    title = stringResource(R.string.cannot_play),
                    message = playerError,
                    iconTint = colors.danger,
                    actionText = stringResource(R.string.back),
                    onAction = onBack,
                    modifier = Modifier.align(Alignment.Center)
                )
                return@Box
            }
            if (exoPlayer == null) {
                LiquidLoader(Modifier.align(Alignment.Center))
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
                    modifier = Modifier.fillMaxSize()
                )
            }

            CompositionLocalProvider(LocalBackdrop provides videoLayer) {
                if (isBuffering && !showControls) {
                    LiquidLoader(Modifier.align(Alignment.Center))
                }

                SeekIndicator(
                    visible = seekFeedback == -1,
                    icon = Icons.Rounded.FastRewind,
                    label = stringResource(R.string.n_seconds, settings.seekTimeSeconds),
                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 64.dp)
                )
                SeekIndicator(
                    visible = seekFeedback == 1,
                    icon = Icons.Rounded.FastForward,
                    label = stringResource(R.string.n_seconds, settings.seekTimeSeconds),
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 64.dp)
                )

                AnimatedVisibility(
                    visible = showControls,
                    enter = fadeIn(tween(220)),
                    exit = fadeOut(tween(300)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    PlayerChrome(
                        meta = meta,
                        isPlaying = isPlaying,
                        isBuffering = isBuffering,
                        position = scrubFraction?.let { (it * duration).toLong() } ?: position,
                        duration = duration,
                        speed = speed,
                        playFocus = playFocus,
                        onBack = onBack,
                        onTogglePlay = ::togglePlay,
                        onSeekBy = { seekBy(it); poke() },
                        onScrub = { fraction ->
                            scrubFraction = fraction
                            poke()
                        },
                        onScrubEnd = {
                            scrubFraction?.let { exoPlayer.seekTo((it * duration).toLong()) }
                            scrubFraction = null
                        },
                        onSpeedClick = { showSpeedPicker = true },
                        videoLayer = videoLayer,
                        seekSeconds = settings.seekTimeSeconds
                    )
                }
            }

            GlassOverlayLayer(overlayHost, videoLayer)
        }

        if (showSpeedPicker) {
            GlassDialog(onDismissRequest = { showSpeedPicker = false }) {
                DialogHeader(title = stringResource(R.string.playback_speed), icon = Icons.Rounded.Speed)
                VideoPlayerSettings.SPEEDS.forEach { option ->
                    DialogAction(
                        text = formatSpeed(option),
                        style = if (option == speed) DialogActionStyle.Primary else DialogActionStyle.Secondary,
                        onClick = {
                            speed = option
                            exoPlayer?.setPlaybackSpeed(option)
                            showSpeedPicker = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerChrome(
    meta: PlayerMeta,
    isPlaying: Boolean,
    isBuffering: Boolean,
    position: Long,
    duration: Long,
    speed: Float,
    playFocus: FocusRequester,
    onBack: () -> Unit,
    onTogglePlay: () -> Unit,
    onSeekBy: (Long) -> Unit,
    onScrub: (Float) -> Unit,
    onScrubEnd: () -> Unit,
    onSpeedClick: () -> Unit,
    videoLayer: com.kyant.backdrop.backdrops.LayerBackdrop,
    seekSeconds: Int,
) {
    val colors = Glass.colors
    val latestScrub by rememberUpdatedState(onScrub)
    Box(Modifier.fillMaxSize()) {
        // Soft scrims keep the chrome legible without dimming the whole picture.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Black.copy(alpha = 0.5f),
                        0.25f to Color.Transparent,
                        0.7f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.6f)
                    )
                )
        )

        // Top bar: back, title, speed
        Row(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LiquidIconButton(
                onClick = onBack,
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                iconTint = Color.White
            )
            if (meta.title.isNotBlank()) {
                Column(Modifier.weight(1f)) {
                    Text(meta.title, style = GlassType.Headline, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    meta.subtitle?.let {
                        Text(it, style = GlassType.Caption, color = Color.White.copy(alpha = 0.75f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            } else {
                Box(Modifier.weight(1f))
            }
            LiquidButton(onClick = onSpeedClick, height = 44.dp, contentPadding = PaddingValues(horizontal = 14.dp)) {
                Icon(Icons.Rounded.Speed, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text(formatSpeed(speed), style = GlassType.CaptionBold, color = Color.White)
            }
        }

        // Transport cluster (laid out left-to-right like every media remote)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidIconButton(
                    onClick = { onSeekBy(-seekSeconds * 1000L) },
                    icon = Icons.Rounded.Replay10,
                    contentDescription = stringResource(R.string.rewind),
                    iconTint = Color.White,
                    size = 56.dp,
                    iconSize = 28.dp
                )
                Box(contentAlignment = Alignment.Center) {
                    LiquidIconButton(
                        onClick = onTogglePlay,
                        icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                        iconTint = Color.White,
                        size = 84.dp,
                        iconSize = 44.dp,
                        modifier = Modifier.focusRequester(playFocus)
                    )
                    if (isBuffering) LiquidLoader(size = 92.dp)
                }
                LiquidIconButton(
                    onClick = { onSeekBy(seekSeconds * 1000L) },
                    icon = Icons.Rounded.Forward10,
                    contentDescription = stringResource(R.string.forward),
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
                    .widthIn(max = 1100.dp)
                    .fillMaxWidth()
                    .glass(
                        backdrop = videoLayer,
                        shape = RoundedRectangle(28.dp),
                        colors = colors,
                        style = GlassStyle(blur = 16.dp, refractionHeight = 16.dp, refractionAmount = 32.dp),
                        tint = Color.Black.copy(alpha = 0.25f)
                    )
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(formatTime(position), style = GlassType.CaptionBold, color = Color.White)
                LiquidSlider(
                    value = if (duration > 0) position.toFloat() / duration else 0f,
                    onValueChange = { latestScrub(it) },
                    onValueChangeFinished = onScrubEnd,
                    valueRange = 0f..1f,
                    accentColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (duration > 0) "-" + formatTime(duration - position) else "--:--",
                    style = GlassType.CaptionBold,
                    color = Color.White.copy(alpha = 0.75f)
                )
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

fun formatSpeed(speed: Float): String =
    if (speed == speed.toInt().toFloat()) "${speed.toInt()}x" else "${speed}x"

fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0) / 1000).toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}
