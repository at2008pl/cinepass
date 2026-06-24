package com.cinepass.utils

import com.cinepass.data.api.models.Rs3FeedPost

/**
 * Mobile devices cannot load http://localhost/... — rewrite to the hosted API origin.
 */
fun resolveMediaUrl(url: String?): String? {
    if (url.isNullOrBlank()) return url

    val mediaOrigin = HOSTED_WEB_BASE_URL.trimEnd('/')

    if (url.startsWith("/")) {
        return "$mediaOrigin$url"
    }

    val host = url.substringAfter("://", "").substringBefore("/")
    if (host.equals("localhost", ignoreCase = true) ||
        host.startsWith("127.0.0.1") ||
        host.startsWith("localhost:")
    ) {
        val path = url.substringAfter("://").substringAfter(host)
        return "$mediaOrigin$path"
    }

    return url
}

fun Rs3FeedPost.withResolvedMedia(): Rs3FeedPost = copy(
    mediaUrl = resolveMediaUrl(mediaUrl),
    thumbnailUrl = resolveMediaUrl(thumbnailUrl),
)
