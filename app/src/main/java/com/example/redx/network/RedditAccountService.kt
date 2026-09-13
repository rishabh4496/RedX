package com.example.redx.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Reads account data from Reddit using the authenticated web session created by the WebView.
 * No account value is inferred locally: a session is considered valid only after Reddit
 * returns an account object containing a real username.
 */
object RedditAccountService {

    data class AccountSnapshot(
        val username: String,
        val totalKarma: Int,
        val linkKarma: Int,
        val commentKarma: Int,
        val avatarUrl: String?,
        val followersCount: Int?,
        val followingCount: Int?,
        val subscribedSubreddits: List<String>?
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private const val BROWSER_USER_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

    suspend fun fetchAccount(cookieHeader: String): Result<AccountSnapshot> = withContext(Dispatchers.IO) {
        if (cookieHeader.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No Reddit session cookie found"))
        }

        val accountBody = ACCOUNT_ENDPOINTS.asSequence()
            .map { executeGet(it, cookieHeader) }
            .firstOrNull { result -> result.isSuccess && parseAccount(result.getOrNull().orEmpty()) != null }
            ?: return@withContext Result.failure(IllegalStateException("Reddit did not return an authenticated account"))

        val account = parseAccount(accountBody.getOrThrow())
            ?: return@withContext Result.failure(IllegalStateException("Reddit returned an invalid account response"))

        // Subscriptions are useful account data, but their endpoint can be unavailable for
        // some sessions. Try multiple known endpoints with browser headers.
        val subscriptions = SUBSCRIPTION_ENDPOINTS.asSequence()
            .map { executeGet(it, cookieHeader) }
            .mapNotNull { it.getOrNull()?.let(::parseSubscriptions) }
            .firstOrNull { it.isNotEmpty() }

        Result.success(account.copy(subscribedSubreddits = subscriptions))
    }

    private fun executeGet(url: String, cookieHeader: String): Result<String> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", BROWSER_USER_AGENT)
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Cookie", cookieHeader)
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body.string()
                if (!response.isSuccessful) {
                    Result.failure(IllegalStateException("HTTP ${response.code} from Reddit"))
                } else {
                    Result.success(body)
                }
            }
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun parseAccount(body: String): AccountSnapshot? {
        return try {
            val root = JSONObject(body)
            val data = root.optJSONObject("data") ?: root
            val username = data.optString("name").trim()
            if (username.isBlank() || username.equals("null", ignoreCase = true)) return null

            val linkKarma = data.optInt("link_karma", 0)
            val commentKarma = data.optInt("comment_karma", 0)
            val totalKarma = if (data.has("total_karma")) {
                data.optInt("total_karma", linkKarma + commentKarma)
            } else {
                linkKarma + commentKarma
            }
            val subreddit = data.optJSONObject("subreddit")
            val followers = subreddit?.takeIf { it.has("subscribers") && !it.isNull("subscribers") }
                ?.optInt("subscribers")
            val avatar = data.optString("icon_img").trim().takeIf { it.isNotBlank() && it != "null" }

            AccountSnapshot(
                username = username,
                totalKarma = totalKarma,
                linkKarma = linkKarma,
                commentKarma = commentKarma,
                avatarUrl = avatar,
                followersCount = followers,
                followingCount = null,
                subscribedSubreddits = null
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun parseSubscriptions(body: String): List<String>? {
        return try {
            val children = JSONObject(body)
                .optJSONObject("data")
                ?.optJSONArray("children")
                ?: return null

            buildList {
                for (index in 0 until children.length()) {
                    val subreddit = children.optJSONObject(index)?.optJSONObject("data") ?: continue
                    val displayName = subreddit.optString("display_name").trim()
                    if (displayName.isNotBlank()) add(displayName)
                }
            }.distinctBy(String::lowercase)
        } catch (_: Exception) {
            null
        }
    }

    private val ACCOUNT_ENDPOINTS = listOf(
        "https://www.reddit.com/api/v1/me?raw_json=1",
        "https://old.reddit.com/api/v1/me?raw_json=1",
        "https://www.reddit.com/api/me.json?raw_json=1",
        "https://old.reddit.com/api/me.json?raw_json=1",
        "https://www.reddit.com/user/me/about.json?raw_json=1"
    )

    private val SUBSCRIPTION_ENDPOINTS = listOf(
        "https://old.reddit.com/subreddits/mine/subscriber.json?limit=100&raw_json=1",
        "https://www.reddit.com/subreddits/mine/subscriber.json?limit=100&raw_json=1",
        "https://old.reddit.com/subreddits/mine.json?limit=100&raw_json=1",
        "https://www.reddit.com/subreddits/mine.json?limit=100&raw_json=1"
    )
}
