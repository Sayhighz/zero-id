package com.zero.id.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:3000/" // ไอพีสำหรับ Emulator ต่อเข้าเครื่องตัวเอง

    val instance: ZeroIdApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ZeroIdApi::class.java)
    }
}