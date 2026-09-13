package com.example.redx.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.redx.auth.RedditAccountManager
import com.example.redx.data.ContentFilterManager
import com.example.redx.data.ReadLaterManager
import com.example.redx.data.ReadPostsManager
import com.example.redx.data.RecentSubredditsManager
import com.example.redx.data.SavedPostsManager
import com.example.redx.model.AppTheme
import com.example.redx.model.FeedSort
import com.example.redx.model.FeedViewMode
import com.example.redx.model.FontScale
import com.example.redx.model.RedditPost
import com.example.redx.model.SearchContentType
import com.example.redx.model.UserProfile
import com.example.redx.network.RedditFeedService
import com.example.redx.network.RedditPostActionService
import com.example.redx.network.RedditUserService
import com.example.redx.ui.components.DEFAULT_SUBREDDITS
import com.example.redx.util.RedditInputValidator
import com.example.redx.util.RedXLogger
import com.example.redx.util.UrlSafety
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RedXUiState(
    val activeSubreddit: String = "popular",
    val activeSort: FeedSort = FeedSort.HOT,
    val posts: List<RedditPost> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val emptyStateMessage: String? = null,
    val selectedPost: RedditPost? = null,
    val isAccountSheetOpen: Boolean = false,
    val isLoginDialogOpen: Boolean = false,
    val isCustomSubredditDialogOpen: Boolean = false,
    val isCommunityExplorerOpen: Boolean = false,
    val isSearchDialogOpen: Boolean = false,
    val isAppearanceDialogOpen: Boolean = false,
    val isSettingsSheetOpen: Boolean = false,
    val isSavedPostsSheetOpen: Boolean = false,
    val isFilterDialogOpen: Boolean = false,
    val isViewStylePickerOpen: Boolean = false,
    val lightboxMediaUrl: String? = null,
    val lightboxMediaTitle: String = "",
    val lightboxVideoUrl: String? = null,
    val lightboxIsVideo: Boolean = false,
    val lightboxGalleryUrls: List<String> = emptyList(),
    val quickActionsPost: RedditPost? = null,
    val hideReadPosts: Boolean = false,
    val activeFlairFilter: String? = null,
    val filteredPostCount: Int = 0,
    val isSearchActive: Boolean = false,
    val activeSearchQuery: String = "",
    val searchContentType: SearchContentType = SearchContentType.ALL,
    val viewMode: FeedViewMode = FeedViewMode.CARDS,
    val appTheme: AppTheme = AppTheme.AMOLED_BLACK,
    val fontScale: FontScale = FontScale.NORMAL,
    // Multi-subreddit
    val multiSubredditMode: Boolean = false,
    val multiSubreddits: List<String> = emptyList(),
    val isMultiSubPickerOpen: Boolean = false,
    // User profile viewer
    val viewedUserName: String? = null,
    val isUserProfileOpen: Boolean = false,
    val userProfilePosts: List<RedditPost> = emptyList(),
    val isUserProfileLoading: Boolean = false,
    // Read later
    val isReadLaterSheetOpen: Boolean = false,
    // Crosspost
    val crosspostTargetPost: RedditPost? = null,
    val isCrosspostDialogOpen: Boolean = false
)

class RedXViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("redx_settings", Context.MODE_PRIVATE)

    val accountManager = RedditAccountManager(application)
    val savedPostsManager = SavedPostsManager(application)
    val readPostsManager = ReadPostsManager(application)
    val contentFilterManager = ContentFilterManager(application)
    val recentSubredditsManager = RecentSubredditsManager(application)
    val readLaterManager = ReadLaterManager(application)

    val userProfile: StateFlow<UserProfile> = accountManager.userProfile
    val savedPosts: StateFlow<List<RedditPost>> = savedPostsManager.savedPosts
    val favoriteSubreddits: StateFlow<List<String>> = savedPostsManager.favoriteSubreddits
    val recentSubreddits: StateFlow<List<String>> = recentSubredditsManager.recentSubreddits
    val readPostIds: StateFlow<Set<String>> = readPostsManager.readPostIds
    val blockedKeywords: StateFlow<List<String>> = contentFilterManager.blockedKeywords
    val blockedDomains: StateFlow<List<String>> = contentFilterManager.blockedDomains
    val isFilterEnabled: StateFlow<Boolean> = contentFilterManager.isFilterEnabled

    val displayedSubreddits: StateFlow<List<String>> = combine(
        userProfile,
        favoriteSubreddits
    ) { profile, favorites ->
        if (profile.isLoggedIn) {
            val userSubs = profile.subscribedSubreddits.orEmpty()
                .filterNot { it.equals("home", true) || it.equals("popular", true) || it.equals("all", true) }
            val extraSubs = if (userSubs.isEmpty() && favorites.isNotEmpty()) favorites else emptyList()
            val combined = (listOf("home", "popular", "all") + userSubs + extraSubs).distinctBy(String::lowercase)
            if (combined.size > 3) {
                combined
            } else {
                (listOf("home", "popular", "all") + DEFAULT_SUBREDDITS.filterNot { it.equals("home", true) || it.equals("popular", true) || it.equals("all", true) }).distinctBy(String::lowercase)
            }
        } else {
            DEFAULT_SUBREDDITS
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = if (accountManager.userProfile.value.isLoggedIn) {
            val initialSubs = accountManager.userProfile.value.subscribedSubreddits.orEmpty()
                .filterNot { it.equals("home", true) || it.equals("popular", true) || it.equals("all", true) }
            (listOf("home", "popular", "all") + initialSubs).distinctBy(String::lowercase)
        } else {
            DEFAULT_SUBREDDITS
        }
    )

    private val _uiState = MutableStateFlow(
        RedXUiState(
            activeSubreddit = if (accountManager.userProfile.value.isLoggedIn) "home" else "popular",
            viewMode = prefs.getString("key_view_mode", null)?.let { name ->
                try { FeedViewMode.valueOf(name) } catch (e: Exception) { FeedViewMode.CARDS }
            } ?: FeedViewMode.CARDS,
            appTheme = prefs.getString("key_app_theme", null)?.let { name ->
                try { AppTheme.valueOf(name) } catch (e: Exception) { AppTheme.AMOLED_BLACK }
            } ?: AppTheme.AMOLED_BLACK,
            fontScale = prefs.getString("key_font_scale", null)?.let { name ->
                try { FontScale.valueOf(name) } catch (e: Exception) { FontScale.NORMAL }
            } ?: FontScale.NORMAL,
            hideReadPosts = prefs.getBoolean("key_hide_read_posts", false)
        )
    )
    val uiState: StateFlow<RedXUiState> = _uiState.asStateFlow()

    private var rawFetchedPosts: List<RedditPost> = emptyList()
    private var previousSubredditBeforeSearch: String = "popular"
    private var previousSortBeforeSearch: FeedSort = FeedSort.HOT
    private var previousPostsBeforeSearch: List<RedditPost> = emptyList()
    private var requestGeneration = 0L
    private var userProfileRequestGeneration = 0L
    private var postActionGeneration = 0L
    private val latestVoteRequest = mutableMapOf<String, Long>()
    private val latestSaveRequest = mutableMapOf<String, Long>()

    init {
        val isAlreadyLoggedIn = accountManager.userProfile.value.isLoggedIn
        val initialSub = if (isAlreadyLoggedIn) "home" else "popular"
        viewModelScope.launch {
            accountManager.refreshAuthenticatedAccount()
        }
        loadFeed(subreddit = initialSub, sort = FeedSort.HOT)
    }

    private fun filterAndMapPosts(rawList: List<RedditPost>): Pair<List<RedditPost>, Int> {
        val totalCount = rawList.size
        val allowedPosts = rawList.filter { !contentFilterManager.shouldFilterPost(it) }
        val filteredOutCount = totalCount - allowedPosts.size

        var displayList = allowedPosts

        // Infinity for Reddit: Post Flair Filtering
        val flairFilter = _uiState.value.activeFlairFilter
        if (!flairFilter.isNullOrBlank()) {
            displayList = displayList.filter {
                it.flair?.equals(flairFilter, ignoreCase = true) == true
            }
        }

        // Boost for Reddit: Hide Read Posts
        if (_uiState.value.hideReadPosts) {
            displayList = displayList.filter { !readPostsManager.isPostRead(it.id) }
        }

        // Search Content Type Filter (All, Images, Videos & GIFs, Discussions, Links)
        if (_uiState.value.isSearchActive) {
            displayList = when (_uiState.value.searchContentType) {
                SearchContentType.ALL -> displayList
                SearchContentType.IMAGES -> displayList.filter {
                    !it.isMediaVideo &&
                    it.videoUrl.isNullOrBlank() &&
                    (!it.previewImageUrl.isNullOrBlank() ||
                     it.contentUrl.endsWith(".jpg", ignoreCase = true) ||
                     it.contentUrl.endsWith(".jpeg", ignoreCase = true) ||
                     it.contentUrl.endsWith(".png", ignoreCase = true) ||
                     it.contentUrl.endsWith(".webp", ignoreCase = true)) &&
                    !it.contentUrl.endsWith(".gif", ignoreCase = true) &&
                    (it.previewImageUrl?.endsWith(".gif", ignoreCase = true) != true)
                }
                SearchContentType.VIDEOS -> displayList.filter {
                    it.isMediaVideo ||
                    !it.videoUrl.isNullOrBlank() ||
                    it.previewImageUrl?.contains(".gif", ignoreCase = true) == true ||
                    it.contentUrl.contains(".gif", ignoreCase = true) ||
                    it.domain.contains("v.redd.it", ignoreCase = true) ||
                    it.domain.contains("redgifs", ignoreCase = true) ||
                    it.domain.contains("gfycat", ignoreCase = true) ||
                    it.domain.contains("youtube", ignoreCase = true) ||
                    it.domain.contains("youtu.be", ignoreCase = true) ||
                    it.contentUrl.endsWith(".mp4", ignoreCase = true) ||
                    it.contentUrl.endsWith(".webm", ignoreCase = true)
                }
                SearchContentType.TEXT -> displayList.filter {
                    !it.selfTextHtml.isNullOrBlank() ||
                    it.domain.startsWith("self.") ||
                    it.contentUrl == it.permalink ||
                    (it.previewImageUrl.isNullOrBlank() && !it.isMediaVideo && it.videoUrl.isNullOrBlank() && (it.contentUrl.isBlank() || it.domain.contains("reddit.com", ignoreCase = true)))
                }
                SearchContentType.LINKS -> displayList.filter {
                    it.contentUrl.isNotBlank() &&
                    it.contentUrl != it.permalink &&
                    !it.domain.startsWith("self.") &&
                    !it.domain.contains("redd.it", ignoreCase = true) &&
                    !it.domain.contains("reddit.com", ignoreCase = true) &&
                    !it.isMediaVideo &&
                    it.videoUrl.isNullOrBlank() &&
                    !it.contentUrl.endsWith(".jpg", ignoreCase = true) &&
                    !it.contentUrl.endsWith(".jpeg", ignoreCase = true) &&
                    !it.contentUrl.endsWith(".png", ignoreCase = true) &&
                    !it.contentUrl.endsWith(".webp", ignoreCase = true) &&
                    !it.contentUrl.endsWith(".gif", ignoreCase = true)
                }
            }
        }

        val mapped = displayList.map { p ->
            p.copy(
                isSaved = p.isSaved || savedPostsManager.isPostSaved(p.id),
                isRead = readPostsManager.isPostRead(p.id)
            )
        }
        return Pair(mapped, filteredOutCount)
    }

    private fun refreshActivePostList() {
        val (filtered, count) = filterAndMapPosts(rawFetchedPosts)
        val emptyMessage = if (filtered.isEmpty()) {
            if (_uiState.value.isSearchActive) {
                if (_uiState.value.searchContentType != SearchContentType.ALL) {
                    "No ${_uiState.value.searchContentType.label.lowercase()} found for \"${_uiState.value.activeSearchQuery}\""
                } else {
                    "No results found for \"${_uiState.value.activeSearchQuery}\""
                }
            } else {
                "No posts found"
            }
        } else null

        _uiState.value = _uiState.value.copy(
            posts = filtered,
            filteredPostCount = count,
            errorMessage = null,
            emptyStateMessage = emptyMessage
        )
    }

    fun loadFeed(subreddit: String? = null, sort: FeedSort? = null, forceRefresh: Boolean = false) {
        val requestId = ++requestGeneration
        val targetSub = (subreddit ?: _uiState.value.activeSubreddit).ifBlank { "popular" }
        recentSubredditsManager.recordVisit(targetSub)
        val targetSort = when {
            sort != null && sort != FeedSort.RELEVANCE -> sort
            _uiState.value.activeSort != FeedSort.RELEVANCE -> _uiState.value.activeSort
            previousSortBeforeSearch != FeedSort.RELEVANCE -> previousSortBeforeSearch
            else -> FeedSort.HOT
        }

        val includeMature = accountManager.userProfile.value.showMatureContent
        val cached = if (!forceRefresh) RedditFeedService.getCachedFeed(targetSub, targetSort, includeMature) else null

        if (cached != null && cached.isNotEmpty()) {
            rawFetchedPosts = cached
            val (markedPosts, filterCount) = filterAndMapPosts(cached)
            _uiState.value = _uiState.value.copy(
                activeSubreddit = targetSub,
                activeSort = targetSort,
                isLoading = false,
                errorMessage = null,
                emptyStateMessage = null,
                isSearchActive = false,
                activeSearchQuery = "",
                activeFlairFilter = null,
                searchContentType = SearchContentType.ALL,
                posts = markedPosts,
                filteredPostCount = filterCount,
                isLoadingMore = false
            )
            return
        }

        val sameSub = _uiState.value.activeSubreddit.equals(targetSub, ignoreCase = true)
        _uiState.value = _uiState.value.copy(
            activeSubreddit = targetSub,
            activeSort = targetSort,
            isLoading = true,
            errorMessage = null,
            emptyStateMessage = null,
            isSearchActive = false,
            activeSearchQuery = "",
            activeFlairFilter = null,
            searchContentType = SearchContentType.ALL,
            posts = if (sameSub) _uiState.value.posts else emptyList(),
            filteredPostCount = 0,
            isLoadingMore = false
        )
        if (!sameSub) {
            rawFetchedPosts = emptyList()
        }

        viewModelScope.launch {
            val cookieHeader = accountManager.getCookieHeader(includeMature)
            val result = RedditFeedService.fetchFeed(
                subreddit = targetSub,
                sort = targetSort,
                cookieHeader = cookieHeader,
                includeMature = includeMature,
                forceRefresh = forceRefresh
            )

            if (requestId != requestGeneration) return@launch

            result.fold(
                onSuccess = { fetchedPosts ->
                    rawFetchedPosts = fetchedPosts
                    val (markedPosts, filterCount) = filterAndMapPosts(fetchedPosts)
                    _uiState.value = _uiState.value.copy(
                        posts = markedPosts,
                        filteredPostCount = filterCount,
                        isLoading = false,
                        errorMessage = null,
                        emptyStateMessage = if (markedPosts.isEmpty()) "No posts found in r/$targetSub" else null
                    )
                    if (targetSub.equals("home", ignoreCase = true) && accountManager.userProfile.value.isLoggedIn) {
                        val feedSubreddits = fetchedPosts.map { it.subreddit }
                            .filter { it.isNotBlank() && !it.equals("home", true) && !it.equals("popular", true) && !it.equals("all", true) }
                            .distinctBy(String::lowercase)
                        if (feedSubreddits.isNotEmpty() && accountManager.userProfile.value.subscribedSubreddits.isNullOrEmpty()) {
                            accountManager.updateSubscribedSubreddits(feedSubreddits)
                        }
                    }
                },
                onFailure = { err ->
                    RedXLogger.warning(
                        "feed_load_failed",
                        "subreddit" to targetSub,
                        "message" to err.localizedMessage
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        emptyStateMessage = null,
                        errorMessage = err.localizedMessage ?: "Failed to connect to Reddit"
                    )
                }
            )
        }
    }

    fun loadMorePosts() {
        if (_uiState.value.isLoadingMore || _uiState.value.isLoading || rawFetchedPosts.isEmpty()) return
        if (_uiState.value.isSearchActive) return // Search results are delivered in full batches

        val lastPost = rawFetchedPosts.lastOrNull() ?: return
        val afterId = lastPost.id.removePrefix("t3_")
        val requestId = requestGeneration
        val targetSub = _uiState.value.activeSubreddit
        val targetSort = _uiState.value.activeSort

        _uiState.value = _uiState.value.copy(isLoadingMore = true)

        viewModelScope.launch {
            val includeMature = accountManager.userProfile.value.showMatureContent
            val cookieHeader = accountManager.getCookieHeader(includeMature)
            val result = RedditFeedService.fetchFeed(
                subreddit = targetSub,
                sort = targetSort,
                cookieHeader = cookieHeader,
                after = "t3_$afterId",
                includeMature = includeMature
            )

            if (requestId != requestGeneration) return@launch

            result.fold(
                onSuccess = { newPosts ->
                    if (newPosts.isNotEmpty()) {
                        val existingIds = rawFetchedPosts.map { it.id }.toSet()
                        val uniqueNewPosts = newPosts.filter { !existingIds.contains(it.id) }
                        rawFetchedPosts = rawFetchedPosts + uniqueNewPosts
                        val (markedPosts, filterCount) = filterAndMapPosts(rawFetchedPosts)
                        _uiState.value = _uiState.value.copy(
                            posts = markedPosts,
                            filteredPostCount = filterCount,
                            isLoadingMore = false,
                            errorMessage = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(isLoadingMore = false)
                    }
                },
                onFailure = { err ->
                    RedXLogger.warning(
                        "feed_load_more_failed",
                        "subreddit" to targetSub,
                        "message" to err.localizedMessage
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoadingMore = false,
                        errorMessage = err.localizedMessage ?: "Couldn't load more posts"
                    )
                }
            )
        }
    }

    fun setFlairFilter(flair: String?) {
        _uiState.value = _uiState.value.copy(activeFlairFilter = flair)
        refreshActivePostList()
    }

    fun clearFlairFilter() {
        _uiState.value = _uiState.value.copy(activeFlairFilter = null)
        refreshActivePostList()
    }

    fun executeSearch(
        query: String,
        searchInSubreddit: Boolean,
        includeMature: Boolean,
        sort: FeedSort
    ) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return
        val requestId = ++requestGeneration
        val targetSub = if (searchInSubreddit) _uiState.value.activeSubreddit else null

        // Save active feed & sort so exiting search smoothly reverts
        if (!_uiState.value.isSearchActive) {
            previousSubredditBeforeSearch = _uiState.value.activeSubreddit.ifBlank { "popular" }
            previousSortBeforeSearch = if (_uiState.value.activeSort == FeedSort.RELEVANCE) FeedSort.HOT else _uiState.value.activeSort
            previousPostsBeforeSearch = rawFetchedPosts
        }

        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null,
            isSearchActive = true,
            activeSearchQuery = cleanQuery,
            activeSort = sort,
            activeFlairFilter = null,
            searchContentType = SearchContentType.ALL,
            posts = emptyList(),
            filteredPostCount = 0,
            isLoadingMore = false,
            emptyStateMessage = null
        )
        rawFetchedPosts = emptyList()

        viewModelScope.launch {
            val includeMatureSetting = includeMature
            val cookieHeader = accountManager.getCookieHeader(includeMatureSetting)
            val result = RedditFeedService.searchReddit(
                query = cleanQuery,
                subreddit = targetSub,
                sort = sort,
                includeMature = includeMatureSetting,
                cookieHeader = cookieHeader
            )

            if (requestId != requestGeneration) return@launch

            result.fold(
                onSuccess = { searchResults ->
                    rawFetchedPosts = searchResults
                    val (markedPosts, filterCount) = filterAndMapPosts(searchResults)
                    _uiState.value = _uiState.value.copy(
                        posts = markedPosts,
                        filteredPostCount = filterCount,
                        isLoading = false,
                        errorMessage = null,
                        emptyStateMessage = if (markedPosts.isEmpty()) "No results found for \"$cleanQuery\"" else null
                    )
                },
                onFailure = { err ->
                    RedXLogger.warning(
                        "search_failed",
                        "queryLength" to cleanQuery.length,
                        "message" to err.localizedMessage
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        emptyStateMessage = null,
                        errorMessage = err.localizedMessage ?: "Search failed"
                    )
                }
            )
        }
    }

    fun clearSearch(targetSubreddit: String? = null, targetSort: FeedSort? = null) {
        // Invalidate the in-flight search before restoring the previous feed.
        ++requestGeneration
        val destSub = targetSubreddit
            ?: if (previousSubredditBeforeSearch.isNotBlank() && !previousSubredditBeforeSearch.equals("search", true)) previousSubredditBeforeSearch
            else "popular"
        val destSort = targetSort
            ?: if (previousSortBeforeSearch != FeedSort.RELEVANCE) previousSortBeforeSearch
            else FeedSort.HOT

        _uiState.value = _uiState.value.copy(
            isSearchActive = false,
            activeSearchQuery = "",
            searchContentType = SearchContentType.ALL,
            activeSubreddit = destSub,
            activeSort = destSort
        )

        // If restoring to the previous feed before search and we have the posts cached, restore instantly!
        if (targetSubreddit == null && previousPostsBeforeSearch.isNotEmpty() && destSub.equals(previousSubredditBeforeSearch, ignoreCase = true)) {
            rawFetchedPosts = previousPostsBeforeSearch
            val (markedPosts, filterCount) = filterAndMapPosts(previousPostsBeforeSearch)
            _uiState.value = _uiState.value.copy(
                posts = markedPosts,
                filteredPostCount = filterCount,
                isLoading = false,
                errorMessage = null,
                emptyStateMessage = null
            )
        } else {
            loadFeed(subreddit = destSub, sort = destSort)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun setSearchContentType(type: SearchContentType) {
        if (_uiState.value.searchContentType == type) return
        _uiState.value = _uiState.value.copy(searchContentType = type)
        refreshActivePostList()
    }

    fun vote(post: RedditPost, newVote: Int) {
        if (!accountManager.userProfile.value.isLoggedIn) {
            _uiState.value = _uiState.value.copy(errorMessage = "Sign in to vote on Reddit")
            return
        }
        val currentPosts = _uiState.value.posts.toMutableList()
        val index = currentPosts.indexOfFirst { it.id == post.id }
        if (index != -1) {
            val currentPost = currentPosts[index]
            val oldVote = currentPost.userVote
            val normalizedVote = newVote.coerceIn(-1, 1)
            val scoreDelta = normalizedVote - oldVote
            val updatedPost = currentPost.copy(
                userVote = normalizedVote,
                score = currentPost.score + scoreDelta
            )
            currentPosts[index] = updatedPost
            rawFetchedPosts = rawFetchedPosts.map { if (it.id == post.id) updatedPost else it }
            _uiState.value = _uiState.value.copy(
                posts = currentPosts,
                selectedPost = if (_uiState.value.selectedPost?.id == post.id) updatedPost else _uiState.value.selectedPost
            )

            val cookieHeader = accountManager.getCookieHeader()
            val actionId = ++postActionGeneration
            latestVoteRequest[post.id] = actionId
            viewModelScope.launch {
                RedditPostActionService.vote(cookieHeader, updatedPost.id, normalizedVote)
                    .onSuccess { if (latestVoteRequest[post.id] == actionId) latestVoteRequest.remove(post.id) }
                    .onFailure {
                        if (latestVoteRequest[post.id] == actionId) {
                            replacePost(currentPost)
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Reddit rejected the vote; your feed was restored"
                            )
                        }
                    }
            }
        }
    }

    fun toggleSave(post: RedditPost) {
        val currentPost = _uiState.value.posts.firstOrNull { it.id == post.id } ?: post
        val isNowSaved = !currentPost.isSaved
        val updatedPost = currentPost.copy(isSaved = isNowSaved)
        savedPostsManager.setPostSaved(updatedPost, isNowSaved)

        val isVisible = _uiState.value.posts.any { it.id == post.id }
        if (isVisible) {
            _uiState.value = _uiState.value.copy(
                posts = _uiState.value.posts.map { if (it.id == post.id) updatedPost else it },
                selectedPost = if (_uiState.value.selectedPost?.id == post.id) updatedPost else _uiState.value.selectedPost
            )
        }
        rawFetchedPosts = rawFetchedPosts.map { if (it.id == post.id) updatedPost else it }

        if (accountManager.userProfile.value.isLoggedIn) {
            val cookieHeader = accountManager.getCookieHeader()
            val actionId = ++postActionGeneration
            latestSaveRequest[post.id] = actionId
            viewModelScope.launch {
                RedditPostActionService.setSaved(cookieHeader, updatedPost.id, isNowSaved)
                    .onSuccess { if (latestSaveRequest[post.id] == actionId) latestSaveRequest.remove(post.id) }
                    .onFailure {
                        if (latestSaveRequest[post.id] == actionId) {
                            savedPostsManager.setPostSaved(currentPost, currentPost.isSaved)
                            replacePost(currentPost)
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Reddit rejected the save; your local bookmark was restored"
                            )
                        }
                    }
            }
        }
    }

    private fun replacePost(post: RedditPost) {
        rawFetchedPosts = rawFetchedPosts.map { if (it.id == post.id) post else it }
        _uiState.value = _uiState.value.copy(
            posts = _uiState.value.posts.map { if (it.id == post.id) post else it },
            selectedPost = if (_uiState.value.selectedPost?.id == post.id) post else _uiState.value.selectedPost
        )
    }

    fun selectPost(post: RedditPost?) {
        if (post != null) {
            readPostsManager.markPostRead(post.id)
            val updatedPosts = _uiState.value.posts.map {
                if (it.id == post.id) it.copy(isRead = true) else it
            }
            _uiState.value = _uiState.value.copy(
                selectedPost = post.copy(isRead = true),
                posts = updatedPosts
            )
        } else {
            _uiState.value = _uiState.value.copy(selectedPost = null)
        }
    }

    fun togglePostRead(post: RedditPost) {
        val isReadNow = readPostsManager.togglePostRead(post.id)
        val currentPosts = _uiState.value.posts.map {
            if (it.id == post.id) it.copy(isRead = isReadNow) else it
        }
        val updatedSelected = _uiState.value.selectedPost?.let { sel ->
            if (sel.id == post.id) sel.copy(isRead = isReadNow) else sel
        }
        _uiState.value = _uiState.value.copy(
            posts = currentPosts,
            selectedPost = updatedSelected
        )
    }

    fun sweepReadPosts() {
        val newHideState = !_uiState.value.hideReadPosts
        setHideReadPosts(newHideState)
    }

    fun setHideReadPosts(enabled: Boolean) {
        prefs.edit().putBoolean("key_hide_read_posts", enabled).apply()
        _uiState.value = _uiState.value.copy(hideReadPosts = enabled)
        refreshActivePostList()
    }

    fun clearReadHistory() {
        readPostsManager.clearAllReadPosts()
        refreshActivePostList()
    }

    fun markAllVisibleRead() {
        _uiState.value.posts.forEach { post ->
            readPostsManager.markPostRead(post.id)
        }
        refreshActivePostList()
    }

    fun openLightbox(
        url: String,
        title: String,
        videoUrl: String? = null,
        isVideo: Boolean = false,
        galleryUrls: List<String> = emptyList()
    ) {
        val detectedVideo = isVideo || !videoUrl.isNullOrBlank() ||
            UrlSafety.hasExtension(url, "mp4", "webm", "m3u8", "gifv") ||
            url.contains("v.redd.it", ignoreCase = true) ||
            url.contains("redgifs.com", ignoreCase = true) ||
            url.contains(".mp4", ignoreCase = true)

        _uiState.value = _uiState.value.copy(
            lightboxMediaUrl = url,
            lightboxMediaTitle = title,
            lightboxVideoUrl = videoUrl ?: if (detectedVideo) url else null,
            lightboxIsVideo = detectedVideo,
            lightboxGalleryUrls = galleryUrls
        )
    }

    fun closeLightbox() {
        _uiState.value = _uiState.value.copy(
            lightboxMediaUrl = null,
            lightboxMediaTitle = "",
            lightboxVideoUrl = null,
            lightboxIsVideo = false,
            lightboxGalleryUrls = emptyList()
        )
    }

    fun openQuickActions(post: RedditPost) {
        _uiState.value = _uiState.value.copy(quickActionsPost = post)
    }

    fun closeQuickActions() {
        _uiState.value = _uiState.value.copy(quickActionsPost = null)
    }

    fun setFilterDialogOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isFilterDialogOpen = isOpen)
    }

    fun toggleFilterEnabled(enabled: Boolean) {
        contentFilterManager.toggleFilterEnabled(enabled)
        refreshActivePostList()
    }

    fun addFilterKeyword(keyword: String) {
        contentFilterManager.addBlockedKeyword(keyword)
        refreshActivePostList()
    }

    fun removeFilterKeyword(keyword: String) {
        contentFilterManager.removeBlockedKeyword(keyword)
        refreshActivePostList()
    }

    fun addFilterDomain(domain: String) {
        contentFilterManager.addBlockedDomain(domain)
        refreshActivePostList()
    }

    fun removeFilterDomain(domain: String) {
        contentFilterManager.removeBlockedDomain(domain)
        refreshActivePostList()
    }

    fun setViewMode(mode: FeedViewMode) {
        prefs.edit().putString("key_view_mode", mode.name).apply()
        _uiState.value = _uiState.value.copy(viewMode = mode, selectedPost = null)
    }

    fun cycleViewMode() {
        val allModes = FeedViewMode.entries
        val currentIndex = allModes.indexOf(_uiState.value.viewMode)
        val nextIndex = if (currentIndex >= 0 && currentIndex < allModes.size - 1) currentIndex + 1 else 0
        val nextMode = allModes[nextIndex]
        setViewMode(nextMode)
    }

    fun setViewStylePickerOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isViewStylePickerOpen = isOpen)
    }

    fun setAppTheme(theme: AppTheme) {
        prefs.edit().putString("key_app_theme", theme.name).apply()
        _uiState.value = _uiState.value.copy(appTheme = theme)
    }

    fun setFontScale(scale: FontScale) {
        prefs.edit().putString("key_font_scale", scale.name).apply()
        _uiState.value = _uiState.value.copy(fontScale = scale)
    }

    fun setAccountSheetOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isAccountSheetOpen = isOpen)
        if (isOpen && accountManager.userProfile.value.sessionActive) {
            viewModelScope.launch { accountManager.refreshAuthenticatedAccount() }
        }
    }

    fun setLoginDialogOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isLoginDialogOpen = isOpen)
    }

    fun setCustomSubredditDialogOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isCustomSubredditDialogOpen = isOpen)
    }

    fun setCommunityExplorerOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isCommunityExplorerOpen = isOpen)
    }

    fun removeRecentSubreddit(subreddit: String) {
        recentSubredditsManager.removeRecent(subreddit)
    }

    fun clearRecentSubreddits() {
        recentSubredditsManager.clearRecent()
    }

    fun toggleFavoriteSubreddit(subreddit: String) {
        savedPostsManager.toggleFavoriteSubreddit(subreddit)
    }

    fun setSearchDialogOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchDialogOpen = isOpen)
    }

    fun setAppearanceDialogOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isAppearanceDialogOpen = isOpen)
    }

    fun setSettingsSheetOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isSettingsSheetOpen = isOpen)
    }

    fun setSavedPostsSheetOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isSavedPostsSheetOpen = isOpen)
    }

    fun toggleMatureContent(show: Boolean) {
        accountManager.toggleMatureContent(show)
        loadFeed()
    }

    fun onLoginDetected() {
        _uiState.value = _uiState.value.copy(
            isLoginDialogOpen = false,
            activeSubreddit = "home"
        )
        viewModelScope.launch {
            val result = accountManager.refreshAuthenticatedAccount()
            if (result.isSuccess) {
                loadFeed(subreddit = "home", forceRefresh = true)
            } else {
                _uiState.value = _uiState.value.copy(isAccountSheetOpen = true)
                loadFeed(subreddit = "home", forceRefresh = true)
            }
        }
    }

    fun logout() {
        val logoutGeneration = ++requestGeneration
        userProfileRequestGeneration++
        latestVoteRequest.clear()
        latestSaveRequest.clear()
        rawFetchedPosts = emptyList()
        _uiState.value = _uiState.value.copy(
            isAccountSheetOpen = false,
            activeSubreddit = "popular",
            multiSubredditMode = false,
            multiSubreddits = emptyList(),
            posts = emptyList(),
            isLoading = true,
            isLoadingMore = false,
            errorMessage = null,
            emptyStateMessage = null,
            isUserProfileOpen = false,
            viewedUserName = null,
            userProfilePosts = emptyList(),
            isUserProfileLoading = false
        )
        accountManager.logout {
            if (logoutGeneration != requestGeneration) return@logout
            loadFeed(subreddit = "popular", forceRefresh = true)
        }
    }

    // ── Multi-subreddit ──────────────────────────────────────────────────────

    fun setMultiSubPickerOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isMultiSubPickerOpen = isOpen)
    }

    fun enableMultiFeed(subs: List<String>) {
        val cleaned = subs.mapNotNull(RedditInputValidator::normalizeSubreddit)
            .map(String::lowercase)
            .take(5)
            .distinct()
        if (cleaned.isEmpty()) return
        val combinedSub = cleaned.joinToString("+")
        _uiState.value = _uiState.value.copy(
            multiSubredditMode = true,
            multiSubreddits = cleaned,
            isMultiSubPickerOpen = false
        )
        loadFeed(subreddit = combinedSub, forceRefresh = true)
    }

    fun disableMultiFeed() {
        _uiState.value = _uiState.value.copy(
            multiSubredditMode = false,
            multiSubreddits = emptyList()
        )
        val fallback = if (accountManager.userProfile.value.isLoggedIn) "home" else "popular"
        loadFeed(subreddit = fallback, forceRefresh = true)
    }

    // ── User Profile Viewer ──────────────────────────────────────────────────

    fun openUserProfile(username: String) {
        val cleanUsername = RedditInputValidator.normalizeUsername(username) ?: return
        val requestId = ++userProfileRequestGeneration
        _uiState.value = _uiState.value.copy(
            viewedUserName = cleanUsername,
            isUserProfileOpen = true,
            userProfilePosts = emptyList(),
            isUserProfileLoading = true
        )
        viewModelScope.launch {
            val cookieHeader = accountManager.getCookieHeader()
            val result = RedditUserService.fetchUserPosts(cleanUsername, cookieHeader)
            if (requestId != userProfileRequestGeneration ||
                !_uiState.value.isUserProfileOpen ||
                !_uiState.value.viewedUserName.equals(cleanUsername, ignoreCase = true)
            ) return@launch
            _uiState.value = _uiState.value.copy(
                userProfilePosts = result.getOrElse { emptyList() },
                isUserProfileLoading = false
            )
        }
    }

    fun closeUserProfile() {
        userProfileRequestGeneration++
        _uiState.value = _uiState.value.copy(
            isUserProfileOpen = false,
            viewedUserName = null,
            userProfilePosts = emptyList()
        )
    }

    // ── Read Later Queue ─────────────────────────────────────────────────────

    fun setReadLaterSheetOpen(isOpen: Boolean) {
        _uiState.value = _uiState.value.copy(isReadLaterSheetOpen = isOpen)
    }

    fun addToReadLater(post: RedditPost) {
        readLaterManager.addToQueue(post)
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun removeFromReadLater(postId: String) {
        readLaterManager.removeFromQueue(postId)
    }

    // ── Crosspost ────────────────────────────────────────────────────────────

    fun openCrosspostDialog(post: RedditPost) {
        if (!accountManager.userProfile.value.isLoggedIn) {
            _uiState.value = _uiState.value.copy(errorMessage = "Sign in to crosspost")
            return
        }
        _uiState.value = _uiState.value.copy(
            crosspostTargetPost = post,
            isCrosspostDialogOpen = true
        )
    }

    fun closeCrosspostDialog() {
        _uiState.value = _uiState.value.copy(
            isCrosspostDialogOpen = false,
            crosspostTargetPost = null
        )
    }

    fun submitCrosspost(post: RedditPost, targetSubreddit: String, title: String) {
        if (!accountManager.userProfile.value.isLoggedIn) {
            _uiState.value = _uiState.value.copy(errorMessage = "Sign in to crosspost")
            return
        }
        closeCrosspostDialog()
        viewModelScope.launch {
            val cookieHeader = accountManager.getCookieHeader()
            RedditPostActionService.crosspost(cookieHeader, post.id, targetSubreddit, title)
                .fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "✓ Crossposted to r/$targetSubreddit"
                        )
                    },
                    onFailure = { err ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Crosspost failed: ${err.localizedMessage ?: "unknown error"}"
                        )
                    }
                )
        }
    }

    // ── Comment Voting ───────────────────────────────────────────────────────

    fun voteComment(commentFullname: String, direction: Int) {
        if (!accountManager.userProfile.value.isLoggedIn) {
            _uiState.value = _uiState.value.copy(errorMessage = "Sign in to vote on comments")
            return
        }
        viewModelScope.launch {
            val cookieHeader = accountManager.getCookieHeader()
            RedditPostActionService.vote(cookieHeader, commentFullname, direction)
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Comment vote failed — Reddit rejected the request"
                    )
                }
        }
    }
}
