package com.example.redx.ui

import com.example.redx.model.UserProfile
import com.example.redx.ui.components.DEFAULT_SUBREDDITS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubredditStateTest {

    private fun computeDisplayedSubreddits(profile: UserProfile, favorites: List<String>): List<String> {
        return if (profile.isLoggedIn) {
            val userSubs = profile.subscribedSubreddits.orEmpty()
                .filterNot { it.equals("home", true) || it.equals("popular", true) || it.equals("all", true) }
            val extraSubs = if (userSubs.isEmpty() && favorites.isNotEmpty()) favorites else emptyList()
            val combined = (listOf("home", "popular", "all") + userSubs + extraSubs).distinctBy(String::lowercase)
            if (combined.size > 3) {
                combined
            } else {
                (listOf("home", "popular", "all") + DEFAULT_SUBREDDITS.filterNot { it.equals("home", true) || it.equals("popular", true) || it.equals("all", true) }).distinctBy(String::lowercase)
            }
        } else {
            DEFAULT_SUBREDDITS
        }
    }

    @Test
    fun guestUserGetsDefaultSubreddits() {
        val anonymousProfile = UserProfile(
            isLoggedIn = false,
            username = "Anonymous",
            karma = 0
        )
        val result = computeDisplayedSubreddits(anonymousProfile, emptyList())
        assertEquals(DEFAULT_SUBREDDITS, result)
    }

    @Test
    fun loggedInUserGetsSubscribedSubredditsInsteadOfDefaults() {
        val userSubscriptions = listOf("kotlin", "AndroidDev", "technology", "compose")
        val loggedInProfile = UserProfile(
            isLoggedIn = true,
            username = "TestUser",
            karma = 500,
            subscribedSubreddits = userSubscriptions
        )
        val result = computeDisplayedSubreddits(loggedInProfile, emptyList())

        // Must start with home, popular, all
        assertEquals("home", result[0])
        assertEquals("popular", result[1])
        assertEquals("all", result[2])

        // Contains user's subreddits
        assertTrue(result.contains("kotlin"))
        assertTrue(result.contains("AndroidDev"))
        assertTrue(result.contains("technology"))
        assertTrue(result.contains("compose"))

        // Does not blindly equal DEFAULT_SUBREDDITS
        assertTrue(result != DEFAULT_SUBREDDITS)
    }

    @Test
    fun loggedInUserDeduplicatesFeeds() {
        val userSubscriptions = listOf("home", "popular", "all", "gaming")
        val loggedInProfile = UserProfile(
            isLoggedIn = true,
            username = "TestUser",
            karma = 100,
            subscribedSubreddits = userSubscriptions
        )
        val result = computeDisplayedSubreddits(loggedInProfile, emptyList())
        assertEquals(listOf("home", "popular", "all", "gaming"), result)
    }
}
