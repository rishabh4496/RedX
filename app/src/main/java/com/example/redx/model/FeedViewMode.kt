package com.example.redx.model

enum class FeedViewMode(
    val label: String,
    val subtitle: String,
    val category: String,
    val badgeText: String,
    val iconName: String
) {
    CARDS("Cards", "Full media preview, post header, and action bar", "Classic", "Standard", "ViewAgenda"),
    COMPACT("Compact", "Dense list with left thumbnail & metadata", "Dense & Text", "Dense", "ViewHeadline"),
    GALLERY("Gallery", "Multi-column media-first masonry grid", "Media First", "Media", "GridView"),
    RELAY("Relay Deck", "Subreddit color bar with quick action strip", "Classic", "Relay", "ViewStream"),
    APOLLO("Apollo Pure", "iOS minimalist flat layout with right thumbnail", "Dense & Text", "iOS", "PhoneIphone"),
    FULL_BLEED("Full-Bleed", "Immersive edge-to-edge media with floating dock", "Media First", "Reels", "SmartDisplay"),
    TEXT_ONLY("Text Reader", "Distraction-free typography with post excerpts", "Dense & Text", "Reader", "Article"),
    MAGAZINE("Magazine", "Editorial hero banner with headline & lead excerpt", "Modern", "Editorial", "AutoStories"),
    BIG_TILES("Big Tiles", "Cinematic 16:9 widescreen media with corner pills", "Media First", "Cinematic", "ViewQuilt"),
    STREAMLINE("Streamline", "Smooth floating cards with 20dp pill actions", "Modern", "Modern", "FilterFrames"),
    SOCIAL_CHAT("Threaded Social", "Avatar-first timeline feed with thread connectors", "Modern", "Social", "Chat")
}

enum class AppTheme(val label: String, val description: String) {
    AMOLED_BLACK("AMOLED Pure Black", "Pitch black for OLED screens & battery savings"),
    MIDNIGHT_BLUE("Midnight Blue", "Deep navy blue palette with cyan accents"),
    SUNSET_ORANGE("Sunset Amber", "Warm dark chocolate with vibrant amber accents"),
    CYBERPUNK_CRIMSON("Cyberpunk Crimson", "Obsidian base with neon crimson & violet accents"),
    MATRIX_EMERALD("Matrix Emerald", "Deep slate with glowing neon emerald & mint accents")
}

enum class FontScale(val label: String, val scale: Float) {
    SMALL("Compact", 0.9f),
    NORMAL("Standard", 1.0f),
    LARGE("Comfortable", 1.18f)
}
