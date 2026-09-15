package com.example.redx.network

import android.text.Html
import com.example.redx.model.FeedSort
import com.example.redx.model.RedditPost
import com.example.redx.util.RedditInputValidator
import com.example.redx.util.UrlSafety
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.URI
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object RedditFeedService {

    private val inMemoryCookieStore = ConcurrentHashMap<String, MutableList<Cookie>>()

    private val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val hostKey = url.host
            val existing = inMemoryCookieStore.getOrPut(hostKey) { mutableListOf() }
            synchronized(existing) {
                cookies.forEach { newCookie ->
                    existing.removeAll { it.name.equals(newCookie.name, ignoreCase = true) }
                    existing.add(newCookie)
                }
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            val hostKey = url.host
            val now = System.currentTimeMillis()
            val list = inMemoryCookieStore[hostKey] ?: return emptyList()
            return synchronized(list) {
                list.removeAll { it.expiresAt < now }
                list.toList()
            }
        }
    }

    private val client = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private const val BROWSER_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

    private val imgPattern = Pattern.compile("<img\\s+[^>]*src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE)
    private val linkPattern = Pattern.compile("<a\\s+href=[\"']([^\"']+)[\"']>\\[link\\]</a>", Pattern.CASE_INSENSITIVE)
    private val commentPattern = Pattern.compile("<a\\s+href=[\"']([^\"']+)[\"']>\\[comments\\]</a>", Pattern.CASE_INSENSITIVE)

    data class CacheEntry(
        val posts: List<RedditPost>,
        val timestamp: Long
    )
    private val feedCache = ConcurrentHashMap<String, CacheEntry>()

    fun getCachedFeed(subreddit: String, sort: FeedSort, includeMature: Boolean): List<RedditPost>? {
        val cleanSub = normalizeSubreddit(subreddit) ?: return null
        val safeSort = if (sort == FeedSort.RELEVANCE) FeedSort.HOT else sort
        val key = "$cleanSub:${safeSort.apiValue}:$includeMature"
        val entry = feedCache[key] ?: return null
        // Cache valid for 3 minutes
        if (System.currentTimeMillis() - entry.timestamp < 3 * 60 * 1000) {
            return entry.posts
        }
        return null
    }

    /**
     * Fetch feed for standard subreddits, with automatic search fallback for mature/NSFW subreddits.
     */
    suspend fun fetchFeed(
        subreddit: String,
        sort: FeedSort = FeedSort.HOT,
        cookieHeader: String? = null,
        after: String? = null,
        includeMature: Boolean = true,
        forceRefresh: Boolean = false
    ): Result<List<RedditPost>> = withContext(Dispatchers.IO) {
        val cleanSub = normalizeSubreddit(subreddit)
            ?: return@withContext Result.failure(IllegalArgumentException("Invalid subreddit name"))
        val safeSort = if (sort == FeedSort.RELEVANCE) FeedSort.HOT else sort
        val cacheKey = "$cleanSub:${safeSort.apiValue}:$includeMature"

        if (after == null && !forceRefresh) {
            val cached = feedCache[cacheKey]
            if (cached != null && System.currentTimeMillis() - cached.timestamp < 3 * 60 * 1000) {
                return@withContext Result.success(cached.posts)
            }
        }

        val afterParam = after?.trim()?.takeIf { it.isNotBlank() }?.let {
            "&after=${URLEncoder.encode(it, "UTF-8")}"
        } ?: ""

        // JSON listings require an authenticated session cookie. Unauthenticated .json calls
        // are rejected with 403 by Reddit, which quickly trips anti-bot IP rate-limiting.
        val hasSessionCookie = !cookieHeader.isNullOrBlank() && cookieHeader.contains("reddit_session", ignoreCase = true)
        if (hasSessionCookie) {
            val jsonUrl = buildJsonFeedUrl(cleanSub, safeSort, after)
            val jsonAttempt = executeJsonRequest(jsonUrl, cleanSub, cookieHeader, includeMature)
            if (jsonAttempt.isSuccess && jsonAttempt.getOrNull()?.isNotEmpty() == true) {
                val posts = jsonAttempt.getOrNull()!!
                if (after == null) {
                    feedCache[cacheKey] = CacheEntry(posts, System.currentTimeMillis())
                }
                return@withContext jsonAttempt
            }
        }

        // Standard RSS/Atom feed is Reddit's publicly supported unauthenticated syndication format.
        val standardUrl = buildFeedUrl(cleanSub, safeSort) + afterParam
        val firstAttempt = executeRequest(standardUrl, cleanSub, cookieHeader, includeMature)

        if (firstAttempt.isSuccess && firstAttempt.getOrNull()?.isNotEmpty() == true) {
            val posts = firstAttempt.getOrNull()!!
            if (after == null) {
                feedCache[cacheKey] = CacheEntry(posts, System.currentTimeMillis())
            }
            return@withContext firstAttempt
        }

        // If network request failed with rate limit (429), do not trigger another immediate request.
        // Return cached posts if available or return the rate limit error directly.
        val firstError = firstAttempt.exceptionOrNull()
        if (firstError?.message?.contains("429") == true) {
            if (after == null) {
                feedCache[cacheKey]?.let { cached ->
                    return@withContext Result.success(cached.posts)
                }
            }
            return@withContext firstAttempt
        }

        // If network request failed but we have a cached copy, return cached posts gracefully
        if (after == null) {
            feedCache[cacheKey]?.let { cached ->
                return@withContext Result.success(cached.posts)
            }
        }

        // If standard feed is empty or failed (typical for mature/NSFW subreddits that 302 to login),
        // use the search endpoint with include_over_18=on.
        // Note: Reddit search RSS accepts "relevance", "hot", "top", "new". For "rising", use "new".
        val searchSort = when (safeSort) {
            FeedSort.RISING -> "new"
            FeedSort.RELEVANCE -> "relevance"
            else -> safeSort.apiValue
        }
        val fallbackSearchUrl = if (cleanSub.isEmpty() || cleanSub.equals("home", true) || cleanSub.equals("popular", true) || cleanSub.equals("all", true)) {
            "https://www.reddit.com/search.rss?q=*&include_over_18=${if (includeMature) "on" else "off"}&sort=$searchSort&limit=50$afterParam"
        } else {
            "https://www.reddit.com/r/$cleanSub/search.rss?q=*&restrict_sr=on&include_over_18=${if (includeMature) "on" else "off"}&sort=$searchSort&limit=50$afterParam"
        }

        val searchAttempt = executeRequest(fallbackSearchUrl, cleanSub, cookieHeader, includeMature)
        if (searchAttempt.isSuccess && searchAttempt.getOrNull()?.isNotEmpty() == true) {
            val posts = searchAttempt.getOrNull()!!
            if (after == null) {
                feedCache[cacheKey] = CacheEntry(posts, System.currentTimeMillis())
            }
            return@withContext searchAttempt
        }

        // Return first error or empty result
        return@withContext firstAttempt
    }

    /**
     * Advanced Search across all Reddit or within a specific subreddit.
     */
    suspend fun searchReddit(
        query: String,
        subreddit: String? = null,
        sort: FeedSort = FeedSort.RELEVANCE,
        includeMature: Boolean = true,
        cookieHeader: String? = null
    ): Result<List<RedditPost>> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Search query cannot be blank"))
        }

        val cleanSub = subreddit?.trim()?.takeIf { it.isNotBlank() }?.let(::normalizeSubreddit)
        if (subreddit != null && !subreddit.isNullOrBlank() && cleanSub == null) {
            return@withContext Result.failure(IllegalArgumentException("Invalid subreddit name"))
        }
        val encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8")
        val matureParam = if (includeMature) "on" else "off"
        val sortParam = if (sort == FeedSort.HOT) "hot" else if (sort == FeedSort.NEW) "new" else if (sort == FeedSort.TOP) "top" else "relevance"

        val searchUrl = if (!cleanSub.isNullOrBlank() && !cleanSub.equals("all", true) && !cleanSub.equals("popular", true)) {
            "https://www.reddit.com/r/$cleanSub/search.rss?q=$encodedQuery&restrict_sr=on&include_over_18=$matureParam&sort=$sortParam&limit=50"
        } else {
            "https://www.reddit.com/search.rss?q=$encodedQuery&include_over_18=$matureParam&sort=$sortParam&limit=50"
        }

        val hasSessionCookie = !cookieHeader.isNullOrBlank() && cookieHeader.contains("reddit_session", ignoreCase = true)
        if (hasSessionCookie) {
            val jsonSearchUrl = buildJsonSearchUrl(cleanSub, cleanQuery, sort)
            val jsonAttempt = executeJsonRequest(jsonSearchUrl, cleanSub ?: "all", cookieHeader, includeMature)
            if (jsonAttempt.isSuccess && jsonAttempt.getOrNull()?.isNotEmpty() == true) {
                return@withContext jsonAttempt
            }
        }

        executeRequest(searchUrl, cleanSub ?: "all", cookieHeader, includeMature)
    }

    private fun executeJsonRequest(
        url: String,
        fallbackSubreddit: String,
        cookieHeader: String?,
        includeMature: Boolean
    ): Result<List<RedditPost>> {
        return try {
            val requestBuilder = Request.Builder()
                .url(url)
                .header("User-Agent", BROWSER_USER_AGENT)
                .header("Accept", "application/json")
                .header("Accept-Language", "en-US,en;q=0.9")

            val cookies = buildCookieHeader(cookieHeader, includeMature)
            if (cookies.isNotBlank()) requestBuilder.header("Cookie", cookies)

            client.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    if (response.code == 429) {
                        val resetSec = response.header("x-ratelimit-reset")?.toIntOrNull()
                        val msg = if (resetSec != null && resetSec > 0) {
                            "Reddit rate limit reached (HTTP 429). Resets in $resetSec seconds."
                        } else {
                            "Reddit rate limit reached (HTTP 429). Please wait a moment."
                        }
                        return Result.failure(Exception(msg))
                    }
                    return Result.failure(Exception("HTTP ${response.code}: ${response.message.ifBlank { "Request failed" }}"))
                }
                val body = response.body.string()
                if (body.isBlank()) return Result.failure(Exception("Empty response body"))
                Result.success(parseJsonListing(body, fallbackSubreddit))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun executeRequest(
        url: String,
        fallbackSubreddit: String,
        cookieHeader: String?,
        includeMature: Boolean
    ): Result<List<RedditPost>> {
        try {
            val requestBuilder = Request.Builder()
                .url(url)
                .header("User-Agent", BROWSER_USER_AGENT)
                .header("Accept", "application/atom+xml,application/xml,text/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")

            val cookies = buildCookieHeader(cookieHeader, includeMature)
            if (cookies.isNotBlank()) requestBuilder.header("Cookie", cookies)

            client.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    if (response.code == 429) {
                        val resetSec = response.header("x-ratelimit-reset")?.toIntOrNull()
                        val msg = if (resetSec != null && resetSec > 0) {
                            "Reddit rate limit reached (HTTP 429). Resets in $resetSec seconds."
                        } else {
                            "Reddit rate limit reached (HTTP 429). Please wait a moment."
                        }
                        return Result.failure(Exception(msg))
                    }
                    return Result.failure(Exception("HTTP ${response.code}: ${response.message.ifBlank { "Request failed" }}"))
                }

                val body = response.body.string()
                if (body.isBlank()) return Result.failure(Exception("Empty response body"))
                return Result.success(parseAtomFeed(body, fallbackSubreddit))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    internal fun buildFeedUrl(cleanSub: String, sort: FeedSort): String {
        val validSort = if (sort == FeedSort.RELEVANCE) FeedSort.HOT else sort
        val sortSegment = if (validSort == FeedSort.HOT) "" else "${validSort.apiValue}/"
        return when {
            cleanSub.isEmpty() || cleanSub.equals("home", ignoreCase = true) ->
                "https://www.reddit.com/${sortSegment}.rss?limit=50"
            cleanSub.equals("popular", ignoreCase = true) ->
                "https://www.reddit.com/r/popular/${sortSegment}.rss?limit=50"
            cleanSub.equals("all", ignoreCase = true) ->
                "https://www.reddit.com/r/all/${sortSegment}.rss?limit=50"
            else ->
                "https://www.reddit.com/r/$cleanSub/${sortSegment}.rss?limit=50"
        }
    }

    internal fun buildJsonFeedUrl(cleanSub: String, sort: FeedSort, after: String? = null): String {
        val sortSegment = when (sort) {
            FeedSort.HOT -> "hot"
            FeedSort.NEW -> "new"
            FeedSort.TOP -> "top"
            FeedSort.RISING -> "rising"
            FeedSort.RELEVANCE -> "hot"
        }
        val base = when {
            cleanSub.isEmpty() || cleanSub.equals("home", ignoreCase = true) ->
                "https://www.reddit.com/$sortSegment.json"
            cleanSub.equals("popular", ignoreCase = true) ->
                "https://www.reddit.com/r/popular/$sortSegment.json"
            cleanSub.equals("all", ignoreCase = true) ->
                "https://www.reddit.com/r/all/$sortSegment.json"
            else ->
                "https://www.reddit.com/r/$cleanSub/$sortSegment.json"
        }
        val afterParam = after?.trim()?.takeIf { it.isNotBlank() }
            ?.let { "&after=${URLEncoder.encode(it, "UTF-8")}" }
            .orEmpty()
        return "$base?limit=50&raw_json=1$afterParam"
    }

    internal fun buildJsonSearchUrl(subreddit: String?, query: String, sort: FeedSort): String {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val sortParam = when (sort) {
            FeedSort.HOT -> "hot"
            FeedSort.NEW -> "new"
            FeedSort.TOP -> "top"
            FeedSort.RISING -> "new"
            FeedSort.RELEVANCE -> "relevance"
        }
        val base = if (!subreddit.isNullOrBlank() &&
            !subreddit.equals("all", true) &&
            !subreddit.equals("popular", true)
        ) {
            "https://www.reddit.com/r/$subreddit/search.json"
        } else {
            "https://www.reddit.com/search.json"
        }
        val restrict = if (!subreddit.isNullOrBlank() &&
            !subreddit.equals("all", true) &&
            !subreddit.equals("popular", true)
        ) "&restrict_sr=on" else ""
        return "$base?q=$encodedQuery&sort=$sortParam&limit=50&raw_json=1$restrict"
    }

    private fun buildCookieHeader(cookieHeader: String?, includeMature: Boolean): String {
        val cleanCookies = cookieHeader.orEmpty()
            .split(';')
            .map(String::trim)
            .filter { it.isNotBlank() }
            .filter { cookie ->
                includeMature || MATURE_COOKIE_NAMES.none {
                    cookie.startsWith("$it=", ignoreCase = true)
                }
            }
            .toMutableList()

        if (includeMature && cleanCookies.none { it.startsWith("over18=", ignoreCase = true) }) {
            cleanCookies += "over18=1"
            cleanCookies += "mweb_nx_over18=1"
        }
        return cleanCookies.joinToString("; ")
    }

    private fun parseJsonListing(json: String, fallbackSubreddit: String): List<RedditPost> {
        val children = JSONObject(json).optJSONObject("data")?.optJSONArray("children") ?: JSONArray()
        return buildList {
            for (index in 0 until children.length()) {
                val data = children.optJSONObject(index)?.optJSONObject("data") ?: continue
                val post = parseJsonPost(data, fallbackSubreddit) ?: continue
                add(post)
            }
        }
    }

    private fun parseJsonPost(data: JSONObject, fallbackSubreddit: String): RedditPost? {
        // Reddit can include promoted listings in normal JSON feeds. They are not user posts
        // and must never enter RedX's feed when the app is intended to be ad-free.
        if (data.optBoolean("promoted", false) ||
            data.optBoolean("promoted_by", false) ||
            data.optBoolean("is_promoted", false) ||
            data.optString("post_hint").equals("promoted", ignoreCase = true)
        ) return null

        val title = data.optString("title").trim()
        if (title.isBlank()) return null

        val rawId = data.optString("name").trim().ifBlank {
            data.optString("id").trim()
        }
        if (rawId.isBlank()) return null
        val idWithoutPrefix = if (rawId.startsWith("t3_", ignoreCase = true)) rawId.substring(3) else rawId
        val cleanId = "t3_$idWithoutPrefix".takeIf { idWithoutPrefix.isNotBlank() } ?: return null
        val subreddit = data.optString("subreddit").trim().ifBlank { fallbackSubreddit.ifBlank { "reddit" } }
        val permalinkValue = data.optString("permalink").trim()
        val permalink = when {
            permalinkValue.startsWith("http://", true) || permalinkValue.startsWith("https://", true) -> permalinkValue
            permalinkValue.startsWith("/") -> "https://www.reddit.com$permalinkValue"
            else -> permalinkValue
        }
        val contentUrl = data.optString("url").trim().takeIf { it.isNotBlank() && it != "null" }
            ?: permalink
        val domain = data.optString("domain").trim().removePrefix("www.").ifBlank {
            try { URI(contentUrl).host?.removePrefix("www.")?.lowercase(Locale.ROOT) ?: "reddit.com" }
            catch (_: Exception) { "reddit.com" }
        }
        val galleryImages = extractGalleryImages(data)
        val previewImage = galleryImages.firstOrNull { UrlSafety.isHttpsUrl(it) } ?: parsePreviewImage(data)
        val thumbnail = cleanRedditMediaUrl(data.optString("thumbnail"))
        val videoUrl = extractVideoUrl(data, contentUrl, domain)
        val createdTime = (data.optDouble("created_utc", 0.0) * 1000).toLong()
            .takeIf { it > 0 } ?: System.currentTimeMillis()
        val likes = when {
            data.isNull("likes") -> 0
            data.optBoolean("likes", false) -> 1
            else -> -1
        }
        val directImage = UrlSafety.hasExtension(contentUrl, "jpg", "png", "jpeg", "webp", "gif") ||
            domain.equals("i.redd.it", true) || domain.equals("i.imgur.com", true)
        val isVideo = data.optBoolean("is_video", false) || !videoUrl.isNullOrBlank() ||
            domain.contains("v.redd.it", true) || UrlSafety.hasExtension(contentUrl, "mp4", "webm", "gifv")
        val flair = data.optString("link_flair_text").trim().takeIf { it.isNotBlank() && it != "null" }

        val rawSelfText = data.optString("selftext").trim()
        val rawSelfTextHtml = data.optString("selftext_html").trim()
        val selfText = when {
            rawSelfText.isNotBlank() && rawSelfText != "null" -> rawSelfText
            rawSelfTextHtml.isNotBlank() && rawSelfTextHtml != "null" && rawSelfTextHtml != "<p></p>" -> {
                try {
                    Html.fromHtml(rawSelfTextHtml, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                } catch (_: Exception) {
                    rawSelfTextHtml
                }
            }
            else -> null
        }

        val allAwardings = data.optJSONArray("all_awardings")
        val awardsCount = allAwardings?.length() ?: 0
        val topAwardIcon = allAwardings?.optJSONObject(0)
            ?.optJSONArray("resized_icons")
            ?.let { icons ->
                val idx = icons.length() - 1
                if (idx >= 0) icons.optJSONObject(idx)?.optString("url") else null
            }?.takeIf { it.isNotBlank() }

        return RedditPost(
            id = cleanId,
            title = title,
            author = data.optString("author").trim().takeIf { it.isNotBlank() && it != "null" } ?: "[deleted]",
            subreddit = subreddit,
            score = data.optInt("score", 0),
            numComments = data.optInt("num_comments", 0),
            publishedTime = formatRelativeTime(createdTime),
            timestampMs = createdTime,
            permalink = permalink,
            contentUrl = if (galleryImages.isNotEmpty()) galleryImages.first() else contentUrl,
            domain = domain,
            thumbnailUrl = thumbnail,
            previewImageUrl = previewImage ?: if (directImage) contentUrl else null,
            videoUrl = videoUrl,
            isVideo = isVideo,
            selfTextHtml = selfText,
            flair = flair,
            isNsfw = data.optBoolean("over_18", false),
            isSpoiler = data.optBoolean("spoiler", false),
            userVote = likes,
            isSaved = data.optBoolean("saved", false),
            galleryImageUrls = galleryImages,
            awardsCount = awardsCount,
            topAwardIcon = topAwardIcon
        )
    }

    private fun extractGalleryImages(data: JSONObject): List<String> {
        val galleryData = data.optJSONObject("gallery_data") ?: return emptyList()
        val mediaMetadata = data.optJSONObject("media_metadata") ?: return emptyList()
        val items = galleryData.optJSONArray("items") ?: return emptyList()
        val urls = mutableListOf<String>()

        for (i in 0 until items.length()) {
            val item = items.optJSONObject(i) ?: continue
            val mediaId = item.optString("media_id").trim()
            if (mediaId.isBlank()) continue

            val meta = mediaMetadata.optJSONObject(mediaId) ?: continue
            val status = meta.optString("status")
            if (status.isNotBlank() && status != "valid") continue

            val sObj = meta.optJSONObject("s")
            val rawUrl = sObj?.optString("u")?.takeIf { it.isNotBlank() }
                ?: sObj?.optString("gif")?.takeIf { it.isNotBlank() }
                ?: meta.optJSONArray("p")?.let { pArr ->
                    pArr.optJSONObject(pArr.length() - 1)?.optString("u")
                }

            val cleaned = cleanRedditMediaUrl(rawUrl)
            if (!cleaned.isNullOrBlank()) {
                urls.add(cleaned)
            }
        }
        return urls
    }

    private fun cleanRedditMediaUrl(rawUrl: String?): String? {
        if (rawUrl.isNullOrBlank()) return null
        val trimmed = rawUrl.trim()
        if (!trimmed.startsWith("http://", true) && !trimmed.startsWith("https://", true)) return null
        return try {
            Html.fromHtml(trimmed, Html.FROM_HTML_MODE_LEGACY).toString().trim()
        } catch (_: Exception) {
            trimmed.replace("&amp;", "&")
        }
    }

    private fun extractVideoUrl(data: JSONObject, contentUrl: String, domain: String): String? {
        // 1. Direct reddit_video in secure_media or media
        val directVideo = data.optJSONObject("secure_media")?.optJSONObject("reddit_video")
            ?: data.optJSONObject("media")?.optJSONObject("reddit_video")
        val directHls = cleanRedditMediaUrl(directVideo?.optString("hls_url"))
        if (!directHls.isNullOrBlank()) return directHls
        val directFallback = cleanRedditMediaUrl(directVideo?.optString("fallback_url"))
        if (!directFallback.isNullOrBlank()) return directFallback

        // 2. Crosspost reddit_video
        val crosspost = data.optJSONArray("crosspost_parent_list")?.optJSONObject(0)
        if (crosspost != null) {
            val crossVideo = crosspost.optJSONObject("secure_media")?.optJSONObject("reddit_video")
                ?: crosspost.optJSONObject("media")?.optJSONObject("reddit_video")
            val crossHls = cleanRedditMediaUrl(crossVideo?.optString("hls_url"))
            if (!crossHls.isNullOrBlank()) return crossHls
            val crossFallback = cleanRedditMediaUrl(crossVideo?.optString("fallback_url"))
            if (!crossFallback.isNullOrBlank()) return crossFallback
        }

        // 3. Preview reddit_video_preview (GIFs and video previews)
        val previewVideo = data.optJSONObject("preview")?.optJSONObject("reddit_video_preview")
            ?: crosspost?.optJSONObject("preview")?.optJSONObject("reddit_video_preview")
        val previewHls = cleanRedditMediaUrl(previewVideo?.optString("hls_url"))
        if (!previewHls.isNullOrBlank()) return previewHls
        val previewFallback = cleanRedditMediaUrl(previewVideo?.optString("fallback_url"))
        if (!previewFallback.isNullOrBlank()) return previewFallback

        // 4. Preview mp4 variant (Reddit auto-transcoded MP4 for animated GIFs)
        val mp4Variant = cleanRedditMediaUrl(
            data.optJSONObject("preview")
                ?.optJSONArray("images")
                ?.optJSONObject(0)
                ?.optJSONObject("variants")
                ?.optJSONObject("mp4")
                ?.optJSONObject("source")
                ?.optString("url")
        )
        if (!mp4Variant.isNullOrBlank()) return mp4Variant

        // 5. Imgur gifv -> direct mp4
        if (domain.contains("imgur.com", ignoreCase = true) && contentUrl.endsWith(".gifv", ignoreCase = true)) {
            return contentUrl.substringBeforeLast(".gifv") + ".mp4"
        }

        // 6. Direct video link (.mp4, .webm, .m3u8)
        if (UrlSafety.hasExtension(contentUrl, "mp4", "webm", "m3u8")) {
            return cleanRedditMediaUrl(contentUrl)
        }

        // 7. v.redd.it domain
        if (domain.contains("v.redd.it", ignoreCase = true) || contentUrl.contains("v.redd.it/", ignoreCase = true)) {
            val vId = contentUrl.substringAfter("v.redd.it/").substringBefore("/").substringBefore("?").trim()
            if (vId.isNotBlank()) {
                return "https://v.redd.it/$vId/HLSPlaylist.m3u8"
            }
        }

        // 8. redgifs.com
        if (domain.contains("redgifs.com", ignoreCase = true) || contentUrl.contains("redgifs.com", ignoreCase = true)) {
            return contentUrl
        }

        return null
    }

    private fun parsePreviewImage(data: JSONObject): String? {
        return try {
            val imagesArray = data.optJSONObject("preview")?.optJSONArray("images")
            val firstImage = imagesArray?.optJSONObject(0)

            // Check for animated GIF variant first
            val gifVariant = cleanRedditMediaUrl(
                firstImage?.optJSONObject("variants")
                    ?.optJSONObject("gif")
                    ?.optJSONObject("source")
                    ?.optString("url")
            )
            if (!gifVariant.isNullOrBlank()) return gifVariant

            val source = firstImage?.optJSONObject("source")
            cleanRedditMediaUrl(source?.optString("url"))
        } catch (_: Exception) {
            null
        }
    }

    private fun normalizeSubreddit(raw: String): String? =
        RedditInputValidator.normalizeSubreddit(raw)

    private fun parseAtomFeed(xml: String, fallbackSubreddit: String): List<RedditPost> {
        val posts = mutableListOf<RedditPost>()
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var eventType = parser.eventType
        var inEntry = false

        var id = ""
        var title = ""
        var author = ""
        var subreddit = fallbackSubreddit
        var permalink = ""
        var updatedStr = ""
        var contentHtml = ""
        var thumbnailUrl: String? = null
        var flair: String? = null

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name?.lowercase(Locale.ROOT) ?: ""

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    if (tagName == "entry") {
                        inEntry = true
                        id = ""
                        title = ""
                        author = ""
                        subreddit = fallbackSubreddit
                        permalink = ""
                        updatedStr = ""
                        contentHtml = ""
                        thumbnailUrl = null
                        flair = null
                    } else if (inEntry) {
                        when (tagName) {
                            "id" -> id = parser.nextText()
                            "title" -> title = parser.nextText()
                            "updated" -> updatedStr = parser.nextText()
                            "author" -> {
                                // Nested <name>
                            }
                            "name" -> {
                                val rawAuthor = parser.nextText()
                                author = rawAuthor.removePrefix("/u/").removePrefix("u/")
                            }
                            "category" -> {
                                val term = parser.getAttributeValue(null, "term") ?: ""
                                val label = parser.getAttributeValue(null, "label") ?: ""
                                if (term.isNotBlank()) {
                                    subreddit = term
                                }
                                if (label.isNotBlank() && label != term) {
                                    flair = label
                                }
                            }
                            "link" -> {
                                val href = parser.getAttributeValue(null, "href") ?: ""
                                val rel = parser.getAttributeValue(null, "rel") ?: "alternate"
                                if (rel == "alternate" && href.isNotBlank()) {
                                    permalink = href
                                }
                            }
                            "media:thumbnail", "thumbnail" -> {
                                val thumb = parser.getAttributeValue(null, "url")
                                if (!thumb.isNullOrBlank()) {
                                    val decoded = Html.fromHtml(thumb, Html.FROM_HTML_MODE_LEGACY).toString().trim()
                                    if (decoded.startsWith("http://", ignoreCase = true) || decoded.startsWith("https://", ignoreCase = true)) {
                                        thumbnailUrl = decoded
                                    }
                                }
                            }
                            "content" -> {
                                contentHtml = parser.nextText()
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (tagName == "entry" && inEntry) {
                        inEntry = false
                        val decodedContent = Html.fromHtml(contentHtml, Html.FROM_HTML_MODE_LEGACY).toString()

                        // Extract external link [link]
                        var contentUrl = permalink
                        val linkMatcher = linkPattern.matcher(contentHtml)
                        if (linkMatcher.find()) {
                            val rawLink = linkMatcher.group(1) ?: ""
                            contentUrl = Html.fromHtml(rawLink, Html.FROM_HTML_MODE_LEGACY).toString()
                        }

                        // Extract comments link if permalink is empty
                        if (permalink.isBlank()) {
                            val commentMatcher = commentPattern.matcher(contentHtml)
                            if (commentMatcher.find()) {
                                val rawComment = commentMatcher.group(1) ?: ""
                                permalink = Html.fromHtml(rawComment, Html.FROM_HTML_MODE_LEGACY).toString()
                            }
                        }

                        // Domain extraction
                        val domain = try {
                            val uri = URI(if (contentUrl.isNotBlank()) contentUrl else permalink)
                            uri.host?.removePrefix("www.")?.lowercase(Locale.ROOT) ?: "reddit.com"
                        } catch (e: Exception) {
                            "reddit.com"
                        }

                        // 1. FULL HD Image & GIF Extraction (Prioritize pristine source over 140px thumbnail)
                        var fullHdImage: String? = null
                        val isDirectImage = UrlSafety.hasExtension(contentUrl, "jpg", "png", "jpeg", "webp", "gif") ||
                                domain.equals("i.redd.it", ignoreCase = true) ||
                                domain.equals("i.imgur.com", ignoreCase = true)

                        if (isDirectImage) {
                            fullHdImage = contentUrl
                        } else {
                            val imgMatcher = imgPattern.matcher(contentHtml)
                            if (imgMatcher.find()) {
                                val rawUrl = Html.fromHtml(imgMatcher.group(1) ?: "", Html.FROM_HTML_MODE_LEGACY).toString().trim()
                                fullHdImage = rawUrl.replace("&amp;", "&")
                            }
                        }

                        val previewImage = fullHdImage?.takeIf { it.isNotBlank() } ?: thumbnailUrl

                        // 2. High-Performance Video Detection & Resolution
                        var isVideo = false
                        var videoUrl: String? = null

                        if (domain.contains("v.redd.it") || contentUrl.contains("v.redd.it")) {
                            isVideo = true
                            val vId = contentUrl.substringAfter("v.redd.it/").substringBefore("/").substringBefore("?").trim()
                            videoUrl = if (vId.isNotBlank()) {
                                "https://v.redd.it/$vId/HLSPlaylist.m3u8"
                            } else {
                                contentUrl
                            }
                        } else if (domain.contains("redgifs.com") || contentUrl.contains("redgifs.com")) {
                            isVideo = true
                            val redgifId = contentUrl.substringAfterLast("/watch/").substringAfterLast("/").substringBefore("?")
                            videoUrl = "https://www.redgifs.com/ifr/$redgifId?autoplay=1&muted=1"
                        } else if (UrlSafety.hasExtension(contentUrl, "mp4", "webm")) {
                            isVideo = true
                            videoUrl = contentUrl
                        } else if (UrlSafety.hasExtension(contentUrl, "gifv")) {
                            isVideo = true
                            videoUrl = contentUrl.replace(Regex("\\.gifv(?=($|[?#]))", RegexOption.IGNORE_CASE), ".mp4")
                        }

                        // NSFW detection
                        val isNsfw = subreddit.contains("nsfw", ignoreCase = true) ||
                                flair?.contains("nsfw", ignoreCase = true) == true ||
                                flair?.contains("18+", ignoreCase = true) == true ||
                                title.contains("nsfw", ignoreCase = true)

                        val parsedTime = parseIsoTimestamp(updatedStr)
                        val relativeTime = formatRelativeTime(parsedTime)

                        val cleanId = when {
                            id.contains("t3_") -> "t3_" + id.substringAfter("t3_").substringBefore("/").substringBefore("?")
                            id.contains("/comments/") -> "t3_" + id.substringAfter("/comments/").substringBefore("/")
                            id.isNotBlank() -> if (id.startsWith("t3_")) id else "t3_$id"
                            else -> "t3_${posts.size}"
                        }

                        if (title.isNotBlank()) {
                            posts.add(
                                RedditPost(
                                    id = cleanId,
                                    title = Html.fromHtml(title, Html.FROM_HTML_MODE_LEGACY).toString().trim(),
                                    author = if (author.isNotBlank()) author else "reddit_user",
                                    subreddit = subreddit.ifBlank { "reddit" },
                                    // Atom feeds do not expose these values. JSON is the primary
                                    // path and supplies real metrics; never invent numbers here.
                                    score = 0,
                                    numComments = 0,
                                    publishedTime = relativeTime,
                                    timestampMs = parsedTime,
                                    permalink = permalink,
                                    contentUrl = contentUrl,
                                    domain = domain,
                                    thumbnailUrl = previewImage,
                                    previewImageUrl = previewImage,
                                    videoUrl = videoUrl,
                                    isVideo = isVideo,
                                    selfTextHtml = decodedContent.trim().takeIf { it.isNotBlank() },
                                    flair = flair,
                                    isNsfw = isNsfw
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        return posts
    }

    private fun parseIsoTimestamp(isoString: String): Long {
        if (isoString.isBlank()) return System.currentTimeMillis()
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.parse(isoString)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            try {
                val sdf2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                sdf2.timeZone = TimeZone.getTimeZone("UTC")
                sdf2.parse(isoString)?.time ?: System.currentTimeMillis()
            } catch (e2: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    fun formatRelativeTime(timeMs: Long): String {
        val diff = System.currentTimeMillis() - timeMs
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 365 -> "${days / 365}y"
            days > 30 -> "${days / 30}mo"
            days > 0 -> "${days}d"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "just now"
        }
    }

    private val MATURE_COOKIE_NAMES = setOf("over18", "mweb_nx_over18", "prompt_shown", "country_code")
}
