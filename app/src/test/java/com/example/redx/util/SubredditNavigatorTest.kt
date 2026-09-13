package com.example.redx.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SubredditNavigatorTest {

    private val feeds = listOf("home", "kotlin", "AndroidDev", "technology")

    @Test
    fun swipingLeftAdvancesAndWrapsAround() {
        assertEquals("AndroidDev", SubredditNavigator.next("kotlin", feeds, direction = 1))
        assertEquals("home", SubredditNavigator.next("technology", feeds, direction = 1))
    }

    @Test
    fun swipingRightMovesBackAndWrapsAround() {
        assertEquals("kotlin", SubredditNavigator.next("AndroidDev", feeds, direction = -1))
        assertEquals("technology", SubredditNavigator.next("home", feeds, direction = -1))
    }

    @Test
    fun invalidAndDuplicateFeedsAreIgnored() {
        val available = listOf("r/home", "kotlin", "kotlin", "bad/feed")
        assertEquals("kotlin", SubredditNavigator.next("r/home", available, direction = 1))
        assertNull(SubredditNavigator.next("home", listOf("home", "bad/feed"), direction = 1))
    }

    @Test
    fun missingCurrentFeedStartsAtTheNearestEdge() {
        assertEquals("home", SubredditNavigator.next("popular", feeds, direction = 1))
        assertEquals("technology", SubredditNavigator.next("popular", feeds, direction = -1))
    }
}
