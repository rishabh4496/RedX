package com.example.redx.network

import com.example.redx.model.FeedSort
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RedditFeedServiceTest {

    @Test
    fun hotFeedUsesValidRssPath() {
        assertEquals(
            "https://www.reddit.com/r/popular/.rss?limit=50",
            RedditFeedService.buildFeedUrl("popular", FeedSort.HOT)
        )
    }

    @Test
    fun nonHotFeedUsesSortRssPath() {
        assertEquals(
            "https://www.reddit.com/r/android/new/.rss?limit=50",
            RedditFeedService.buildFeedUrl("android", FeedSort.NEW)
        )
    }

    @Test
    fun jsonFeedUsesRealListingEndpointAndPagination() {
        assertEquals(
            "https://www.reddit.com/r/android/new.json?limit=50&raw_json=1&after=t3_abc123",
            RedditFeedService.buildJsonFeedUrl("android", FeedSort.NEW, "t3_abc123")
        )
    }

    @Test
    fun jsonSearchRestrictsSubredditWhenRequested() {
        assertEquals(
            "https://www.reddit.com/r/android/search.json?q=compose+ui&sort=relevance&limit=50&raw_json=1&restrict_sr=on",
            RedditFeedService.buildJsonSearchUrl("android", "compose ui", FeedSort.RELEVANCE)
        )
    }

    @Test
    fun blankSearchIsRejectedBeforeNetworkAccess() = runTest {
        val result = RedditFeedService.searchReddit("   ")

        assertTrue(result.isFailure)
        assertEquals("Search query cannot be blank", result.exceptionOrNull()?.message)
    }

    @Test
    fun invalidSubredditIsRejectedBeforeNetworkAccess() = runTest {
        val result = RedditFeedService.fetchFeed("android/search")

        assertTrue(result.isFailure)
        assertEquals("Invalid subreddit name", result.exceptionOrNull()?.message)
    }
}
