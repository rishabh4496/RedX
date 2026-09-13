package com.example.redx.data

import android.content.Context
import android.content.SharedPreferences
import com.example.redx.model.ReadLaterEntry
import com.example.redx.model.RedditPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class ReadLaterManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("redx_read_later", Context.MODE_PRIVATE)

    private val _queue = MutableStateFlow<List<ReadLaterEntry>>(loadQueue())
    val queue: StateFlow<List<ReadLaterEntry>> = _queue.asStateFlow()

    private fun loadQueue(): List<ReadLaterEntry> {
        val jsonString = prefs.getString(KEY_QUEUE, "[]") ?: "[]"
        val list = mutableListOf<ReadLaterEntry>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(
                    ReadLaterEntry(
                        postId = obj.optString("postId"),
                        title = obj.optString("title"),
                        subreddit = obj.optString("subreddit"),
                        permalink = obj.optString("permalink"),
                        previewImageUrl = obj.optString("previewImageUrl").takeIf { it.isNotBlank() },
                        author = obj.optString("author"),
                        addedAt = obj.optLong("addedAt", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    private fun persist(list: List<ReadLaterEntry>) {
        val array = JSONArray()
        list.forEach { entry ->
            val obj = JSONObject().apply {
                put("postId", entry.postId)
                put("title", entry.title)
                put("subreddit", entry.subreddit)
                put("permalink", entry.permalink)
                put("previewImageUrl", entry.previewImageUrl ?: "")
                put("author", entry.author)
                put("addedAt", entry.addedAt)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_QUEUE, array.toString()).apply()
    }

    fun isQueued(postId: String): Boolean =
        _queue.value.any { it.postId == postId }

    fun addToQueue(post: RedditPost) {
        if (isQueued(post.id)) return
        val entry = ReadLaterEntry(
            postId = post.id,
            title = post.title,
            subreddit = post.subreddit,
            permalink = post.permalink,
            previewImageUrl = post.displayImageUrl,
            author = post.author
        )
        val updated = listOf(entry) + _queue.value
        _queue.value = updated
        persist(updated)
    }

    fun removeFromQueue(postId: String) {
        val updated = _queue.value.filter { it.postId != postId }
        _queue.value = updated
        persist(updated)
    }

    fun clearQueue() {
        _queue.value = emptyList()
        prefs.edit().remove(KEY_QUEUE).apply()
    }

    companion object {
        private const val KEY_QUEUE = "read_later_queue"
    }
}
