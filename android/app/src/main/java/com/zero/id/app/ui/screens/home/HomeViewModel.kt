package com.zero.id.app.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zero.id.app.network.ChallengeResponse
import com.zero.id.app.network.RetrofitInstance
import com.zero.id.app.network.VerificationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI

class HomeViewModel : ViewModel() {

    private val _challengeResponse = MutableStateFlow<ChallengeResponse?>(null)
    val challengeResponse: StateFlow<ChallengeResponse?> = _challengeResponse

    private val _lastVerificationRequest = MutableStateFlow<VerificationRequest?>(null)
    val lastVerificationRequest: StateFlow<VerificationRequest?> = _lastVerificationRequest

    private val _lastProofJson = MutableStateFlow<String?>(null)
    val lastProofJson: StateFlow<String?> = _lastProofJson

    private val _lastPublicSignalsJson = MutableStateFlow<String?>(null)
    val lastPublicSignalsJson: StateFlow<String?> = _lastPublicSignalsJson

    private val client = OkHttpClient()
    private val gson = Gson()

    fun setLastProofData(request: VerificationRequest, proofJson: String, publicSignalsJson: String) {
        _lastVerificationRequest.value = request
        _lastProofJson.value = proofJson
        _lastPublicSignalsJson.value = publicSignalsJson
    }

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
                // Use the host from RetrofitInstance for local development
                val host = try {
                    URI(RetrofitInstance.BASE_URL).host
                } catch (e: Exception) {
                    "10.0.2.2"
                }

                val finalUrl = url.replace("localhost", host ?: "10.0.2.2")
                                  .replace(" ", "%20")

                Log.d("HomeViewModel", "Fetching challenge from: $finalUrl")

                val request = Request.Builder()
                    .url(finalUrl)
                    .build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val body = response.body?.string()
                    Log.d("HomeViewModel", "Server response: $body")

                    val challenge = gson.fromJson(body, ChallengeResponse::class.java)
                    _challengeResponse.value = challenge
                    Log.d("HomeViewModel", "Challenge fetched successfully: $challenge")
                } else {
                    Log.e("HomeViewModel", "Failed to fetch: ${response.code} ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching challenge", e)
            }
        }
    }

    fun clearChallenge() {
        _challengeResponse.value = null
    }

    fun clearLastProof() {
        _lastVerificationRequest.value = null
        _lastProofJson.value = null
        _lastPublicSignalsJson.value = null
    }
}
