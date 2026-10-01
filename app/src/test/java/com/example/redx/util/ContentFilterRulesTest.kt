package com.example.redx.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContentFilterRulesTest {

    @Test
    fun blockedSubredditIsHiddenInOtherFeeds() {
        assertTrue(ContentFilterRules.isSubredditBlocked(listOf("memes"), "Memes", "popular"))
        assertTrue(ContentFilterRules.isSubredditBlocked(listOf("memes"), "memes", null))
    }

    @Test
    fun blockedSubredditIsVisibleWhenBrowsingItDirectly() {
        assertFalse(ContentFilterRules.isSubredditBlocked(listOf("memes"), "memes", "Memes"))
    }

    @Test
    fun unblockedSubredditsAndEmptyListPass() {
        assertFalse(ContentFilterRules.isSubredditBlocked(listOf("memes"), "android", "popular"))
        assertFalse(ContentFilterRules.isSubredditBlocked(emptyList(), "memes", null))
    }

    @Test
    fun namesAreNormalised() {
        assertEquals("foo", ContentFilterRules.normalizeSubredditName("r/Foo"))
        assertEquals("foo_bar", ContentFilterRules.normalizeSubredditName(" /r/Foo_Bar "))
        assertNull(ContentFilterRules.normalizeSubredditName("foo bar"))
        assertNull(ContentFilterRules.normalizeSubredditName(""))
    }
}
