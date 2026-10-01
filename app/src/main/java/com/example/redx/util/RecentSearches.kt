package com.example.redx.util

/** Pure helpers for the recent-search list so the ordering rules can be unit tested. */
object RecentSearches {

    const val MAX_ENTRIES = 8
    private const val MAX_QUERY_LENGTH = 100

    /**
     * Returns [current] with [query] moved to the front. Matching ignores case and
     * surrounding/duplicate whitespace; blank queries are ignored; the list is capped.
     */
    fun push(current: List<String>, query: String, max: Int = MAX_ENTRIES): List<String> {
        val clean = query.trim().replace(Regex("\\s+"), " ").take(MAX_QUERY_LENGTH)
        if (clean.isBlank()) return current
        return (listOf(clean) + current.filterNot { it.equals(clean, ignoreCase = true) }).take(max)
    }

    fun remove(current: List<String>, query: String): List<String> =
        current.filterNot { it.equals(query.trim(), ignoreCase = true) }
}
