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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurface
import com.example.redx.theme.AmoledSurfaceElevated
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary
import com.example.redx.theme.TextTertiary

private const val MAX_MULTI_SUBS = 5

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiSubredditPickerDialog(
    availableSubreddits: List<String>,
    currentMultiSubs: List<String>,
    onConfirm: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedSubs = remember {
        mutableStateListOf<String>().apply {
            addAll(currentMultiSubs.ifEmpty { listOf("android", "technology") })
        }
    }
    var customInput by remember { mutableStateOf("") }

    val defaultPool = listOf(
        "android", "technology", "programming", "gadgets", "science",
        "gaming", "news", "worldnews", "todayilearned", "aww"
    )
    val pool = (availableSubreddits + defaultPool)
        .filterNot { it.equals("home", true) || it.equals("popular", true) || it.equals("all", true) }
        .distinct()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AmoledSurface,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DynamicFeed,
                    contentDescription = null,
                    tint = RedditOrange,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Multi-Subreddit Feed",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Combine up to $MAX_MULTI_SUBS subreddits into one unified feed.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Custom input row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { customInput = it.removePrefix("r/").trim() },
                        placeholder = { Text("Add custom subreddit...", fontSize = 12.sp, color = TextTertiary) },
                        prefix = { Text("r/", color = RedditOrange, fontWeight = FontWeight.Bold) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RedditOrange,
                            unfocusedBorderColor = AmoledBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = RedditOrange
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val clean = customInput.trim().lowercase()
                            if (clean.isNotBlank() && !selectedSubs.contains(clean) && selectedSubs.size < MAX_MULTI_SUBS) {
                                selectedSubs.add(clean)
                                customInput = ""
                            }
                        },
                        enabled = customInput.isNotBlank() && selectedSubs.size < MAX_MULTI_SUBS,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (customInput.isNotBlank() && selectedSubs.size < MAX_MULTI_SUBS) RedditOrange else AmoledSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = if (customInput.isNotBlank() && selectedSubs.size < MAX_MULTI_SUBS) Color.White else TextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selected chips
                Text(
                    text = "Selected (${selectedSubs.size}/$MAX_MULTI_SUBS):",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectedSubs.forEach { sub ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(RedditOrange.copy(alpha = 0.2f))
                                .border(1.dp, RedditOrange.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .clickable { selectedSubs.remove(sub) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "r/$sub",
                                    color = RedditOrange,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = RedditOrange,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Available to pick
                Text(
                    text = "Suggestions:",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pool.filterNot { selectedSubs.contains(it) }.take(10).forEach { sub ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AmoledSurfaceElevated)
                                .border(1.dp, AmoledBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    if (selectedSubs.size < MAX_MULTI_SUBS) {
                                        selectedSubs.add(sub)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+ r/$sub",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                if (selectedSubs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AmoledSurfaceElevated)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "URL: r/${selectedSubs.joinToString("+")}",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedSubs.toList()) },
                enabled = selectedSubs.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RedditOrange,
                    contentColor = Color.White,
                    disabledContainerColor = AmoledBorder,
                    disabledContentColor = TextTertiary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Browse Multi-Feed", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
