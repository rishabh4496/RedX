package com.example.redx.network

/**
 * Pure-Kotlin helpers for Reddit's Atom/RSS entries. They deliberately avoid android.text.Html
 * so the behaviour can be unit tested on the JVM.
 */
internal object AtomContent {

    private val selfTextBlock = Regex("<!--\\s*SC_OFF\\s*-->(.*?)<!--\\s*SC_ON\\s*-->", RegexOption.DOT_MATCHES_ALL)
    private val lineBreakTags = Regex("(?i)</p>|<br\\s*/?>|</li>|</h[1-6]>|</blockquote>")
    private val anyTag = Regex("<[^>]*>")
    private val numericEntity = Regex("&#(x?)([0-9a-fA-F]+);")
    private val blankLines = Regex("\\n{3,}")

    /**
     * Returns only the post body from an entry's `<content>` HTML.
     *
     * The raw content is a table that also holds the thumbnail and the
     * "submitted by /u/x [link] [comments]" footer. The old parser used all of it as the post
     * text, so every link post claimed to be a text post whose body was that footer. Only the
     * markdown block Reddit wraps in SC_OFF/SC_ON is the author's real text.
     */
    fun extractSelfText(contentHtml: String): String? {
        val block = selfTextBlock.find(contentHtml)?.groupValues?.get(1) ?: return null
        val text = decodeEntities(
            block.replace(lineBreakTags, "\n").replace(anyTag, "")
        )
            .lines().joinToString("\n") { it.trim() }
            .replace(blankLines, "\n\n")
            .trim()
        return text.takeIf { it.isNotBlank() }
    }

    /**
     * Reddit tags every entry with `<category term="sub" label="r/sub">`. That label is the
     * subreddit itself, not a post flair, and must not be surfaced as one.
     */
    fun isSubredditLabel(term: String, label: String): Boolean {
        val cleanLabel = label.trim()
        if (cleanLabel.isEmpty()) return true
        if (cleanLabel.equals(term.trim(), ignoreCase = true)) return true
        return cleanLabel.removePrefix("/").equals("r/${term.trim()}", ignoreCase = true)
    }

    /** Decodes the entities Reddit emits, without stripping angle brackets in titles. */
    fun decodeEntities(value: String): String {
        if (!value.contains('&')) return value
        val withNumeric = numericEntity.replace(value) { match ->
            val isHex = match.groupValues[1].isNotEmpty()
            val code = match.groupValues[2].toIntOrNull(if (isHex) 16 else 10)
            if (code != null && code in 1..0x10FFFF) String(Character.toChars(code)) else match.value
        }
        return withNumeric
            .replace("&nbsp;", " ")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&amp;", "&")
    }
}
