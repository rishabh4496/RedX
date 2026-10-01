package com.example.redx.util

/**
 * Splits text into pieces that fit Android's TextToSpeech input limit
 * (`TextToSpeech.getMaxSpeechInputLength()`, 4000 characters). Longer text is rejected by
 * the engine, which made "Listen to post" silently do nothing for long self-posts.
 *
 * Pieces break at sentence ends or line breaks where possible, then at spaces, and only
 * hard-cut a single unbroken word as a last resort. Pure Kotlin so it can be unit tested.
 */
object SpeechChunker {

    const val DEFAULT_MAX_LENGTH = 3900

    fun chunk(text: String, maxLength: Int = DEFAULT_MAX_LENGTH): List<String> {
        require(maxLength > 0) { "maxLength must be positive" }
        val clean = text.trim()
        if (clean.isEmpty()) return emptyList()

        val chunks = mutableListOf<String>()
        var remaining = clean
        while (remaining.length > maxLength) {
            val window = remaining.substring(0, maxLength)
            val cut = bestBreak(window)
            chunks += remaining.substring(0, cut).trim()
            remaining = remaining.substring(cut).trim()
        }
        if (remaining.isNotEmpty()) chunks += remaining
        return chunks.filter { it.isNotEmpty() }
    }

    private fun bestBreak(window: String): Int {
        val minUseful = window.length / 2
        // Prefer a sentence end / newline in the second half of the window.
        for (i in window.length - 1 downTo minUseful) {
            val c = window[i]
            if (c == '\n' || ((c == '.' || c == '!' || c == '?') && (i + 1 == window.length || window[i + 1].isWhitespace()))) {
                return i + 1
            }
        }
        val space = window.lastIndexOf(' ')
        if (space >= minUseful) return space + 1
        return window.length
    }
}
