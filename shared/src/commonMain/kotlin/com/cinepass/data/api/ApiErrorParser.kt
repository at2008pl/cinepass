package com.cinepass.data.api

import com.cinepass.data.api.models.ApiResponse
import com.cinepass.data.api.models.Rs3Error
import com.cinepass.data.api.models.Rs3Response
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

private val errorJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

suspend fun HttpResponse.readApiErrorMessage(fallback: String): String {
    val status = status.value
    val bodyText = runCatching { bodyAsText() }.getOrNull().orEmpty()
    if (bodyText.isBlank()) return "$fallback (HTTP $status)"

    runCatching {
        errorJson.decodeFromString<ApiResponse<String>>(bodyText).message
    }.getOrNull()?.takeIf { it.isNotBlank() }?.let { return it }

    runCatching {
        errorJson.decodeFromString<Rs3Response<String>>(bodyText).error?.message
    }.getOrNull()?.takeIf { it.isNotBlank() }?.let { return it }

    runCatching {
        errorJson.decodeFromString<Rs3Error>(bodyText).message
    }.getOrNull()?.takeIf { it.isNotBlank() }?.let { return it }

    runCatching {
        errorJson.decodeFromString<Map<String, String>>(bodyText)["message"]
    }.getOrNull()?.takeIf { it.isNotBlank() }?.let { return it }

    return "$fallback (HTTP $status)"
}
