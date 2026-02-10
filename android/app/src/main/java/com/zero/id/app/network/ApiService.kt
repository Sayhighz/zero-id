package com.zero.id.app.network

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ApiService {
    @Headers("Content-Type: application/json")
    @GET("api/generate-challenge")
    suspend fun generateChallenge(
        @Query("verifierName") verifierName: String = "ZeroID Bank",
        @Query("minAge") minAge: Int = 20,
        @Query("minSalary") minSalary: Int = 15000
    ): ChallengeResponse
}
