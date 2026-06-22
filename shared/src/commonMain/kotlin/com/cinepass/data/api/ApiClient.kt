package com.cinepass.data.api

import com.cinepass.data.api.createAppHttpClient
import kotlinx.serialization.json.Json

object ApiClient {
    val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    val httpClient = createAppHttpClient(jsonConfig)

    val apiService = ApiService(httpClient)
    val rs3Api = Rs3ApiService(httpClient)
}
