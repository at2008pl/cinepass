package com.cinepass.data.api

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import com.cinepass.data.api.models.*
import com.cinepass.data.models.*
import retrofit2.Response

expect suspend fun registerMultipart(
    client: HttpClient,
    name: Any,
    email: Any,
    phone: Any,
    gender: Any?,
    dob: Any?,
    addressLine: Any?,
    city: Any?,
    state: Any?,
    pincode: Any?,
    password: Any,
    confirmPassword: Any,
    referralCode: Any?,
    otp: Any,
    selfie: Any
): Response<ApiResponse<AuthData>>

class ApiService(private val client: HttpClient) {

    private suspend inline fun <reified T> safeGet(path: String, crossinline block: HttpRequestBuilder.() -> Unit = {}): Response<T> {
        return try {
            val httpResponse = client.get(path) {
                block()
            }
            if (httpResponse.status.isSuccess()) {
                Response.success(httpResponse.body<T>())
            } else {
                Response.error(httpResponse.status.value)
            }
        } catch (e: Exception) {
            Response.error(500)
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
                Response.error(httpResponse.status.value)
            }
        } catch (e: Exception) {
            Response.error(500)
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
                Response.error(httpResponse.status.value)
            }
        } catch (e: Exception) {
            Response.error(500)
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
                Response.error(httpResponse.status.value)
            }
        } catch (e: Exception) {
            Response.error(500)
        }
    }

    suspend fun login(request: LoginRequest): Response<ApiResponse<AuthData>> =
        safePost("auth/login", request)

    suspend fun register(
        name: Any,
        email: Any,
        phone: Any,
        gender: Any?,
        dob: Any?,
        addressLine: Any?,
        city: Any?,
        state: Any?,
        pincode: Any?,
        password: Any,
        confirmPassword: Any,
        referralCode: Any?,
        otp: Any,
        selfie: Any,
    ): Response<ApiResponse<AuthData>> = registerMultipart(
        client, name, email, phone, gender, dob, addressLine, city, state, pincode, password, confirmPassword, referralCode, otp, selfie
    )

    suspend fun sendOtp(request: OtpSendRequest): Response<ApiResponse<Any>> =
        safePost("auth/app/otp/send", request)

    suspend fun verifyOtp(request: VerifyOtpRequest): Response<ApiResponse<AuthData>> =
        safePost("auth/app/otp/verify", request)
    
    suspend fun getProfile(): Response<User> =
        safeGet("app/member/profile")

    suspend fun getProfile(token: String): Response<ApiResponse<UserData>> =
        safeGet("app/member/profile") {
            header("Authorization", token)
        }
    
    suspend fun updateProfile(request: com.cinepass.data.api.models.UpdateProfileRequest): Response<User> =
        safePut("app/member/profile", request)
    
    suspend fun getWallet(): Response<Wallet> =
        safeGet("app/member/wallet")

    suspend fun getWallet(token: String): Response<ApiResponse<WalletData>> =
        safeGet("app/member/wallet") {
            header("Authorization", token)
        }
    
    suspend fun getTransactions(
        page: Int = 1,
        limit: Int = 20
    ): Response<ApiResponse<List<WalletTransaction>>> =
        safeGet("app/member/transactions") {
            parameter("page", page)
            parameter("limit", limit)
        }

    suspend fun getTransactions(
        token: String,
        type: String? = null,
        page: Int = 1,
        limit: Int = 20,
    ): Response<ApiResponse<List<TransactionData>>> =
        safeGet("app/member/transactions") {
            header("Authorization", token)
            if (type != null) parameter("type", type)
            parameter("page", page)
            parameter("limit", limit)
        }
    
    suspend fun getOffers(
        page: String = "referral"
    ): Response<ApiResponse<List<Offer>>> =
        safeGet("app/offers") {
            parameter("page", page)
        }
    
    suspend fun claimOffer(
        offerId: Int
    ): Response<com.cinepass.data.api.models.ClaimOfferResponse> =
        safePostEmpty("app/offers/$offerId/claim")
    
    suspend fun getFeed(
        page: Int = 1,
        limit: Int = 20
    ): Response<ApiResponse<List<FeedPost>>> =
        safeGet("app/feed") {
            parameter("page", page)
            parameter("limit", limit)
        }

    suspend fun getHomeFeed(
        city: String? = null,
        token: String? = null,
    ): Response<ApiResponse<HomeFeedData>> =
        safeGet("feed/home") {
            if (city != null) parameter("city", city)
            if (token != null) header("Authorization", token)
        }
    
    suspend fun getCmsContent(
        section: String
    ): Response<ApiResponse<List<AppContent>>> =
        safeGet("app/cms/$section")
    
    suspend fun getAppConfig(): Response<Map<String, String>> =
        safeGet("app/config")
    
    suspend fun getReferralStats(): Response<ReferralStats> =
        safeGet("app/member/referrals")
    
    suspend fun getReferralTree(): Response<ReferralTreeResponse> =
        safeGet("app/member/referral-tree")

    suspend fun getReferralTree(token: String): Response<ApiResponse<ReferralTreeResponse>> =
        safeGet("app/member/referral-tree") {
            header("Authorization", token)
        }

    suspend fun listEvents(
        city: String? = null,
    ): Response<ApiResponse<List<EventData>>> =
        safeGet("events") {
            if (city != null) parameter("city", city)
        }

    suspend fun getEvent(
        eventId: String,
    ): Response<ApiResponse<EventData>> =
        safeGet("events/$eventId")

    suspend fun registerForEvent(
        eventId: String,
        token: String,
    ): Response<ApiResponse<RegisterEventResponse>> =
        safePostEmpty("events/$eventId/register") {
            header("Authorization", token)
        }

    suspend fun bookTicket(
        token: String,
        request: BookTicketRequest,
    ): Response<ApiResponse<BookTicketResponse>> =
        safePost("tickets/book", request) {
            header("Authorization", token)
        }

    suspend fun getMyTickets(
        token: String,
    ): Response<ApiResponse<MyTicketsResponse>> =
        safeGet("tickets/me") {
            header("Authorization", token)
        }

    suspend fun getTicketById(
        token: String,
        ticketId: String,
    ): Response<ApiResponse<TicketData>> =
        safeGet("tickets/$ticketId") {
            header("Authorization", token)
        }

    suspend fun cancelTicket(
        token: String,
        ticketId: String,
    ): Response<ApiResponse<Any>> =
        safePostEmpty("tickets/$ticketId/cancel") {
            header("Authorization", token)
        }
}
