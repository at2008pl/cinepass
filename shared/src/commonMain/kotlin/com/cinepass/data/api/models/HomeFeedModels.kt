package com.cinepass.data.api.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class HomeFeedData(
    val posts: List<FeedPostData>
)

@Serializable
data class FeedPostData(
    val id: String,
    val slug: String?,
    val title: String,
    val subtitle: String?,
    val description: String?,
    val section: String,
    @SerialName("media_type") val mediaType: String,
    @SerialName("media_url") val mediaUrl: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String?,
    @SerialName("cta_text") val ctaText: String?,
    @SerialName("sort_order") val sortOrder: Int?,
    @SerialName("is_active") val isActive: Boolean?,
    @SerialName("created_at") val createdAt: String?
)

