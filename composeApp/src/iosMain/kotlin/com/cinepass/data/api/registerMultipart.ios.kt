package com.cinepass.data.api

import io.ktor.client.HttpClient
import com.cinepass.data.api.models.ApiResponse
import com.cinepass.data.api.models.AuthData
import retrofit2.Response

actual suspend fun registerMultipart(
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
): Response<ApiResponse<AuthData>> {
    return Response.error(501) // Not Implemented / Swift wrapper placeholder on iOS
}
