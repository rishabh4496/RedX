package com.example.redx.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redx.model.RedditPost
import com.example.redx.theme.AmoledSurfaceElevated
import com.example.redx.theme.DownvotePeriwinkle
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.SavedGold
import com.example.redx.theme.TextSecondary
import com.example.redx.theme.UpvoteOrange
import kotlin.math.roundToInt

@Composable
fun RelaySwipeablePostCard(
    post: RedditPost,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onCommentsClick: (RedditPost) -> Unit,
    onMoreClick: (RedditPost) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val maxRevealPx = with(density) { -260.dp.toPx() }

    var targetOffset by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset,
        label = "relayOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AmoledSurfaceElevated)
    ) {
        // Background: Relay Action Drawer
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Upvote Action
            RelayActionButton(
                icon = Icons.Default.ArrowUpward,
                label = "Upvote",
                bgColor = if (post.userVote == 1) UpvoteOrange else UpvoteOrange.copy(alpha = 0.2f),
                tint = if (post.userVote == 1) Color.White else UpvoteOrange,
                onClick = {
                    val newVote = if (post.userVote == 1) 0 else 1
                    onVote(post, newVote)
                    targetOffset = 0f
                }
            )

            // 2. Downvote Action
            RelayActionButton(
                icon = Icons.Default.ArrowDownward,
                label = "Down",
                bgColor = if (post.userVote == -1) DownvotePeriwinkle else DownvotePeriwinkle.copy(alpha = 0.2f),
                tint = if (post.userVote == -1) Color.White else DownvotePeriwinkle,
                onClick = {
                    val newVote = if (post.userVote == -1) 0 else -1
                    onVote(post, newVote)
                    targetOffset = 0f
                }
            )

            // 3. Save Action
            RelayActionButton(
                icon = if (post.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                label = "Save",
                bgColor = if (post.isSaved) SavedGold else SavedGold.copy(alpha = 0.2f),
                tint = if (post.isSaved) Color.Black else SavedGold,
                onClick = {
                    onToggleSave(post)
                    targetOffset = 0f
                }
            )

            // 4. Comments Action
            RelayActionButton(
                icon = Icons.Default.ChatBubbleOutline,
                label = post.displayComments,
                bgColor = RedditOrange.copy(alpha = 0.2f),
                tint = RedditOrange,
                onClick = {
                    onCommentsClick(post)
                    targetOffset = 0f
                }
            )

            // 5. More Action
            RelayActionButton(
                icon = Icons.Default.MoreVert,
                label = "More",
                bgColor = Color.White.copy(alpha = 0.15f),
                tint = Color.White,
                onClick = {
                    onMoreClick(post)
                    targetOffset = 0f
                }
            )
        }

        // Foreground: Post Card with Drag Gestures
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newOffset = (targetOffset + delta).coerceIn(maxRevealPx, 0f)
                        targetOffset = newOffset
                    },
                    onDragStopped = {
                        targetOffset = if (targetOffset < maxRevealPx / 2.5f) {
                            maxRevealPx
                        } else {
                            0f
                        }
                    }
                )
                .clickable(enabled = targetOffset != 0f) {
                    targetOffset = 0f
                }
        ) {
            content()
        }
    }
}

@Composable
private fun RelayActionButton(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 9.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
