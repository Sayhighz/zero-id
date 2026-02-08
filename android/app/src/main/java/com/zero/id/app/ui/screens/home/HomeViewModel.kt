package com.zero.id.app.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zero.id.app.network.ChallengeResponse
import com.zero.id.app.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson

class HomeViewModel : ViewModel() {

    private val _challengeResponse = MutableStateFlow<ChallengeResponse?>(null)
    val challengeResponse: StateFlow<ChallengeResponse?> = _challengeResponse

    private val client = OkHttpClient()
    private val gson = Gson()

    fun generateChallenge() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.generateChallenge()
                _challengeResponse.value = response
                Log.d("HomeViewModel", "Challenge generated: $response")
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error generating challenge", e)
            }
        }
    }

    fun fetchChallengeFromUrl(url: String) {
        viewModelScope.launch {
            try {
                // สำหรับ Android Emulator, localhost ต้องเปลี่ยนเป็น 10.0.2.2
                val finalUrl = url.replace("localhost", "10.0.2.2")

                val request = Request.Builder()
                    .url(finalUrl)
                    .build()

                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val body = response.body?.string()
                    val challenge = gson.fromJson(body, ChallengeResponse::class.java)
                    _challengeResponse.value = challenge
                    Log.d("HomeViewModel", "Challenge fetched from URL: $challenge")
                } else {
                    Log.e("HomeViewModel", "Failed to fetch challenge: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching challenge from URL", e)
            }
        }
    }

    fun clearChallenge() {
        _challengeResponse.value = null
    }
}
