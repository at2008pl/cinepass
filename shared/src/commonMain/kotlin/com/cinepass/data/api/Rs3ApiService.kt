package com.cinepass.data.api

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import com.cinepass.data.api.models.*
import retrofit2.Response

class Rs3ApiService(private val client: HttpClient) {

    private suspend inline fun <reified T> safeGet(path: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): Response<T> {
        return try {
            val httpResponse = client.get(path) {
                block()
            }
            if (httpResponse.status.isSuccess()) {
                Response.success(httpResponse.body<T>())
            } else {
                Response.error(httpResponse.status.value, null)
            }
        } catch (e: Exception) {
            Response.error(500, null)
        }
    }

    private suspend inline fun <reified T, reified B> safePost(path: String, body: B, crossinline block: HttpRequestBuilder.() -> Unit = {}): Response<T> {
        return try {
            val httpResponse = client.post(path) {
                contentType(ContentType.Application.Json)
                setBody(body)
                block()
            }
            if (httpResponse.status.isSuccess()) {
                Response.success(httpResponse.body<T>())
            } else {
                Response.error(httpResponse.status.value, null)
            }
        } catch (e: Exception) {
            Response.error(500, null)
        }
    }

    private suspend inline fun <reified T> safePostEmpty(path: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): Response<T> {
        return try {
            val httpResponse = client.post(path) {
                block()
            }
            if (httpResponse.status.isSuccess()) {
                Response.success(httpResponse.body<T>())
            } else {
                Response.error(httpResponse.status.value, null)
            }
        } catch (e: Exception) {
            Response.error(500, null)
        }
    }

    private suspend inline fun <reified T, reified B> safePut(path: String, body: B, crossinline block: HttpRequestBuilder.() -> Unit = {}): Response<T> {
        return try {
            val httpResponse = client.put(path) {
                contentType(ContentType.Application.Json)
                setBody(body)
                block()
            }
            if (httpResponse.status.isSuccess()) {
                Response.success(httpResponse.body<T>())
            } else {
                Response.error(httpResponse.status.value, null)
            }
        } catch (e: Exception) {
            Response.error(500, null)
        }
    }

    suspend fun sendOtp(request: OtpSendRequest): Response<Rs3Response<Any>> =
        safePost("auth/app/otp/send", request)

    suspend fun verifyOtp(request: OtpVerifyRequest): Response<OtpVerifyResponse> =
        safePost("auth/app/otp/verify", request)

    suspend fun getProfile(token: String): Response<Rs3Profile> =
        safeGet("app/member/profile") {
            header("Authorization", token)
        }

    suspend fun updateProfile(token: String, request: UpdateProfileRequest): Response<Rs3Profile> =
        safePut("app/member/profile", request) {
            header("Authorization", token)
        }

    suspend fun getOffers(token: String, page: String? = null): Response<Rs3Response<List<Rs3Offer>>> =
        safeGet("app/offers") {
            header("Authorization", token)
            if (page != null) parameter("page", page)
        }

    suspend fun claimOffer(token: String, offerId: Int): Response<ClaimOfferResponse> =
        safePostEmpty("app/offers/$offerId/claim") {
            header("Authorization", token)
        }

    suspend fun getFeed(page: Int = 1, limit: Int = 20): Response<Rs3Response<List<Rs3FeedPost>>> =
        safeGet("app/feed") {
            parameter("page", page)
            parameter("limit", limit)
        }

    suspend fun getReferralTree(token: String): Response<Rs3ReferralTreeResponse> =
        safeGet("app/member/referral-tree") {
            header("Authorization", token)
        }

    suspend fun getMyRedemptions(token: String): Response<List<Rs3Redemption>> =
        safeGet("app/member/redemptions") {
            header("Authorization", token)
        }

    suspend fun applyReferral(token: String, request: ApplyReferralRequest): Response<Rs3Response<Any>> =
        safePost("app/referral/apply", request) {
            header("Authorization", token)
        }

    suspend fun getWallet(token: String): Response<Rs3WalletResponse> =
        safeGet("app/member/wallet") {
            header("Authorization", token)
        }

    suspend fun getTransactions(token: String, limit: Int = 50, offset: Int = 0): Response<List<Rs3CoinTransaction>> =
        safeGet("app/member/transactions") {
            header("Authorization", token)
            parameter("limit", limit)
            parameter("offset", offset)
        }

    suspend fun getAppConfig(): Response<Rs3AppConfig> =
        safeGet("app/config")

    suspend fun getCmsSection(section: String): Response<Rs3Response<List<Rs3CmsItem>>> =
        safeGet("app/cms/$section")
}
