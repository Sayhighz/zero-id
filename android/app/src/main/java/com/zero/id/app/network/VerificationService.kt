package com.zero.id.app.network

import retrofit2.http.Body
import retrofit2.http.POST

interface VerificationService {
    @POST("/api/verify")
    suspend fun verify(@Body request: VerificationRequest): VerificationResponse

    @POST("/api/verify-citizen")
    suspend fun verifyCitizen(@Body request: VerificationRequest): VerificationResponse
}
