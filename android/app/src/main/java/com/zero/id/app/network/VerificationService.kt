package com.zero.id.app.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VerificationService {
    @POST("/api/verify-profile")
    suspend fun verify(@Body request: VerificationRequest): Response<VerificationResponse>

    @POST("/api/verify-citizen")
    suspend fun verifyCitizen(@Body request: VerificationRequest): Response<VerificationResponse>

    @POST("/api/verify-direct")
    suspend fun verifyDirect(@Body request: VerificationRequest): Response<VerificationResponse>
}
