package com.zero.id.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // ⚠️ สำคัญ: หากรันใน Emulator ให้ใช้ http://10.0.2.2:3000/
    // ⚠️ หากรันในเครื่องจริง ให้ใช้ IP ของคอมพิวเตอร์คุณ (เช่น http://192.168.1.XX:3000/)
    private const val BASE_URL = "http://49.228.30.252:3000/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS) // เผื่อเวลาให้ ZK Proof ประมวลผล
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // แปลง JSON เป็น Kotlin Object
            .client(client)
            .build()
            .create(ApiService::class.java)
    }
}