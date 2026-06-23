package com.cinepass.data.repository

import com.cinepass.data.api.AuthIdentifiers
import com.cinepass.data.api.readApiErrorMessage
import com.cinepass.data.api.RegisterFormData
import com.cinepass.data.api.readApiErrorMessage
import com.cinepass.data.api.models.ApiResponse
import com.cinepass.data.api.models.AuthData
import com.cinepass.data.api.models.LoginRequest
import com.cinepass.data.api.models.MessageResponse
import com.cinepass.data.api.models.OtpSendRequest
import com.cinepass.data.api.models.OtpVerifyRequest
import com.cinepass.data.api.models.OtpVerifyResponse
import com.cinepass.data.prefs.UserPrefs
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class AuthRepository(
    private val client: HttpClient,
    private val userPrefs: UserPrefs,
) {
    suspend fun loginWithPassword(identifier: String, password: String): Result<Unit> {
        return runCatching {
            val normalizedIdentifier = AuthIdentifiers.normalizeLoginIdentifier(identifier)
            val response = client.post("auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(identifier = normalizedIdentifier, password = password))
            }

            if (!response.status.isSuccess()) {
                error(response.readApiErrorMessage("Invalid credentials"))
            }

            val body = response.body<ApiResponse<AuthData>>()
            if (!body.success || body.data == null) {
                error(body.message ?: "Login failed")
            }

            persistAuth(body.data, normalizedIdentifier)
        }
    }

    suspend fun sendLoginOtp(phone: String): Result<String> {
        return runCatching {
            if (!AuthIdentifiers.isValidPhone(phone)) {
                error("Enter a valid 10-digit mobile number")
            }

            val normalizedPhone = AuthIdentifiers.normalizePhone(phone)
            val response = client.post("auth/app/otp/send") {
                contentType(ContentType.Application.Json)
                setBody(OtpSendRequest(phone = normalizedPhone))
            }

            if (!response.status.isSuccess()) {
                error(response.readApiErrorMessage("Failed to send OTP"))
            }

            val body = response.body<MessageResponse>()
            body.message?.takeIf { it.isNotBlank() } ?: "OTP sent successfully"
        }
    }

    suspend fun verifyLoginOtp(phone: String, otp: String): Result<Unit> {
        return runCatching {
            val normalizedPhone = AuthIdentifiers.normalizePhone(phone)
            val cleanOtp = otp.trim()
            if (!Regex("""^\d{4,6}$""").matches(cleanOtp)) {
                error("Enter the 4–6 digit OTP")
            }

            val response = client.post("auth/app/otp/verify") {
                contentType(ContentType.Application.Json)
                setBody(OtpVerifyRequest(phone = normalizedPhone, otp = cleanOtp))
            }

            if (!response.status.isSuccess()) {
                error(response.readApiErrorMessage("Invalid or expired OTP"))
            }

            val body = response.body<OtpVerifyResponse>()
            if (body.token.isBlank()) {
                error("Login failed — no token received")
            }

            userPrefs.saveAuthData(
                accessToken = body.token,
                refreshToken = null,
                userId = body.user.id.toString(),
                name = body.user.name,
                email = body.user.email,
                phone = body.user.phone,
                referralCode = body.user.referralCode,
                coins = body.user.coins,
                isVerified = true,
                selfieUrl = null,
            )
            userPrefs.saveRememberedIdentifier(normalizedPhone)
        }
    }

    suspend fun register(form: RegisterFormData): Result<Unit> {
        return runCatching {
            val response = client.post("auth/register") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("name", form.name)
                            append("email", form.email)
                            append("phone", form.phone)
                            append("password", form.password)
                            append("confirmPassword", form.confirmPassword)
                            append("otp", form.otp)
                            form.gender?.let { append("gender", it) }
                            form.dob?.let { append("dob", it) }
                            form.addressLine?.let { append("addressLine", it) }
                            form.city?.let { append("city", it) }
                            form.state?.let { append("state", it) }
                            form.pincode?.let { append("pincode", it) }
                            form.referralCode?.let { append("referralCode", it) }
                            append(
                                "selfie",
                                form.selfieBytes,
                                Headers.build {
                                    append(HttpHeaders.ContentType, "image/jpeg")
                                    append(
                                        HttpHeaders.ContentDisposition,
                                        "filename=\"${form.selfieFileName}\"",
                                    )
                                },
                            )
                        },
                    ),
                )
            }

            if (!response.status.isSuccess()) {
                val status = response.status.value
                val message = response.readApiErrorMessage("Registration failed")
                error(
                    when (status) {
                        405 -> "Registration API blocked by server (HTTP 405). Run the backend locally on port 8055, or fix nginx to allow POST /v1/auth/register."
                        409 -> message
                        else -> message
                    }
                )
            }

            val body = response.body<ApiResponse<AuthData>>()
            if (!body.success || body.data == null) {
                error(body.message ?: "Registration failed")
            }

            persistAuth(body.data, form.phone)
        }
    }

    private fun persistAuth(auth: AuthData, rememberedIdentifier: String) {
        userPrefs.saveAuthData(
            accessToken = auth.accessToken,
            refreshToken = auth.refreshToken,
            userId = auth.user.id,
            name = auth.user.name,
            email = auth.user.email,
            phone = auth.user.phone,
            referralCode = auth.user.referralCode,
            coins = auth.user.coins,
            isVerified = auth.user.isVerified,
            selfieUrl = auth.user.selfieUrl,
        )
        userPrefs.saveRememberedIdentifier(rememberedIdentifier)
    }
}
