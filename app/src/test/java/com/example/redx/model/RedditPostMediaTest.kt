package com.example.redx.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RedditPostMediaTest {

    @Test
    fun animatedGifPrioritizesContentUrlOverStaticPreview() {
        val post = RedditPost(
            id = "t3_gif1",
            title = "Funny Cat GIF",
            author = "cat_fan",
            subreddit = "gifs",
            contentUrl = "https://i.redd.it/cat_dancing.gif",
            domain = "i.redd.it",
            previewImageUrl = "https://preview.redd.it/cat_dancing.jpg?width=640&crop=smart&auto=webp&s=abcd",
            thumbnailUrl = "https://b.thumbs.redditmedia.com/thumb.jpg"
        )

        assertTrue("Should detect animated GIF", post.isAnimatedGif)
        assertEquals(
            "displayImageUrl must return animated GIF contentUrl rather than static JPEG preview",
            "https://i.redd.it/cat_dancing.gif",
            post.displayImageUrl
        )
    }

    @Test
    fun standardImageUsesPreviewUrl() {
        val post = RedditPost(
            id = "t3_img1",
            title = "Beautiful Landscape",
            author = "photo_user",
            subreddit = "earthporn",
            contentUrl = "https://i.redd.it/mountain.jpg",
            domain = "i.redd.it",
            previewImageUrl = "https://preview.redd.it/mountain.jpg?width=1080&crop=smart&auto=webp&s=1234",
            thumbnailUrl = "https://b.thumbs.redditmedia.com/thumb.jpg"
        )

        assertFalse("Static JPG is not animated GIF", post.isAnimatedGif)
        assertEquals(
            "https://preview.redd.it/mountain.jpg?width=1080&crop=smart&auto=webp&s=1234",
            post.displayImageUrl
        )
    }

    @Test
    fun videoPostDetection() {
        val redditVideoPost = RedditPost(
            id = "t3_vid1",
            title = "Amazing goal",
            author = "sports_guy",
            subreddit = "soccer",
            contentUrl = "https://v.redd.it/goal123",
            domain = "v.redd.it",
            videoUrl = "https://v.redd.it/goal123/HLSPlaylist.m3u8",
            isVideo = true
        )
        assertTrue(redditVideoPost.isMediaVideo)

        val directMp4Post = RedditPost(
            id = "t3_vid2",
            title = "Clip",
            author = "user2",
            subreddit = "videos",
            contentUrl = "https://example.com/clip.mp4",
            domain = "example.com"
        )
        assertTrue(directMp4Post.isMediaVideo)

        val redgifsPost = RedditPost(
            id = "t3_vid3",
            title = "Dance",
            author = "user3",
            subreddit = "dance",
            contentUrl = "https://redgifs.com/watch/cleverdancer",
            domain = "redgifs.com"
        )
        assertTrue(redgifsPost.isMediaVideo)

        val imgurGifvPost = RedditPost(
            id = "t3_vid4",
            title = "Reaction",
            author = "user4",
            subreddit = "reactiongifs",
            contentUrl = "https://i.imgur.com/reaction.gifv",
            domain = "imgur.com"
        )
        assertTrue(imgurGifvPost.isMediaVideo)
    }

    @Test
    fun urlWithQueryParamsAndHashesDetectedCorrectly() {
        val gifWithQuery = RedditPost(
            id = "t3_gif2",
            title = "GIF with params",
            author = "author",
            subreddit = "gifs",
            contentUrl = "https://i.redd.it/animated.gif?format=mp4&s=abcdef#hash",
            domain = "i.redd.it"
        )
        assertTrue(gifWithQuery.isAnimatedGif)
        assertEquals(
            "https://i.redd.it/animated.gif?format=mp4&s=abcdef#hash",
            gifWithQuery.displayImageUrl
        )

        val mp4WithQuery = RedditPost(
            id = "t3_vid5",
            title = "MP4 with params",
            author = "author",
            subreddit = "videos",
            contentUrl = "https://v.redd.it/xyz/DASH_720.mp4?source=fallback&token=123",
            domain = "v.redd.it"
        )
        assertTrue(mp4WithQuery.isMediaVideo)
    }

    @Test
    fun galleryPostDetectionAndDisplay() {
        val galleryPost = RedditPost(
            id = "t3_gal1",
            title = "Trip to Tokyo",
            author = "traveler",
            subreddit = "travel",
            contentUrl = "https://www.reddit.com/gallery/gal1",
            domain = "reddit.com",
            galleryImageUrls = listOf(
                "https://i.redd.it/tokyo1.jpg",
                "https://i.redd.it/tokyo2.jpg",
                "https://i.redd.it/tokyo3.jpg"
            ),
            selfTextHtml = "Here are my photos from Tokyo!"
        )

        assertTrue("Should detect gallery post", galleryPost.isGallery)
        assertEquals(3, galleryPost.galleryImageUrls.size)
        assertEquals(
            "displayImageUrl must use first gallery image when previewImageUrl is absent",
            "https://i.redd.it/tokyo1.jpg",
            galleryPost.displayImageUrl
        )
        assertEquals("Here are my photos from Tokyo!", galleryPost.cleanSelfText)
    }
}
