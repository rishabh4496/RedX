package com.example.redx.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.redx.theme.RedditOrange
import com.example.redx.util.MediaDownloadHelper
import com.example.redx.util.UrlSafety
import java.util.Locale

@Composable
fun MediaLightboxDialog(
    imageUrl: String,
    title: String,
    videoUrl: String? = null,
    isVideo: Boolean = false,
    galleryUrls: List<String> = emptyList(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var showControls by remember { mutableStateOf(true) }

    val allImages = remember(imageUrl, galleryUrls) {
        if (galleryUrls.isNotEmpty()) galleryUrls else listOf(imageUrl)
    }
    var currentImageIndex by remember(allImages, imageUrl) {
        val initialIdx = allImages.indexOf(imageUrl).let { if (it >= 0) it else 0 }
        mutableIntStateOf(initialIdx)
    }
    val currentImageUrl = allImages.getOrElse(currentImageIndex) { imageUrl }

    val isMediaVideo = isVideo || !videoUrl.isNullOrBlank() ||
            UrlSafety.hasExtension(imageUrl, "mp4", "webm", "m3u8", "gifv") ||
            imageUrl.contains("v.redd.it", ignoreCase = true) ||
            imageUrl.contains("redgifs.com", ignoreCase = true) ||
            imageUrl.contains(".mp4", ignoreCase = true)

    val activeVideoUrl = videoUrl?.takeIf { it.isNotBlank() } ?: (if (isMediaVideo) imageUrl else null)
    val mediaToDownload = activeVideoUrl ?: currentImageUrl

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false
        )
    ) {
        val dialogView = LocalView.current
        DisposableEffect(dialogView) {
            val window = (dialogView.parent as? DialogWindowProvider)?.window
            val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
            if (window != null && controller != null) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            onDispose {
                if (window != null && controller != null) {
                    controller.show(WindowInsetsCompat.Type.systemBars())
                    WindowCompat.setDecorFitsSystemWindows(window, true)
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (isMediaVideo && !activeVideoUrl.isNullOrBlank()) {
                // Fullscreen Video Player Area
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    VideoPlayerView(
                        videoUrl = activeVideoUrl,
                        thumbnailUrl = imageUrl,
                        modifier = Modifier.fillMaxSize(),
                        autoPlay = true,
                        isMuted = false
                    )
                }
            } else {
                // Interactive Image / GIF Area
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    showControls = !showControls
                                },
                                onDoubleTap = {
                                    if (scale > 1.2f) {
                                        scale = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                    } else {
                                        scale = 2.5f
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 5f)
                                scale = newScale

                                if (newScale > 1f) {
                                    val maxOffsetX = 800f * (newScale - 1f)
                                    val maxOffsetY = 1200f * (newScale - 1f)
                                    offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                    offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val imageRequest = remember(currentImageUrl) {
                        ImageRequest.Builder(context)
                            .data(currentImageUrl)
                            .setHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile)")
                            .crossfade(true)
                            .build()
                    }
                    SubcomposeAsyncImage(
                        model = imageRequest,
                        contentDescription = title,
                        loading = {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = RedditOrange, modifier = Modifier.size(48.dp))
                            }
                        },
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offsetX
                                translationY = offsetY
                            }
                    )

                    // Multi-Image Gallery Navigation Overlays
                    if (allImages.size > 1 && showControls) {
                        if (currentImageIndex > 0) {
                            IconButton(
                                onClick = {
                                    currentImageIndex--
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous Image",
                                    tint = Color.White
                                )
                            }
                        }

                        if (currentImageIndex < allImages.size - 1) {
                            IconButton(
                                onClick = {
                                    currentImageIndex++
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(12.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f))
                                    .size(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Image",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Top Bar Overlay
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .size(38.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    val displayTitle = if (allImages.size > 1) {
                        "${title.ifBlank { "Gallery" }} (${currentImageIndex + 1}/${allImages.size})"
                    } else {
                        title.ifBlank { if (isMediaVideo) "Video Player" else "Media Viewer" }
                    }

                    Text(
                        text = displayTitle,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Reset zoom if zoomed in (images only)
                        if (!isMediaVideo && scale > 1.1f) {
                            IconButton(
                                onClick = {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .size(38.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset Zoom", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        // Share
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, title)
                                    putExtra(Intent.EXTRA_TEXT, "$title\n$mediaToDownload")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Media Link"))
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                                .size(38.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Download
                        IconButton(
                            onClick = {
                                MediaDownloadHelper.downloadMedia(context, mediaToDownload, title)
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(RedditOrange)
                                .size(38.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = "Download Media", tint = Color.White)
                        }
                    }
                }
            }

            // Bottom Bar Overlay with Info & Controls
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isMediaVideo) {
                            "Tap video for playback controls • Floating audio & speed at top-right"
                        } else if (allImages.size > 1) {
                            "Image ${currentImageIndex + 1} of ${allImages.size} • Double-tap to zoom • Pinch to resize"
                        } else {
                            "Double-tap to zoom • Pinch to resize"
                        },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )

                    if (!isMediaVideo) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1fx", scale),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(RedditOrange.copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "VIDEO",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
