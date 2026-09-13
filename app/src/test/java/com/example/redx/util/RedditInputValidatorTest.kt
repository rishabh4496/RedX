package com.example.redx.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RedditInputValidatorTest {

    @Test
    fun normalizesValidRedditPathSegments() {
        assertEquals("Android", RedditInputValidator.normalizeSubreddit("  r/Android  "))
        assertEquals("alice_example", RedditInputValidator.normalizeUsername("u/alice_example"))
    }

    @Test
    fun rejectsPathSeparatorsAndQueryCharacters() {
        assertNull(RedditInputValidator.normalizeSubreddit("android/search"))
        assertNull(RedditInputValidator.normalizeSubreddit("android?sort=new"))
        assertNull(RedditInputValidator.normalizeUsername("alice@example.com"))
    }
}
