package com.example.redx.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.abs

private val EDGE_WIDTH = 56.dp
private val EDGE_RAIL_WIDTH = 72.dp
private const val SWIPE_TRIGGER_PX = 120f

/**
 * Adds a deliberate edge swipe so post-card gestures keep their existing meaning.
 * Swipe left advances to the next feed; swipe right returns to the previous feed.
 */
@Composable
fun Modifier.edgeSubredditSwipe(
    enabled: Boolean,
    onSwipe: (direction: Int) -> Unit
): Modifier {
    val edgeWidthPx = with(LocalDensity.current) { EDGE_WIDTH.toPx() }
    return pointerInput(enabled, edgeWidthPx) {
        if (!enabled) return@pointerInput

        awaitEachGesture {
            val down = awaitFirstDown(
                requireUnconsumed = false,
                pass = PointerEventPass.Initial
            )
            val startX = down.position.x
            var previousPosition = down.position
            var totalX = 0f
            var totalY = 0f
            var triggered = false
            val startedAtEdge = startX <= edgeWidthPx || startX >= size.width - edgeWidthPx

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                val position = change.position
                totalX += position.x - previousPosition.x
                totalY += position.y - previousPosition.y
                previousPosition = position

                if (!triggered && startedAtEdge &&
                    abs(totalX) >= SWIPE_TRIGGER_PX && abs(totalX) > abs(totalY)
                ) {
                    triggered = true
                    onSwipe(if (totalX < 0f) 1 else -1)
                }
                if (!change.pressed) break
            }
        }
    }
}

/** Places transparent gesture rails above feed content without stealing card gestures. */
@Composable
fun EdgeSubredditSwipeOverlay(
    enabled: Boolean,
    onSwipe: (direction: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxSize().zIndex(1f)) {
        Box(
            modifier = Modifier
                .width(EDGE_RAIL_WIDTH)
                .fillMaxHeight()
                .edgeSubredditSwipe(enabled, onSwipe)
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .width(EDGE_RAIL_WIDTH)
                .fillMaxHeight()
                .edgeSubredditSwipe(enabled, onSwipe)
        )
    }
}
