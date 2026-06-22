package com.cinepass.data.repository

import com.cinepass.data.api.ApiService
import com.cinepass.data.models.FeedPost

class HomeFeedRepository(
    private val apiService: ApiService,
) {

    suspend fun getFeedPosts(): List<FeedPost> {
        return try {
            val response = apiService.getFeed(page = 1, limit = 50)
            if (response.isSuccessful) {
                response.body()?.data.orEmpty()
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

}
