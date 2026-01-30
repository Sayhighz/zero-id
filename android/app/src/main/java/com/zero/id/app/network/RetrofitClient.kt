package com.zero.id.app.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000" // Use 10.0.2.2 for Android emulator

    private val okHttpClient = OkHttpClient.Builder()
        .build()

    val instance: VerificationService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(VerificationService::class.java)
    }
}
