package com.example.redx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.redx.model.RedditPost
import com.example.redx.theme.AmoledBackground
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurface
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.SavedGold
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary

private enum class SavedFilter(val label: String) {
    ALL("All"),
    THREADS("Threads"),
    IMAGES("Images"),
    VIDEOS("Videos")
}

@Composable
fun SavedPostsSheet(
    savedPosts: List<RedditPost>,
    onDismiss: () -> Unit,
    onPostClick: (RedditPost) -> Unit,
    onToggleSave: (RedditPost) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(SavedFilter.ALL) }

    val threadCount = savedPosts.count { !it.isMediaVideo && it.displayImageUrl == null }
    val imageCount = savedPosts.count { !it.isMediaVideo && it.displayImageUrl != null }
    val videoCount = savedPosts.count { it.isMediaVideo }

    val filteredPosts = remember(savedPosts, searchQuery, selectedFilter) {
        val byType = when (selectedFilter) {
            SavedFilter.ALL -> savedPosts
            SavedFilter.THREADS -> savedPosts.filter { !it.isMediaVideo && it.displayImageUrl == null }
            SavedFilter.IMAGES -> savedPosts.filter { !it.isMediaVideo && it.displayImageUrl != null }
            SavedFilter.VIDEOS -> savedPosts.filter { it.isMediaVideo }
        }
        if (searchQuery.isBlank()) byType
        else byType.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.subreddit.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 700.dp)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, AmoledBorder, RoundedCornerShape(18.dp))
                    .background(AmoledBackground)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AmoledSurface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = SavedGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Saved Bookmarks (${savedPosts.size})",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${threadCount} threads • ${imageCount} images • ${videoCount} videos",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Filter Search Bar
                if (savedPosts.isNotEmpty()) {
                    Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Filter saved posts...", color = TextSecondary) },
                            singleLine = true,
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RedditOrange,
                                unfocusedBorderColor = AmoledBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SavedFilter.entries.forEach { filter ->
                        val count = when (filter) {
                            SavedFilter.ALL -> savedPosts.size
                            SavedFilter.THREADS -> threadCount
                            SavedFilter.IMAGES -> imageCount
                            SavedFilter.VIDEOS -> videoCount
                        }
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedFilter == filter) SavedGold.copy(alpha = 0.2f) else AmoledSurface)
                                .border(1.dp, if (selectedFilter == filter) SavedGold else AmoledBorder, RoundedCornerShape(16.dp))
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (filter) {
                                    SavedFilter.ALL -> Icons.Default.Bookmark
                                    SavedFilter.THREADS -> Icons.Default.Forum
                                    SavedFilter.IMAGES -> Icons.Default.Image
                                    SavedFilter.VIDEOS -> Icons.Default.PlayCircle
                                },
                                contentDescription = null,
                                tint = if (selectedFilter == filter) SavedGold else TextSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${filter.label} $count",
                                color = if (selectedFilter == filter) TextPrimary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // List
                if (filteredPosts.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (savedPosts.isEmpty()) "No saved bookmarks yet" else "No matching saved items",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bookmark a thread, image, or video from any card or its quick-actions menu.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(filteredPosts, key = { it.id }) { post ->
                            CompactPostCard(
                                post = post,
                                onPostClick = {
                                    onPostClick(post)
                                    onDismiss()
                                },
                                onVote = { _, _ -> },
                                onToggleSave = onToggleSave,
                                onSubredditClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
