package com.example.redx.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redx.model.RedditPost
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurface
import com.example.redx.theme.AmoledSurfaceElevated
import com.example.redx.theme.NsfwRed
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.SavedGold
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary
import com.example.redx.theme.TextTertiary
import com.example.redx.util.MediaDownloadHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactPostCard(
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) RedditOrange else AmoledBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .combinedClickable(
                onClick = { onPostClick(post) },
                onLongClick = { onLongClick?.invoke(post) }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AmoledSurfaceElevated else AmoledSurface
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fixed Left Thumbnail (RIF signature alignment)
            val mediaUrl = post.displayImageUrl
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AmoledSurfaceElevated)
                    .border(1.dp, AmoledBorder, RoundedCornerShape(8.dp))
                    .clickable {
                        val targetMedia = if (post.isMediaVideo) (post.videoUrl ?: post.contentUrl) else mediaUrl
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
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Video",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    // Fallback placeholder with themed icon for text/link posts
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        Icon(
                            imageVector = if (post.cleanDomain != "reddit.com") Icons.Default.OpenInBrowser else Icons.AutoMirrored.Filled.Article,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(20.dp)
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

            Spacer(modifier = Modifier.width(10.dp))

            // Right Content: Metadata, Title & Actions
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "r/${post.subreddit}",
                        color = RedditOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onSubredditClick(post.subreddit) }
                    )

                    if (post.isNsfw) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(NsfwRed)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(text = "18+", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = "•", color = TextTertiary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = post.publishedTime, color = TextTertiary, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Post Title (dimmed if read)
                Text(
                    text = post.title,
                    color = if (post.isRead) TextTertiary else TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (post.isRead) FontWeight.Normal else FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "▲ ${post.displayScore}",
                            color = RedditOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💬 ${post.displayComments}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        if (post.awardsCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "★ ${post.awardsCount}",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val saveScale by animateFloatAsState(
                            targetValue = if (post.isSaved) 1.25f else 1.0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "compactSaveScale"
                        )
                        val saveTint by animateColorAsState(
                            targetValue = if (post.isSaved) SavedGold else TextTertiary,
                            animationSpec = tween(200),
                            label = "compactSaveTint"
                        )

                        IconButton(
                            onClick = { onToggleSave(post) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save",
                                tint = saveTint,
                                modifier = Modifier
                                    .size(15.dp)
                                    .scale(saveScale)
                            )
                        }

                        if (!mediaUrl.isNullOrBlank() || !post.videoUrl.isNullOrBlank()) {
                            IconButton(
                                onClick = {
                                    val target = post.videoUrl ?: mediaUrl ?: ""
                                    MediaDownloadHelper.downloadMedia(context, target, post.title)
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        if (onLongClick != null) {
                            IconButton(
                                onClick = { onLongClick(post) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
