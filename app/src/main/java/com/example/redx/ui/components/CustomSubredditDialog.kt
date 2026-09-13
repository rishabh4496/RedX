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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import com.example.redx.theme.SavedGold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary

val POPULAR_SUGGESTIONS = listOf(
    "technology", "android", "gaming", "AskReddit", "science",
    "worldnews", "todayilearned", "pcmasterrace", "wallstreetbets",
    "space", "gadgets", "movies", "books", "memes"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomSubredditDialog(
    onDismiss: () -> Unit,
    onSubredditSelected: (String) -> Unit,
    favoriteSubreddits: List<String> = emptyList(),
    userSubreddits: List<String> = emptyList(),
    onToggleFavorite: (String) -> Unit = {}
) {
    var textInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, AmoledBorder, RoundedCornerShape(18.dp))
                .background(AmoledSurface)
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jump to Subreddit",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subreddit Input
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it.trim().removePrefix("r/").removePrefix("/") },
                placeholder = { Text("e.g. android, technology, gaming", color = TextSecondary) },
                prefix = { Text("r/", color = RedditOrange, fontWeight = FontWeight.Bold) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = {
                    if (textInput.isNotBlank()) {
                        onSubredditSelected(textInput)
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
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSubredditSelected(textInput)
                        onDismiss()
                    }
                },
                enabled = textInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RedditOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Go to r/${textInput.ifBlank { "..." }}", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (favoriteSubreddits.isNotEmpty()) {
                Text(
                    text = "Your Favorites",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    favoriteSubreddits.forEach { sub ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(RedditOrange.copy(alpha = 0.16f))
                                .border(1.dp, RedditOrange.copy(alpha = 0.55f), RoundedCornerShape(16.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        onSubredditSelected(sub)
                                        onDismiss()
                                    }
                                    .padding(start = 10.dp, top = 5.dp, bottom = 5.dp)
                            ) {
                                Text(text = "r/$sub", color = TextPrimary, fontSize = 12.sp)
                            }
                            IconButton(
                                onClick = { onToggleFavorite(sub) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Remove $sub from favorites",
                                    tint = RedditOrange,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            val customUserSubs = userSubreddits.filterNot { it.equals("home", true) || it.equals("popular", true) || it.equals("all", true) }
            if (customUserSubs.isNotEmpty()) {
                Text(
                    text = "Your Subscribed Communities",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    customUserSubs.forEach { sub ->
                        val isFav = favoriteSubreddits.contains(sub.lowercase())
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(AmoledSurfaceElevated)
                                .border(1.dp, AmoledBorder, RoundedCornerShape(16.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        onSubredditSelected(sub)
                                        onDismiss()
                                    }
                                    .padding(start = 10.dp, top = 5.dp, bottom = 5.dp, end = 4.dp)
                            ) {
                                Text(text = "r/$sub", color = TextPrimary, fontSize = 12.sp)
                            }
                            IconButton(
                                onClick = { onToggleFavorite(sub) },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Favorite $sub",
                                    tint = if (isFav) SavedGold else TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = "Popular Communities",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                POPULAR_SUGGESTIONS.forEach { sub ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(AmoledSurfaceElevated)
                            .border(1.dp, AmoledBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                onSubredditSelected(sub)
                                onDismiss()
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "r/$sub",
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
