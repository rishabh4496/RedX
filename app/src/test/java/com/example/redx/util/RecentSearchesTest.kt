package com.example.redx.util

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentSearchesTest {

    @Test
    fun newestQueryGoesFirst() {
        assertEquals(listOf("b", "a"), RecentSearches.push(listOf("a"), "b"))
    }

    @Test
    fun duplicatesAreMovedToFrontIgnoringCaseAndWhitespace() {
        assertEquals(listOf("Jetpack Compose", "kotlin"), RecentSearches.push(listOf("kotlin", "jetpack compose"), "  Jetpack   Compose "))
    }

    @Test
    fun blankQueriesAreIgnored() {
        val current = listOf("a")
        assertEquals(current, RecentSearches.push(current, "   "))
    }

    @Test
    fun listIsCapped() {
        val full = (1..8).map { "q$it" }
        val updated = RecentSearches.push(full, "new")
        assertEquals(8, updated.size)
        assertEquals("new", updated.first())
        assertEquals("q7", updated.last())
    }

    @Test
    fun removeIgnoresCase() {
        assertEquals(listOf("b"), RecentSearches.remove(listOf("A", "b"), "a"))
    }
}
