package com.example.redx.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.draw.rotate
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.redx.R
import com.example.redx.model.FeedSort
import com.example.redx.model.FeedViewMode
import com.example.redx.model.RedditPost
import com.example.redx.model.SearchContentType
import com.example.redx.model.UserProfile
import com.example.redx.theme.AmoledBackground
import com.example.redx.theme.AmoledBorder
import com.example.redx.theme.AmoledSurface
import com.example.redx.theme.AmoledSurfaceElevated
import com.example.redx.theme.FlairBackground
import com.example.redx.theme.FlairText
import com.example.redx.theme.RedditOrange
import com.example.redx.theme.TextPrimary
import com.example.redx.theme.TextSecondary
import com.example.redx.theme.TextTertiary
import com.example.redx.ui.components.AccountSheet
import com.example.redx.ui.components.AppearanceDialog
import com.example.redx.ui.components.CompactPostCard
import com.example.redx.ui.components.CommunityExplorerDialog
import com.example.redx.ui.components.CustomSubredditDialog
import com.example.redx.ui.components.FilterSettingsDialog
import com.example.redx.ui.components.GalleryPostCard
import com.example.redx.ui.components.MediaLightboxDialog
import com.example.redx.ui.components.PostCard
import com.example.redx.ui.components.PostDetailContent
import com.example.redx.ui.components.PostDetailSheet
import com.example.redx.ui.components.PostQuickActionsSheet
import com.example.redx.ui.components.RedditLoginDialog
import com.example.redx.ui.components.RelaySwipeablePostCard
import com.example.redx.ui.components.SavedPostsSheet
import com.example.redx.ui.components.SearchDialog
import com.example.redx.ui.components.SearchFilterBar
import com.example.redx.ui.components.SortBar
import com.example.redx.ui.components.SubredditBar
import com.example.redx.ui.components.SettingsSheet
import com.example.redx.ui.components.ApolloPostCard
import com.example.redx.ui.components.BigTilesPostCard
import com.example.redx.ui.components.FullBleedPostCard
import com.example.redx.ui.components.MagazinePostCard
import com.example.redx.ui.components.RelayPostCard
import com.example.redx.ui.components.SocialChatPostCard
import com.example.redx.ui.components.StreamlinePostCard
import com.example.redx.ui.components.TextOnlyPostCard
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.example.redx.ui.components.CrosspostDialog
import com.example.redx.ui.components.EdgeSubredditSwipeOverlay
import com.example.redx.ui.components.MultiSubredditPickerDialog
import com.example.redx.ui.components.ReadLaterSheet
import com.example.redx.ui.components.SwipeablePostCardWrapper
import com.example.redx.ui.components.UserProfileSheet
import com.example.redx.ui.components.ViewingStylePickerSheet
import com.example.redx.ui.components.getFeedViewModeIcon
import kotlinx.coroutines.launch

