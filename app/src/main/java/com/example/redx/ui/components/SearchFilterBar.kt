package com.example.redx.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redx.model.SearchContentType
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurface
import com.example.redx.theme.AmoledSurfaceElevated
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.TextSecondary
import com.example.redx.theme.TextTertiary

@Composable
fun SearchFilterBar(
    activeFilter: SearchContentType,
    onFilterSelected: (SearchContentType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AmoledSurface)
            .border(1.dp, AmoledBorder)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchContentType.values().forEach { filter ->
            val isSelected = filter == activeFilter
            val chipScale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "searchFilterScale"
            )
            val chipBgColor by animateColorAsState(
                targetValue = if (isSelected) RedditOrange.copy(alpha = 0.2f) else AmoledSurfaceElevated,
                animationSpec = tween(220),
                label = "searchFilterBg"
            )
            val chipBorderColor by animateColorAsState(
                targetValue = if (isSelected) RedditOrange else AmoledBorder,
                animationSpec = tween(220),
                label = "searchFilterBorder"
            )
            val chipContentColor by animateColorAsState(
                targetValue = if (isSelected) RedditOrange else TextSecondary,
                animationSpec = tween(220),
                label = "searchFilterContent"
            )

            val icon = when (filter) {
                SearchContentType.ALL -> Icons.Default.Explore
                SearchContentType.IMAGES -> Icons.Default.Image
                SearchContentType.VIDEOS -> Icons.Default.PlayCircle
                SearchContentType.TEXT -> Icons.Default.Forum
                SearchContentType.LINKS -> Icons.Default.Link
            }

            Box(
                modifier = Modifier
                    .scale(chipScale)
                    .clip(RoundedCornerShape(20.dp))
                    .background(chipBgColor)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = chipBorderColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) chipContentColor else TextTertiary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = filter.label,
                        color = chipContentColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
