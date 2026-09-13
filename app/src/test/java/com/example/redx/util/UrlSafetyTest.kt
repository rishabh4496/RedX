package com.example.redx.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlSafetyTest {

    @Test
    fun acceptsOnlyHttpsUrlsWithAHostForMedia() {
        assertTrue(UrlSafety.isHttpsUrl("https://i.redd.it/image.jpg"))
        assertFalse(UrlSafety.isHttpsUrl("http://i.redd.it/image.jpg"))
        assertFalse(UrlSafety.isHttpsUrl("javascript:alert(1)"))
        assertFalse(UrlSafety.isHttpsUrl("https:///missing-host"))
    }
}
