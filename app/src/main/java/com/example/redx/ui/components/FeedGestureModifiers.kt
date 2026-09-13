package com.example.redx.ui.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private val EDGE_WIDTH = 56.dp
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

        var startX = 0f
        var totalDrag = 0f
        var triggered = false

        detectHorizontalDragGestures(
            onDragStart = { offset ->
                startX = offset.x
                totalDrag = 0f
                triggered = false
            },
            onHorizontalDrag = { change, dragAmount ->
                totalDrag += dragAmount
                val startedAtEdge = startX <= edgeWidthPx || startX >= size.width - edgeWidthPx
                if (!triggered && startedAtEdge && abs(totalDrag) >= SWIPE_TRIGGER_PX) {
                    triggered = true
                    onSwipe(if (totalDrag < 0f) 1 else -1)
                }
            },
            onDragEnd = {
                startX = 0f
                totalDrag = 0f
                triggered = false
            },
            onDragCancel = {
                startX = 0f
                totalDrag = 0f
                triggered = false
            }
        )
    }
}
