package com.cinepass.data.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class PincodeResponse(
    val Status: String,
    @SerialName("PostOffice") val postOffice: List<PostOffice>? = null,
)

@Serializable
private data class PostOffice(
    val District: String,
    val State: String,
)

suspend fun lookupPincode(client: HttpClient, pincode: String): Pair<String, String>? {
    return try {
        val responseText = client.get("https://api.postalpincode.in/pincode/$pincode").bodyAsText()
        val results = Json { ignoreUnknownKeys = true }.decodeFromString<List<PincodeResponse>>(responseText)
        val first = results.firstOrNull() ?: return null
        if (first.Status != "Success") return null
        val office = first.postOffice?.firstOrNull() ?: return null
        office.District to office.State
    } catch (_: Exception) {
        null
    }
}
