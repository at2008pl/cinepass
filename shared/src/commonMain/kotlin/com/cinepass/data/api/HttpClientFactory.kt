package com.cinepass.data.api

import com.cinepass.utils.Constants
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun createPlatformHttpClient(configure: HttpClientConfig<*>.() -> Unit = {}): HttpClient

fun createAppHttpClient(json: Json): HttpClient = createPlatformHttpClient {
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 60_000
        requestTimeoutMillis = 90_000
    }
    install(ContentNegotiation) {
        json(json)
    }
    defaultRequest {
        url(Constants.BASE_URL)
    }
}
