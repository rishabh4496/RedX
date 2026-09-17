package com.example.redx.theme

import com.example.redx.model.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Regression coverage for the theme palette. Before v2.3.0 the palette was a plain
 * mutable field, so switching themes left custom feed components rendering the old
 * colors until an unrelated recomposition happened.
 */
class RedXPaletteStateTest {

    @Test
    fun paletteUpdatesArePublishedToPaletteAccessors() {
        val original = RedXPaletteState.current
        try {
            val midnight = paletteFor(AppTheme.MIDNIGHT_BLUE)
            RedXPaletteState.current = midnight

            assertEquals(midnight.accent, RedditOrange)
            assertEquals(midnight.background, AmoledBackground)
            assertEquals(midnight.textPrimary, TextPrimary)

            val matrix = paletteFor(AppTheme.MATRIX_EMERALD)
            RedXPaletteState.current = matrix

            assertEquals(matrix.accent, RedditOrange)
            assertNotEquals(midnight.accent, RedditOrange)
        } finally {
            RedXPaletteState.current = original
        }
    }

    @Test
    fun everyThemeResolvesToADistinctAccent() {
        val accents = AppTheme.entries.map { paletteFor(it).accent }
        assertEquals(accents.size, accents.distinct().size)
    }
}
