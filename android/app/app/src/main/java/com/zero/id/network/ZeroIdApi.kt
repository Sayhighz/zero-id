package com.zero.id.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ZeroIdApi {
    @POST("api/verify")
    suspend fun verifyAge(@Body request: VerifyRequest): Response<VerifyResponse>
}