enum class TabletLayoutMode(val label: String) {
    SPLIT("Split View"),
    MAGAZINE("Magazine Grid")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedXMainScreen(
    viewModel: RedXViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val savedPosts by viewModel.savedPosts.collectAsState()
    val favoriteSubreddits by viewModel.favoriteSubreddits.collectAsState()
    val recentSubreddits by viewModel.recentSubreddits.collectAsState()
    val blockedKeywords by viewModel.blockedKeywords.collectAsState()
    val blockedDomains by viewModel.blockedDomains.collectAsState()
    val isFilterEnabled by viewModel.isFilterEnabled.collectAsState()
    val displayedSubreddits by viewModel.displayedSubreddits.collectAsState()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var tabletLayoutMode by rememberSaveable { mutableStateOf(TabletLayoutMode.SPLIT) }

    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 4 }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "refreshRotation")
    val rawRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rawRotation"
    )
    val refreshRotation = if (uiState.isLoading) rawRotation else 0f

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTabletLandscape = maxWidth >= 760.dp
        val showDetailModal = uiState.selectedPost != null && (!isTabletLandscape || tabletLayoutMode == TabletLayoutMode.MAGAZINE)

        val isLightboxOpen = uiState.lightboxMediaUrl != null || uiState.lightboxVideoUrl != null

        // Native Hardware & Gesture Back Navigation
        BackHandler(enabled = isLightboxOpen) {
            viewModel.closeLightbox()
        }
        BackHandler(enabled = !isLightboxOpen && uiState.isSearchActive) {
            viewModel.clearSearch()
        }
        BackHandler(enabled = !isLightboxOpen && !uiState.isSearchActive && showDetailModal) {
            viewModel.selectPost(null)
        }

        // Clear selected post whenever layout mode or tablet orientation changes so no unwanted modal sheet pops up
        LaunchedEffect(tabletLayoutMode) {
            viewModel.selectPost(null)
        }
        LaunchedEffect(isTabletLandscape) {
            viewModel.selectPost(null)
        }

        if (isTabletLandscape) {
            // ==========================================
            // TABLET LANDSCAPE UNIFIED REDESIGN
            // ==========================================
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = AmoledBackground,
                topBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AmoledSurface)
                    ) {
                        // Full-Width Tablet Top App Bar
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.redx_logo),
                                        contentDescription = "RedX App Logo",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "RedX",
                                        color = RedditOrange,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AmoledSurfaceElevated)
                                            .border(1.dp, RedditOrange.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "XIAOMI PAD 7",
                                            color = RedditOrange,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(RedditOrange.copy(alpha = 0.15f))
                                            .clickable { viewModel.setCommunityExplorerOpen(true) }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "r/${uiState.activeSubreddit}",
                                            color = RedditOrange,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            },
                            actions = {
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                // 1. Tablet Layout Toggle (Split View vs Magazine Grid)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AmoledSurfaceElevated)
                                        .border(1.dp, AmoledBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.selectPost(null)
                                            tabletLayoutMode = if (tabletLayoutMode == TabletLayoutMode.SPLIT) {
                                                TabletLayoutMode.MAGAZINE
                                            } else {
                                                TabletLayoutMode.SPLIT
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (tabletLayoutMode == TabletLayoutMode.SPLIT) Icons.Default.VerticalSplit else Icons.Default.Dashboard,
                                            contentDescription = "Toggle Tablet Mode",
                                            tint = RedditOrange,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = tabletLayoutMode.label,
                                            color = TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // 2. View Mode (11 Styles with Visual Picker)
                                IconButton(onClick = { viewModel.setViewStylePickerOpen(true) }) {
                                    Icon(
                                        imageVector = getFeedViewModeIcon(uiState.viewMode),
                                        contentDescription = "Switch View (${uiState.viewMode.label})",
                                        tint = RedditOrange
                                    )
                                }

                                // 3. Hide Read Posts (Boost feature)
                                IconButton(onClick = { viewModel.sweepReadPosts() }) {
                                    Icon(
                                        imageVector = if (uiState.hideReadPosts) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (uiState.hideReadPosts) "Show Read Posts" else "Hide Read Posts",
                                        tint = if (uiState.hideReadPosts) RedditOrange else TextSecondary
                                    )
                                }

                                // 4. Content Filters
                                IconButton(onClick = { viewModel.setFilterDialogOpen(true) }) {
                                    Icon(
                                        imageVector = Icons.Default.FilterAlt,
                                        contentDescription = "Content Filters",
                                        tint = if (uiState.filteredPostCount > 0) RedditOrange else TextSecondary
                                    )
                                }

                                // 5. Saved Posts
                                IconButton(onClick = { viewModel.setSavedPostsSheetOpen(true) }) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Saved Posts",
                                        tint = TextPrimary
                                    )
                                }

                                // 6. Search Reddit
                                IconButton(onClick = { viewModel.setSearchDialogOpen(true) }) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Reddit",
                                        tint = TextPrimary
                                    )
                                }

                                // 7. Subreddit Jump
                                IconButton(onClick = { viewModel.setCustomSubredditDialogOpen(true) }) {
                                    Icon(
                                        imageVector = Icons.Default.Tag,
                                        contentDescription = "Jump to Subreddit",
                                        tint = TextSecondary
                                    )
                                }

                                // 8. Appearance / Themes
                                IconButton(onClick = { viewModel.setSettingsSheetOpen(true) }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = TextSecondary
                                    )
                                }

                                // 9. Refresh Feed
                                IconButton(onClick = { viewModel.loadFeed(forceRefresh = true) }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh Feed",
                                        tint = TextPrimary,
                                        modifier = Modifier.rotate(refreshRotation)
                                    )
                                }

                                // 10. Reddit Account
                                IconButton(onClick = { viewModel.setAccountSheetOpen(true) }) {
                                    Box(contentAlignment = Alignment.TopEnd) {
                                        Icon(
                                            imageVector = Icons.Default.AccountCircle,
                                            contentDescription = "Reddit Account",
                                            tint = if (userProfile.isLoggedIn) RedditOrange else TextSecondary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                        if (userProfile.isLoggedIn) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF4CAF50))
                                            )
                                        }
                                    }
                                }
                            }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = AmoledSurface,
                                titleContentColor = TextPrimary
                            )
                        )

                        // Flair Filter Banner
                        if (uiState.activeFlairFilter != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(FlairBackground)
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Flair Filter: ", color = TextSecondary, fontSize = 12.sp)
                                    Text(text = uiState.activeFlairFilter.orEmpty(), color = FlairText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.35f))
                                        .clickable { viewModel.clearFlairFilter() }
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "Clear ✕", color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }

                        // Content Filter Banner
                        if (uiState.filteredPostCount > 0) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(RedditOrange.copy(alpha = 0.15f))
                                    .border(1.dp, RedditOrange.copy(alpha = 0.35f))
                                    .clickable { viewModel.setFilterDialogOpen(true) }
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FilterAlt,
                                        contentDescription = null,
                                        tint = RedditOrange,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${uiState.filteredPostCount} post(s) hidden by keyword/domain filters",
                                        color = RedditOrange,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "Manage",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Subreddit Bar (Full Width across tablet)
                        if (uiState.isSearchActive) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(AmoledSurface)
                                        .border(1.dp, AmoledBorder)
                                        .padding(horizontal = 14.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = RedditOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Search: \"${uiState.activeSearchQuery}\"",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Quick "Popular"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(AmoledSurfaceElevated)
                                                .border(1.dp, AmoledBorder, RoundedCornerShape(6.dp))
                                                .clickable { viewModel.clearSearch("popular", FeedSort.HOT) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Whatshot, contentDescription = null, tint = RedditOrange, modifier = Modifier.size(11.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(text = "Popular", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }

                                        // Quick "New"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(AmoledSurfaceElevated)
                                                .border(1.dp, AmoledBorder, RoundedCornerShape(6.dp))
                                                .clickable { viewModel.clearSearch("popular", FeedSort.NEW) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.NewReleases, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(text = "New", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }

                                        // Quick "Home"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(AmoledSurfaceElevated)
                                                .border(1.dp, AmoledBorder, RoundedCornerShape(6.dp))
                                                .clickable { viewModel.clearSearch("home", FeedSort.HOT) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(text = "Home", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                            }
                                        }

                                        // Clear / Exit Button
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(RedditOrange.copy(alpha = 0.2f))
                                                .border(1.dp, RedditOrange, RoundedCornerShape(6.dp))
                                                .clickable { viewModel.clearSearch() }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = RedditOrange, modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(text = "Exit", color = RedditOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                SearchFilterBar(
                                    activeFilter = uiState.searchContentType,
                                    onFilterSelected = { viewModel.setSearchContentType(it) }
                                )
                            }
                        } else {
                            SubredditBar(
                                activeSubreddit = uiState.activeSubreddit,
                                subreddits = displayedSubreddits,
                                onSubredditSelected = { sub ->
                                    viewModel.loadFeed(subreddit = sub)
                                    scope.launch { listState.scrollToItem(0) }
                                },
                                onOpenCustomPicker = { viewModel.setCustomSubredditDialogOpen(true) },
                                onOpenMultiPicker = { viewModel.setMultiSubPickerOpen(true) }
                            )
                        }

                        // Full-Width Sort Bar
                        SortBar(
                            activeSort = uiState.activeSort,
                            onSortSelected = { sort ->
                                viewModel.loadFeed(sort = sort)
                                scope.launch { listState.scrollToItem(0) }
                            }
                        )

                        if (uiState.isLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp),
                                color = RedditOrange
                            )
                        }

                        // Subtle notification banner when an error occurs but cached posts are available
                        AnimatedVisibility(
                            visible = uiState.errorMessage != null && uiState.posts.isNotEmpty(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            val errorMsg = uiState.errorMessage
                            if (errorMsg != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF2E1B17))
                                        .border(1.dp, RedditOrange.copy(alpha = 0.4f))
                                        .padding(horizontal = 14.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = RedditOrange,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = errorMsg,
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.clearError() },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss message",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                floatingActionButton = {
                    AnimatedVisibility(
                        visible = showScrollToTop,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        FloatingActionButton(
                            onClick = { scope.launch { listState.animateScrollToItem(0) } },
                            containerColor = RedditOrange,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Scroll to top"
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(AmoledBackground)
                ) {
                    if (tabletLayoutMode == TabletLayoutMode.SPLIT) {
                        // Mode 1: Split View (Master Feed on Left, Reader on Right)
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Left Column (420dp width)
                            Box(
                                modifier = Modifier
                                    .width(420.dp)
                                    .fillMaxHeight()
                                    .background(AmoledBackground)
                            ) {
                                PostFeedContent(
                                    uiState = uiState,
                                    listState = listState,
                                    selectedPostId = uiState.selectedPost?.id,
                                    isTabletMagazine = false,
                                    onPostClick = { post -> viewModel.selectPost(post) },
                                    onVote = { post, vote -> viewModel.vote(post, vote) },
                                    onToggleSave = { post -> viewModel.toggleSave(post) },
                                    onSubredditClick = { sub ->
                                        viewModel.loadFeed(subreddit = sub)
                                        scope.launch { listState.scrollToItem(0) }
                                    },
                                    onSwitchSubreddit = { direction ->
                                        if (viewModel.switchSubreddit(direction)) {
                                            scope.launch { listState.scrollToItem(0) }
                                        }
                                    },
                                    onLongClickPost = { post -> viewModel.openQuickActions(post) },
                                    onOpenLightbox = { url, title -> viewModel.openLightbox(url, title) },
                                    onFlairClick = { flair -> viewModel.setFlairFilter(flair) },
                                    onLoadMore = { viewModel.loadMorePosts() },
                                    onRefresh = { viewModel.loadFeed() },
                                    onAuthorClick = { author -> viewModel.openUserProfile(author) }
                                )
                            }

                            // Vertical Divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(AmoledBorder)
                            )

                            // Right Column (Takes remaining width)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(AmoledBackground)
                            ) {
                                val tabletDetailPost = uiState.selectedPost
                                if (tabletDetailPost != null) {
                                    androidx.compose.runtime.key(tabletDetailPost.id) {
                                        PostDetailContent(
                                            post = tabletDetailPost,
                                            onVote = { p, vote -> viewModel.vote(p, vote) },
                                            onToggleSave = { p -> viewModel.toggleSave(p) },
                                            onDismiss = { viewModel.selectPost(null) },
                                            isTabletPane = true,
                                            onOpenLightbox = { url, title -> viewModel.openLightbox(url, title) }
                                        )
                                    }
                                } else {
                                    TabletHubWelcomeView(
                                        activeSubreddit = uiState.activeSubreddit,
                                        postCount = uiState.posts.size,
                                        onOpenFirstPost = {
                                            if (uiState.posts.isNotEmpty()) {
                                                viewModel.selectPost(uiState.posts.first())
                                            }
                                        },
                                        onSubredditClick = { sub ->
                                            viewModel.loadFeed(subreddit = sub)
                                            scope.launch { listState.scrollToItem(0) }
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Mode 2: Multi-Column Magazine Cards Grid
                        PostFeedContent(
                            uiState = uiState,
                            listState = listState,
                            selectedPostId = uiState.selectedPost?.id,
                            isTabletMagazine = true,
                            onPostClick = { post -> viewModel.selectPost(post) },
                            onVote = { post, vote -> viewModel.vote(post, vote) },
                            onToggleSave = { post -> viewModel.toggleSave(post) },
                            onSubredditClick = { sub ->
                                viewModel.loadFeed(subreddit = sub)
                                scope.launch { listState.scrollToItem(0) }
                            },
                            onSwitchSubreddit = { direction ->
                                if (viewModel.switchSubreddit(direction)) {
                                    scope.launch { listState.scrollToItem(0) }
                                }
                            },
                            onLongClickPost = { post -> viewModel.openQuickActions(post) },
                            onOpenLightbox = { url, title -> viewModel.openLightbox(url, title) },
                            onFlairClick = { flair -> viewModel.setFlairFilter(flair) },
                            onLoadMore = { viewModel.loadMorePosts() },
                            onRefresh = { viewModel.loadFeed() },
                            onAuthorClick = { author -> viewModel.openUserProfile(author) }
                        )
                    }
                }
            }
        } else {
            // ==========================================
            // PHONE / PORTRAIT LAYOUT
            // ==========================================
            FeedPanel(
                uiState = uiState,
                userProfile = userProfile,
                displayedSubreddits = displayedSubreddits,
                listState = listState,
                onPostClick = { post -> viewModel.selectPost(post) },
                onVote = { post, vote -> viewModel.vote(post, vote) },
                onToggleSave = { post -> viewModel.toggleSave(post) },
                onSubredditClick = { sub ->
                    viewModel.loadFeed(subreddit = sub)
                    scope.launch { listState.scrollToItem(0) }
                },
                onSwitchSubreddit = { direction ->
                    if (viewModel.switchSubreddit(direction)) {
                        scope.launch { listState.scrollToItem(0) }
                    }
                },
                onOpenSearch = { viewModel.setSearchDialogOpen(true) },
                onOpenSubredditPicker = { viewModel.setCommunityExplorerOpen(true) },
                onOpenMultiPicker = { viewModel.setMultiSubPickerOpen(true) },
                onDisableMultiFeed = { viewModel.disableMultiFeed() },
                onAuthorClick = { author -> viewModel.openUserProfile(author) },
                onRefresh = { viewModel.loadFeed(forceRefresh = true) },
                onClearError = { viewModel.clearError() },
                onOpenAccount = { viewModel.setAccountSheetOpen(true) },
                        onOpenAppearance = { viewModel.setSettingsSheetOpen(true) },
                onOpenSaved = { viewModel.setSavedPostsSheetOpen(true) },
                onOpenFilters = { viewModel.setFilterDialogOpen(true) },
                onSweepReadPosts = { viewModel.sweepReadPosts() },
                onCycleViewMode = { viewModel.setViewStylePickerOpen(true) },
                onSelectSort = { sort ->
                    viewModel.loadFeed(sort = sort)
                    scope.launch { listState.scrollToItem(0) }
                },
                onClearSearch = { viewModel.clearSearch() },
                onRevertFeed = { sub, sort -> viewModel.clearSearch(sub, sort) },
                onSearchContentTypeSelected = { viewModel.setSearchContentType(it) },
                onOpenLightbox = { url, title -> viewModel.openLightbox(url, title) },
                onLongClickPost = { post -> viewModel.openQuickActions(post) },
                onFlairClick = { flair -> viewModel.setFlairFilter(flair) },
                onClearFlairFilter = { viewModel.clearFlairFilter() },
                onLoadMore = { viewModel.loadMorePosts() },
                showScrollToTop = showScrollToTop,
                onScrollToTop = { scope.launch { listState.animateScrollToItem(0) } },
                selectedPostId = null
            )
        }

        // Modal Sheet Reader for Phone mode OR Tablet Magazine mode
        val detailModalPost = uiState.selectedPost
        if (showDetailModal && detailModalPost != null) {
            androidx.compose.runtime.key(detailModalPost.id) {
                PostDetailSheet(
                    post = detailModalPost,
                    onDismiss = { viewModel.selectPost(null) },
                    onVote = { p, vote -> viewModel.vote(p, vote) },
                    onToggleSave = { p -> viewModel.toggleSave(p) },
                    onOpenLightbox = { url, title -> viewModel.openLightbox(url, title) }
                )
            }
        }
    }

    // Modal Overlays
    if (uiState.lightboxMediaUrl != null || uiState.lightboxVideoUrl != null) {
        MediaLightboxDialog(
            imageUrl = uiState.lightboxMediaUrl ?: uiState.lightboxVideoUrl.orEmpty(),
            title = uiState.lightboxMediaTitle,
            videoUrl = uiState.lightboxVideoUrl,
            isVideo = uiState.lightboxIsVideo,
            galleryUrls = uiState.lightboxGalleryUrls,
            onDismiss = { viewModel.closeLightbox() }
        )
    }

    val quickActionsPost = uiState.quickActionsPost
    if (quickActionsPost != null) {
        PostQuickActionsSheet(
            post = quickActionsPost,
            onDismiss = { viewModel.closeQuickActions() },
            onToggleSave = { viewModel.toggleSave(it) },
            onToggleRead = { viewModel.togglePostRead(it) },
            onAddFilterKeyword = { viewModel.addFilterKeyword(it) },
            onAddFilterDomain = { viewModel.addFilterDomain(it) },
            onAddToReadLater = { viewModel.addToReadLater(it) },
            onCrosspost = { viewModel.openCrosspostDialog(it) },
            onViewAuthor = { viewModel.openUserProfile(it) }
        )
    }

    if (uiState.isFilterDialogOpen) {
        FilterSettingsDialog(
            blockedKeywords = blockedKeywords,
            blockedDomains = blockedDomains,
            isFilterEnabled = isFilterEnabled,
            onToggleFilterEnabled = { viewModel.toggleFilterEnabled(it) },
            onAddKeyword = { viewModel.addFilterKeyword(it) },
            onRemoveKeyword = { viewModel.removeFilterKeyword(it) },
            onAddDomain = { viewModel.addFilterDomain(it) },
            onRemoveDomain = { viewModel.removeFilterDomain(it) },
            onDismiss = { viewModel.setFilterDialogOpen(false) }
        )
    }

    if (uiState.isSettingsSheetOpen) {
        SettingsSheet(
            currentTheme = uiState.appTheme,
            currentViewMode = uiState.viewMode,
            currentFontScale = uiState.fontScale,
            hideReadPosts = uiState.hideReadPosts,
            showMatureContent = userProfile.showMatureContent,
            onToggleHideRead = { viewModel.setHideReadPosts(it) },
            onToggleMature = { viewModel.toggleMatureContent(it) },
            onOpenAppearance = {
                viewModel.setSettingsSheetOpen(false)
                viewModel.setAppearanceDialogOpen(true)
            },
            onOpenFilters = {
                viewModel.setSettingsSheetOpen(false)
                viewModel.setFilterDialogOpen(true)
            },
            onOpenSaved = {
                viewModel.setSettingsSheetOpen(false)
                viewModel.setSavedPostsSheetOpen(true)
            },
            onOpenAccount = {
                viewModel.setSettingsSheetOpen(false)
                viewModel.setAccountSheetOpen(true)
            },
            onOpenCommunityExplorer = {
                viewModel.setSettingsSheetOpen(false)
                viewModel.setCommunityExplorerOpen(true)
            },
            onOpenReadLater = {
                viewModel.setSettingsSheetOpen(false)
                viewModel.setReadLaterSheetOpen(true)
            },
            onMarkAllRead = { viewModel.markAllVisibleRead() },
            onClearReadHistory = { viewModel.clearReadHistory() },
            onDismiss = { viewModel.setSettingsSheetOpen(false) }
        )
    }

    if (uiState.isAppearanceDialogOpen) {
        AppearanceDialog(
            currentTheme = uiState.appTheme,
            currentViewMode = uiState.viewMode,
            currentFontScale = uiState.fontScale,
            onThemeChange = { viewModel.setAppTheme(it) },
            onViewModeChange = { viewModel.setViewMode(it) },
            onFontScaleChange = { viewModel.setFontScale(it) },
            onDismiss = { viewModel.setAppearanceDialogOpen(false) }
        )
    }

    if (uiState.isViewStylePickerOpen) {
        ViewingStylePickerSheet(
            currentMode = uiState.viewMode,
            onModeSelect = { viewModel.setViewMode(it) },
            onDismiss = { viewModel.setViewStylePickerOpen(false) }
        )
    }

    if (uiState.isSavedPostsSheetOpen) {
        SavedPostsSheet(
            savedPosts = savedPosts,
            onDismiss = { viewModel.setSavedPostsSheetOpen(false) },
            onPostClick = { post -> viewModel.selectPost(post) },
            onToggleSave = { post -> viewModel.toggleSave(post) }
        )
    }

    if (uiState.isSearchDialogOpen) {
        SearchDialog(
            currentSubreddit = uiState.activeSubreddit,
            onDismiss = { viewModel.setSearchDialogOpen(false) },
            onExecuteSearch = { query, inSub, mature, sort ->
                viewModel.executeSearch(query, inSub, mature, sort)
                scope.launch { listState.scrollToItem(0) }
            }
        )
    }

    if (uiState.isLoginDialogOpen) {
        RedditLoginDialog(
            onDismiss = { viewModel.setLoginDialogOpen(false) },
            onLoginDetected = { viewModel.onLoginDetected() }
        )
    }

    if (uiState.isAccountSheetOpen) {
        AccountSheet(
            userProfile = userProfile,
            onDismiss = { viewModel.setAccountSheetOpen(false) },
            onOpenLogin = { viewModel.setLoginDialogOpen(true) },
            onLogout = { viewModel.logout() },
            onSelectHomeFeed = {
                viewModel.loadFeed(subreddit = "home")
                scope.launch { listState.scrollToItem(0) }
            },
            onSelectSubreddit = { sub ->
                viewModel.loadFeed(subreddit = sub)
                scope.launch { listState.scrollToItem(0) }
            }
        )
    }

    if (uiState.isCustomSubredditDialogOpen) {
        CustomSubredditDialog(
            onDismiss = { viewModel.setCustomSubredditDialogOpen(false) },
            favoriteSubreddits = favoriteSubreddits,
            userSubreddits = displayedSubreddits,
            onToggleFavorite = { viewModel.toggleFavoriteSubreddit(it) },
            onSubredditSelected = { sub ->
                viewModel.loadFeed(subreddit = sub)
                scope.launch { listState.scrollToItem(0) }
            }
        )
    }

    if (uiState.isCommunityExplorerOpen) {
        CommunityExplorerDialog(
            onDismiss = { viewModel.setCommunityExplorerOpen(false) },
            onSubredditSelected = { sub ->
                viewModel.loadFeed(subreddit = sub)
                scope.launch { listState.scrollToItem(0) }
            },
            recentSubreddits = recentSubreddits,
            favoriteSubreddits = favoriteSubreddits,
            userSubreddits = displayedSubreddits,
            onToggleFavorite = { viewModel.toggleFavoriteSubreddit(it) },
            onClearRecents = { viewModel.clearRecentSubreddits() }
        )
    }

    val readLaterQueue by viewModel.readLaterManager.queue.collectAsState()
    if (uiState.isReadLaterSheetOpen) {
        ReadLaterSheet(
            queue = readLaterQueue,
            onRemove = { viewModel.removeFromReadLater(it) },
            onClearAll = { viewModel.readLaterManager.clearQueue() },
            onDismiss = { viewModel.setReadLaterSheetOpen(false) }
        )
    }

    val viewedUserName = uiState.viewedUserName
    if (uiState.isUserProfileOpen && viewedUserName != null) {
        UserProfileSheet(
            username = viewedUserName,
            posts = uiState.userProfilePosts,
            isLoading = uiState.isUserProfileLoading,
            onPostClick = { post ->
                viewModel.closeUserProfile()
                viewModel.selectPost(post)
            },
            onDismiss = { viewModel.closeUserProfile() }
        )
    }

    val crosspostTargetPost = uiState.crosspostTargetPost
    if (uiState.isCrosspostDialogOpen && crosspostTargetPost != null) {
        CrosspostDialog(
            post = crosspostTargetPost,
            suggestedSubreddits = displayedSubreddits,
            onConfirm = { targetSub, title ->
                viewModel.submitCrosspost(crosspostTargetPost, targetSub, title)
            },
            onDismiss = { viewModel.closeCrosspostDialog() }
        )
    }

    if (uiState.isMultiSubPickerOpen) {
        MultiSubredditPickerDialog(
            availableSubreddits = displayedSubreddits,
            currentMultiSubs = uiState.multiSubreddits,
            onConfirm = { subs -> viewModel.enableMultiFeed(subs) },
            onDismiss = { viewModel.setMultiSubPickerOpen(false) }
        )
    }
}

@Composable
private fun TabletHubWelcomeView(
    activeSubreddit: String,
    postCount: Int,
    onOpenFirstPost: () -> Unit,
    onSubredditClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(RedditOrange.copy(alpha = 0.15f))
                .border(2.dp, RedditOrange, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = RedditOrange,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Xiaomi Pad 7 Command Center",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Browsing r/$activeSubreddit • $postCount stories ready in Full HD",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "TRENDING SUBREDDIT JUMP",
            color = TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val quickSubs = listOf("technology", "gaming", "science", "worldnews", "memes")
            quickSubs.forEach { sub ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmoledSurfaceElevated)
                        .border(1.dp, AmoledBorder, RoundedCornerShape(8.dp))
                        .clickable { onSubredditClick(sub) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "r/$sub",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (postCount > 0) {
            Button(
                onClick = onOpenFirstPost,
                colors = ButtonDefaults.buttonColors(containerColor = RedditOrange),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open Top Story in Split View",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PostFeedContent(
    uiState: RedXUiState,
    listState: LazyListState,
    selectedPostId: String?,
    isTabletMagazine: Boolean = false,
    onPostClick: (RedditPost) -> Unit,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onSubredditClick: (String) -> Unit,
    onSwitchSubreddit: (Int) -> Unit = {},
    onLongClickPost: (RedditPost) -> Unit,
    onOpenLightbox: (String, String) -> Unit,
    onFlairClick: (String) -> Unit,
    onLoadMore: () -> Unit,
    onRefresh: () -> Unit,
    onAuthorClick: (String) -> Unit = {}
) {
    val galleryGridState = rememberLazyGridState()
    val tabletGridState = rememberLazyGridState()

    // Continuous auto-loader for standard feed
    val shouldLoadMoreList by remember(uiState.posts.size, uiState.isLoading, uiState.isLoadingMore, uiState.canLoadMore) {
        derivedStateOf {
            if (uiState.isLoading || uiState.isLoadingMore || !uiState.canLoadMore || uiState.posts.isEmpty()) return@derivedStateOf false
            val layoutInfo = listState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 5
        }
    }
    LaunchedEffect(shouldLoadMoreList) {
        if (shouldLoadMoreList) {
            onLoadMore()
        }
    }

    // Continuous auto-loader for gallery grid
    val shouldLoadMoreGallery by remember(uiState.posts.size, uiState.isLoading, uiState.isLoadingMore, uiState.canLoadMore) {
        derivedStateOf {
            if (uiState.isLoading || uiState.isLoadingMore || !uiState.canLoadMore || uiState.posts.isEmpty()) return@derivedStateOf false
            val layoutInfo = galleryGridState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 6
        }
    }
    LaunchedEffect(shouldLoadMoreGallery) {
        if (shouldLoadMoreGallery) {
            onLoadMore()
        }
    }

    // Continuous auto-loader for tablet grid
    val shouldLoadMoreTablet by remember(uiState.posts.size, uiState.isLoading, uiState.isLoadingMore, uiState.canLoadMore) {
        derivedStateOf {
            if (uiState.isLoading || uiState.isLoadingMore || !uiState.canLoadMore || uiState.posts.isEmpty()) return@derivedStateOf false
            val layoutInfo = tabletGridState.layoutInfo
            val total = layoutInfo.totalItemsCount
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 5
        }
    }
    LaunchedEffect(shouldLoadMoreTablet) {
        if (shouldLoadMoreTablet) {
            onLoadMore()
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        when {
        uiState.isLoading && uiState.posts.isEmpty() -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.redx_logo),
                    contentDescription = "RedX App Logo",
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, RedditOrange.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "RedX",
                    color = RedditOrange,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (uiState.isSearchActive) "Searching Reddit for \"${uiState.activeSearchQuery}\"..." else "Loading r/${uiState.activeSubreddit} in Full HD...",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(18.dp))
                CircularProgressIndicator(
                    color = RedditOrange,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            }
        }

        (uiState.errorMessage != null || uiState.emptyStateMessage != null) && uiState.posts.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when {
                        uiState.emptyStateMessage != null && uiState.isSearchActive -> "No Matching Content"
                        uiState.emptyStateMessage != null -> "Nothing to show"
                        else -> "Unable to load feed"
                    },
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.emptyStateMessage ?: uiState.errorMessage.orEmpty(),
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (!uiState.isSearchActive) {
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(containerColor = RedditOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Try Again")
                    }
                }
            }
        }

        else -> {
            AnimatedContent(
                targetState = Pair(uiState.viewMode, isTabletMagazine),
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220)) + scaleIn(initialScale = 0.98f, animationSpec = tween(220))) togetherWith
                            fadeOut(animationSpec = tween(180))
                },
                label = "feedViewTransition"
            ) { (currentViewMode, currentTabletMagazine) ->
                if (currentViewMode == FeedViewMode.GALLERY) {
                    LazyVerticalGrid(
                        state = galleryGridState,
                        columns = GridCells.Adaptive(minSize = 130.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        items(
                            items = uiState.posts,
                            key = { post -> post.id }
                        ) { post ->
                            GalleryPostCard(
                                post = post,
                                isSelected = (selectedPostId == post.id),
                                onPostClick = onPostClick,
                                onLongClick = onLongClickPost,
                                onMediaClick = onOpenLightbox
                            )
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isLoadingMore) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(color = RedditOrange, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = "Loading more posts...", color = TextSecondary, fontSize = 13.sp)
                                    }
                                } else if (!uiState.canLoadMore) {
                                    Text(text = "• All caught up •", color = TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 20.dp))
                                } else {
                                    LaunchedEffect(Unit) { onLoadMore() }
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }
                        }
                    }
                } else if (currentTabletMagazine) {
                    // Multi-Column Responsive Grid across wide tablet screen
                    LazyVerticalGrid(
                        state = tabletGridState,
                        columns = GridCells.Adaptive(minSize = 360.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.posts,
                            key = { post -> post.id }
                        ) { post ->
                            PostFeedItemRenderer(
                                viewMode = currentViewMode,
                                post = post,
                                selectedPostId = selectedPostId,
                                onPostClick = onPostClick,
                                onVote = onVote,
                                onToggleSave = onToggleSave,
                                onSubredditClick = onSubredditClick,
                                onLongClickPost = onLongClickPost,
                                onOpenLightbox = onOpenLightbox,
                                onFlairClick = onFlairClick,
                                onAuthorClick = onAuthorClick
                            )
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isLoadingMore) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(color = RedditOrange, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = "Loading more posts...", color = TextSecondary, fontSize = 13.sp)
                                    }
                                } else if (!uiState.canLoadMore) {
                                    Text(text = "• All caught up •", color = TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 20.dp))
                                } else {
                                    LaunchedEffect(Unit) { onLoadMore() }
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = uiState.posts,
                            key = { post -> post.id }
                        ) { post ->
                            Box(
                                modifier = Modifier.animateItem()
                            ) {
                                PostFeedItemRenderer(
                                    viewMode = currentViewMode,
                                    post = post,
                                    selectedPostId = selectedPostId,
                                    onPostClick = onPostClick,
                                    onVote = onVote,
                                    onToggleSave = onToggleSave,
                                    onSubredditClick = onSubredditClick,
                                    onLongClickPost = onLongClickPost,
                                    onOpenLightbox = onOpenLightbox,
                                    onFlairClick = onFlairClick,
                                    onAuthorClick = onAuthorClick
                                )
                            }
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.isLoadingMore) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(color = RedditOrange, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = "Loading more posts...", color = TextSecondary, fontSize = 13.sp)
                                    }
                                } else if (!uiState.canLoadMore) {
                                    Text(text = "• All caught up •", color = TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 20.dp))
                                } else {
                                    LaunchedEffect(Unit) { onLoadMore() }
                                    Spacer(modifier = Modifier.height(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        }
        EdgeSubredditSwipeOverlay(
            enabled = !uiState.isSearchActive && !uiState.multiSubredditMode,
            onSwipe = onSwitchSubreddit
        )
    }
}

