package com.example.redx.util

/** Provides circular navigation through the subreddit chips shown in the feed. */
object SubredditNavigator {

    fun next(
        current: String,
        available: List<String>,
        direction: Int
    ): String? {
        val feeds = available
            .mapNotNull(RedditInputValidator::normalizeSubreddit)
            .distinctBy(String::lowercase)
        if (feeds.size < 2) return null

        val step = if (direction < 0) -1 else 1
        val normalizedCurrent = RedditInputValidator.normalizeSubreddit(current)
        val currentIndex = feeds.indexOfFirst { it.equals(normalizedCurrent, ignoreCase = true) }
        if (currentIndex < 0) return if (step < 0) feeds.last() else feeds.first()
        val startIndex = currentIndex
        return feeds[(startIndex + step + feeds.size) % feeds.size]
    }
}
