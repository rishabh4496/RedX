package com.example.redx.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
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
import com.example.redx.util.MediaDownloadHelper
import kotlin.math.abs

// Helper: Curated color palette for subreddit indicator bars and avatar backgrounds
private val subredditAccentColors = listOf(
    Color(0xFFFF4500), // Reddit Orange
    Color(0xFF0079D3), // Cyan Blue
    Color(0xFF00A878), // Emerald Green
    Color(0xFF8E44AD), // Amethyst Violet
    Color(0xFFE67E22), // Warm Amber
    Color(0xFFE91E63), // Pink
    Color(0xFF00BCD4), // Ocean Teal
    Color(0xFFFF9800)  // Gold
)

fun getSubredditColor(sub: String): Color {
    val index = abs(sub.hashCode()) % subredditAccentColors.size
    return subredditAccentColors[index]
}

// Clean HTML tags from selftext
private fun stripHtml(html: String?): String {
    if (html.isNullOrBlank()) return ""
    return html.replace(Regex("<[^>]*>"), "")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")
        .replace(Regex("\\s+"), " ")
        .trim()
}

// ============================================================================
// 1. RELAY FOR REDDIT STYLE
// Colored indicator strip on the left, card outline, swipe gesture friendly
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RelayPostCard(
    post: RedditPost,
    onPostClick: (RedditPost) -> Unit,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onSubredditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onLongClick: ((RedditPost) -> Unit)? = null,
    onMediaClick: ((String, String) -> Unit)? = null,
    onFlairClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val subColor = getSubredditColor(post.subreddit)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) RedditOrange else AmoledBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .combinedClickable(
                onClick = { onPostClick(post) },
                onLongClick = { onLongClick?.invoke(post) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AmoledSurfaceElevated else AmoledSurface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Accent Bar (Relay signature feature)
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxSize()
                    .background(subColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                // Header: Subreddit pill, author, time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = "r/${post.subreddit}",
                            color = subColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onSubredditClick(post.subreddit) }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${post.publishedTime}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    if (post.flair != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = FlairBackground,
                            modifier = Modifier.clickable { onFlairClick?.invoke(post.flair) }
                        ) {
                            Text(
                                text = post.flair,
                                color = FlairText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Post Title
                Text(
                    text = post.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp
                )

                // Media Preview if available
                val mediaUrl = post.displayImageUrl
                if (!mediaUrl.isNullOrBlank()) {
                    val targetMedia = if (post.isMediaVideo) (post.videoUrl ?: post.contentUrl) else mediaUrl
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, AmoledBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                onMediaClick?.invoke(targetMedia, post.title)
                                    ?: onPostClick(post)
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
                            modifier = Modifier.fillMaxSize()
                        )
                        if (post.isMediaVideo) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        } else if (post.isGallery) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RedditOrange.copy(alpha = 0.85f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "GALLERY (${post.galleryImageUrls.size})",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Relay Action Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Upvote / Score / Downvote
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AmoledBorder.copy(alpha = 0.4f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = { onVote(post, if (post.userVote == 1) 0 else 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Upvote",
                                tint = if (post.userVote == 1) UpvoteOrange else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = post.displayScore,
                            color = when (post.userVote) {
                                1 -> UpvoteOrange
                                -1 -> DownvotePeriwinkle
                                else -> TextPrimary
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = { onVote(post, if (post.userVote == -1) 0 else -1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Downvote",
                                tint = if (post.userVote == -1) DownvotePeriwinkle else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Comments button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AmoledBorder.copy(alpha = 0.4f))
                            .clickable { onPostClick(post) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.displayComments,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Save Bookmark
                    IconButton(
                        onClick = { onToggleSave(post) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (post.isSaved) SavedGold else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, post.title)
                                putExtra(Intent.EXTRA_TEXT, "${post.title}\n${post.permalink.ifBlank { post.contentUrl }}")
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Post"))
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. APOLLO PURE (iOS Minimalist Style)
// Clean flat layout with hairline divider, SF-style typography, right thumbnail
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApolloPostCard(
    post: RedditPost,
    onPostClick: (RedditPost) -> Unit,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onSubredditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onLongClick: ((RedditPost) -> Unit)? = null,
    onMediaClick: ((String, String) -> Unit)? = null,
    onFlairClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val mediaUrl = post.displayImageUrl

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isSelected) AmoledSurfaceElevated else AmoledSurface)
            .combinedClickable(
                onClick = { onPostClick(post) },
                onLongClick = { onLongClick?.invoke(post) }
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left content column: Title, Metadata, Pill Stats
            Column(modifier = Modifier.weight(1f)) {
                // Post Title
                Text(
                    text = post.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(5.dp))

                // Apollo Subtitle (r/sub • u/author • time)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "r/${post.subreddit}",
                        color = RedditOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onSubredditClick(post.subreddit) }
                    )
                    Text(
                        text = " • u/${post.author} • ${post.publishedTime}",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Apollo Compact Pill Stats
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Upvote Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (post.userVote) {
                            1 -> UpvoteOrange.copy(alpha = 0.2f)
                            -1 -> DownvotePeriwinkle.copy(alpha = 0.2f)
                            else -> AmoledBorder.copy(alpha = 0.5f)
                        },
                        modifier = Modifier.clickable {
                            onVote(post, if (post.userVote == 1) 0 else 1)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = if (post.userVote == 1) UpvoteOrange else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = post.displayScore,
                                color = if (post.userVote == 1) UpvoteOrange else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Comments Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AmoledBorder.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { onPostClick(post) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = post.displayComments,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Bookmark
                    IconButton(
                        onClick = { onToggleSave(post) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (post.isSaved) SavedGold else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Right Thumbnail (Apollo hallmark) - Always 76dp uniform size for razor-sharp alignment
            val targetMedia = if (post.isMediaVideo) (post.videoUrl ?: post.contentUrl) else mediaUrl
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AmoledSurfaceElevated)
                    .border(1.dp, AmoledBorder, RoundedCornerShape(12.dp))
                    .clickable {
                        if (!targetMedia.isNullOrBlank()) {
                            onMediaClick?.invoke(targetMedia, post.title) ?: onPostClick(post)
                        } else {
                            onPostClick(post)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!mediaUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(mediaUrl)
                            .setHeader("User-Agent", "Mozilla/5.0")
                            .crossfade(true)
                            .build(),
                        contentDescription = post.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (post.isMediaVideo) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Video",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else if (post.isGallery) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(RedditOrange.copy(alpha = 0.85f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${post.galleryImageUrls.size}",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Fallback placeholder tile so all Apollo cards stay aligned
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (post.cleanDomain != "reddit.com") Icons.Default.OpenInBrowser else Icons.AutoMirrored.Filled.Article,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (post.cleanDomain != "reddit.com") "LINK" else "TEXT",
                            color = TextTertiary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        // Hairline Divider
        HorizontalDivider(color = AmoledBorder.copy(alpha = 0.4f), thickness = 0.8.dp)
    }
}

// ============================================================================
// 3. FULL-BLEED (TikTok / Reels / Immersive Media Card)
// Edge-to-edge media with bottom gradient scrim overlay & floating vertical dock
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullBleedPostCard(
    post: RedditPost,
    onPostClick: (RedditPost) -> Unit,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onSubredditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onLongClick: ((RedditPost) -> Unit)? = null,
    onMediaClick: ((String, String) -> Unit)? = null,
    onFlairClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val mediaUrl = post.displayImageUrl

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .height(390.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) RedditOrange else AmoledBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .combinedClickable(
                onClick = { onPostClick(post) },
                onLongClick = { onLongClick?.invoke(post) }
            ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Media
            if (!mediaUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(mediaUrl)
                        .setHeader("User-Agent", "Mozilla/5.0")
                        .crossfade(true)
                        .build(),
                    contentDescription = post.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fallback rich gradient canvas for text posts
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1E1E2C), Color(0xFF0F0F14))
                            )
                        )
                )
            }

            // Dark Scrim Overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.2f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.75f),
                                Color.Black.copy(alpha = 0.95f)
                            ),
                            startY = 0f,
                            endY = 1000f
                        )
                    )
            )

            // Top-left Badges
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = "r/${post.subreddit}",
                        color = RedditOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onSubredditClick(post.subreddit) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (post.isMediaVideo) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Red.copy(alpha = 0.85f)
                    ) {
                        Text(
                            text = "▶ VIDEO",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Bottom-Left Overlay: Title, Author, Excerpt
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 14.dp, end = 68.dp, bottom = 14.dp)
            ) {
                Text(
                    text = post.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "u/${post.author} • ${post.publishedTime}",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.sp
                )
            }

            // Right Vertical Floating Action Dock (TikTok/Reels style)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Upvote button + count
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (post.userVote == 1) UpvoteOrange else Color.Black.copy(alpha = 0.6f)
                            )
                            .clickable { onVote(post, if (post.userVote == 1) 0 else 1) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Upvote",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = post.displayScore,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Comments button + count
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onPostClick(post) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = post.displayComments,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bookmark Save
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { onToggleSave(post) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (post.isSaved) SavedGold else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Lightbox fullscreen button
                if (!mediaUrl.isNullOrBlank()) {
                    val targetMedia = if (post.isMediaVideo) (post.videoUrl ?: post.contentUrl) else mediaUrl
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onMediaClick?.invoke(targetMedia, post.title) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 4. TEXT READER (Hacker News / Minimalist Typography Style)
// Distraction-free reading with selftext excerpts, reading time, no media clutter
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TextOnlyPostCard(
    post: RedditPost,
    onPostClick: (RedditPost) -> Unit,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onSubredditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onLongClick: ((RedditPost) -> Unit)? = null,
    onMediaClick: ((String, String) -> Unit)? = null,
    onFlairClick: ((String) -> Unit)? = null
) {
    val cleanText = stripHtml(post.selfTextHtml)
    val wordCount = cleanText.split("\\s+".toRegex()).size
    val readMinutes = (wordCount / 180).coerceAtLeast(1)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) RedditOrange else AmoledBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .combinedClickable(
                onClick = { onPostClick(post) },
                onLongClick = { onLongClick?.invoke(post) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AmoledSurfaceElevated else AmoledSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Subreddit, reading time, author
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "r/${post.subreddit}",
                        color = RedditOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onSubredditClick(post.subreddit) }
                    )
                    Text(
                        text = " • u/${post.author}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "~$readMinutes min read",
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Post Title
            Text(
                text = post.title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )

            // SelfText excerpt preview if available
            if (cleanText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = cleanText,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Minimal Quiet Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Score
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            onVote(post, if (post.userVote == 1) 0 else 1)
                        }
                    ) {
                        Text(
                            text = "▲ ${post.displayScore}",
                            color = if (post.userVote == 1) UpvoteOrange else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Comments
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onPostClick(post) }
                    ) {
                        Text(
                            text = "💬 ${post.displayComments} comments",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Domain badge
                    if (post.cleanDomain.isNotBlank() && post.cleanDomain != "reddit.com") {
                        Text(
                            text = "🔗 ${post.cleanDomain}",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleSave(post) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (post.isSaved) SavedGold else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ============================================================================
// 5. MAGAZINE EDITORIAL (Flipboard / Newsstand Style)
// Large bold serif headline, 16:9 hero media, category banner, editorial byline
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MagazinePostCard(
    post: RedditPost,
    onPostClick: (RedditPost) -> Unit,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onSubredditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onLongClick: ((RedditPost) -> Unit)? = null,
    onMediaClick: ((String, String) -> Unit)? = null,
    onFlairClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val mediaUrl = post.displayImageUrl
    val cleanText = post.cleanSelfText ?: stripHtml(post.selfTextHtml)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
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
        Column(modifier = Modifier.fillMaxWidth()) {
            // Editorial Category Header Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "R/${post.subreddit.uppercase()} • ${post.flair?.uppercase() ?: "FEATURED"}",
                    color = RedditOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.clickable { onSubredditClick(post.subreddit) }
                )
                Text(
                    text = post.publishedTime,
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Editorial Headline Title
            Text(
                text = post.title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Serif,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            // Hero 16:9 Image if present
            if (!mediaUrl.isNullOrBlank()) {
                val targetMedia = if (post.isMediaVideo) (post.videoUrl ?: post.contentUrl) else mediaUrl
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable {
                            onMediaClick?.invoke(targetMedia, post.title)
                                ?: onPostClick(post)
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
                        modifier = Modifier.fillMaxSize()
                    )

                    if (post.isMediaVideo) {
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
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "GALLERY (${post.galleryImageUrls.size})",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Excerpt Paragraph
            if (cleanText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = cleanText,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Editorial Byline & Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Circular author badge
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(getSubredditColor(post.author)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.author.firstOrNull()?.uppercase() ?: "A",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "By u/${post.author}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "▲ ${post.displayScore}",
                        color = if (post.userVote == 1) UpvoteOrange else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "💬 ${post.displayComments}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    IconButton(
                        onClick = { onToggleSave(post) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (post.isSaved) SavedGold else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 6. BIG TILES (Slide for Reddit Style)
// 16:9 cinematic widescreen media on top with glassmorphism corner pills
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BigTilesPostCard(
    post: RedditPost,
    onPostClick: (RedditPost) -> Unit,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onSubredditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onLongClick: ((RedditPost) -> Unit)? = null,
    onMediaClick: ((String, String) -> Unit)? = null,
    onFlairClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val mediaUrl = post.displayImageUrl

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) RedditOrange else AmoledBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = { onPostClick(post) },
                onLongClick = { onLongClick?.invoke(post) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AmoledSurfaceElevated else AmoledSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Widescreen Top Media Tile
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
            ) {
                if (!mediaUrl.isNullOrBlank()) {
                    val targetMedia = if (post.isMediaVideo) (post.videoUrl ?: post.contentUrl) else mediaUrl
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(mediaUrl)
                            .setHeader("User-Agent", "Mozilla/5.0")
                            .crossfade(true)
                            .build(),
                        contentDescription = post.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                onMediaClick?.invoke(targetMedia, post.title)
                                    ?: onPostClick(post)
                            }
                    )
                    if (post.isMediaVideo) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else if (post.isGallery) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(RedditOrange.copy(alpha = 0.85f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "GALLERY (${post.galleryImageUrls.size})",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF2C3E50), Color(0xFF000000))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "r/${post.subreddit}", color = Color.White.copy(alpha = 0.5f), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Top-Left Floating Glass Pill (Subreddit)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "r/${post.subreddit}",
                        color = RedditOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onSubredditClick(post.subreddit) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Top-Right Floating Glass Pill (Score & Time)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "▲ ${post.displayScore} • ${post.publishedTime}",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Post Title & Details underneath
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = post.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 20.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Quick Action Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Upvote chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (post.userVote == 1) UpvoteOrange.copy(alpha = 0.2f) else AmoledBorder.copy(alpha = 0.5f),
                            modifier = Modifier.clickable { onVote(post, if (post.userVote == 1) 0 else 1) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (post.userVote == 1) UpvoteOrange else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = post.displayScore,
                                    color = if (post.userVote == 1) UpvoteOrange else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Comments chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AmoledBorder.copy(alpha = 0.5f),
                            modifier = Modifier.clickable { onPostClick(post) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = post.displayComments,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Right save and share buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { onToggleSave(post) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save",
                                tint = if (post.isSaved) SavedGold else TextSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, post.title)
                                    putExtra(Intent.EXTRA_TEXT, "${post.title}\n${post.permalink.ifBlank { post.contentUrl }}")
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Post"))
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = TextSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 7. STREAMLINE (Sync for Reddit Modern Floating Card)
// Ultra-modern 20dp super-rounded card, pill action capsule, glow border
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StreamlinePostCard(
    post: RedditPost,
    onPostClick: (RedditPost) -> Unit,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onSubredditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onLongClick: ((RedditPost) -> Unit)? = null,
    onMediaClick: ((String, String) -> Unit)? = null,
    onFlairClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val mediaUrl = post.displayImageUrl

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) RedditOrange else AmoledBorder.copy(alpha = 0.7f),
                shape = RoundedCornerShape(20.dp)
            )
            .combinedClickable(
                onClick = { onPostClick(post) },
                onLongClick = { onLongClick?.invoke(post) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = AmoledSurfaceElevated
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Round Subreddit Badge + Author + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(getSubredditColor(post.subreddit)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "r/",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "r/${post.subreddit}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onSubredditClick(post.subreddit) }
                    )
                    Text(
                        text = "u/${post.author} • ${post.publishedTime}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Post Title
            Text(
                text = post.title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )

            // Rounded Media Container
            if (!mediaUrl.isNullOrBlank()) {
                val targetMedia = if (post.isMediaVideo) (post.videoUrl ?: post.contentUrl) else mediaUrl
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, AmoledBorder, RoundedCornerShape(16.dp))
                        .clickable {
                            onMediaClick?.invoke(targetMedia, post.title)
                                ?: onPostClick(post)
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
                        modifier = Modifier.fillMaxSize()
                    )
                    if (post.isMediaVideo) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else if (post.isGallery) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(RedditOrange.copy(alpha = 0.85f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "GALLERY (${post.galleryImageUrls.size})",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Floating Capsule Pill Action Bar (Sync hallmark)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Connected Capsule Pill for Voting
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AmoledBorder.copy(alpha = 0.4f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        IconButton(
                            onClick = { onVote(post, if (post.userVote == 1) 0 else 1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Upvote",
                                tint = if (post.userVote == 1) UpvoteOrange else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = post.displayScore,
                            color = if (post.userVote == 1) UpvoteOrange else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(
                            onClick = { onVote(post, if (post.userVote == -1) 0 else -1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Downvote",
                                tint = if (post.userVote == -1) DownvotePeriwinkle else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Comments Capsule Pill
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AmoledBorder.copy(alpha = 0.4f),
                    modifier = Modifier.clickable { onPostClick(post) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comments",
                            tint = TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = post.displayComments,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Bookmark Capsule Pill
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AmoledBorder.copy(alpha = 0.4f),
                    modifier = Modifier.clickable { onToggleSave(post) }
                ) {
                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (post.isSaved) SavedGold else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 8. SOCIAL CHAT (Threaded Twitter / Bluesky / Discord Timeline Style)
// Avatar-first timeline with author badge and vertical thread connector lines
// ============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SocialChatPostCard(
    post: RedditPost,
    onPostClick: (RedditPost) -> Unit,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onSubredditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onLongClick: ((RedditPost) -> Unit)? = null,
    onMediaClick: ((String, String) -> Unit)? = null,
    onFlairClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val mediaUrl = post.displayImageUrl

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isSelected) AmoledSurfaceElevated else AmoledSurface)
            .combinedClickable(
                onClick = { onPostClick(post) },
                onLongClick = { onLongClick?.invoke(post) }
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left Column: Avatar & Timeline Connector Line
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(42.dp)
            ) {
                // Circular Author Avatar
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(getSubredditColor(post.author), RedditOrange)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.author.firstOrNull()?.uppercase() ?: "U",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Vertical Thread Connector Line
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(80.dp)
                        .background(AmoledBorder.copy(alpha = 0.5f))
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right Column: Author, Subreddit, Post, Media, Action Row
            Column(modifier = Modifier.weight(1f)) {
                // Header: u/author @r/subreddit • time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "u/${post.author}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "r/${post.subreddit}",
                        color = RedditOrange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onSubredditClick(post.subreddit) }
                    )
                    Text(
                        text = " • ${post.publishedTime}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Post Title (as tweet/message text)
                Text(
                    text = post.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                )

                // Inline Media Card
                if (!mediaUrl.isNullOrBlank()) {
                    val targetMedia = if (post.isMediaVideo) (post.videoUrl ?: post.contentUrl) else mediaUrl
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, AmoledBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                onMediaClick?.invoke(targetMedia, post.title)
                                    ?: onPostClick(post)
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
                            modifier = Modifier.fillMaxSize()
                        )
                        if (post.isMediaVideo) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.65f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        } else if (post.isGallery) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(RedditOrange.copy(alpha = 0.85f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "GALLERY (${post.galleryImageUrls.size})",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Twitter-style Inline Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Reply / Comment
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onPostClick(post) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Reply",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.displayComments,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // Like / Upvote
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            onVote(post, if (post.userVote == 1) 0 else 1)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Like",
                            tint = if (post.userVote == 1) UpvoteOrange else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = post.displayScore,
                            color = if (post.userVote == 1) UpvoteOrange else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Save / Bookmark
                    IconButton(
                        onClick = { onToggleSave(post) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (post.isSaved) SavedGold else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, post.title)
                                putExtra(Intent.EXTRA_TEXT, "${post.title}\n${post.permalink.ifBlank { post.contentUrl }}")
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Post"))
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = AmoledBorder.copy(alpha = 0.4f), thickness = 0.8.dp)
    }
}
