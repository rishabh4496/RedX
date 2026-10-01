package com.example.redx.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.redx.util.RecentSearches
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

/** Search queries the user ran recently, newest first. */
class RecentSearchesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("redx_recent_searches", Context.MODE_PRIVATE)

    private val _recent = MutableStateFlow<List<String>>(load())
    val recentSearches: StateFlow<List<String>> = _recent.asStateFlow()

    private fun load(): List<String> {
        val list = mutableListOf<String>()
        runCatching {
            val array = JSONArray(prefs.getString(KEY_RECENT, "[]") ?: "[]")
            for (i in 0 until array.length()) {
                array.optString(i).trim().takeIf { it.isNotBlank() }?.let(list::add)
            }
        }
        return list.take(RecentSearches.MAX_ENTRIES)
    }

    private fun persist(list: List<String>) {
        prefs.edit { putString(KEY_RECENT, JSONArray(list).toString()) }
    }

    fun record(query: String) {
        val updated = RecentSearches.push(_recent.value, query)
        if (updated == _recent.value) return
        _recent.value = updated
        persist(updated)
    }

    fun remove(query: String) {
        val updated = RecentSearches.remove(_recent.value, query)
        _recent.value = updated
        persist(updated)
    }

    fun clear() {
        _recent.value = emptyList()
        prefs.edit { remove(KEY_RECENT) }
    }

    companion object {
        private const val KEY_RECENT = "recent_searches"
    }
}
