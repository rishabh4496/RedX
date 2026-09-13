package com.example.redx.network

import com.example.redx.util.RedditInputValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Writes post actions through Reddit's authenticated web API. */
object RedditPostActionService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private const val USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

    suspend fun vote(cookieHeader: String, postId: String, direction: Int): Result<Unit> =
        postAction(
            url = "https://www.reddit.com/api/vote",
            cookieHeader = cookieHeader,
            fields = mapOf(
                "id" to postId,
                "dir" to direction.coerceIn(-1, 1).toString(),
                "api_type" to "json"
            )
        )

    suspend fun setSaved(cookieHeader: String, postId: String, saved: Boolean): Result<Unit> =
        postAction(
            url = if (saved) "https://www.reddit.com/api/save" else "https://www.reddit.com/api/unsave",
            cookieHeader = cookieHeader,
            fields = mapOf(
                "id" to postId,
                "api_type" to "json"
            )
        )

    suspend fun crosspost(
        cookieHeader: String,
        sourcePostId: String,
        targetSubreddit: String,
        title: String
    ): Result<Unit> {
        val cleanSubreddit = RedditInputValidator.normalizeSubreddit(targetSubreddit)
            ?: return Result.failure(IllegalArgumentException("Invalid destination subreddit"))
        val cleanTitle = title.trim().take(300)
        if (cleanTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Crosspost title cannot be blank"))
        }
        val cleanId = if (sourcePostId.startsWith("t3_")) sourcePostId else "t3_$sourcePostId"
        return postAction(
            url = "https://www.reddit.com/api/submit",
            cookieHeader = cookieHeader,
            fields = mapOf(
                "kind" to "crosspost",
                "crosspost_fullname" to cleanId,
                "sr" to cleanSubreddit,
                "title" to cleanTitle,
                "api_type" to "json",
                "resubmit" to "true"
            )
        )
    }

    private suspend fun postAction(
        url: String,
        cookieHeader: String,
        fields: Map<String, String>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (cookieHeader.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No Reddit session cookie found"))
        }

        val primaryResult = executePostAction(url, cookieHeader, fields)
        if (primaryResult.isSuccess) {
            return@withContext primaryResult
        }

        // Fallback to old.reddit.com if www.reddit.com is rejected
        if (url.contains("www.reddit.com")) {
            val fallbackUrl = url.replace("www.reddit.com", "old.reddit.com")
            val fallbackResult = executePostAction(fallbackUrl, cookieHeader, fields)
            if (fallbackResult.isSuccess) {
                return@withContext fallbackResult
            }
        }

        primaryResult
    }

    private fun executePostAction(
        url: String,
        cookieHeader: String,
        fields: Map<String, String>
    ): Result<Unit> {
        return try {
            val modhash = fetchModhash(cookieHeader).getOrNull()
            val form = FormBody.Builder().apply {
                fields.forEach { (key, value) -> add(key, value) }
                if (!modhash.isNullOrBlank()) add("uh", modhash)
            }.build()
            val requestBuilder = Request.Builder()
                .url(url)
                .post(form)
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json, text/javascript, */*; q=0.01")
                .header("Origin", "https://www.reddit.com")
                .header("Referer", "https://www.reddit.com/")
                .header("Cookie", cookieHeader)
            if (!modhash.isNullOrBlank()) requestBuilder.header("X-Modhash", modhash)

            client.newCall(requestBuilder.build()).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return Result.failure(IllegalStateException("HTTP ${response.code} from Reddit"))
                }
                val errors = parseErrors(body)
                if (errors.isNotEmpty()) {
                    Result.failure(IllegalStateException(errors.joinToString("; ")))
                } else if (body.isNotBlank() && !body.trimStart().startsWith("{")) {
                    Result.failure(IllegalStateException("Reddit returned an unexpected action response"))
                } else {
                    Result.success(Unit)
                }
            }
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun fetchModhash(cookieHeader: String): Result<String> {
        val endpoints = listOf(
            "https://old.reddit.com/api/me.json?raw_json=1",
            "https://www.reddit.com/api/me.json?raw_json=1"
        )
        for (endpoint in endpoints) {
            try {
                val request = Request.Builder()
                    .url(endpoint)
                    .get()
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .header("Referer", "https://www.reddit.com/")
                    .header("Cookie", cookieHeader)
                    .build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string().orEmpty()
                        val modhash = JSONObject(body)
                            .optJSONObject("data")
                            ?.optString("modhash")
                            .orEmpty()
                        if (modhash.isNotBlank()) return Result.success(modhash)
                    }
                }
            } catch (_: Exception) {
                // Try next endpoint
            }
        }
        return Result.failure(IllegalStateException("Reddit did not return a modhash"))
    }

    private fun parseErrors(body: String): List<String> {
        return try {
            val errors = JSONObject(body).optJSONObject("json")?.optJSONArray("errors") ?: return emptyList()
            buildList {
                for (index in 0 until errors.length()) {
                    val error = errors.optJSONArray(index) ?: continue
                    val message = error.optString(1).trim()
                    if (message.isNotBlank()) add(message)
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
