package com.example.redx.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RedditPostMetricsTest {

    private fun post(score: Int, comments: Int, hasMetrics: Boolean) = RedditPost(
        id = "t3_x",
        title = "t",
        author = "a",
        subreddit = "s",
        score = score,
        numComments = comments,
        hasMetrics = hasMetrics
    )

    @Test
    fun unknownMetricsAreNotShownAsZero() {
        val p = post(score = 0, comments = 0, hasMetrics = false)
        assertEquals("–", p.displayScore)
        assertEquals("–", p.displayComments)
    }

    @Test
    fun knownMetricsAreFormatted() {
        val p = post(score = 12_345, comments = 1_500, hasMetrics = true)
        assertEquals("12.3k", p.displayScore)
        assertEquals("1.5k", p.displayComments)
        assertEquals("0", post(0, 0, true).displayScore)
    }
}
