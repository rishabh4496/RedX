package com.example.redx.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.redx.model.RedditPost
import com.example.redx.theme.AmoledBackground
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurface
import com.example.redx.theme.AmoledSurfaceElevated
import com.example.redx.theme.DownvotePeriwinkle
import com.example.redx.theme.NsfwRed
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.SavedGold
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary
import com.example.redx.theme.UpvoteOrange
import com.example.redx.util.AdBlocker
import com.example.redx.util.PostSpeechManager
import com.example.redx.util.UrlSafety

@Composable
fun PostDetailSheet(
    post: RedditPost,
    onDismiss: () -> Unit,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onOpenLightbox: ((String, String) -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PostDetailContent(
            post = post,
            onDismiss = onDismiss,
            onVote = onVote,
            onToggleSave = onToggleSave,
            isTabletPane = false,
            onOpenLightbox = onOpenLightbox
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PostDetailContent(
    post: RedditPost,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onDismiss: (() -> Unit)? = null,
    isTabletPane: Boolean = false,
    onOpenLightbox: ((String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember(post.id) { mutableIntStateOf(0) } // 0: Overview, 1: Discussion
    var useOldReddit by remember(post.id) { mutableStateOf(false) } // Toggle for bypassing mobile app nag
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isShareDialogOpen by remember(post.id) { mutableStateOf(false) }
    val speechManager = remember(post.id) { PostSpeechManager(context) }
    val isSpeaking by speechManager.isSpeaking.collectAsState()
    val speechRate by speechManager.speechRate.collectAsState()
    val scrollState = remember(post.id) { androidx.compose.foundation.ScrollState(0) }
    val postId = post.id.removePrefix("t3_")
    val baseCommentUrl = UrlSafety.httpUriOrNull(post.permalink)
        ?.takeIf { UrlSafety.isAllowedHttpsHost(it.toString(), "reddit.com") }
        ?.toString()
        ?: "https://www.reddit.com/r/${Uri.encode(post.subreddit)}/comments/${Uri.encode(postId)}/"

    DisposableEffect(post.id) {
        onDispose {
            speechManager.shutdown()
            webViewRef?.stopLoading()
            webViewRef?.destroy()
            webViewRef = null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AmoledBackground)
        ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AmoledSurface)
                .border(1.dp, AmoledBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (onDismiss != null && !isTabletPane) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Back", tint = TextPrimary)
                }
            } else {
                Spacer(modifier = Modifier.width(4.dp))
            }

            Column(
                horizontalAlignment = if (isTabletPane) Alignment.Start else Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "r/${post.subreddit}",
                        color = RedditOrange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (post.isNsfw) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NsfwRed)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = "18+", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(
                    text = "u/${post.author} • ${post.publishedTime}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onToggleSave(post) }) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (post.isSaved) SavedGold else TextSecondary
                    )
                }

                IconButton(onClick = { isShareDialogOpen = true }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = TextSecondary)
                }

                val downloadTarget = post.videoUrl ?: post.displayImageUrl
                if (!downloadTarget.isNullOrBlank()) {
                    IconButton(onClick = {
                        com.example.redx.util.MediaDownloadHelper.downloadMedia(context, downloadTarget, post.title)
                    }) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Download Media", tint = TextSecondary)
                    }
                }

                if (isTabletPane && onDismiss != null) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Pane", tint = TextSecondary)
                    }
                }
            }
        }

        // Tabs: Overview vs Live Comments Thread
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = AmoledSurface,
            contentColor = RedditOrange,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(selectedTab),
                    color = RedditOrange
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = if (post.isMediaVideo) "Video & Post" else "Post & Media",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) RedditOrange else TextSecondary
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "Comments (${post.displayComments})",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) RedditOrange else TextSecondary
                    )
                }
            )
        }

        if (selectedTab == 0) {
            // Post Overview Scroll
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(
                    text = post.title,
                    color = TextPrimary,
                    fontSize = if (isTabletPane) 22.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // In-App Text-to-Speech Audio Narrator
                val speechContent = buildString {
                    append(post.title)
                    val textToRead = post.cleanSelfText ?: post.selfTextHtml
                    if (!textToRead.isNullOrBlank()) {
                        append(". ")
                        append(textToRead)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmoledSurface)
                        .border(1.dp, AmoledBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSpeaking) RedditOrange else AmoledSurfaceElevated)
                                    .clickable {
                                        if (isSpeaking) {
                                            speechManager.stop()
                                        } else {
                                            speechManager.speak(speechContent)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isSpeaking) "Pause" else "Play",
                                    tint = if (isSpeaking) Color.White else RedditOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isSpeaking) "Playing Story Audio..." else "Listen to Post & Story",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isSpeaking) "Tap to pause speech" else "AI Text-to-Speech narrator",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AmoledBorder)
                                    .clickable { speechManager.cycleRate() }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "${speechRate}x",
                                    color = RedditOrange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isSpeaking) {
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { speechManager.stop() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop",
                                        tint = NsfwRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Media Area: Gallery, Video Player, or Image Viewer
                val mediaHeight = if (isTabletPane) 360.dp else 280.dp
                val detailImageUrl = post.displayImageUrl
                val activeVideoUrl = post.videoUrl ?: if (post.isMediaVideo) post.contentUrl else null

                if (post.isGallery && post.galleryImageUrls.isNotEmpty()) {
                    var activeGalleryIndex by remember(post.id) { mutableIntStateOf(0) }
                    val currentGalleryImage = post.galleryImageUrls.getOrElse(activeGalleryIndex) { post.galleryImageUrls.first() }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmoledBorder)
                            .clickable {
                                onOpenLightbox?.invoke(currentGalleryImage, "${post.title} (${activeGalleryIndex + 1}/${post.galleryImageUrls.size})")
                            }
                    ) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(currentGalleryImage)
                                .setHeader("User-Agent", "Mozilla/5.0")
                                .crossfade(true)
                                .build(),
                            contentDescription = "${post.title} - Image ${activeGalleryIndex + 1}",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "GALLERY ${activeGalleryIndex + 1} / ${post.galleryImageUrls.size} • Tap for Zoom",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (post.galleryImageUrls.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(post.galleryImageUrls.size) { idx ->
                                val imgUrl = post.galleryImageUrls[idx]
                                val isSelected = idx == activeGalleryIndex
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) RedditOrange else AmoledBorder,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { activeGalleryIndex = idx }
                                ) {
                                    AsyncImage(
                                        model = coil.request.ImageRequest.Builder(context)
                                            .data(imgUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                } else if (post.isMediaVideo && !activeVideoUrl.isNullOrBlank()) {
                    VideoPlayerView(
                        videoUrl = activeVideoUrl,
                        thumbnailUrl = detailImageUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(mediaHeight),
                        autoPlay = true,
                        isMuted = false
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                } else if (!detailImageUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmoledBorder)
                            .clickable {
                                val targetMedia = if (post.isMediaVideo) (activeVideoUrl ?: post.contentUrl) else detailImageUrl
                                onOpenLightbox?.invoke(targetMedia, post.title)
                            }
                    ) {
                        AsyncImage(
                            model = coil.request.ImageRequest.Builder(context)
                                .data(detailImageUrl)
                                .setHeader("User-Agent", "Mozilla/5.0")
                                .crossfade(true)
                                .build(),
                            contentDescription = post.title,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth()
                        )

                        val isGif = detailImageUrl.contains(".gif", ignoreCase = true) ||
                                post.contentUrl.contains(".gif", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isGif) "HD GIF • Tap for Lightbox" else "FULL HD • Tap for 5x Zoom",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                val displayText = post.cleanSelfText ?: post.selfTextHtml
                if (!displayText.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmoledSurface)
                            .border(1.dp, AmoledBorder, RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = displayText,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // External Link Card
                if (post.contentUrl.isNotBlank() && post.contentUrl != post.permalink) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmoledSurface)
                            .border(1.dp, AmoledBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "External Link Source",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = post.cleanDomain,
                                    color = RedditOrange,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Button(
                                onClick = {
                                    try {
                                        UrlSafety.httpUriOrNull(post.contentUrl)?.let { uri ->
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        }
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AmoledBorder),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Open Link", fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Action Bar inside Detail View
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AmoledSurface)
                            .border(1.dp, AmoledBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        IconButton(onClick = {
                            val newVote = if (post.userVote == 1) 0 else 1
                            onVote(post, newVote)
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Upvote",
                                tint = if (post.userVote == 1) UpvoteOrange else TextSecondary
                            )
                        }
                        Text(
                            text = post.displayScore,
                            color = when (post.userVote) {
                                1 -> UpvoteOrange
                                -1 -> DownvotePeriwinkle
                                else -> TextPrimary
                            },
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(onClick = {
                            val newVote = if (post.userVote == -1) 0 else -1
                            onVote(post, newVote)
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Downvote",
                                tint = if (post.userVote == -1) DownvotePeriwinkle else TextSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { selectedTab = 1 },
                        colors = ButtonDefaults.buttonColors(containerColor = RedditOrange),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${post.displayComments} Comments", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Live Discussion Comments View with "View in App" & Mature Content Blocker Bypass
            Column(modifier = Modifier.fillMaxSize()) {
                // Bypass Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AmoledSurface)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (useOldReddit) "Classic / Unblocked View" else "Modern View (Anti-Nag Active)",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Row {
                        Button(
                            onClick = {
                                useOldReddit = !useOldReddit
                                val targetUrl = if (useOldReddit) {
                                    baseCommentUrl.replace("www.reddit.com", "old.reddit.com")
                                } else {
                                    baseCommentUrl
                                }
                                webViewRef?.loadUrl(targetUrl)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (useOldReddit) RedditOrange else AmoledBorder
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DesktopWindows,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (useOldReddit) "Classic" else "Bypass Nag",
                                fontSize = 11.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { webViewRef?.reload() },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = TextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                androidx.compose.runtime.key(post.id) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewRef = this
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                                settings.allowFileAccess = false
                                settings.allowContentAccess = false

                                // Use Desktop Chrome User-Agent to avoid mobile web "View in Reddit app" lockouts
                                settings.userAgentString =
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

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

                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val url = request?.url?.toString() ?: return true
                                        if (!UrlSafety.isAllowedHttpsHost(url, "reddit.com")) {
                                            val externalUri = UrlSafety.httpUriOrNull(url) ?: return true
                                            val intent = Intent(Intent.ACTION_VIEW, externalUri).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            runCatching { context.startActivity(intent) }
                                            return true
                                        }
                                        return false
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        // Auto-remove "To view the full version, view this page in the Reddit app" and promo modals
                                        val antiNagJs = """
                                            (function() {
                                                var selectors = [
                                                    'shreddit-app-promo',
                                                    '.app-download-banner',
                                                    '.XPromo',
                                                    'shreddit-async-loader[bundlename="bottom_sheet"]',
                                                    'div[slot="action-bar"]',
                                                    '.interstitial',
                                                    '.xpromo-overlay'
                                                ];
                                                selectors.forEach(function(s) {
                                                    document.querySelectorAll(s).forEach(function(el) { el.remove(); });
                                                });
                                                
                                                var btns = document.querySelectorAll('button, a');
                                                btns.forEach(function(b) {
                                                    var txt = (b.innerText || '').toLowerCase();
                                                    if (txt.includes('yes') || txt.includes('continue') || txt.includes('i am 18') || txt.includes('stay on web') || txt.includes('view in browser')) {
                                                        b.click();
                                                    }
                                                });
                                            })();
                                        """.trimIndent()
                                        evaluateJavascript(antiNagJs, null)
                                        evaluateJavascript(AdBlocker.HIDE_ADS_SCRIPT, null)
                                    }
                                }

                                val loadUrl = if (useOldReddit) {
                                    baseCommentUrl.replace("www.reddit.com", "old.reddit.com")
                                } else {
                                    baseCommentUrl
                                }
                                loadUrl(loadUrl)
                            }
                        },
                        update = { webView ->
                            val loadUrl = if (useOldReddit) {
                                baseCommentUrl.replace("www.reddit.com", "old.reddit.com")
                            } else {
                                baseCommentUrl
                            }
                            if (webView.url != loadUrl) {
                                webView.loadUrl(loadUrl)
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
    }

    if (isShareDialogOpen) {
        PostShareDialog(
            post = post,
            onDismiss = { isShareDialogOpen = false }
        )
    }
}
}
