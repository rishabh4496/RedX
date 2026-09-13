package com.example.redx.model

data class ReadLaterEntry(
    val postId: String,
    val title: String,
    val subreddit: String,
    val permalink: String,
    val previewImageUrl: String?,
    val author: String,
    val addedAt: Long = System.currentTimeMillis()
)
