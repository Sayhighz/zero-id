package com.zero.id.network

import com.zero.id.app.model.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ZeroIdApi {
    @POST("api/verify")
    suspend fun verify(@Body request: VerificationRequest): Response<VerificationResponse>

    @POST("api/session/create")
    suspend fun createSession(): Response<SessionResponse>

    @POST("api/session/{sessionId}/submit")
    suspend fun submitUserData(
        @Path("sessionId") sessionId: String,
        @Body userData: UserProfile
    ): Response<Unit>

    @GET("api/session/{sessionId}")
    suspend fun getSessionData(@Path("sessionId") sessionId: String): Response<UserProfile>
}

data class SessionResponse(val sessionId: String)
