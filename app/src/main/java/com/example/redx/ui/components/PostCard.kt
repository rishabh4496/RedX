package com.example.redx.ui.components

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.redx.model.RedditPost
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurface
import com.example.redx.theme.AmoledSurfaceElevated
import com.example.redx.theme.DownvotePeriwinkle
import com.example.redx.theme.FlairBackground
import com.example.redx.theme.FlairText
import com.example.redx.theme.NsfwRed
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.SavedGold
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary
import com.example.redx.theme.TextTertiary
import com.example.redx.theme.UpvoteOrange
import com.example.redx.util.UrlSafety

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.MoreVert

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun PostCard(
    post: RedditPost,
    onPostClick: (RedditPost) -> Unit,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onSubredditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onLongClick: ((RedditPost) -> Unit)? = null,
    onMediaClick: ((String, String) -> Unit)? = null,
    onFlairClick: ((String) -> Unit)? = null,
    onAuthorClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var isInlineVideoPlaying by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) RedditOrange else AmoledBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .combinedClickable(
                onClick = { onPostClick(post) },
                onLongClick = { onLongClick?.invoke(post) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AmoledSurfaceElevated else AmoledSurface
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Row: Subreddit, NSFW, Author, Time, Domain
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalArrangement = Arrangement.Center
            ) {
                // Subreddit Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(RedditOrange.copy(alpha = 0.15f))
                        .clickable { onSubredditClick(post.subreddit) }
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "r/${post.subreddit}",
                        color = RedditOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // NSFW / 18+ Badge
                if (post.isNsfw) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NsfwRed)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NSFW 18+",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "•",
                    color = TextTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "u/${post.author}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .clickable(enabled = onAuthorClick != null) { onAuthorClick?.invoke(post.author) }
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "•",
                    color = TextTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = post.publishedTime,
                    color = TextTertiary,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )

                if (post.cleanDomain.isNotBlank() && post.cleanDomain != "reddit.com") {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${post.cleanDomain})",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Post Title (dimmed if read)
            Text(
                text = post.title,
                color = if (post.isRead) TextTertiary else TextPrimary,
                fontSize = 16.sp,
                fontWeight = if (post.isRead) FontWeight.Normal else FontWeight.SemiBold,
                lineHeight = 22.sp
            )

            // Flair & Awards Badges
            if (!post.flair.isNullOrBlank() || post.awardsCount > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!post.flair.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(FlairBackground)
                                .clickable(enabled = onFlairClick != null) { onFlairClick?.invoke(post.flair) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = post.flair,
                                color = FlairText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (post.awardsCount > 0) {
                        if (!post.flair.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF2A2208))
                                .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "★",
                                    color = Color(0xFFFFD700),
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${post.awardsCount}",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Media Preview (Video or Image)
            val mediaUrl = post.displayImageUrl
            if (post.isMediaVideo && isInlineVideoPlaying && !post.videoUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    VideoPlayerView(
                        videoUrl = post.videoUrl,
                        thumbnailUrl = mediaUrl,
                        modifier = Modifier.fillMaxSize(),
                        autoPlay = true,
                        isMuted = true,
                        onFullscreen = { onMediaClick?.invoke(post.videoUrl, post.title) }
                    )

                }
            } else if (!mediaUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmoledBorder)
                        .clickable {
                            val targetMedia = if (post.isMediaVideo) (post.videoUrl ?: post.contentUrl) else mediaUrl
                            onMediaClick?.invoke(targetMedia, post.title) ?: onPostClick(post)
                        }
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(mediaUrl)
                            .setHeader("User-Agent", "Mozilla/5.0")
                            .crossfade(true)
                            .build(),
                        contentDescription = post.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                    )

                    // Video Play Badge Overlay
                    if (post.isMediaVideo) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.75f))
                                .clickable {
                                    if (!post.videoUrl.isNullOrBlank()) {
                                        isInlineVideoPlaying = true
                                    } else {
                                        onMediaClick?.invoke(post.contentUrl, post.title)
                                    }
                                }
                                .align(Alignment.Center),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                tint = RedditOrange,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        // Video tag
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.75f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "VIDEO",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (post.isGallery) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(RedditOrange.copy(alpha = 0.9f))
                                .clickable {
                                    if (onMediaClick != null && !mediaUrl.isNullOrBlank()) {
                                        onMediaClick(mediaUrl, post.title)
                                    } else {
                                        onPostClick(post)
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "GALLERY (${post.galleryImageUrls.size})",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        val isGif = mediaUrl.contains(".gif", ignoreCase = true) ||
                                post.contentUrl.contains(".gif", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isGif) Color(0xFF8A2BE2).copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.75f))
                                .clickable {
                                    if (onMediaClick != null && !mediaUrl.isNullOrBlank()) {
                                        onMediaClick(mediaUrl, post.title)
                                    } else {
                                        onPostClick(post)
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isGif) "HD GIF" else "FULL HD",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                val cardText = post.cleanSelfText ?: post.selfTextHtml
                if (!cardText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = cardText.take(220) + if (cardText.length > 220) "..." else "",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Voting Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AmoledBorder.copy(alpha = 0.5f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    val upvoteScale by animateFloatAsState(
                        targetValue = if (post.userVote == 1) 1.25f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        label = "upvoteScale"
                    )
                    val upvoteTint by animateColorAsState(
                        targetValue = if (post.userVote == 1) UpvoteOrange else TextSecondary,
                        animationSpec = tween(180),
                        label = "upvoteTint"
                    )
                    IconButton(
                        onClick = {
                            val newVote = if (post.userVote == 1) 0 else 1
                            onVote(post, newVote)
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .scale(upvoteScale)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Upvote",
                            tint = upvoteTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    val scoreColor by animateColorAsState(
                        targetValue = when (post.userVote) {
                            1 -> UpvoteOrange
                            -1 -> DownvotePeriwinkle
                            else -> TextPrimary
                        },
                        animationSpec = tween(180),
                        label = "scoreColor"
                    )
                    Text(
                        text = post.displayScore,
                        color = scoreColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    val downvoteScale by animateFloatAsState(
                        targetValue = if (post.userVote == -1) 1.25f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        label = "downvoteScale"
                    )
                    val downvoteTint by animateColorAsState(
                        targetValue = if (post.userVote == -1) DownvotePeriwinkle else TextSecondary,
                        animationSpec = tween(180),
                        label = "downvoteTint"
                    )
                    IconButton(
                        onClick = {
                            val newVote = if (post.userVote == -1) 0 else -1
                            onVote(post, newVote)
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .scale(downvoteScale)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Downvote",
                            tint = downvoteTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Comments Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AmoledBorder.copy(alpha = 0.5f))
                        .clickable { onPostClick(post) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = post.displayComments,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Right Utility Actions: Bookmark, Share, Open External
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val bookmarkScale by animateFloatAsState(
                        targetValue = if (post.isSaved) 1.3f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        label = "saveScale"
                    )
                    val bookmarkTint by animateColorAsState(
                        targetValue = if (post.isSaved) SavedGold else TextSecondary,
                        animationSpec = tween(180),
                        label = "saveTint"
                    )
                    IconButton(
                        onClick = { onToggleSave(post) },
                        modifier = Modifier
                            .size(32.dp)
                            .scale(bookmarkScale)
                    ) {
                        Icon(
                            imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = bookmarkTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, post.title)
                                putExtra(Intent.EXTRA_TEXT, "${post.title}\n${post.permalink.ifBlank { post.contentUrl }}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Reddit Post"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    val downloadTarget = post.videoUrl ?: post.displayImageUrl
                    if (!downloadTarget.isNullOrBlank()) {
                        IconButton(
                            onClick = {
                                com.example.redx.util.MediaDownloadHelper.downloadMedia(context, downloadTarget, post.title)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Media",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (post.contentUrl.isNotBlank()) {
                        IconButton(
                            onClick = {
                                try {
                                    UrlSafety.httpUriOrNull(post.contentUrl)?.let { uri ->
                                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    }
                                } catch (e: Exception) {
                                    // Ignore
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "Open Link",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (onLongClick != null) {
                        IconButton(
                            onClick = { onLongClick(post) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Quick Actions",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
