package com.zero.id.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // IMPORTANT: REPLACE WITH YOUR COMPUTER'S LOCAL IP ADDRESS
    // This address is for connecting from a real device on the same Wi-Fi network.
    // Find your IP using 'ipconfig' (Windows) or 'ifconfig' (macOS/Linux).
    private const val BASE_URL = "http://192.168.8.162:3000/"

    val instance: ZeroIdApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ZeroIdApi::class.java)
    }
}
