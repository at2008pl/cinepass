package com.cinepass.data.api

import com.cinepass.data.api.models.ApiResponse
import com.cinepass.data.api.models.AuthData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import retrofit2.Response

data class RegisterFormData(
    val name: String,
    val email: String,
    val phone: String,
    val gender: String?,
    val dob: String?,
    val addressLine: String?,
    val city: String?,
    val state: String?,
    val pincode: String?,
    val password: String,
    val confirmPassword: String,
    val referralCode: String?,
    val otp: String,
    val selfieBytes: ByteArray,
    val selfieFileName: String = "selfie.jpg",
)

suspend fun registerMultipart(
    client: HttpClient,
    form: RegisterFormData,
): Response<ApiResponse<AuthData>> {
    return try {
        val httpResponse = client.post("auth/register") {
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
        if (httpResponse.status.isSuccess()) {
            Response.success(httpResponse.body())
        } else {
            Response.error(httpResponse.status.value)
        }
    } catch (_: Exception) {
        Response.error(500)
    }
}
