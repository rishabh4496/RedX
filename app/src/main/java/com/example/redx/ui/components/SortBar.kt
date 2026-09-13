package com.example.redx.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redx.model.FeedSort
import com.example.redx.theme.AmoledBackground
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary

@Composable
fun SortBar(
    activeSort: FeedSort,
    onSortSelected: (FeedSort) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayedSorts = listOf(FeedSort.HOT, FeedSort.NEW, FeedSort.TOP, FeedSort.RISING)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AmoledBackground)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayedSorts.forEach { sort ->
            val isSelected = activeSort == sort
            val icon: ImageVector = when (sort) {
                FeedSort.HOT -> Icons.Default.Whatshot
                FeedSort.NEW -> Icons.Default.NewReleases
                FeedSort.TOP -> Icons.AutoMirrored.Filled.TrendingUp
                FeedSort.RISING -> Icons.Default.ElectricBolt
                FeedSort.RELEVANCE -> Icons.Default.Search
            }

            val bgColor by animateColorAsState(
                targetValue = if (isSelected) RedditOrange.copy(alpha = 0.22f) else AmoledBorder.copy(alpha = 0.35f),
                animationSpec = tween(220),
                label = "sortBg"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) RedditOrange.copy(alpha = 0.7f) else Color.Transparent,
                animationSpec = tween(220),
                label = "sortBorder"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) RedditOrange else TextSecondary,
                animationSpec = tween(220),
                label = "sortContent"
            )
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.03f else 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "sortScale"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .scale(scale)
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable { onSortSelected(sort) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = sort.label,
                        tint = contentColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = sort.label,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
