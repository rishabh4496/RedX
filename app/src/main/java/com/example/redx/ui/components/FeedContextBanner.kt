package com.example.redx.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redx.theme.AmoledBackground
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurfaceElevated
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary
import com.example.redx.theme.TextTertiary

/**
 * Compact feed identity header. It keeps multi-feed context visible without allowing a
 * long list of tags to grow into a large, wrapped banner on narrow phones.
 */
@Composable
fun FeedContextBanner(
    activeSubreddit: String,
    multiSubreddits: List<String>,
    postCount: Int,
    onOpenMultiPicker: () -> Unit,
    onDisableMultiFeed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMultiFeed = multiSubreddits.isNotEmpty()
    val accent = RedditOrange
    val surfaceColor = if (isMultiFeed) AmoledSurfaceElevated else AmoledBackground
    val borderColor = if (isMultiFeed) accent.copy(alpha = 0.45f) else AmoledBorder.copy(alpha = 0.75f)
    val label = if (isMultiFeed) "MULTI-FEED" else "CURRENT FEED"
    val subtitle = if (isMultiFeed) {
        "${multiSubreddits.size} communities unified"
    } else {
        "$postCount posts ready to explore"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        color = surfaceColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (isMultiFeed) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = if (isMultiFeed) 0.13f else 0.07f), Color.Transparent)
                    )
                )
                .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMultiFeed) Icons.Default.DynamicFeed else Icons.Default.Forum,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = accent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isMultiFeed) {
                    Row(
                        modifier = Modifier
                            .padding(top = 7.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        multiSubreddits.forEach { subreddit ->
                            Text(
                                text = "r/$subreddit",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmoledBackground.copy(alpha = 0.7f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "r/${activeSubreddit.removePrefix("r/")}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isMultiFeed) {
                IconButton(
                    onClick = onOpenMultiPicker,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit multi-feed communities",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDisableMultiFeed,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit multi-feed",
                        tint = accent,
                        modifier = Modifier.size(19.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = onOpenMultiPicker,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create multi-feed",
                        tint = TextTertiary,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}
