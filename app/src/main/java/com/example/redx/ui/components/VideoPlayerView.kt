package com.example.redx.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.core.text.htmlEncode
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import com.example.redx.theme.RedditOrange
import com.example.redx.util.UrlSafety
import com.example.redx.util.AdBlocker

private fun formatPlayerTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

@OptIn(UnstableApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun VideoPlayerView(
    videoUrl: String,
    modifier: Modifier = Modifier,
    thumbnailUrl: String? = null,
    autoPlay: Boolean = true,
    isMuted: Boolean = true,
    onFullscreen: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val unescapedUrl = remember(videoUrl) {
        val trimmed = videoUrl.trim()
        try {
            android.text.Html.fromHtml(trimmed, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
        } catch (_: Exception) {
            trimmed.replace("&amp;", "&")
        }
    }
    val safeVideoUrl = UrlSafety.httpsUriOrNull(unescapedUrl)?.toString()
    if (safeVideoUrl == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("Unsupported video URL", color = Color.White, fontSize = 13.sp)
        }
        return
    }

    val isRedGifsEmbed = safeVideoUrl.contains("redgifs.com/watch/", ignoreCase = true) ||
            safeVideoUrl.contains("redgifs.com/ifr/", ignoreCase = true)
    val isDirectVideo = !isRedGifsEmbed

    val resolvedUrl = remember(safeVideoUrl) {
        when {
            safeVideoUrl.contains("v.redd.it", ignoreCase = true) &&
                    !UrlSafety.hasExtension(safeVideoUrl, "m3u8") &&
                    !safeVideoUrl.contains("DASH_") -> {
                val vId = safeVideoUrl.substringAfter("v.redd.it/").substringBefore("/").substringBefore("?").trim()
                if (vId.isNotBlank()) "https://v.redd.it/$vId/HLSPlaylist.m3u8" else safeVideoUrl
            }
            safeVideoUrl.contains("imgur.com", ignoreCase = true) &&
                    safeVideoUrl.endsWith(".gifv", ignoreCase = true) -> {
                safeVideoUrl.substringBeforeLast(".gifv") + ".mp4"
            }
            else -> safeVideoUrl
        }
    }

    if (isDirectVideo) {
        // Use Media3 ExoPlayer with HLS/DASH/MP4 support and cross-protocol redirect support
        var isBuffering by remember { mutableStateOf(true) }
        var hasError by remember { mutableStateOf(false) }
        var mutedState by remember { mutableStateOf(isMuted) }
        var currentSpeed by remember { mutableFloatStateOf(1.0f) }
        var isLooping by remember { mutableStateOf(true) }
        var isPlaying by remember { mutableStateOf(autoPlay) }
        var isHolding2x by remember { mutableStateOf(false) }
        var isScrubbing by remember { mutableStateOf(false) }
        var scrubStartPosition by remember { mutableLongStateOf(0L) }
        var scrubPosition by remember { mutableLongStateOf(0L) }
        var currentPosition by remember { mutableLongStateOf(0L) }
        var duration by remember { mutableLongStateOf(0L) }
        var seekFeedbackText by remember { mutableStateOf<String?>(null) }
        var seekFeedbackSide by remember { mutableIntStateOf(0) } // -1 left, 1 right
        val coroutineScope = rememberCoroutineScope()

        val httpDataSourceFactory = remember {
            DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile; rv:128.0) Gecko/128.0 Firefox/128.0")
                .setDefaultRequestProperties(
                    mapOf(
                        "Referer" to "https://www.reddit.com/",
                        "Origin" to "https://www.reddit.com"
                    )
                )
                .setAllowCrossProtocolRedirects(false)
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(20000)
        }

        val mediaSourceFactory = remember {
            DefaultMediaSourceFactory(context)
                .setDataSourceFactory(httpDataSourceFactory)
        }

        val exoPlayer = remember(resolvedUrl) {
            ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build().apply {
                    val mediaItem = MediaItem.fromUri(resolvedUrl.toUri())
                    setMediaItem(mediaItem)
                    prepare()
                    playWhenReady = autoPlay
                    repeatMode = Player.REPEAT_MODE_ONE
                    volume = if (isMuted) 0f else 1f
                    setPlaybackSpeed(currentSpeed)

                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            isBuffering = playbackState == Player.STATE_BUFFERING
                            if (playbackState == Player.STATE_READY) {
                                hasError = false
                                if (this@apply.duration > 0) {
                                    duration = this@apply.duration
                                }
                            }
                        }

                        override fun onIsPlayingChanged(playing: Boolean) {
                            isPlaying = playing
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            val vId = if (resolvedUrl.contains("v.redd.it/")) {
                                resolvedUrl.substringAfter("v.redd.it/").substringBefore("/").substringBefore("?")
                            } else null

                            val currentUri = currentMediaItem?.localConfiguration?.uri?.toString() ?: ""
                            if (vId != null && vId.isNotBlank()) {
                                if (!currentUri.contains("DASH_720") && !currentUri.contains("DASH_480") && !currentUri.contains("DASH_360")) {
                                    val fallback720 = "https://v.redd.it/$vId/DASH_720.mp4?source=fallback"
                                    setMediaItem(MediaItem.fromUri(fallback720.toUri()))
                                    prepare()
                                    playWhenReady = true
                                } else if (currentUri.contains("DASH_720")) {
                                    val fallback480 = "https://v.redd.it/$vId/DASH_480.mp4?source=fallback"
                                    setMediaItem(MediaItem.fromUri(fallback480.toUri()))
                                    prepare()
                                    playWhenReady = true
                                } else if (currentUri.contains("DASH_480")) {
                                    val fallback360 = "https://v.redd.it/$vId/DASH_360.mp4?source=fallback"
                                    setMediaItem(MediaItem.fromUri(fallback360.toUri()))
                                    prepare()
                                    playWhenReady = true
                                } else {
                                    hasError = true
                                    isBuffering = false
                                }
                            } else {
                                hasError = true
                                isBuffering = false
                            }
                        }
                    })
                }
        }

        // Only poll while something is actually moving. Polling a paused player 5x a
        // second kept the CPU awake and drained battery in long feed sessions.
        LaunchedEffect(exoPlayer, isPlaying, isScrubbing) {
            if (!isPlaying && !isScrubbing) {
                currentPosition = exoPlayer.currentPosition
                val stoppedDuration = exoPlayer.duration
                if (stoppedDuration > 0) duration = stoppedDuration
                return@LaunchedEffect
            }
            while (isActive) {
                currentPosition = exoPlayer.currentPosition
                val dur = exoPlayer.duration
                if (dur > 0) duration = dur
                isPlaying = exoPlayer.isPlaying
                delay(200)
            }
        }

        // Keep the mute button in sync when the caller flips the muted default.
        LaunchedEffect(isMuted) {
            mutedState = isMuted
            exoPlayer.volume = if (isMuted) 0f else 1f
        }

        LaunchedEffect(seekFeedbackText) {
            if (seekFeedbackText != null) {
                delay(650)
                seekFeedbackText = null
            }
        }

        val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
        androidx.compose.runtime.DisposableEffect(lifecycleOwner, exoPlayer) {
            // Remember whether playback was user-paused so returning to the app does not
            // force-restart a video the user deliberately stopped.
            var wasPlayingBeforePause = autoPlay
            val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                when (event) {
                    androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                        wasPlayingBeforePause = exoPlayer.isPlaying
                        exoPlayer.pause()
                    }
                    androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                        if (wasPlayingBeforePause) {
                            exoPlayer.play()
                        }
                    }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                exoPlayer.release()
            }
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                onRelease = { playerView ->
                    playerView.player = null
                }
            )

            // Touch gesture detector layer (tap, double tap, drag scrub, hold for 2x)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(exoPlayer) {
                        var lastTapTime = 0L
                        var lastTapPos = androidx.compose.ui.geometry.Offset.Zero

                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val downTime = System.currentTimeMillis()
                            val downPos = down.position
                            val isDoubleTapCandidate = (downTime - lastTapTime < 320L) &&
                                    ((downPos - lastTapPos).getDistance() < 120f)

                            var isHold2xActive = false
                            var isDragStarted = false
                            val startScrub = exoPlayer.currentPosition
                            var dragDistanceX = 0f

                            val holdJob = coroutineScope.launch {
                                delay(350)
                                if (!isDragStarted && !isDoubleTapCandidate) {
                                    isHold2xActive = true
                                    isHolding2x = true
                                    exoPlayer.setPlaybackSpeed(2.0f)
                                }
                            }

                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break

                                if (!change.pressed) {
                                    holdJob.cancel()
                                    if (isHold2xActive) {
                                        isHolding2x = false
                                        exoPlayer.setPlaybackSpeed(currentSpeed)
                                    } else if (isDragStarted) {
                                        exoPlayer.seekTo(scrubPosition)
                                        isScrubbing = false
                                    } else if (isDoubleTapCandidate) {
                                        val width = size.width
                                        val dur = if (exoPlayer.duration > 0) exoPlayer.duration else 60000L
                                        if (downPos.x < width * 0.4f) {
                                            val newPos = maxOf(0L, exoPlayer.currentPosition - 10000L)
                                            exoPlayer.seekTo(newPos)
                                            seekFeedbackText = "-10s"
                                            seekFeedbackSide = -1
                                        } else if (downPos.x > width * 0.6f) {
                                            val newPos = minOf(dur, exoPlayer.currentPosition + 10000L)
                                            exoPlayer.seekTo(newPos)
                                            seekFeedbackText = "+10s"
                                            seekFeedbackSide = 1
                                        } else {
                                            if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                                        }
                                        lastTapTime = 0L
                                    } else {
                                        lastTapTime = downTime
                                        lastTapPos = downPos
                                        if (exoPlayer.isPlaying) {
                                            exoPlayer.pause()
                                        } else {
                                            exoPlayer.play()
                                        }
                                    }
                                    break
                                }

                                val deltaX = change.position.x - downPos.x
                                val deltaY = change.position.y - downPos.y

                                if (!isDragStarted && !isHold2xActive && abs(deltaX) > 24f && abs(deltaX) > abs(deltaY)) {
                                    holdJob.cancel()
                                    isDragStarted = true
                                    isScrubbing = true
                                    scrubStartPosition = startScrub
                                    dragDistanceX = deltaX
                                    change.consume()
                                } else if (isDragStarted) {
                                    change.consume()
                                    dragDistanceX = change.position.x - downPos.x
                                    val dur = if (exoPlayer.duration > 0) exoPlayer.duration else 60000L
                                    val deltaMs = (dragDistanceX * 120f).toLong()
                                    scrubPosition = (startScrub + deltaMs).coerceIn(0L, dur)
                                }
                            }
                        }
                    }
            )

            if (isBuffering && !hasError) {
                CircularProgressIndicator(color = RedditOrange)
            }

            // Paused State Indicator
            if (!isPlaying && !isBuffering && !hasError && !isScrubbing) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // 2X Speed Hold Pill Indicator
            if (isHolding2x) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.88f))
                        .border(1.dp, RedditOrange, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = RedditOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "2X SPEED ▶▶",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Drag to Scrub / Seek HUD Overlay
            if (isScrubbing) {
                val deltaMs = scrubPosition - scrubStartPosition
                val deltaSign = if (deltaMs >= 0) "+" else "-"
                val deltaSec = abs(deltaMs) / 1000
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.88f))
                        .border(1.dp, RedditOrange.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${formatPlayerTime(scrubPosition)} / ${formatPlayerTime(duration)}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "[$deltaSign${deltaSec}s]",
                            color = RedditOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Double Tap Jump Feedback (-10s / +10s)
            if (seekFeedbackText != null) {
                val align = if (seekFeedbackSide < 0) Alignment.CenterStart else Alignment.CenterEnd
                Box(
                    modifier = Modifier
                        .align(align)
                        .padding(horizontal = 24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.75f))
                        .border(1.dp, RedditOrange, CircleShape)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (seekFeedbackSide < 0) "⏪ $seekFeedbackText" else "$seekFeedbackText ⏩",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (hasError && !isBuffering) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.88f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Video stream issue on Reddit CDN",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            hasError = false
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedditOrange),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(text = "Retry Stream", fontSize = 12.sp)
                    }
                }
            }

            // Bottom Progress Line
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                val progress = if (duration > 0) {
                    val current = if (isScrubbing) scrubPosition else currentPosition
                    (current.toFloat() / duration).coerceIn(0f, 1f)
                } else 0f
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(RedditOrange)
                )
            }

            // Apollo-Style Quick Floating Bar (Top Right: Audio & Speed controls)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onFullscreen != null) {
                    IconButton(
                        onClick = onFullscreen,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                // 1. Playback Speed Button (Apollo feature: 0.5x, 1x, 1.25x, 1.5x, 2x)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .clickable {
                            val speeds = listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f)
                            val nextIndex = (speeds.indexOf(currentSpeed) + 1) % speeds.size
                            currentSpeed = speeds[nextIndex]
                            exoPlayer.setPlaybackSpeed(currentSpeed)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = RedditOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${currentSpeed}x",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // 2. Loop Toggle Button
                IconButton(
                    onClick = {
                        isLooping = !isLooping
                        exoPlayer.repeatMode = if (isLooping) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .size(30.dp)
                ) {
                    Icon(
                        imageVector = if (isLooping) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Loop",
                        tint = if (isLooping) RedditOrange else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // 3. Audio Mute / Unmute Button (Apollo 1-tap audio)
                IconButton(
                    onClick = {
                        mutedState = !mutedState
                        exoPlayer.volume = if (mutedState) 0f else 1f
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .size(30.dp)
                ) {
                    Icon(
                        imageVector = if (mutedState) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Mute",
                        tint = if (!mutedState) RedditOrange else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    } else {
        // Hardware-Accelerated Embed Player for redgifs and web embeds
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(android.graphics.Color.BLACK)

                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                        settings.allowFileAccess = false
                        settings.allowContentAccess = false
                        settings.userAgentString =
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                val url = request?.url?.toString()
                                return if (AdBlocker.shouldBlock(url)) {
                                    AdBlocker.emptyResponse()
                                } else {
                                    super.shouldInterceptRequest(view, request)
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                evaluateJavascript(AdBlocker.HIDE_ADS_SCRIPT, null)
                                val js = "var v = document.querySelector('video'); if (v) { v.play(); v.loop = true; }"
                                evaluateJavascript(js, null)
                            }
                        }

                        val iframeUrl = if (safeVideoUrl.contains("redgifs.com/watch/")) {
                            val id = safeVideoUrl.substringAfter("redgifs.com/watch/").substringBefore("/").substringBefore("?").trim()
                            if (id.isNotBlank()) "https://www.redgifs.com/ifr/$id" else safeVideoUrl
                        } else safeVideoUrl
                        val escapedVideoUrl = iframeUrl.htmlEncode()
                        val embedHtml = if (iframeUrl.contains("redgifs.com/ifr/")) {
                            "<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'><style>body{margin:0;background:#000;display:flex;justify-content:center;align-items:center;height:100vh;overflow:hidden;}iframe{width:100%;height:100%;border:none;}</style></head><body><iframe src='$escapedVideoUrl' allowfullscreen allow='autoplay'></iframe></body></html>"
                        } else {
                            "<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width, initial-scale=1.0'><style>body{margin:0;background:#000;display:flex;justify-content:center;align-items:center;height:100vh;overflow:hidden;}video{width:100%;max-height:100%;}</style></head><body><video src='$escapedVideoUrl' controls autoplay loop playsinline></video></body></html>"
                        }

                        loadDataWithBaseURL("https://www.reddit.com", embedHtml, "text/html", "UTF-8", null)
                    }
                },
                onRelease = { webView ->
                    webView.stopLoading()
                    webView.destroy()
                }
            )
        }
    }
}
