package com.example.redx.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

/** Posts the user chose to hide ("Hide post"); kept on-device and bounded in size. */
class HiddenPostsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("redx_hidden_posts", Context.MODE_PRIVATE)

    private val _hiddenIds = MutableStateFlow<Set<String>>(load())
    val hiddenIds: StateFlow<Set<String>> = _hiddenIds.asStateFlow()

    private fun load(): Set<String> {
        val set = LinkedHashSet<String>()
        runCatching {
            val array = JSONArray(prefs.getString(KEY_HIDDEN, "[]") ?: "[]")
            for (i in 0 until array.length()) {
                array.optString(i).trim().takeIf { it.isNotBlank() }?.let(set::add)
            }
        }
        return set
    }

    private fun persist(ids: Set<String>) {
        prefs.edit { putString(KEY_HIDDEN, JSONArray(ids.toList()).toString()) }
    }

    fun isHidden(id: String): Boolean = _hiddenIds.value.contains(id)

    fun hide(id: String) {
        if (id.isBlank() || isHidden(id)) return
        val updated = LinkedHashSet(_hiddenIds.value).apply { add(id) }
        // Keep the newest entries if the list ever grows past the cap.
        val bounded = if (updated.size > MAX_HIDDEN) updated.toList().takeLast(MAX_HIDDEN).toCollection(LinkedHashSet()) else updated
        _hiddenIds.value = bounded
        persist(bounded)
    }

    fun clearAll() {
        _hiddenIds.value = emptySet()
        prefs.edit { remove(KEY_HIDDEN) }
    }

    companion object {
        private const val KEY_HIDDEN = "hidden_post_ids"
        private const val MAX_HIDDEN = 3_000
    }
}
