package com.cinepass.data.api

import com.cinepass.data.models.*
import retrofit2.http.*

interface ApiService {

    // ─── Auth ────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body body: Map<String, String>): ApiResponse<User>

    @POST("auth/register")
    suspend fun register(@Body body: Map<String, String>): ApiResponse<User>

    // ─── Events ──────────────────────────────────────────
    @GET("events")
    suspend fun getEvents(
        @Query("city") city: String? = null,
        @Query("type") type: String? = null
    ): ApiResponse<List<Event>>

    @GET("events/{id}")
    suspend fun getEventById(@Path("id") id: String): ApiResponse<Event>

    @POST("events/{id}/register")
    suspend fun registerForEvent(
        @Path("id") eventId: String,
        @Body body: Map<String, String> // referralCode if any
    ): ApiResponse<String>

    // ─── Tickets ─────────────────────────────────────────
    @GET("tickets/my")
    suspend fun getMyTickets(): ApiResponse<List<Ticket>>

    @POST("tickets/book")
    suspend fun bookTicket(@Body body: Map<String, String>): ApiResponse<Ticket>

    // ─── Wallet ──────────────────────────────────────────
    @GET("wallet/my")
    suspend fun getMyWallet(): ApiResponse<Wallet>

    @POST("wallet/redeem")
    suspend fun redeemCoins(@Body body: Map<String, Int>): ApiResponse<String>

    // ─── Referral ────────────────────────────────────────
    @GET("referrals/my")
    suspend fun getMyReferrals(): ApiResponse<List<Referral>>

    // ─── User ────────────────────────────────────────────
    @GET("users/profile")
    suspend fun getProfile(): ApiResponse<User>
}
