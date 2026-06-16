package com.rs3films.app.data.api

import com.rs3films.app.data.api.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── Auth ─────────────────────────────────────────────────────────
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthData>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthData>>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<ApiResponse<Any>>

    // ── Events ───────────────────────────────────────────────────────
    @GET("events")
    suspend fun listEvents(
        @Query("city") city: String? = null,
        @Query("type") type: String? = null,
        @Query("page") page: Int = 1
    ): Response<PaginatedResponse<EventData>>

    @GET("events/{id}")
    suspend fun getEvent(@Path("id") id: String): Response<ApiResponse<EventData>>

    @POST("events/{eventId}/register")
    suspend fun registerForEvent(
        @Path("eventId") eventId: String,
        @Header("Authorization") token: String
    ): Response<ApiResponse<RegisterEventResponse>>

    // ── Tickets ──────────────────────────────────────────────────────
    @POST("tickets/book")
    suspend fun bookTicket(
        @Header("Authorization") token: String,
        @Body request: BookTicketRequest
    ): Response<ApiResponse<BookTicketResponse>>

    @GET("tickets/mine")
    suspend fun getMyTickets(
        @Header("Authorization") token: String
    ): Response<ApiResponse<MyTicketsResponse>>

    @DELETE("tickets/{ticketId}")
    suspend fun cancelTicket(
        @Header("Authorization") token: String,
        @Path("ticketId") ticketId: String
    ): Response<ApiResponse<Any>>

    // ── Wallet ───────────────────────────────────────────────────────
    @GET("users/wallet")
    suspend fun getWallet(
        @Header("Authorization") token: String
    ): Response<ApiResponse<WalletData>>

    @GET("users/wallet/transactions")
    suspend fun getTransactions(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1
    ): Response<PaginatedResponse<TransactionData>>

    @POST("users/wallet/redeem")
    suspend fun redeemCoins(
        @Header("Authorization") token: String,
        @Body request: RedeemRequest
    ): Response<ApiResponse<Any>>

    // ── User ─────────────────────────────────────────────────────────
    @GET("users/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<ApiResponse<UserData>>

    @GET("users/leaderboard")
    suspend fun getLeaderboard(
        @Header("Authorization") token: String
    ): Response<ApiResponse<Any>>
}
