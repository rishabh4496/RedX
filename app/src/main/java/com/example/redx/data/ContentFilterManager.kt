package com.example.redx.data

import android.content.Context
import androidx.core.content.edit
import androidx.core.net.toUri
import android.content.SharedPreferences
import android.net.Uri
import com.example.redx.model.RedditPost
import com.example.redx.util.UrlSafety
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.util.Locale

class ContentFilterManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("redx_content_filters", Context.MODE_PRIVATE)

    private val _blockedKeywords = MutableStateFlow<List<String>>(loadList(KEY_BLOCKED_KEYWORDS))
    val blockedKeywords: StateFlow<List<String>> = _blockedKeywords.asStateFlow()

    private val _blockedDomains = MutableStateFlow<List<String>>(loadList(KEY_BLOCKED_DOMAINS))
    val blockedDomains: StateFlow<List<String>> = _blockedDomains.asStateFlow()

    private val _isFilterEnabled = MutableStateFlow(prefs.getBoolean(KEY_FILTER_ENABLED, true))
    val isFilterEnabled: StateFlow<Boolean> = _isFilterEnabled.asStateFlow()

    private fun loadList(key: String): List<String> {
        val jsonString = prefs.getString(key, "[]") ?: "[]"
        val list = mutableListOf<String>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val rawItem = array.getString(i).trim().lowercase(Locale.ROOT)
                val item = if (key == KEY_BLOCKED_DOMAINS) normalizeDomain(rawItem) else rawItem
                if (!item.isNullOrBlank() && !list.contains(item)) {
                    list.add(item)
                }
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
        return list
    }

    private fun persistList(key: String, list: List<String>) {
        val array = JSONArray()
        for (item in list) {
            array.put(item)
        }
        prefs.edit { putString(key, array.toString()) }
    }

    fun addBlockedKeyword(keyword: String) {
        val clean = keyword.trim().lowercase(Locale.ROOT)
        if (clean.isBlank() || _blockedKeywords.value.contains(clean)) return
        val updated = _blockedKeywords.value + clean
        _blockedKeywords.value = updated
        persistList(KEY_BLOCKED_KEYWORDS, updated)
    }

    fun removeBlockedKeyword(keyword: String) {
        val clean = keyword.trim().lowercase(Locale.ROOT)
        val updated = _blockedKeywords.value.filter { it != clean }
        _blockedKeywords.value = updated
        persistList(KEY_BLOCKED_KEYWORDS, updated)
    }

    fun addBlockedDomain(domain: String) {
        val clean = normalizeDomain(domain) ?: return
        if (_blockedDomains.value.contains(clean)) return
        val updated = _blockedDomains.value + clean
        _blockedDomains.value = updated
        persistList(KEY_BLOCKED_DOMAINS, updated)
    }

    fun removeBlockedDomain(domain: String) {
        val clean = normalizeDomain(domain) ?: return
        val updated = _blockedDomains.value.filter { it != clean }
        _blockedDomains.value = updated
        persistList(KEY_BLOCKED_DOMAINS, updated)
    }

    fun toggleFilterEnabled(enabled: Boolean) {
        _isFilterEnabled.value = enabled
        prefs.edit { putBoolean(KEY_FILTER_ENABLED, enabled) }
    }

    fun shouldFilterPost(post: RedditPost): Boolean {
        if (!_isFilterEnabled.value) return false

        // Check blocked domains
        val domain = normalizeDomain(post.domain) ?: UrlSafety.host(post.contentUrl)
        for (blockedDomain in _blockedDomains.value) {
            if (domain != null && (domain == blockedDomain || domain.endsWith(".$blockedDomain"))) {
                return true
            }
        }

        // Check blocked keywords against title, self text, flair, and subreddit
        val titleLower = post.title.lowercase(Locale.ROOT)
        val selfTextLower = (post.selfTextHtml ?: "").lowercase(Locale.ROOT)
        val flairLower = (post.flair ?: "").lowercase(Locale.ROOT)
        val subredditLower = post.subreddit.lowercase(Locale.ROOT)

        for (kw in _blockedKeywords.value) {
            if (titleLower.contains(kw) ||
                selfTextLower.contains(kw) ||
                flairLower.contains(kw) ||
                subredditLower.contains(kw)
            ) {
                return true
            }
        }

        return false
    }

    private fun normalizeDomain(value: String): String? {
        val raw = value.trim().lowercase(Locale.ROOT)
        if (raw.isBlank()) return null
        val uri = if (raw.startsWith("http://") || raw.startsWith("https://")) {
            raw.toUri()
        } else {
            "https://$raw".toUri()
        }
        return uri.host?.lowercase(Locale.ROOT)?.removePrefix("www.")?.trimEnd('.')
            ?.takeIf { it.isNotBlank() }
    }

    companion object {
        private const val KEY_BLOCKED_KEYWORDS = "blocked_keywords"
        private const val KEY_BLOCKED_DOMAINS = "blocked_domains"
        private const val KEY_FILTER_ENABLED = "filter_enabled"
    }
}
