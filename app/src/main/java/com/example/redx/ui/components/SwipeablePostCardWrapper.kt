package com.example.redx.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.redx.model.RedditPost
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.SavedGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val SWIPE_THRESHOLD = 160f
private const val SWIPE_MAX = 240f

/**
 * Wraps any card composable with swipe gesture support:
 *  - Swipe RIGHT -> upvote the post (orange reveal)
 *  - Swipe LEFT  -> save/unsave the post (gold reveal)
 */
@Composable
fun SwipeablePostCardWrapper(
    post: RedditPost,
    onSwipeUpvote: (RedditPost) -> Unit,
    onSwipeToggleSave: (RedditPost) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isActionTriggered by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "swipeOffset"
    )

    val rightRevealAlpha = (offsetX / SWIPE_MAX).coerceIn(0f, 1f)
    val leftRevealAlpha = (-offsetX / SWIPE_MAX).coerceIn(0f, 1f)

    val bgColor by animateColorAsState(
        targetValue = when {
            offsetX > 0f -> RedditOrange.copy(alpha = rightRevealAlpha.coerceIn(0f, 0.85f))
            offsetX < 0f -> SavedGold.copy(alpha = leftRevealAlpha.coerceIn(0f, 0.85f))
            else -> Color.Transparent
        },
        label = "swipeBgColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(bgColor),
            contentAlignment = if (offsetX > 0f) Alignment.CenterStart else Alignment.CenterEnd
        ) {
            if (abs(offsetX) > 40f) {
                Icon(
                    imageVector = if (offsetX > 0f) Icons.Default.ArrowUpward else Icons.Default.Bookmark,
                    contentDescription = if (offsetX > 0f) "Upvote" else "Save",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .size(28.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { translationX = animatedOffset }
                .then(
                    if (enabled) {
                        Modifier.pointerInput(post.id) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (!isActionTriggered) {
                                        scope.launch {
                                            if (offsetX > SWIPE_THRESHOLD) {
                                                isActionTriggered = true
                                                onSwipeUpvote(post)
                                            } else if (offsetX < -SWIPE_THRESHOLD) {
                                                isActionTriggered = true
                                                onSwipeToggleSave(post)
                                            }
                                            offsetX = 0f
                                            delay(350)
                                            isActionTriggered = false
                                        }
                                    } else {
                                        offsetX = 0f
                                    }
                                },
                                onDragCancel = { offsetX = 0f },
                                onHorizontalDrag = { _, delta ->
                                    if (!isActionTriggered) {
                                        offsetX = (offsetX + delta).coerceIn(-SWIPE_MAX, SWIPE_MAX)
                                    }
                                }
                            )
                        }
                    } else Modifier
                )
        ) {
            content()
        }
    }
}
