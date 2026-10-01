package com.example.redx.util

/** Pure filter rules shared with [com.example.redx.data.ContentFilterManager]. */
object ContentFilterRules {

    /**
     * A blocked subreddit is hidden everywhere except while the user is deliberately
     * browsing that subreddit, otherwise blocking a community would also make its own
     * page look permanently empty.
     */
    fun isSubredditBlocked(
        blockedSubreddits: Collection<String>,
        postSubreddit: String,
        viewingSubreddit: String?
    ): Boolean {
        if (blockedSubreddits.isEmpty()) return false
        val sub = postSubreddit.trim().lowercase()
        if (sub.isEmpty()) return false
        if (viewingSubreddit != null && sub == viewingSubreddit.trim().lowercase()) return false
        return blockedSubreddits.any { it.trim().lowercase() == sub }
    }

    /** Normalises user input like "r/Foo", "/r/foo" or " foo " to a lowercase name, or null. */
    fun normalizeSubredditName(raw: String): String? {
        val clean = raw.trim()
            .removePrefix("/")
            .removePrefix("r/")
            .removePrefix("R/")
            .trim()
            .lowercase()
        return clean.takeIf { Regex("[a-z0-9_-]{1,70}").matches(it) }
    }
}
