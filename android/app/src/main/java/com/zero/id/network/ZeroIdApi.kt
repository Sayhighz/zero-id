package com.zero.id.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ZeroIdApi {
    @POST("api/verify")
    suspend fun verify(@Body request: VerificationRequest): Response<VerificationResponse>
}
