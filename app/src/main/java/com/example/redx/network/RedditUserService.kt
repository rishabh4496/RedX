package com.example.redx.network

import android.text.Html
import com.example.redx.model.RedditPost
import com.example.redx.util.RedditInputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URI
import java.util.Locale
import java.util.concurrent.TimeUnit

object RedditUserService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

    suspend fun fetchUserPosts(
        username: String,
        cookieHeader: String? = null,
        after: String? = null
    ): Result<List<RedditPost>> = withContext(Dispatchers.IO) {
        val cleanName = RedditInputValidator.normalizeUsername(username)
            ?: return@withContext Result.failure(IllegalArgumentException("Invalid Reddit username"))
        val afterParam = after?.trim()?.takeIf { it.isNotBlank() }
            ?.let { "&after=${java.net.URLEncoder.encode(it, "UTF-8")}" }
            .orEmpty()
        val url = "https://www.reddit.com/user/$cleanName/submitted.json?limit=25&raw_json=1$afterParam"
        try {
            val requestBuilder = Request.Builder()
                .url(url)
                .get()
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .header("Referer", "https://www.reddit.com/")
            if (!cookieHeader.isNullOrBlank()) {
                requestBuilder.header("Cookie", cookieHeader)
            }
            client.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP ${response.code}"))
                }
                val body = response.body.string()
                val posts = parseUserPosts(body, cleanName)
                Result.success(posts)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseUserPosts(body: String, username: String): List<RedditPost> {
        val posts = mutableListOf<RedditPost>()
        try {
            val root = JSONObject(body)
            val children = root.optJSONObject("data")?.optJSONArray("children") ?: return emptyList()
            for (i in 0 until children.length()) {
                val child = children.optJSONObject(i) ?: continue
                val kind = child.optString("kind")
                if (kind != "t3") continue
                val data = child.optJSONObject("data") ?: continue
                val post = parsePost(data, username) ?: continue
                posts.add(post)
            }
        } catch (_: Exception) {}
        return posts
    }

    private fun parsePost(data: JSONObject, fallbackAuthor: String): RedditPost? {
        val title = data.optString("title").trim()
        if (title.isBlank()) return null
        val rawId = data.optString("name").ifBlank { "t3_${System.currentTimeMillis()}" }
        val cleanId = if (rawId.startsWith("t3_")) rawId else "t3_$rawId"
        val subreddit = data.optString("subreddit").trim().ifBlank { "reddit" }
        val permalinkValue = data.optString("permalink").trim()
        val permalink = when {
            permalinkValue.startsWith("http", ignoreCase = true) -> permalinkValue
            permalinkValue.startsWith("/") -> "https://www.reddit.com$permalinkValue"
            else -> "https://www.reddit.com/$permalinkValue"
        }
        val contentUrl = data.optString("url").trim().takeIf { it.isNotBlank() && it != "null" } ?: permalink
        val domain = data.optString("domain").trim().removePrefix("www.").ifBlank {
            try { URI(contentUrl).host?.removePrefix("www.")?.lowercase(Locale.ROOT) ?: "reddit.com" }
            catch (_: Exception) { "reddit.com" }
        }
        val thumbnail = data.optString("thumbnail").trim().takeIf {
            it.isNotBlank() && it != "null" && it != "default" && it != "self" && it != "nsfw" &&
                (it.startsWith("http://") || it.startsWith("https://"))
        }
        val previewImage = data.optJSONObject("preview")
            ?.optJSONArray("images")?.optJSONObject(0)
            ?.optJSONObject("source")?.optString("url")
            ?.let { Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY).toString().trim() }
            ?.takeIf { it.isNotBlank() }
        val createdTime = (data.optDouble("created_utc", 0.0) * 1000).toLong()
            .takeIf { it > 0 } ?: System.currentTimeMillis()
        val videoUrl = data.optJSONObject("secure_media")?.optJSONObject("reddit_video")
            ?.let { it.optString("hls_url").ifBlank { it.optString("fallback_url") } }
            ?.replace("&amp;", "&")
            ?.takeIf { it.isNotBlank() }
        val isVideo = data.optBoolean("is_video", false) || !videoUrl.isNullOrBlank()
        val flair = data.optString("link_flair_text").trim().takeIf { it.isNotBlank() && it != "null" }

        val elapsed = ((System.currentTimeMillis() - createdTime) / 1000).coerceAtLeast(0)
        val timeLabel = when {
            elapsed < 60 -> "${elapsed}s"
            elapsed < 3600 -> "${elapsed / 60}m"
            elapsed < 86400 -> "${elapsed / 3600}h"
            elapsed < 2592000 -> "${elapsed / 86400}d"
            elapsed < 31536000 -> "${elapsed / 2592000}mo"
            else -> "${elapsed / 31536000}y"
        }

        return RedditPost(
            id = cleanId,
            title = title,
            author = data.optString("author").trim().takeIf { it.isNotBlank() && it != "null" } ?: fallbackAuthor,
            subreddit = subreddit,
            score = data.optInt("score", 0),
            numComments = data.optInt("num_comments", 0),
            publishedTime = timeLabel,
            timestampMs = createdTime,
            permalink = permalink,
            contentUrl = contentUrl,
            domain = domain,
            thumbnailUrl = thumbnail,
            previewImageUrl = previewImage,
            videoUrl = videoUrl,
            isVideo = isVideo,
            flair = flair,
            isNsfw = data.optBoolean("over_18", false),
            isSpoiler = data.optBoolean("spoiler", false)
        )
    }
}
