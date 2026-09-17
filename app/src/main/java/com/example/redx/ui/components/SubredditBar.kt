package com.example.redx.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurface
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary

val DEFAULT_SUBREDDITS = listOf(
    "popular",
    "all",
    "home",
    "technology",
    "android",
    "gaming",
    "AskReddit",
    "science",
    "programming",
    "memes",
    "worldnews",
    "todayilearned",
    "space",
    "gadgets"
)

@Composable
fun SubredditBar(
    activeSubreddit: String,
    onSubredditSelected: (String) -> Unit,
    onOpenCustomPicker: () -> Unit,
    modifier: Modifier = Modifier,
    subreddits: List<String> = DEFAULT_SUBREDDITS,
    onOpenMultiPicker: (() -> Unit)? = null
) {
    val scrollState = rememberScrollState()

    val chips = androidx.compose.runtime.remember(subreddits, activeSubreddit) {
        val cleanActive = activeSubreddit.trim().removePrefix("r/").removePrefix("/")
        if (cleanActive.isNotBlank() && subreddits.none { it.equals(cleanActive, ignoreCase = true) }) {
            listOf(cleanActive) + subreddits
        } else {
            subreddits
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AmoledSurface)
            .padding(vertical = 8.dp, horizontal = 8.dp)
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom / Add Subreddit Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(AmoledBorder)
                .clickable { onOpenCustomPicker() }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Search / Custom Subreddit",
                    tint = TextPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Subreddit",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (onOpenMultiPicker != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(RedditOrange.copy(alpha = 0.15f))
                    .clickable { onOpenMultiPicker() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DynamicFeed,
                        contentDescription = "Multi-Subreddit",
                        tint = RedditOrange,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Multi",
                        color = RedditOrange,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Subreddit Chips
        chips.forEach { sub ->
            val isSelected = activeSubreddit.equals(sub, ignoreCase = true)
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) RedditOrange else AmoledBorder.copy(alpha = 0.5f),
                animationSpec = tween(220),
                label = "subBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else TextSecondary,
                animationSpec = tween(220),
                label = "subText"
            )
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "subScale"
            )
            val label = when (sub.lowercase()) {
                "popular" -> "Popular"
                "all" -> "All"
                "home" -> "Home"
                else -> "r/$sub"
            }

            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .scale(scale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(backgroundColor)
                    .clickable { onSubredditSelected(sub) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = label,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        if (chips.size > 1) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Swipe from either screen edge to switch feeds",
                tint = TextSecondary.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(start = 2.dp, end = 4.dp)
                    .size(17.dp)
            )
        }
    }
}
