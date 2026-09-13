package com.example.redx.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.redx.model.RedditPost
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurface
import com.example.redx.theme.AmoledSurfaceElevated
import com.example.redx.theme.NsfwRed
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.SavedGold
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary
import com.example.redx.util.UrlSafety
import com.example.redx.util.MediaDownloadHelper

@Composable
fun PostQuickActionsSheet(
    post: RedditPost,
    onDismiss: () -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onToggleRead: (RedditPost) -> Unit,
    onAddFilterKeyword: (String) -> Unit,
    onAddFilterDomain: (String) -> Unit,
    onAddToReadLater: ((RedditPost) -> Unit)? = null,
    onCrosspost: ((RedditPost) -> Unit)? = null,
    onViewAuthor: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, AmoledBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = AmoledSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "r/${post.subreddit}",
                                color = RedditOrange,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• u/${post.author}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = post.title,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AmoledSurfaceElevated)
                        .border(1.dp, AmoledBorder, RoundedCornerShape(10.dp))
                ) {
                    // 1. Copy Link
                    ActionRow(
                        icon = Icons.Default.ContentCopy,
                        title = "Copy Post Link",
                        subtitle = "Copy Reddit URL to clipboard",
                        iconTint = RedditOrange,
                        onClick = {
                            val link = post.permalink.ifBlank { post.contentUrl }
                            val clip = ClipData.newPlainText("Reddit Link", link)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )

                    // 2. Copy Text / Title
                    ActionRow(
                        icon = Icons.Default.TextFields,
                        title = "Copy Post Text",
                        subtitle = "Copy title and self-text",
                        iconTint = TextPrimary,
                        onClick = {
                            val textToCopy = "${post.title}\n\n${post.cleanSelfText.orEmpty()}"
                            val clip = ClipData.newPlainText("Reddit Post Text", textToCopy)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Post text copied to clipboard", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )

                    // 3. Mark as Read / Unread
                    ActionRow(
                        icon = if (post.isRead) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        title = if (post.isRead) "Mark as Unread" else "Mark as Read",
                        subtitle = if (post.isRead) "Restore unread title styling" else "Dim this post in feed",
                        iconTint = TextSecondary,
                        onClick = {
                            onToggleRead(post)
                            onDismiss()
                        }
                    )

                    // 4. Save / Bookmark
                    ActionRow(
                        icon = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        title = if (post.isSaved) "Remove from Saved" else "Save to Offline Bookmarks",
                        subtitle = if (post.isSaved) "Remove local bookmark and Reddit save" else "Save locally; sync to Reddit when signed in",
                        iconTint = if (post.isSaved) SavedGold else TextSecondary,
                        onClick = {
                            onToggleSave(post)
                            onDismiss()
                        }
                    )

                    // 4b. Read Later
                    if (onAddToReadLater != null) {
                        ActionRow(
                            icon = Icons.Default.AccessTime,
                            title = "Read Later",
                            subtitle = "Add to local offline reading queue",
                            iconTint = Color(0xFFFFB74D),
                            onClick = {
                                onAddToReadLater.invoke(post)
                                Toast.makeText(context, "Added to Read Later queue", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        )
                    }

                    // 4c. Crosspost
                    if (onCrosspost != null) {
                        ActionRow(
                            icon = Icons.Default.Repeat,
                            title = "Crosspost to Subreddit",
                            subtitle = "Share this post to another community",
                            iconTint = Color(0xFF64B5F6),
                            onClick = {
                                onCrosspost.invoke(post)
                                onDismiss()
                            }
                        )
                    }

                    // 4d. View Author
                    if (onViewAuthor != null && post.author.isNotBlank() && post.author != "[deleted]") {
                        ActionRow(
                            icon = Icons.Default.Person,
                            title = "View u/${post.author}",
                            subtitle = "Browse author's post history",
                            iconTint = RedditOrange,
                            onClick = {
                                onViewAuthor.invoke(post.author)
                                onDismiss()
                            }
                        )
                    }

                    // 5. Download Media (if available)
                    val downloadMediaTarget = post.videoUrl ?: post.displayImageUrl
                    if (!downloadMediaTarget.isNullOrBlank()) {
                        ActionRow(
                            icon = Icons.Default.Download,
                            title = "Download Media",
                            subtitle = if (post.isMediaVideo) "Save video to Downloads" else "Save image to Gallery",
                            iconTint = RedditOrange,
                            onClick = {
                                MediaDownloadHelper.downloadMedia(context, downloadMediaTarget, post.title)
                                onDismiss()
                            }
                        )
                    }

                    // 6. Open in Browser
                    ActionRow(
                        icon = Icons.Default.OpenInBrowser,
                        title = "Open in Browser",
                        subtitle = "Launch link in external web browser",
                        iconTint = TextSecondary,
                        onClick = {
                            try {
                                val url = post.contentUrl.ifBlank { post.permalink }
                                UrlSafety.httpUriOrNull(url)?.let { uri ->
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                            }
                            onDismiss()
                        }
                    )

                    // 7. Filter Subreddit
                    ActionRow(
                        icon = Icons.Default.Block,
                        title = "Filter r/${post.subreddit}",
                        subtitle = "Hide posts from this subreddit",
                        iconTint = NsfwRed,
                        onClick = {
                            onAddFilterKeyword(post.subreddit)
                            Toast.makeText(context, "Filtered r/${post.subreddit}", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )

                    // 8. Filter Domain (if external)
                    if (post.cleanDomain.isNotBlank() && post.cleanDomain != "reddit.com") {
                        ActionRow(
                            icon = Icons.Default.Block,
                            title = "Filter ${post.cleanDomain}",
                            subtitle = "Hide posts linking to this domain",
                            iconTint = NsfwRed,
                            onClick = {
                                onAddFilterDomain(post.cleanDomain)
                                Toast.makeText(context, "Filtered domain ${post.cleanDomain}", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }
    }
}
