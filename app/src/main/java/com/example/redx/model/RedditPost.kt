package com.example.redx.model

import com.example.redx.util.UrlSafety
import java.util.Locale

data class RedditPost(
    val id: String,
    val title: String,
    val author: String,
    val subreddit: String,
    val score: Int = 1,
    val numComments: Int = 0,
    val publishedTime: String = "",
    val timestampMs: Long = System.currentTimeMillis(),
    val permalink: String = "",
    val contentUrl: String = "",
    val domain: String = "",
    val thumbnailUrl: String? = null,
    val previewImageUrl: String? = null,
    val videoUrl: String? = null,
    val isVideo: Boolean = false,
    val selfTextHtml: String? = null,
    val flair: String? = null,
    val isNsfw: Boolean = false,
    val isSpoiler: Boolean = false,
    val userVote: Int = 0, // -1, 0, 1
    val isSaved: Boolean = false,
    val isRead: Boolean = false,
    val galleryImageUrls: List<String> = emptyList(),
    val awardsCount: Int = 0,
    val topAwardIcon: String? = null
) {
    val isGallery: Boolean
        get() = galleryImageUrls.isNotEmpty()

    val displayScore: String
        get() = when {
            score >= 1_000_000 -> String.format(Locale.US, "%.1fM", score / 1_000_000.0)
            score >= 1_000 -> String.format(Locale.US, "%.1fk", score / 1_000.0)
            else -> score.toString()
        }

    val displayComments: String
        get() = when {
            numComments >= 1_000 -> String.format(Locale.US, "%.1fk", numComments / 1_000.0)
            else -> numComments.toString()
        }

    val cleanDomain: String
        get() = domain.removePrefix("www.").takeIf { it.isNotBlank() } ?: "reddit.com"

    val cleanSelfText: String?
        get() {
            val raw = selfTextHtml?.trim() ?: return null
            if (raw.isBlank() || raw == "<p></p>" || raw.contains("<!-- SC_OFF --><div class=\"md\"><p></p></div><!-- SC_ON -->")) return null
            return try {
                android.text.Html.fromHtml(raw, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
            } catch (_: Throwable) {
                raw.replace(Regex("<[^>]*>"), "").trim()
            }.takeIf { it.isNotBlank() }
        }

    val isMediaVideo: Boolean
        get() = isVideo ||
                !videoUrl.isNullOrBlank() ||
                domain.contains("v.redd.it", ignoreCase = true) ||
                domain.contains("redgifs.com", ignoreCase = true) ||
                contentUrl.contains("redgifs.com", ignoreCase = true) ||
                contentUrl.contains("v.redd.it", ignoreCase = true) ||
                UrlSafety.hasExtension(contentUrl, "mp4", "gifv", "webm", "m3u8")

    val isAnimatedGif: Boolean
        get() = UrlSafety.hasExtension(contentUrl, "gif") ||
                contentUrl.contains(".gif", ignoreCase = true) ||
                (domain.contains("i.redd.it", ignoreCase = true) && contentUrl.endsWith(".gif", ignoreCase = true)) ||
                (previewImageUrl != null && previewImageUrl.contains(".gif", ignoreCase = true))

    val displayImageUrl: String?
        get() {
            // When content is an animated GIF, prefer the actual GIF contentUrl so Coil receives
            // and animates the GIF, rather than Reddit's static JPEG preview still!
            if (isAnimatedGif) {
                val directGif = contentUrl.trim().takeIf {
                    UrlSafety.isHttpsUrl(it) &&
                            (UrlSafety.hasExtension(it, "gif") || it.contains(".gif", ignoreCase = true))
                }
                if (directGif != null) return directGif
            }

            // If it's a gallery, use the first high-res gallery image
            if (galleryImageUrls.isNotEmpty()) {
                val firstGallery = galleryImageUrls.firstOrNull()?.takeIf { UrlSafety.isHttpsUrl(it) }
                if (firstGallery != null) return firstGallery
            }

            val preview = previewImageUrl?.trim()?.takeIf {
                it.isNotBlank() && UrlSafety.isHttpsUrl(it) &&
                        !it.endsWith("default") && !it.endsWith("self") && !it.endsWith("nsfw")
            }
            if (preview != null) return preview

            val thumb = thumbnailUrl?.trim()?.takeIf {
                it.isNotBlank() && UrlSafety.isHttpsUrl(it) &&
                        !it.endsWith("default") && !it.endsWith("self") && !it.endsWith("nsfw")
            }
            if (thumb != null) return thumb

            val direct = contentUrl.trim().takeIf {
                UrlSafety.isHttpsUrl(it)
            }?.takeIf {
                UrlSafety.hasExtension(it, "jpg", "jpeg", "png", "webp", "gif") ||
                        domain.contains("i.redd.it") ||
                        domain.contains("i.imgur.com")
            }
            return direct
        }
}

enum class FeedSort(val label: String, val apiValue: String) {
    HOT("Hot", "hot"),
    NEW("New", "new"),
    TOP("Top", "top"),
    RISING("Rising", "rising"),
    RELEVANCE("Relevance", "relevance")
}

data class UserProfile(
    val isLoggedIn: Boolean = false,
    val username: String = "Anonymous",
    val karma: Int = 0,
    val linkKarma: Int = 0,
    val commentKarma: Int = 0,
    val avatarUrl: String? = null,
    val followersCount: Int? = null,
    val followingCount: Int? = null,
    val subscribedSubreddits: List<String>? = null,
    val sessionActive: Boolean = false,
    val showMatureContent: Boolean = true,
    val accountError: String? = null
)
