package com.zero.id.network

interface ApiService {
    @POST("api/verify")
    suspend fun verifyProof(@Body request: VerifyRequest): Response<VerifyResponse>
}