package com.cinepass.data.api

import io.ktor.client.HttpClient
import com.cinepass.data.api.models.ApiResponse
import com.cinepass.data.api.models.AuthData
import retrofit2.Response
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.serialization.json.Json
import com.cinepass.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    return withContext(Dispatchers.IO) {
        try {
            val okHttpClient = OkHttpClient()
            val requestBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addPart(selfie as MultipartBody.Part)
                .addFormDataPart("name", null, name as RequestBody)
                .addFormDataPart("email", null, email as RequestBody)
                .addFormDataPart("phone", null, phone as RequestBody)
                .addFormDataPart("password", null, password as RequestBody)
                .addFormDataPart("confirmPassword", null, confirmPassword as RequestBody)
                .addFormDataPart("otp", null, otp as RequestBody)

            if (gender != null) requestBodyBuilder.addFormDataPart("gender", null, gender as RequestBody)
            if (dob != null) requestBodyBuilder.addFormDataPart("dob", null, dob as RequestBody)
            if (addressLine != null) requestBodyBuilder.addFormDataPart("addressLine", null, addressLine as RequestBody)
            if (city != null) requestBodyBuilder.addFormDataPart("city", null, city as RequestBody)
            if (state != null) requestBodyBuilder.addFormDataPart("state", null, state as RequestBody)
            if (pincode != null) requestBodyBuilder.addFormDataPart("pincode", null, pincode as RequestBody)
            if (referralCode != null) requestBodyBuilder.addFormDataPart("referralCode", null, referralCode as RequestBody)

            val okRequest = Request.Builder()
                .url(Constants.BASE_URL + "auth/register")
                .post(requestBodyBuilder.build())
                .build()

            val response = okHttpClient.newCall(okRequest).execute()
            if (response.isSuccessful) {
                val jsonString = response.body?.string() ?: ""
                val json = Json { ignoreUnknownKeys = true }
                val apiResponse = json.decodeFromString<ApiResponse<AuthData>>(jsonString)
                Response.success(apiResponse)
            } else {
                Response.error(response.code)
            }
        } catch (e: Exception) {
            Response.error(500)
        }
    }
}
