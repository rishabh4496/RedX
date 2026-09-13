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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redx.model.RedditPost
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurface
import com.example.redx.theme.AmoledSurfaceElevated
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary
import com.example.redx.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CrosspostDialog(
    post: RedditPost,
    suggestedSubreddits: List<String>,
    onConfirm: (targetSubreddit: String, title: String) -> Unit,
    onDismiss: () -> Unit
) {
    var targetSubreddit by remember { mutableStateOf("") }
    var title by remember { mutableStateOf(post.title) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AmoledSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = null,
                    tint = RedditOrange,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Crosspost",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Source post banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmoledSurfaceElevated)
                        .border(1.dp, AmoledBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Original from r/${post.subreddit}",
                            color = RedditOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = post.title,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target Subreddit input
                OutlinedTextField(
                    value = targetSubreddit,
                    onValueChange = { targetSubreddit = it.removePrefix("r/").trim() },
                    label = { Text("Destination Subreddit", fontSize = 12.sp) },
                    placeholder = { Text("e.g. android, technology", fontSize = 12.sp, color = TextTertiary) },
                    prefix = { Text("r/", color = RedditOrange, fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedditOrange,
                        unfocusedBorderColor = AmoledBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = RedditOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick suggestions
                val suggestions = suggestedSubreddits
                    .filterNot { it.equals(post.subreddit, ignoreCase = true) || it.equals("home", true) || it.equals("popular", true) || it.equals("all", true) }
                    .take(4)
                if (suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        suggestions.forEach { sub ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AmoledSurfaceElevated)
                                    .border(1.dp, AmoledBorder, RoundedCornerShape(12.dp))
                                    .clickable { targetSubreddit = sub }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "r/$sub",
                                    color = if (targetSubreddit.equals(sub, ignoreCase = true)) RedditOrange else TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Editable Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Post Title", fontSize = 12.sp) },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RedditOrange,
                        unfocusedBorderColor = AmoledBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = RedditOrange
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (targetSubreddit.isNotBlank() && title.isNotBlank()) {
                        onConfirm(targetSubreddit.trim(), title.trim())
                    }
                },
                enabled = targetSubreddit.isNotBlank() && title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedditOrange,
                    contentColor = Color.White,
                    disabledContainerColor = AmoledBorder,
                    disabledContentColor = TextTertiary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Post", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
