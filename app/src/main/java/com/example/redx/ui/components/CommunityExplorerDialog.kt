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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurface
import com.example.redx.theme.AmoledSurfaceElevated
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.SavedGold
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary
import com.example.redx.theme.TextTertiary

enum class CommunityCategory(val label: String) {
    TRENDING("🔥 Trending"),
    TECH("💻 Tech & Dev"),
    GAMING("🎮 Gaming & Pop"),
    FINANCE("📈 Finance"),
    CREATIVE("🎨 Creative")
}

private val COMMUNITY_CATEGORIES_MAP = mapOf(
    CommunityCategory.TRENDING to listOf(
        "AskReddit", "technology", "gaming", "worldnews", "science",
        "todayilearned", "memes", "explainlikeimfive", "movies", "space"
    ),
    CommunityCategory.TECH to listOf(
        "android", "androiddev", "kotlin", "programming", "webdev",
        "gadgets", "artificial", "linux", "apple", "hardware"
    ),
    CommunityCategory.GAMING to listOf(
        "pcmasterrace", "nintendo", "PlayStation", "XboxSeriesX", "anime",
        "television", "formula1", "soccer", "nba", "dankmemes"
    ),
    CommunityCategory.FINANCE to listOf(
        "wallstreetbets", "stocks", "CryptoCurrency", "personalfinance",
        "investing", "Economics", "realestate"
    ),
    CommunityCategory.CREATIVE to listOf(
        "EarthPorn", "photography", "design", "Art", "ArchitecturePorn",
        "DIY", "web_design"
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommunityExplorerDialog(
    onDismiss: () -> Unit,
    onSubredditSelected: (String) -> Unit,
    recentSubreddits: List<String> = emptyList(),
    favoriteSubreddits: List<String> = emptyList(),
    userSubreddits: List<String> = emptyList(),
    onToggleFavorite: (String) -> Unit = {},
    onClearRecents: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CommunityCategory.TRENDING) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, AmoledBorder, RoundedCornerShape(20.dp))
                .background(AmoledSurface)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = RedditOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Community Explorer",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar & Direct Go
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it.trim().removePrefix("r/").removePrefix("/") },
                placeholder = { Text("Search or jump to r/...", color = TextSecondary, fontSize = 13.sp) },
                prefix = { Text("r/", color = RedditOrange, fontWeight = FontWeight.Bold) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = {
                            onSubredditSelected(searchQuery)
                            onDismiss()
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Go", tint = RedditOrange)
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    if (searchQuery.isNotBlank()) {
                        onSubredditSelected(searchQuery)
                        onDismiss()
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedditOrange,
                    unfocusedBorderColor = AmoledBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = RedditOrange
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Direct Jump Button if user typed something
            if (searchQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RedditOrange.copy(alpha = 0.15f))
                        .border(1.dp, RedditOrange.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .clickable {
                            onSubredditSelected(searchQuery)
                            onDismiss()
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Open \"r/$searchQuery\"",
                        color = RedditOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(text = "Go →", color = RedditOrange, fontWeight = FontWeight.ExtraBold)
                }
            }

            // Favorites Section
            if (favoriteSubreddits.isNotEmpty() && searchQuery.isBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = SavedGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FAVORITE COMMUNITIES",
                            color = SavedGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    favoriteSubreddits.forEach { sub ->
                        CommunityChip(
                            name = sub,
                            isFavorite = true,
                            onSelect = {
                                onSubredditSelected(sub)
                                onDismiss()
                            },
                            onToggleFavorite = { onToggleFavorite(sub) }
                        )
                    }
                }
            }

            // User Subscribed Communities Section
            val cleanUserSubs = userSubreddits.filterNot { it.equals("home", true) || it.equals("popular", true) || it.equals("all", true) }
            if (cleanUserSubs.isNotEmpty() && searchQuery.isBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Explore, contentDescription = null, tint = RedditOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "YOUR SUBSCRIBED COMMUNITIES",
                        color = RedditOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cleanUserSubs.forEach { sub ->
                        val isFav = favoriteSubreddits.any { it.equals(sub, ignoreCase = true) }
                        CommunityChip(
                            name = sub,
                            isFavorite = isFav,
                            onSelect = {
                                onSubredditSelected(sub)
                                onDismiss()
                            },
                            onToggleFavorite = { onToggleFavorite(sub) }
                        )
                    }
                }
            }

            // Recent Subreddits History
            if (recentSubreddits.isNotEmpty() && searchQuery.isBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "RECENTLY VISITED",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                    IconButton(onClick = onClearRecents, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    recentSubreddits.take(8).forEach { sub ->
                        val isFav = favoriteSubreddits.any { it.equals(sub, ignoreCase = true) }
                        CommunityChip(
                            name = sub,
                            isFavorite = isFav,
                            onSelect = {
                                onSubredditSelected(sub)
                                onDismiss()
                            },
                            onToggleFavorite = { onToggleFavorite(sub) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Categories Selector
            Text(
                text = "EXPLORE BY TOPIC",
                color = TextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CommunityCategory.values().forEach { category ->
                    val isSelected = selectedCategory == category
                    val chipScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        label = "categoryScale"
                    )
                    val bg by animateColorAsState(
                        targetValue = if (isSelected) RedditOrange else AmoledSurfaceElevated,
                        animationSpec = tween(180),
                        label = "categoryBg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else TextSecondary,
                        animationSpec = tween(180),
                        label = "categoryText"
                    )

                    Box(
                        modifier = Modifier
                            .scale(chipScale)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bg)
                            .border(1.dp, if (isSelected) RedditOrange else AmoledBorder, RoundedCornerShape(16.dp))
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = category.label, color = textColor, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subreddits under Selected Category (or filtered by search)
            val baseList = COMMUNITY_CATEGORIES_MAP[selectedCategory] ?: emptyList()
            val displayedList = if (searchQuery.isNotBlank()) {
                COMMUNITY_CATEGORIES_MAP.values.flatten().distinct().filter {
                    it.contains(searchQuery, ignoreCase = true)
                }
            } else {
                baseList
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                displayedList.forEach { sub ->
                    val isFav = favoriteSubreddits.any { it.equals(sub, ignoreCase = true) }
                    CommunityChip(
                        name = sub,
                        isFavorite = isFav,
                        onSelect = {
                            onSubredditSelected(sub)
                            onDismiss()
                        },
                        onToggleFavorite = { onToggleFavorite(sub) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityChip(
    name: String,
    isFavorite: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(AmoledSurfaceElevated)
            .border(1.dp, AmoledBorder, RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "r/$name",
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .size(22.dp)
                .clickable { onToggleFavorite() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) SavedGold else TextTertiary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
