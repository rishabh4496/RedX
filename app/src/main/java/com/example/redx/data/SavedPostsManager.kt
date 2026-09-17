package com.example.redx.data

import android.content.Context
import androidx.core.content.edit
import android.content.SharedPreferences
import com.example.redx.model.RedditPost
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class SavedPostsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("redx_saved_data", Context.MODE_PRIVATE)

    private val _savedPosts = MutableStateFlow<List<RedditPost>>(loadSavedPosts())
    val savedPosts: StateFlow<List<RedditPost>> = _savedPosts.asStateFlow()

    private val _favoriteSubreddits = MutableStateFlow<List<String>>(loadFavoriteSubreddits())
    val favoriteSubreddits: StateFlow<List<String>> = _favoriteSubreddits.asStateFlow()

    private fun loadSavedPosts(): List<RedditPost> {
        val jsonString = prefs.getString(KEY_SAVED_POSTS, "[]") ?: "[]"
        val list = mutableListOf<RedditPost>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    RedditPost(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        author = obj.getString("author"),
                        subreddit = obj.getString("subreddit"),
                        score = obj.optInt("score", 1),
                        numComments = obj.optInt("numComments", 0),
                        publishedTime = obj.optString("publishedTime", ""),
                        timestampMs = obj.optLong("timestampMs", System.currentTimeMillis()),
                        permalink = obj.optString("permalink", ""),
                        contentUrl = obj.optString("contentUrl", ""),
                        domain = obj.optString("domain", "reddit.com"),
                        thumbnailUrl = obj.optString("thumbnailUrl").takeIf { it.isNotBlank() },
                        previewImageUrl = obj.optString("previewImageUrl").takeIf { it.isNotBlank() },
                        videoUrl = obj.optString("videoUrl").takeIf { it.isNotBlank() },
                        isVideo = obj.optBoolean("isVideo", false),
                        selfTextHtml = obj.optString("selfTextHtml").takeIf { it.isNotBlank() },
                        flair = obj.optString("flair").takeIf { it.isNotBlank() },
                        isNsfw = obj.optBoolean("isNsfw", false),
                        isSpoiler = obj.optBoolean("isSpoiler", false),
                        userVote = obj.optInt("userVote", 0),
                        isSaved = true,
                        galleryImageUrls = obj.optJSONArray("galleryImageUrls")?.let { arr ->
                            (0 until arr.length()).mapNotNull { idx -> arr.optString(idx).takeIf { s -> s.isNotBlank() } }
                        } ?: emptyList()
                    )
                )
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return list
    }

    private fun persistSavedPosts(posts: List<RedditPost>) {
        val array = JSONArray()
        for (p in posts) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("title", p.title)
                put("author", p.author)
                put("subreddit", p.subreddit)
                put("score", p.score)
                put("numComments", p.numComments)
                put("publishedTime", p.publishedTime)
                put("timestampMs", p.timestampMs)
                put("permalink", p.permalink)
                put("contentUrl", p.contentUrl)
                put("domain", p.domain)
                put("thumbnailUrl", p.thumbnailUrl ?: "")
                put("previewImageUrl", p.previewImageUrl ?: "")
                put("videoUrl", p.videoUrl ?: "")
                put("isVideo", p.isVideo)
                put("selfTextHtml", p.selfTextHtml ?: "")
                put("flair", p.flair ?: "")
                put("isNsfw", p.isNsfw)
                put("isSpoiler", p.isSpoiler)
                put("userVote", p.userVote)
                if (p.galleryImageUrls.isNotEmpty()) {
                    put("galleryImageUrls", JSONArray(p.galleryImageUrls))
                }
            }
            array.put(obj)
        }
        prefs.edit { putString(KEY_SAVED_POSTS, array.toString()) }
        _savedPosts.value = posts
    }

    fun toggleSavePost(post: RedditPost): Boolean {
        val current = _savedPosts.value.toMutableList()
        val index = current.indexOfFirst { it.id == post.id }
        val isNowSaved: Boolean

        if (index != -1) {
            current.removeAt(index)
            isNowSaved = false
        } else {
            current.add(0, post.copy(isSaved = true))
            isNowSaved = true
        }

        persistSavedPosts(current)
        return isNowSaved
    }

    /** Synchronizes a post with Reddit's server-side saved state. */
    fun setPostSaved(post: RedditPost, saved: Boolean) {
        val current = _savedPosts.value.toMutableList()
        current.removeAll { it.id == post.id }
        if (saved) current.add(0, post.copy(isSaved = true))
        persistSavedPosts(current)
    }

    fun isPostSaved(postId: String): Boolean {
        return _savedPosts.value.any { it.id == postId }
    }

    private fun loadFavoriteSubreddits(): List<String> {
        val saved = prefs.getStringSet(KEY_FAVORITES, setOf("technology", "android", "gaming", "AskReddit"))
        return saved?.toList() ?: listOf("technology", "android", "gaming", "AskReddit")
    }

    fun toggleFavoriteSubreddit(sub: String): Boolean {
        val clean = sub.trim().removePrefix("r/").removePrefix("/")
        val current = _favoriteSubreddits.value.toMutableList()
        val isNowFav: Boolean

        if (current.any { it.equals(clean, ignoreCase = true) }) {
            current.removeAll { it.equals(clean, ignoreCase = true) }
            isNowFav = false
        } else {
            current.add(0, clean)
            isNowFav = true
        }

        prefs.edit { putStringSet(KEY_FAVORITES, current.toSet()) }
        _favoriteSubreddits.value = current
        return isNowFav
    }

    fun isFavoriteSubreddit(sub: String): Boolean {
        val clean = sub.trim().removePrefix("r/").removePrefix("/")
        return _favoriteSubreddits.value.any { it.equals(clean, ignoreCase = true) }
    }

    companion object {
        private const val KEY_SAVED_POSTS = "saved_posts_json"
        private const val KEY_FAVORITES = "favorite_subreddits_set"
    }
}
