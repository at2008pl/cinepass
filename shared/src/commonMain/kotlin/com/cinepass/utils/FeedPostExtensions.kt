package com.cinepass.utils

import com.cinepass.data.api.models.Rs3FeedPost

private val VIDEO_EXTENSIONS = listOf(".mp4", ".mov", ".webm", ".m4v", ".mkv", ".m3u8")

private fun isVideoFileUrl(url: String?): Boolean {
    if (url.isNullOrBlank()) return false
    val path = url.substringBefore("?").lowercase()
    return VIDEO_EXTENSIONS.any { path.endsWith(it) }
}

/** True when the post should use the inline looping video player (not Coil image). */
fun Rs3FeedPost.shouldPlayInlineVideo(): Boolean {
    if (mediaUrl.isNullOrBlank()) return false
    if (type.equals("video", ignoreCase = true)) return true
    if (layout.equals("reel", ignoreCase = true)) return true
    return isVideoFileUrl(mediaUrl)
}
