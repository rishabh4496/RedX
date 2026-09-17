package com.example.redx.auth

import android.content.Context
import android.content.SharedPreferences
import android.webkit.CookieManager
import androidx.core.content.edit
import com.example.redx.model.UserProfile
import com.example.redx.network.RedditAccountService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RedditAccountManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("redx_auth_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadInitialProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    init {
        if (prefs.getBoolean(KEY_SHOW_MATURE, true)) {
            primeMatureContentCookies()
        } else {
            clearMatureContentCookies()
        }
        refreshSession()
    }

    fun primeMatureContentCookies() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)

        // Force adult confirmation and anti-app nag cookies across reddit domains
        val matureCookies = listOf(
            "over18=1; Path=/; Domain=.reddit.com; Max-Age=31536000",
            "mweb_nx_over18=1; Path=/; Domain=.reddit.com; Max-Age=31536000",
            "prompt_shown=1; Path=/; Domain=.reddit.com; Max-Age=31536000",
            "country_code=US; Path=/; Domain=.reddit.com; Max-Age=31536000",
            "over18=1; Path=/; Domain=.old.reddit.com; Max-Age=31536000"
        )

        for (c in matureCookies) {
            cookieManager.setCookie("https://www.reddit.com", c)
            cookieManager.setCookie("https://old.reddit.com", c)
            cookieManager.setCookie("https://sh.reddit.com", c)
        }
        cookieManager.flush()
    }

    private fun clearMatureContentCookies() {
        val cookieManager = CookieManager.getInstance()
        val expired = listOf("over18", "mweb_nx_over18", "prompt_shown", "country_code")
        for (name in expired) {
            cookieManager.setCookie("https://www.reddit.com", "$name=; Path=/; Domain=.reddit.com; Max-Age=0")
            cookieManager.setCookie("https://old.reddit.com", "$name=; Path=/; Domain=.old.reddit.com; Max-Age=0")
            cookieManager.setCookie("https://sh.reddit.com", "$name=; Path=/; Domain=.reddit.com; Max-Age=0")
        }
        cookieManager.flush()
    }

    private fun loadInitialProfile(): UserProfile {
        val showMature = prefs.getBoolean(KEY_SHOW_MATURE, true)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val username = prefs.getString(KEY_USERNAME, "Anonymous") ?: "Anonymous"
        val karma = prefs.getInt(KEY_KARMA, 0)
        val linkKarma = prefs.getInt(KEY_LINK_KARMA, 0)
        val commentKarma = prefs.getInt(KEY_COMMENT_KARMA, 0)
        val avatarUrl = prefs.getString(KEY_AVATAR_URL, null)
        val subsString = prefs.getString(KEY_SUBSCRIBED_SUBREDDITS, null)
        val subscribedSubreddits = subsString?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }

        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie("https://www.reddit.com") ?: ""
        val hasSessionCookie = cookies.split(';').any { it.trim().startsWith("reddit_session=") }

        val authenticated = isLoggedIn && hasSessionCookie

        return UserProfile(
            isLoggedIn = authenticated,
            username = if (authenticated) username else "Anonymous",
            karma = if (authenticated) karma else 0,
            linkKarma = if (authenticated) linkKarma else 0,
            commentKarma = if (authenticated) commentKarma else 0,
            avatarUrl = if (authenticated) avatarUrl else null,
            subscribedSubreddits = if (authenticated) subscribedSubreddits else null,
            sessionActive = hasSessionCookie,
            showMatureContent = showMature
        )
    }

    fun refreshSession() {
        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie("https://www.reddit.com") ?: ""

        val hasSessionCookie = cookies.split(';').any { it.trim().startsWith("reddit_session=") }
        val showMature = prefs.getBoolean(KEY_SHOW_MATURE, true)

        if (!hasSessionCookie) {
            clearPersistedAccount()
            _userProfile.value = anonymousProfile(showMature)
        } else {
            val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
            if (isLoggedIn) {
                val username = prefs.getString(KEY_USERNAME, "Anonymous") ?: "Anonymous"
                val karma = prefs.getInt(KEY_KARMA, 0)
                val linkKarma = prefs.getInt(KEY_LINK_KARMA, 0)
                val commentKarma = prefs.getInt(KEY_COMMENT_KARMA, 0)
                val avatarUrl = prefs.getString(KEY_AVATAR_URL, null)
                val subsString = prefs.getString(KEY_SUBSCRIBED_SUBREDDITS, null)
                val subscribedSubreddits = subsString?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }

                _userProfile.value = UserProfile(
                    isLoggedIn = true,
                    username = username,
                    karma = karma,
                    linkKarma = linkKarma,
                    commentKarma = commentKarma,
                    avatarUrl = avatarUrl,
                    subscribedSubreddits = subscribedSubreddits ?: _userProfile.value.subscribedSubreddits,
                    sessionActive = true,
                    showMatureContent = showMature
                )
            } else {
                _userProfile.value = _userProfile.value.copy(
                    sessionActive = true,
                    showMatureContent = showMature
                )
            }
        }
    }

    /**
     * Validates the WebView session against Reddit and loads real account data.
     * A cookie by itself is not treated as a logged-in account.
     */
    suspend fun refreshAuthenticatedAccount(): Result<Unit> {
        val cookieManager = CookieManager.getInstance()
        cookieManager.flush()
        val cookies = cookieManager.getCookie("https://www.reddit.com").orEmpty()
        val hasSessionCookie = cookies.split(';').any { it.trim().startsWith("reddit_session=") }
        val showMature = prefs.getBoolean(KEY_SHOW_MATURE, true)

        if (!hasSessionCookie) {
            clearPersistedAccount()
            _userProfile.value = anonymousProfile(showMature)
            return Result.failure(IllegalStateException("No active Reddit session"))
        }

        val result = RedditAccountService.fetchAccount(getCookieHeader(showMature))
        result.fold(
            onSuccess = { account ->
                val finalSubs = account.subscribedSubreddits
                    ?: _userProfile.value.subscribedSubreddits
                    ?: prefs.getString(KEY_SUBSCRIBED_SUBREDDITS, null)
                        ?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() }

                prefs.edit {
                    putBoolean(KEY_IS_LOGGED_IN, true)
                    putString(KEY_USERNAME, account.username)
                    putInt(KEY_KARMA, account.totalKarma)
                    putInt(KEY_LINK_KARMA, account.linkKarma)
                    putInt(KEY_COMMENT_KARMA, account.commentKarma)
                    if (account.avatarUrl != null) {
                        putString(KEY_AVATAR_URL, account.avatarUrl)
                    }
                    if (!finalSubs.isNullOrEmpty()) {
                        putString(KEY_SUBSCRIBED_SUBREDDITS, finalSubs.joinToString(","))
                    }
                }

                _userProfile.value = UserProfile(
                    isLoggedIn = true,
                    username = account.username,
                    karma = account.totalKarma,
                    linkKarma = account.linkKarma,
                    commentKarma = account.commentKarma,
                    avatarUrl = account.avatarUrl,
                    followersCount = account.followersCount,
                    followingCount = account.followingCount,
                    subscribedSubreddits = finalSubs,
                    sessionActive = true,
                    showMatureContent = showMature,
                    accountError = null
                )
            },
            onFailure = { error ->
                val hasValidSession = cookieManager.getCookie("https://www.reddit.com").orEmpty()
                    .split(';').any { it.trim().startsWith("reddit_session=") }

                if (!hasValidSession) {
                    clearPersistedAccount()
                    _userProfile.value = anonymousProfile(showMature).copy(
                        sessionActive = false,
                        accountError = error.message ?: "Reddit account validation failed"
                    )
                } else {
                    _userProfile.value = _userProfile.value.copy(
                        sessionActive = true,
                        accountError = error.message ?: "Reddit account validation failed"
                    )
                }
            }
        )
        return result.map { Unit }
    }

    fun updateSubscribedSubreddits(subreddits: List<String>) {
        if (subreddits.isEmpty()) return
        val currentSubs = _userProfile.value.subscribedSubreddits.orEmpty()
        val merged = (currentSubs + subreddits).distinctBy(String::lowercase)
        prefs.edit { putString(KEY_SUBSCRIBED_SUBREDDITS, merged.joinToString(",")) }
        _userProfile.value = _userProfile.value.copy(subscribedSubreddits = merged)
    }

    fun toggleMatureContent(show: Boolean) {
        prefs.edit { putBoolean(KEY_SHOW_MATURE, show) }
        _userProfile.value = _userProfile.value.copy(showMatureContent = show)
        if (show) primeMatureContentCookies() else clearMatureContentCookies()
    }

    fun logout(onComplete: () -> Unit = {}) {
        val cookieManager = CookieManager.getInstance()
        val showMatureContent = prefs.getBoolean(KEY_SHOW_MATURE, true)

        // Clear the local account state immediately, but wait for WebView's asynchronous
        // cookie removal before restoring the mature-content preference/cookies.
        prefs.edit {
            clear()
            putBoolean(KEY_SHOW_MATURE, showMatureContent)
        }
        _userProfile.value = anonymousProfile(showMatureContent)

        cookieManager.removeAllCookies {
            if (showMatureContent) primeMatureContentCookies() else clearMatureContentCookies()
            cookieManager.flush()
            onComplete()
        }
        cookieManager.flush()
    }

    fun getCookieHeader(includeMature: Boolean = _userProfile.value.showMatureContent): String {
        val cookieManager = CookieManager.getInstance()
        val cookies = cookieManager.getCookie("https://www.reddit.com") ?: ""
        if (!includeMature) {
            return cookies.split(';')
                .map(String::trim)
                .filter { cookie ->
                    cookie.isNotBlank() && MATURE_COOKIE_NAMES.none {
                        cookie.startsWith("$it=", ignoreCase = true)
                    }
                }
                .joinToString("; ")
        }
        if (cookies.contains("over18=1")) return cookies
        val matureCookies = "over18=1; mweb_nx_over18=1"
        return if (cookies.isBlank()) matureCookies else "$cookies; $matureCookies"
    }

    private fun clearPersistedAccount() {
        prefs.edit {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_USERNAME)
            remove(KEY_KARMA)
            remove(KEY_LINK_KARMA)
            remove(KEY_COMMENT_KARMA)
            remove(KEY_AVATAR_URL)
            remove(KEY_SUBSCRIBED_SUBREDDITS)
        }
    }

    private fun anonymousProfile(showMatureContent: Boolean) = UserProfile(
        isLoggedIn = false,
        username = "Anonymous",
        karma = 0,
        sessionActive = false,
        showMatureContent = showMatureContent
    )

    companion object {
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USERNAME = "key_username"
        private const val KEY_KARMA = "key_karma"
        private const val KEY_LINK_KARMA = "key_link_karma"
        private const val KEY_COMMENT_KARMA = "key_comment_karma"
        private const val KEY_AVATAR_URL = "key_avatar_url"
        private const val KEY_SUBSCRIBED_SUBREDDITS = "key_subscribed_subreddits"
        private const val KEY_SHOW_MATURE = "key_show_mature"
        private val MATURE_COOKIE_NAMES = setOf("over18", "mweb_nx_over18", "prompt_shown", "country_code")
    }
}
