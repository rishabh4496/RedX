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

    @Test
    fun relevanceSortFallsBackToHotForListingFeeds() {
        assertEquals(
            "https://www.reddit.com/r/android/.rss?limit=50",
            RedditFeedService.buildFeedUrl("android", FeedSort.RELEVANCE)
        )
        assertEquals(
            "https://www.reddit.com/r/android/hot.json?limit=50&raw_json=1",
            RedditFeedService.buildJsonFeedUrl("android", FeedSort.RELEVANCE)
        )
    }

    @Test
    fun risingSearchSortDegradesToNew() {
        assertEquals(
            "https://www.reddit.com/search.json?q=compose&sort=new&limit=50&raw_json=1",
            RedditFeedService.buildJsonSearchUrl(null, "compose", FeedSort.RISING)
        )
    }

    @Test
    fun homeFeedUsesFrontPageEndpoints() {
        assertEquals(
            "https://www.reddit.com/.rss?limit=50",
            RedditFeedService.buildFeedUrl("home", FeedSort.HOT)
        )
        assertEquals(
            "https://www.reddit.com/top.json?limit=50&raw_json=1",
            RedditFeedService.buildJsonFeedUrl("home", FeedSort.TOP)
        )
    }

    @Test
    fun searchQueriesAreUrlEncoded() {
        assertTrue(
            RedditFeedService.buildJsonSearchUrl("android", "jetpack compose & r8", FeedSort.TOP)
                .contains("q=jetpack+compose+%26+r8")
        )
    }

    @Test
    fun compositeSubredditTargetIsAcceptedAndKeptAsPathSegments() = runTest {
        val target = "android+technology"
        assertEquals(
            "https://www.reddit.com/r/android+technology/.rss?limit=50",
            RedditFeedService.buildFeedUrl(target, FeedSort.HOT)
        )
        assertEquals(
            "https://www.reddit.com/r/android+technology/new.json?limit=50&raw_json=1",
            RedditFeedService.buildJsonFeedUrl(target, FeedSort.NEW)
        )

    }

    @Test
    fun compositeSubredditTargetRejectsInvalidPathInjection() = runTest {
        val result = RedditFeedService.fetchFeed("android+bad/name")

        assertTrue(result.isFailure)
        assertEquals("Invalid subreddit name", result.exceptionOrNull()?.message)
    }

    @Test
    fun anonymousRequestsDoNotSendMatureCookies() {
        assertEquals(
            "",
            RedditFeedService.buildCookieHeader(
                "over18=1; mweb_nx_over18=1",
                includeMature = true
            )
        )
    }

    @Test
    fun authenticatedRequestsKeepSessionAndMatureCookies() {
        val cookies = RedditFeedService.buildCookieHeader(
            "reddit_session=session123",
            includeMature = true
        )

        assertTrue(cookies.contains("reddit_session=session123"))
        assertTrue(cookies.contains("over18=1"))
        assertTrue(cookies.contains("mweb_nx_over18=1"))
    }

    @Test
    fun matureCookiesAreRemovedWhenMatureContentIsDisabled() {
        val cookies = RedditFeedService.buildCookieHeader(
            "reddit_session=session123; over18=1; mweb_nx_over18=1",
            includeMature = false
        )

        assertTrue(cookies == "reddit_session=session123")
    }
}
