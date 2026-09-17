package com.example.redx.util

/** Normalizes user-controlled Reddit path segments before they reach a URL or API form. */
object RedditInputValidator {

    private val subredditPattern = Regex("[A-Za-z0-9_-]{1,70}")
    private val usernamePattern = Regex("[A-Za-z0-9_-]{1,20}")

    fun normalizeSubreddit(raw: String): String? {
        val clean = raw.trim()
            .removePrefixIgnoreCase("/r/")
            .removePrefixIgnoreCase("r/")
            .removePrefix("/")
        return clean.takeIf { subredditPattern.matches(it) }
    }

    /** Normalizes a Reddit multi-subreddit path target without accepting arbitrary path syntax. */
    fun normalizeSubredditTarget(raw: String): String? {
        val parts = raw.trim().split('+')
        if (parts.isEmpty() || parts.any { it.isBlank() }) return null
        return parts.map { normalizeSubreddit(it) }
            .takeIf { normalized -> normalized.all { it != null } }
            ?.filterNotNull()
            ?.joinToString("+")
    }

    fun normalizeUsername(raw: String): String? {
        val clean = raw.trim()
            .removePrefixIgnoreCase("/u/")
            .removePrefixIgnoreCase("u/")
        return clean.takeIf { usernamePattern.matches(it) }
    }

    private fun String.removePrefixIgnoreCase(prefix: String): String =
        if (startsWith(prefix, ignoreCase = true)) substring(prefix.length) else this
}
