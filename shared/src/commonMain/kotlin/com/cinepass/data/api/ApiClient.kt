package com.cinepass.data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import com.cinepass.utils.Constants

object ApiClient {
    val jsonConfig = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
        defaultRequest {
            url(Constants.BASE_URL)
        }
    }

    val apiService = ApiService(httpClient)
    val rs3Api = Rs3ApiService(httpClient)
}