@Composable
private fun PostFeedItemRenderer(
    viewMode: FeedViewMode,
    post: RedditPost,
    selectedPostId: String?,
    onPostClick: (RedditPost) -> Unit,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onSubredditClick: (String) -> Unit,
    onLongClickPost: (RedditPost) -> Unit,
    onOpenLightbox: (String, String) -> Unit,
    onFlairClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit = {}
) {
    val isSelected = (selectedPostId == post.id)
    when (viewMode) {
        FeedViewMode.CARDS -> {
            RelaySwipeablePostCard(
                post = post,
                onVote = onVote,
                onToggleSave = onToggleSave,
                onCommentsClick = onPostClick,
                onMoreClick = onLongClickPost
            ) {
                PostCard(
                    post = post,
                    isSelected = isSelected,
                    onPostClick = onPostClick,
                    onVote = onVote,
                    onToggleSave = onToggleSave,
                    onSubredditClick = onSubredditClick,
                    onLongClick = onLongClickPost,
                    onMediaClick = onOpenLightbox,
                    onFlairClick = onFlairClick,
                    onAuthorClick = onAuthorClick
                )
            }
        }
        FeedViewMode.COMPACT -> {
            SwipeablePostCardWrapper(
                post = post,
                onSwipeUpvote = { onVote(it, 1) },
                onSwipeToggleSave = onToggleSave
            ) {
                CompactPostCard(
                    post = post,
                    isSelected = isSelected,
                    onPostClick = onPostClick,
                    onVote = onVote,
                    onToggleSave = onToggleSave,
                    onSubredditClick = onSubredditClick,
                    onLongClick = onLongClickPost,
                    onMediaClick = onOpenLightbox,
                    onFlairClick = onFlairClick,
                    onAuthorClick = onAuthorClick
                )
            }
        }
        FeedViewMode.GALLERY -> {
            GalleryPostCard(
                post = post,
                isSelected = isSelected,
                onPostClick = onPostClick,
                onLongClick = onLongClickPost,
                onMediaClick = onOpenLightbox
            )
        }
        FeedViewMode.RELAY -> {
            SwipeablePostCardWrapper(
                post = post,
                onSwipeUpvote = { onVote(it, 1) },
                onSwipeToggleSave = onToggleSave
            ) {
                RelayPostCard(
                    post = post,
                    isSelected = isSelected,
                    onPostClick = onPostClick,
                    onVote = onVote,
                    onToggleSave = onToggleSave,
                    onSubredditClick = onSubredditClick,
                    onLongClick = onLongClickPost,
                    onMediaClick = onOpenLightbox,
                    onFlairClick = onFlairClick
                )
            }
        }
        FeedViewMode.APOLLO -> {
            SwipeablePostCardWrapper(
                post = post,
                onSwipeUpvote = { onVote(it, 1) },
                onSwipeToggleSave = onToggleSave
            ) {
                ApolloPostCard(
                    post = post,
                    isSelected = isSelected,
                    onPostClick = onPostClick,
                    onVote = onVote,
                    onToggleSave = onToggleSave,
                    onSubredditClick = onSubredditClick,
                    onLongClick = onLongClickPost,
                    onMediaClick = onOpenLightbox,
                    onFlairClick = onFlairClick
                )
            }
        }
        FeedViewMode.FULL_BLEED -> {
            SwipeablePostCardWrapper(
                post = post,
                onSwipeUpvote = { onVote(it, 1) },
                onSwipeToggleSave = onToggleSave
            ) {
                FullBleedPostCard(
                    post = post,
                    isSelected = isSelected,
                    onPostClick = onPostClick,
                    onVote = onVote,
                    onToggleSave = onToggleSave,
                    onSubredditClick = onSubredditClick,
                    onLongClick = onLongClickPost,
                    onMediaClick = onOpenLightbox,
                    onFlairClick = onFlairClick
                )
            }
        }
        FeedViewMode.TEXT_ONLY -> {
            SwipeablePostCardWrapper(
                post = post,
                onSwipeUpvote = { onVote(it, 1) },
                onSwipeToggleSave = onToggleSave
            ) {
                TextOnlyPostCard(
                    post = post,
                    isSelected = isSelected,
                    onPostClick = onPostClick,
                    onVote = onVote,
                    onToggleSave = onToggleSave,
                    onSubredditClick = onSubredditClick,
                    onLongClick = onLongClickPost,
                    onMediaClick = onOpenLightbox,
                    onFlairClick = onFlairClick
                )
            }
        }
        FeedViewMode.MAGAZINE -> {
            SwipeablePostCardWrapper(
                post = post,
                onSwipeUpvote = { onVote(it, 1) },
                onSwipeToggleSave = onToggleSave
            ) {
                MagazinePostCard(
                    post = post,
                    isSelected = isSelected,
                    onPostClick = onPostClick,
                    onVote = onVote,
                    onToggleSave = onToggleSave,
                    onSubredditClick = onSubredditClick,
                    onLongClick = onLongClickPost,
                    onMediaClick = onOpenLightbox,
                    onFlairClick = onFlairClick
                )
            }
        }
        FeedViewMode.BIG_TILES -> {
            SwipeablePostCardWrapper(
                post = post,
                onSwipeUpvote = { onVote(it, 1) },
                onSwipeToggleSave = onToggleSave
            ) {
                BigTilesPostCard(
                    post = post,
                    isSelected = isSelected,
                    onPostClick = onPostClick,
                    onVote = onVote,
                    onToggleSave = onToggleSave,
                    onSubredditClick = onSubredditClick,
                    onLongClick = onLongClickPost,
                    onMediaClick = onOpenLightbox,
                    onFlairClick = onFlairClick
                )
            }
        }
        FeedViewMode.STREAMLINE -> {
            SwipeablePostCardWrapper(
                post = post,
                onSwipeUpvote = { onVote(it, 1) },
                onSwipeToggleSave = onToggleSave
            ) {
                StreamlinePostCard(
                    post = post,
                    isSelected = isSelected,
                    onPostClick = onPostClick,
                    onVote = onVote,
                    onToggleSave = onToggleSave,
                    onSubredditClick = onSubredditClick,
                    onLongClick = onLongClickPost,
                    onMediaClick = onOpenLightbox,
                    onFlairClick = onFlairClick
                )
            }
        }
        FeedViewMode.SOCIAL_CHAT -> {
            SwipeablePostCardWrapper(
                post = post,
                onSwipeUpvote = { onVote(it, 1) },
                onSwipeToggleSave = onToggleSave
            ) {
                SocialChatPostCard(
                    post = post,
                    isSelected = isSelected,
                    onPostClick = onPostClick,
                    onVote = onVote,
                    onToggleSave = onToggleSave,
                    onSubredditClick = onSubredditClick,
                    onLongClick = onLongClickPost,
                    onMediaClick = onOpenLightbox,
                    onFlairClick = onFlairClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedPanel(
    uiState: RedXUiState,
    userProfile: UserProfile,
    listState: LazyListState,
    onPostClick: (RedditPost) -> Unit,
    onVote: (RedditPost, Int) -> Unit,
    onToggleSave: (RedditPost) -> Unit,
    onSubredditClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    displayedSubreddits: List<String> = com.example.redx.ui.components.DEFAULT_SUBREDDITS,
    onSwitchSubreddit: (Int) -> Unit = {},
    onOpenSearch: () -> Unit,
    onOpenSubredditPicker: () -> Unit,
    onRefresh: () -> Unit,
    onClearError: () -> Unit = {},
    onOpenAccount: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenSaved: () -> Unit,
    onOpenFilters: () -> Unit,
    onSweepReadPosts: () -> Unit,
    onCycleViewMode: () -> Unit,
    onSelectSort: (FeedSort) -> Unit,
    onClearSearch: () -> Unit,
    onRevertFeed: (String, FeedSort) -> Unit = { _, _ -> },
    onSearchContentTypeSelected: (SearchContentType) -> Unit = {},
    onOpenLightbox: (String, String) -> Unit,
    onLongClickPost: (RedditPost) -> Unit,
    onFlairClick: (String) -> Unit,
    onClearFlairFilter: () -> Unit,
    onLoadMore: () -> Unit,
    showScrollToTop: Boolean,
    onScrollToTop: () -> Unit,
    selectedPostId: String?,
    onOpenMultiPicker: () -> Unit = {},
    onDisableMultiFeed: () -> Unit = {},
    onAuthorClick: (String) -> Unit = {}
) {
    val phoneInfiniteTransition = rememberInfiniteTransition(label = "phoneRefreshRotation")
    val phoneRawRotation by phoneInfiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phoneRawRotation"
    )
    val refreshRotation = if (uiState.isLoading) phoneRawRotation else 0f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AmoledBackground,
        topBar = {
            Column {
                // Phone Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(AmoledSurface)
                        .padding(start = 12.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onOpenAppearance() }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.redx_logo),
                            contentDescription = "RedX App Logo",
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(7.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RedX",
                            color = RedditOrange,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Only the highest-frequency actions stay on the bar; everything else
                    // moves into an overflow menu. The previous horizontally scrolling row
                    // pushed 6 of 9 actions off-screen on ordinary phone widths with no
                    // visual hint that they existed.
                    var isOverflowMenuOpen by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onOpenSearch) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Reddit",
                                tint = TextPrimary
                            )
                        }

                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Feed",
                                tint = TextPrimary,
                                modifier = Modifier.rotate(refreshRotation)
                            )
                        }

                        IconButton(onClick = onOpenAccount) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Reddit Account",
                                    tint = if (userProfile.isLoggedIn) RedditOrange else TextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                                if (userProfile.isLoggedIn) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50))
                                    )
                                }
                            }
                        }

                        Box {
                            IconButton(onClick = { isOverflowMenuOpen = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More actions",
                                    tint = TextPrimary
                                )
                            }
                            DropdownMenu(
                                expanded = isOverflowMenuOpen,
                                onDismissRequest = { isOverflowMenuOpen = false },
                                containerColor = AmoledSurfaceElevated,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, AmoledBorder)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("View: ${uiState.viewMode.label}", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = getFeedViewModeIcon(uiState.viewMode),
                                            contentDescription = null,
                                            tint = RedditOrange
                                        )
                                    },
                                    onClick = {
                                        isOverflowMenuOpen = false
                                        onCycleViewMode()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (uiState.hideReadPosts) "Show read posts" else "Hide read posts",
                                            color = TextPrimary
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (uiState.hideReadPosts) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = if (uiState.hideReadPosts) RedditOrange else TextSecondary
                                        )
                                    },
                                    onClick = {
                                        isOverflowMenuOpen = false
                                        onSweepReadPosts()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Content filters", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.FilterAlt,
                                            contentDescription = null,
                                            tint = if (uiState.filteredPostCount > 0) RedditOrange else TextSecondary
                                        )
                                    },
                                    onClick = {
                                        isOverflowMenuOpen = false
                                        onOpenFilters()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Saved posts", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = TextSecondary
                                        )
                                    },
                                    onClick = {
                                        isOverflowMenuOpen = false
                                        onOpenSaved()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Jump to subreddit", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Tag,
                                            contentDescription = null,
                                            tint = TextSecondary
                                        )
                                    },
                                    onClick = {
                                        isOverflowMenuOpen = false
                                        onOpenSubredditPicker()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings", color = TextPrimary) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null,
                                            tint = TextSecondary
                                        )
                                    },
                                    onClick = {
                                        isOverflowMenuOpen = false
                                        onOpenAppearance()
                                    }
                                )
                            }
                        }
                    }
                }

                // Inline Dismissible Rate-Limit or Network Error Banner
                AnimatedVisibility(
                    visible = uiState.errorMessage != null && uiState.posts.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    val errorMsg = uiState.errorMessage
                    if (errorMsg != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2C1010))
                                .border(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5252),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = errorMsg,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onClearError,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss message",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                if (uiState.multiSubredditMode && uiState.multiSubreddits.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RedditOrange.copy(alpha = 0.2f))
                            .border(1.dp, RedditOrange.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DynamicFeed,
                                contentDescription = null,
                                tint = RedditOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Multi-Feed: ", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                text = uiState.multiSubreddits.joinToString(" + "),
                                color = RedditOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .clickable { onDisableMultiFeed() }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Exit ✕", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                if (uiState.activeFlairFilter != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FlairBackground)
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Flair Filter: ", color = TextSecondary, fontSize = 12.sp)
                            Text(text = uiState.activeFlairFilter, color = FlairText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .clickable { onClearFlairFilter() }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Clear ✕", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }

                if (uiState.filteredPostCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RedditOrange.copy(alpha = 0.15f))
                            .border(1.dp, RedditOrange.copy(alpha = 0.35f))
                            .clickable { onOpenFilters() }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FilterAlt,
                                contentDescription = null,
                                tint = RedditOrange,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${uiState.filteredPostCount} post(s) hidden by keyword/domain filters",
                                color = RedditOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "Manage",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (uiState.isSearchActive) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AmoledSurface)
                                .border(1.dp, AmoledBorder)
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f, fill = false)) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = RedditOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Search: \"${uiState.activeSearchQuery}\"",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                // Quick "Popular"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AmoledSurfaceElevated)
                                        .border(1.dp, AmoledBorder, RoundedCornerShape(6.dp))
                                        .clickable { onRevertFeed("popular", FeedSort.HOT) }
                                        .padding(horizontal = 7.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Whatshot, contentDescription = null, tint = RedditOrange, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = "Popular", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }

                                // Quick "New"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AmoledSurfaceElevated)
                                        .border(1.dp, AmoledBorder, RoundedCornerShape(6.dp))
                                        .clickable { onRevertFeed("popular", FeedSort.NEW) }
                                        .padding(horizontal = 7.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.NewReleases, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = "New", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }

                                // Quick "Home"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AmoledSurfaceElevated)
                                        .border(1.dp, AmoledBorder, RoundedCornerShape(6.dp))
                                        .clickable { onRevertFeed("home", FeedSort.HOT) }
                                        .padding(horizontal = 7.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(text = "Home", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }

                                // Clear / Exit Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(RedditOrange.copy(alpha = 0.2f))
                                        .border(1.dp, RedditOrange, RoundedCornerShape(6.dp))
                                        .clickable { onClearSearch() }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = RedditOrange, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(text = "Exit", color = RedditOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        SearchFilterBar(
                            activeFilter = uiState.searchContentType,
                            onFilterSelected = onSearchContentTypeSelected
                        )
                    }
                } else {
                    SubredditBar(
                        activeSubreddit = uiState.activeSubreddit,
                        subreddits = displayedSubreddits,
                        onSubredditSelected = onSubredditClick,
                        onOpenCustomPicker = onOpenSubredditPicker,
                        onOpenMultiPicker = onOpenMultiPicker
                    )
                }

                SortBar(
                    activeSort = uiState.activeSort,
                    onSortSelected = onSelectSort
                )

                if (uiState.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = RedditOrange
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FloatingActionButton(
                    onClick = onScrollToTop,
                    containerColor = RedditOrange,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Scroll to top"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AmoledBackground)
        ) {
            PostFeedContent(
                uiState = uiState,
                listState = listState,
                selectedPostId = selectedPostId,
                isTabletMagazine = false,
                onPostClick = onPostClick,
                onVote = onVote,
                onToggleSave = onToggleSave,
                onSubredditClick = onSubredditClick,
                onSwitchSubreddit = onSwitchSubreddit,
                onLongClickPost = onLongClickPost,
                onOpenLightbox = onOpenLightbox,
                onFlairClick = onFlairClick,
                onLoadMore = onLoadMore,
                onRefresh = onRefresh,
                onAuthorClick = onAuthorClick
            )
        }
    }
}
