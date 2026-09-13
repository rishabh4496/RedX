package com.example.redx.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

class RecentSubredditsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("redx_recent_subreddits", Context.MODE_PRIVATE)

    private val _recentSubreddits = MutableStateFlow<List<String>>(loadRecent())
    val recentSubreddits: StateFlow<List<String>> = _recentSubreddits.asStateFlow()

    private fun loadRecent(): List<String> {
        val jsonString = prefs.getString(KEY_RECENTS, "[]") ?: "[]"
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val sub = array.getString(i).trim().lowercase()
                if (sub.isNotBlank() && !list.contains(sub)) {
                    list.add(sub)
                }
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return if (list.isEmpty()) {
            listOf("popular", "technology", "gaming", "science", "worldnews")
        } else {
            list
        }
    }

    private fun persist(list: List<String>) {
        val array = JSONArray()
        list.take(20).forEach { array.put(it) }
        prefs.edit().putString(KEY_RECENTS, array.toString()).apply()
    }

    fun recordVisit(subreddit: String) {
        val clean = subreddit.trim().lowercase().removePrefix("r/").removePrefix("/")
        if (clean.isBlank()) return
        // Don't record meta-feeds as "recent"
        if (clean == "popular" || clean == "home" || clean == "all") return
        val current = _recentSubreddits.value.toMutableList()
        current.remove(clean)
        current.add(0, clean)
        val trimmed = current.take(20)
        _recentSubreddits.value = trimmed
        persist(trimmed)
    }

    fun removeRecent(subreddit: String) {
        val clean = subreddit.trim().lowercase().removePrefix("r/").removePrefix("/")
        val updated = _recentSubreddits.value.filter { it != clean }
        _recentSubreddits.value = updated
        persist(updated)
    }

    fun clearRecent() {
        _recentSubreddits.value = emptyList()
        prefs.edit().remove(KEY_RECENTS).apply()
    }

    companion object {
        private const val KEY_RECENTS = "recent_subs_list"
    }
}
