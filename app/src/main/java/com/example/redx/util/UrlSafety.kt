package com.example.redx.util

import android.net.Uri
import java.util.Locale

/** Small URL helpers shared by network, WebView, and external-link code paths. */
object UrlSafety {

    fun httpUriOrNull(rawUrl: String): Uri? {
        val value = rawUrl.trim()
        if (value.isBlank()) return null

        val uri = runCatching { Uri.parse(value) }.getOrNull() ?: return null
        val scheme = uri.scheme?.lowercase(Locale.ROOT)
        return uri.takeIf { scheme == "http" || scheme == "https" }
            ?.takeIf { !it.host.isNullOrBlank() }
    }

    fun isHttpUrl(rawUrl: String): Boolean = httpUriOrNull(rawUrl) != null

    fun host(rawUrl: String): String? {
        val uriHost = httpUriOrNull(rawUrl)?.host?.lowercase(Locale.ROOT)?.removePrefix("www.")
        if (!uriHost.isNullOrBlank()) return uriHost

        return runCatching {
            java.net.URI(rawUrl.trim()).host?.lowercase(Locale.ROOT)?.removePrefix("www.")
        }.getOrNull()
    }

    fun isAllowedHost(rawUrl: String, vararg allowedHosts: String): Boolean {
        val actualHost = host(rawUrl) ?: return false
        return allowedHosts.any { allowed ->
            val normalized = allowed.lowercase(Locale.ROOT).removePrefix("www.")
            actualHost == normalized || actualHost.endsWith(".$normalized")
        }
    }

    fun isAllowedHttpsHost(rawUrl: String, vararg allowedHosts: String): Boolean =
        (rawUrl.trim().startsWith("https://", ignoreCase = true) || httpUriOrNull(rawUrl)?.scheme.equals("https", ignoreCase = true)) &&
            isAllowedHost(rawUrl, *allowedHosts)

    fun hasExtension(rawUrl: String, vararg extensions: String): Boolean {
        val cleanUrl = rawUrl.substringBefore('?').substringBefore('#').trim()
        val fileName = cleanUrl.substringAfterLast('/')
        val extension = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        if (extension.isNotBlank() && extensions.any { it.trimStart('.').lowercase(Locale.ROOT) == extension }) {
            return true
        }

        val uriPath = httpUriOrNull(rawUrl)?.path ?: return false
        val uriExt = uriPath.substringAfterLast('/', "")
            .substringAfterLast('.', "")
            .lowercase(Locale.ROOT)
        return extensions.any { it.trimStart('.').lowercase(Locale.ROOT) == uriExt }
    }
}
