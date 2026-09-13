package com.example.redx.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdBlockerTest {

    @Test
    fun blocksKnownAdvertisingHosts() {
        assertTrue(AdBlocker.shouldBlock("https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js"))
        assertTrue(AdBlocker.shouldBlock("https://example.doubleclick.net/ad/request"))
    }

    @Test
    fun keepsRedditAndMediaContentHostsAvailable() {
        assertFalse(AdBlocker.shouldBlock("https://www.reddit.com/r/android/comments/example/"))
        assertFalse(AdBlocker.shouldBlock("https://v.redd.it/example/HLSPlaylist.m3u8"))
    }
}
