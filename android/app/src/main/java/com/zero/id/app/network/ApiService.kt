package com.zero.id.app.network

import retrofit2.http.GET
import retrofit2.http.Headers

interface ApiService {
    @Headers("Content-Type: application/json")
    @GET("api/generate-challenge")
    suspend fun generateChallenge(): ChallengeResponse
}
