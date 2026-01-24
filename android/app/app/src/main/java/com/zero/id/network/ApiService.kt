package com.zero.id.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/verify")
    suspend fun verifyProof(@Body request: ProofRequest): Response<ProofResponse>
}