package com.example.redx.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.redx.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.redx.model.AppTheme
import com.example.redx.model.FeedViewMode
import com.example.redx.model.FontScale
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurface
import com.example.redx.theme.AmoledSurfaceElevated
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary
import com.example.redx.theme.TextTertiary

@Composable
fun SettingsSheet(
    currentTheme: AppTheme,
    currentViewMode: FeedViewMode,
    currentFontScale: FontScale,
    hideReadPosts: Boolean,
    showMatureContent: Boolean,
    onToggleHideRead: (Boolean) -> Unit,
    onToggleMature: (Boolean) -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenFilters: () -> Unit,
    onOpenSaved: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenCommunityExplorer: (() -> Unit)? = null,
    onOpenReadLater: (() -> Unit)? = null,
    onMarkAllRead: () -> Unit,
    onClearReadHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, AmoledBorder, RoundedCornerShape(20.dp))
                .background(AmoledSurface)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.redx_logo),
                        contentDescription = "RedX Logo",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(9.dp))
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("RedX Settings", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("v2.0 • AMOLED Edition", color = RedditOrange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(Modifier.height(18.dp))
            SettingsSectionTitle("COMMUNITIES & SYNC")
            if (onOpenCommunityExplorer != null) {
                SettingsRow(
                    icon = Icons.Default.Explore,
                    title = "Community Explorer Hub",
                    subtitle = "Discover trending subreddits, tech, gaming, finance & history",
                    onClick = { onDismiss(); onOpenCommunityExplorer() }
                )
            }
            SettingsRow(
                icon = Icons.Default.AccountCircle,
                title = "Reddit account",
                subtitle = "Sign in, refresh account data, or switch accounts",
                onClick = { onDismiss(); onOpenAccount() }
            )
            SettingsRow(
                icon = Icons.Default.Bookmark,
                title = "Saved posts & media",
                subtitle = "Threads, images, and videos bookmarked on this device",
                onClick = { onDismiss(); onOpenSaved() }
            )
            if (onOpenReadLater != null) {
                SettingsRow(
                    icon = Icons.Default.AccessTime,
                    title = "Read Later queue",
                    subtitle = "Offline queue of posts saved to read later",
                    onClick = { onDismiss(); onOpenReadLater() }
                )
            }

            Spacer(Modifier.height(16.dp))
            SettingsSectionTitle("FEED")
            SettingsSwitchRow(
                icon = if (hideReadPosts) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                title = "Hide read posts",
                subtitle = "Keep posts you have opened out of the feed",
                checked = hideReadPosts,
                onCheckedChange = onToggleHideRead
            )
            SettingsSwitchRow(
                icon = Icons.Default.Security,
                title = "Show mature content",
                subtitle = "Use Reddit's mature-content preference for feeds and search",
                checked = showMatureContent,
                onCheckedChange = onToggleMature
            )

            Spacer(Modifier.height(16.dp))
            SettingsSectionTitle("APPEARANCE")
            SettingsRow(
                icon = Icons.Default.Palette,
                title = "Appearance & viewing style",
                subtitle = "${currentTheme.label} • ${currentViewMode.label} • ${currentFontScale.label} text",
                onClick = { onDismiss(); onOpenAppearance() }
            )
            SettingsRow(
                icon = Icons.Default.FilterAlt,
                title = "Content filters",
                subtitle = "Block keywords, subreddits, and domains",
                onClick = { onDismiss(); onOpenFilters() }
            )

            Spacer(Modifier.height(16.dp))
            SettingsSectionTitle("STORAGE & PRIVACY")
            SettingsRow(
                icon = Icons.Default.CleaningServices,
                title = "Mark visible posts as read",
                subtitle = "Clear the current feed from your unread queue",
                onClick = onMarkAllRead
            )
            SettingsRow(
                icon = Icons.Default.CleaningServices,
                title = "Clear read history",
                subtitle = "Reset local read markers without removing bookmarks",
                onClick = onClearReadHistory
            )
            Text(
                text = "Bookmarks can be kept locally for offline access. When signed in, Reddit saves are synchronized when the server accepts the action.",
                color = TextTertiary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AmoledSurfaceElevated)
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = RedditOrange, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "RedX keeps Reddit credentials inside the secure WebView session and does not store your password.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RedditOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        color = TextTertiary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = RedditOrange, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (checked) RedditOrange else TextSecondary, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = RedditOrange,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = AmoledBorder
            )
        )
    }
}
