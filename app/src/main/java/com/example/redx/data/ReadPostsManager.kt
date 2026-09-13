package com.example.redx.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

class ReadPostsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("redx_read_history", Context.MODE_PRIVATE)

    private val _readPostIds = MutableStateFlow<Set<String>>(loadReadIds())
    val readPostIds: StateFlow<Set<String>> = _readPostIds.asStateFlow()

    private fun loadReadIds(): Set<String> {
        val jsonString = prefs.getString(KEY_READ_IDS, "[]") ?: "[]"
        val set = mutableSetOf<String>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                array.optString(i).trim().takeIf { it.isNotBlank() }?.let(set::add)
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return set
    }

    private fun persistReadIds(ids: Set<String>) {
        val array = JSONArray()
        val bounded = boundReadIds(ids)
        for (id in bounded) {
            array.put(id)
        }
        prefs.edit().putString(KEY_READ_IDS, array.toString()).apply()
    }

    private fun boundReadIds(ids: Set<String>): Set<String> =
        if (ids.size > MAX_READ_HISTORY) ids.toList().takeLast(MAX_READ_HISTORY).toSet() else ids

    fun isPostRead(id: String): Boolean {
        return _readPostIds.value.contains(id)
    }

    fun markPostRead(id: String) {
        if (id.isBlank() || _readPostIds.value.contains(id)) return
        val updated = boundReadIds(_readPostIds.value + id)
        _readPostIds.value = updated
        persistReadIds(updated)
    }

    fun markPostUnread(id: String) {
        if (id.isBlank() || !_readPostIds.value.contains(id)) return
        val updated = _readPostIds.value - id
        _readPostIds.value = updated
        persistReadIds(updated)
    }

    fun togglePostRead(id: String): Boolean {
        return if (isPostRead(id)) {
            markPostUnread(id)
            false
        } else {
            markPostRead(id)
            true
        }
    }

    fun clearAllReadPosts() {
        _readPostIds.value = emptySet()
        prefs.edit().remove(KEY_READ_IDS).apply()
    }

    companion object {
        private const val KEY_READ_IDS = "read_post_ids"
        private const val MAX_READ_HISTORY = 2_000
    }
}
