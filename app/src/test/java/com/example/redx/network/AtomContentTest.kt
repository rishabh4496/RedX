package com.example.redx.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AtomContentTest {

    private val linkPostContent = """
        <table> <tr><td> <a href="https://www.reddit.com/r/pics/comments/abc/x/"><img src="https://preview.redd.it/x.jpg" /></a> </td><td>
        &#32; submitted by &#32; <a href="https://www.reddit.com/user/someone"> /u/someone </a> <br/>
        <span><a href="https://i.redd.it/x.jpg">[link]</a></span> &#32; <span><a href="https://www.reddit.com/r/pics/comments/abc/x/">[comments]</a></span>
        </td></tr></table>
    """.trimIndent()

    private val selfPostContent = """
        <table> <tr><td> <!-- SC_OFF --><div class="md"><p>First paragraph &amp; more.</p>
        <p>Second <strong>bold</strong> line</p></div><!-- SC_ON --> &#32; submitted by &#32;
        <a href="https://www.reddit.com/user/someone"> /u/someone </a> <span><a href="https://www.reddit.com/r/x/comments/abc/">[comments]</a></span>
        </td></tr></table>
    """.trimIndent()

    @Test
    fun linkPostHasNoSelfText() {
        // Previously this returned "submitted by /u/someone [link] [comments]".
        assertNull(AtomContent.extractSelfText(linkPostContent))
    }

    @Test
    fun selfPostKeepsOnlyTheAuthorsText() {
        val text = AtomContent.extractSelfText(selfPostContent)
        assertEquals("First paragraph & more.\n\nSecond bold line", text)
        assertFalse(text!!.contains("submitted by"))
        assertFalse(text.contains("[comments]"))
    }

    @Test
    fun emptyMarkdownBlockIsNotText() {
        assertNull(AtomContent.extractSelfText("<!-- SC_OFF --><div class=\"md\"><p></p></div><!-- SC_ON -->"))
    }

    @Test
    fun subredditCategoryLabelIsNotAFlair() {
        assertTrue(AtomContent.isSubredditLabel("funny", "r/funny"))
        assertTrue(AtomContent.isSubredditLabel("Funny", "r/funny"))
        assertTrue(AtomContent.isSubredditLabel("funny", ""))
        assertTrue(AtomContent.isSubredditLabel("funny", "funny"))
        assertFalse(AtomContent.isSubredditLabel("funny", "Discussion"))
    }

    @Test
    fun decodeEntitiesKeepsAngleBracketsInTitles() {
        assertEquals("Use <b> tags & more", AtomContent.decodeEntities("Use <b> tags &amp; more"))
        assertEquals("Tom & Jerry's", AtomContent.decodeEntities("Tom &amp; Jerry&#39;s"))
        assertEquals("a < b", AtomContent.decodeEntities("a &lt; b"))
        assertEquals("plain", AtomContent.decodeEntities("plain"))
    }

    @Test
    fun decodeEntitiesIgnoresInvalidNumericReferences() {
        assertEquals("&#99999999999;", AtomContent.decodeEntities("&#99999999999;"))
    }
}
