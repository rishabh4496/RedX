package com.example.redx.util

import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import java.net.URI
import java.util.Locale

/** Lightweight, dependency-free ad/tracker blocking for content rendered inside RedX. */
object AdBlocker {

    private val blockedHosts = setOf(
        "doubleclick.net",
        "googlesyndication.com",
        "googleadservices.com",
        "adservice.google.com",
        "googletagmanager.com",
        "google-analytics.com",
        "amazon-adsystem.com",
        "adsrvr.org",
        "adnxs.com",
        "criteo.com",
        "taboola.com",
        "outbrain.com",
        "scorecardresearch.com",
        "quantserve.com",
        "moatads.com",
        "adsafeprotected.com",
        "rubiconproject.com",
        "pubmatic.com",
        "smartadserver.com",
        "advertising.com",
        "adform.net",
        "yieldmo.com"
    )

    fun shouldBlock(url: String?): Boolean {
        val host = runCatching { URI(url).host?.lowercase(Locale.ROOT) }.getOrNull() ?: return false
        return blockedHosts.any { host == it || host.endsWith(".$it") }
    }

    fun emptyResponse(): WebResourceResponse = WebResourceResponse(
        "text/plain",
        "UTF-8",
        ByteArrayInputStream(ByteArray(0))
    )

    /** CSS selectors for ad slots and promotional UI owned by the rendered Reddit page. */
    const val HIDE_ADS_SCRIPT = """
        (function() {
            var selectors = [
                'shreddit-ad-post',
                'shreddit-ad-slot',
                '[data-testid="ad-container"]',
                '[data-testid="promoted-post"]',
                '[data-testid*="ad"]',
                '.promotedlink',
                '.promoted-post',
                '.ad-container',
                '.ad-holder',
                '.banner-ad',
                '.display-ad',
                'iframe[src*="doubleclick"]',
                'iframe[src*="googlesyndication"]',
                'iframe[src*="amazon-adsystem"]'
            ];
            var style = document.getElementById('redx-ad-free-style');
            if (!style) {
                style = document.createElement('style');
                style.id = 'redx-ad-free-style';
                style.textContent = selectors.join(',') + '{display:none!important;visibility:hidden!important;height:0!important;min-height:0!important;}';
                document.head && document.head.appendChild(style);
            }
            selectors.forEach(function(selector) {
                document.querySelectorAll(selector).forEach(function(element) { element.remove(); });
            });
        })();
    """
}
