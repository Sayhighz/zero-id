package com.zero.id.app.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zero.id.app.network.ChallengeResponse
import com.zero.id.app.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _challengeResponse = MutableStateFlow<ChallengeResponse?>(null)
    val challengeResponse: StateFlow<ChallengeResponse?> = _challengeResponse

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

    fun clearChallenge() {
        _challengeResponse.value = null
    }
}
