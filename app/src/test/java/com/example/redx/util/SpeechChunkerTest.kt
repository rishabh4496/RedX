package com.example.redx.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechChunkerTest {

    @Test
    fun shortTextIsOneChunk() {
        assertEquals(listOf("Hello world."), SpeechChunker.chunk("  Hello world.  "))
    }

    @Test
    fun blankTextHasNoChunks() {
        assertTrue(SpeechChunker.chunk("   \n ").isEmpty())
    }

    @Test
    fun longTextIsSplitWithinLimitAndLosesNoWords() {
        val sentence = "This is a fairly ordinary sentence. "
        val text = sentence.repeat(400) // ~14k chars
        val chunks = SpeechChunker.chunk(text, maxLength = 1000)
        assertTrue(chunks.size > 1)
        assertTrue(chunks.all { it.length <= 1000 })
        assertEquals(
            text.trim().split(Regex("\\s+")),
            chunks.joinToString(" ").split(Regex("\\s+"))
        )
    }

    @Test
    fun prefersSentenceBoundaries() {
        val text = "A".repeat(40) + ". " + "B".repeat(40) + ". " + "C".repeat(40) + "."
        val chunks = SpeechChunker.chunk(text, maxLength = 60)
        assertTrue(chunks.first().endsWith("."))
        assertTrue(chunks.all { it.length <= 60 })
    }

    @Test
    fun unbrokenWordIsHardCut() {
        val chunks = SpeechChunker.chunk("x".repeat(25), maxLength = 10)
        assertEquals(listOf("x".repeat(10), "x".repeat(10), "x".repeat(5)), chunks)
    }
}
