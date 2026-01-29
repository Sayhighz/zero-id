package com.zero.id.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ZeroIdApi {
    @POST("api/verify")
    suspend fun verify(@Body request: VerificationRequest): Response<VerificationResponse>

    /**
     * Submits consented user data to the backend.
     */
    @POST("api/submit-data")
    suspend fun submitUserData(@Body userData: Map<String, String?>): Response<ResponseBody>
}
