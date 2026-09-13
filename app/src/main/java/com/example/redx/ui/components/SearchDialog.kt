package com.example.redx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.redx.model.FeedSort
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurface
import com.example.redx.theme.AmoledSurfaceElevated
import com.example.redx.theme.NsfwRed
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary

val POPULAR_SEARCH_TOPICS = listOf(
    "Android 15", "Artificial Intelligence", "Gaming setup",
    "OpenAI", "NASA", "SpaceX", "Linux", "Pixel 9", "Cyberpunk",
    "WallStreetBets", "Science News"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchDialog(
    currentSubreddit: String,
    onDismiss: () -> Unit,
    onExecuteSearch: (query: String, searchInSubreddit: Boolean, includeMature: Boolean, sort: FeedSort) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchInCurrentSub by remember { mutableStateOf(false) }
    var includeMature by remember { mutableStateOf(true) }
    var selectedSort by remember { mutableStateOf(FeedSort.RELEVANCE) }

    val canSearchCurrentSub = currentSubreddit.isNotBlank() &&
            !currentSubreddit.equals("popular", true) &&
            !currentSubreddit.equals("all", true) &&
            !currentSubreddit.equals("home", true)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, AmoledBorder, RoundedCornerShape(20.dp))
                .background(AmoledSurface)
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = RedditOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Search Reddit Engine",
                        color = TextPrimary,
                        fontSize = 18.sp,
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

            // Search Bar Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search posts, topics, keywords...", color = TextSecondary) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (searchQuery.isNotBlank()) {
                        onExecuteSearch(searchQuery, searchInCurrentSub, includeMature, selectedSort)
                        onDismiss()
                    }
                }),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedditOrange,
                    unfocusedBorderColor = AmoledBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = RedditOrange
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scope Options (All Reddit vs Current Subreddit)
            if (canSearchCurrentSub) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = !searchInCurrentSub,
                        onClick = { searchInCurrentSub = false },
                        colors = RadioButtonDefaults.colors(selectedColor = RedditOrange)
                    )
                    Text(
                        text = "All Reddit",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { searchInCurrentSub = false }
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    RadioButton(
                        selected = searchInCurrentSub,
                        onClick = { searchInCurrentSub = true },
                        colors = RadioButtonDefaults.colors(selectedColor = RedditOrange)
                    )
                    Text(
                        text = "Only in r/$currentSubreddit",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { searchInCurrentSub = true }
                    )
                }
            }

            // Mature / 18+ Content Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = includeMature,
                    onCheckedChange = { includeMature = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = NsfwRed,
                        uncheckedColor = AmoledBorder
                    )
                )
                Text(
                    text = "Include 18+ / Mature Content (Unrestricted)",
                    color = if (includeMature) Color.White else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (includeMature) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.clickable { includeMature = !includeMature }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sort Selector for Search
            Text(
                text = "Sort Results By",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(FeedSort.RELEVANCE, FeedSort.HOT, FeedSort.TOP, FeedSort.NEW).forEach { s ->
                    val isSelected = selectedSort == s
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) RedditOrange else AmoledBorder.copy(alpha = 0.5f))
                            .clickable { selectedSort = s }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = s.label,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Button
            Button(
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        onExecuteSearch(searchQuery, searchInCurrentSub, includeMature, selectedSort)
                        onDismiss()
                    }
                },
                enabled = searchQuery.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RedditOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Search Reddit", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Trending Searches
            Text(
                text = "Trending Topics",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                POPULAR_SEARCH_TOPICS.forEach { topic ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(AmoledSurfaceElevated)
                            .border(1.dp, AmoledBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                searchQuery = topic
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = topic,
                            color = TextPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
