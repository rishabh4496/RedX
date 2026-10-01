package com.example.redx.util

import android.app.DownloadManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.widget.Toast

object MediaDownloadHelper {

    private const val DOWNLOAD_USER_AGENT =
        "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"

    fun downloadMedia(context: Context, mediaUrl: String, title: String) {
        if (mediaUrl.isBlank()) {
            Toast.makeText(context, "No media URL to download", Toast.LENGTH_SHORT).show()
            return
        }

        // RedGifs "videos" are embed pages and other HLS playlists are not single files:
        // DownloadManager would save the HTML/playlist text under an image/video name.
        if (mediaUrl.contains("redgifs.com", ignoreCase = true) ||
            (UrlSafety.hasExtension(mediaUrl, "m3u8") && !mediaUrl.contains("v.redd.it", ignoreCase = true))
        ) {
            Toast.makeText(context, "This video type can't be downloaded. Use Open in Browser instead.", Toast.LENGTH_LONG).show()
            return
        }

        try {
            // If it's a Reddit video URL or HLS playlist, resolve to downloadable MP4 fallback
            val targetUrl = resolveDownloadUrl(mediaUrl)

            val uri = UrlSafety.httpsUriOrNull(targetUrl)
                ?: throw IllegalArgumentException("Only secure HTTPS media URLs can be downloaded")

            val extension = when {
                UrlSafety.hasExtension(targetUrl, "mp4") || targetUrl.contains(".mp4", ignoreCase = true) -> ".mp4"
                UrlSafety.hasExtension(targetUrl, "webm") -> ".webm"
                UrlSafety.hasExtension(targetUrl, "gif") -> ".gif"
                UrlSafety.hasExtension(targetUrl, "png") -> ".png"
                UrlSafety.hasExtension(targetUrl, "webp") -> ".webp"
                else -> ".jpg"
            }

            val mimeType = when (extension) {
                ".mp4" -> "video/mp4"
                ".webm" -> "video/webm"
                ".gif" -> "image/gif"
                ".png" -> "image/png"
                ".webp" -> "image/webp"
                else -> "image/jpeg"
            }

            val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9.-]"), "_").take(35)
            val fileName = "RedX_${sanitizedTitle}_${System.currentTimeMillis()}$extension"

            val request = DownloadManager.Request(uri).apply {
                setTitle("RedX: $fileName")
                setDescription("Downloading Reddit media...")
                setMimeType(mimeType)
                // Add headers to prevent 403 Forbidden on Reddit CDN / external hosts
                addRequestHeader("User-Agent", DOWNLOAD_USER_AGENT)
                addRequestHeader("Referer", "https://www.reddit.com/")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                } else {
                    // Writing to the shared Downloads folder on Android 8-9 needs the legacy
                    // storage permission, which RedX deliberately doesn't request; the app's
                    // own downloads folder works without it.
                    setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
                }
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(context, "Downloading… check the notification when it finishes", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun resolveDownloadUrl(url: String): String {
        val trimmed = url.trim()
        if (trimmed.contains("v.redd.it", ignoreCase = true)) {
            val vIdMatch = Regex("""v\.redd\.it/([^/?#]+)""").find(trimmed)
            if (vIdMatch != null) {
                val vId = vIdMatch.groupValues[1]
                if (trimmed.contains(".m3u8", ignoreCase = true) || !trimmed.contains(".mp4", ignoreCase = true)) {
                    return "https://v.redd.it/$vId/DASH_720.mp4?source=fallback"
                }
            }
        }
        return trimmed
    }
}
