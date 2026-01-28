package com.zero.id.app.ui.screens.proof

import android.app.Application
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.zero.id.app.zkp.ProofResult
import com.zero.id.app.zkp.ZKProver
import com.zero.id.network.VerificationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ZkpState {
    object Idle : ZkpState()
    object Initializing : ZkpState()
    data class Initialized(val zkProver: ZKProver) : ZkpState()
    data class Error(val message: String) : ZkpState()
}

class ProofGenerationViewModel(
    application: Application,
    private val zkProver: ZKProver
) : AndroidViewModel(application) {

    private val _zkpState = MutableStateFlow<ZkpState>(ZkpState.Idle)
    val zkpState = _zkpState.asStateFlow()

    private val _proofState = MutableStateFlow<ProofGenerationState>(ProofGenerationState.Idle)
    val proofState = _proofState.asStateFlow()

    val birthYear = MutableStateFlow("")

    fun initializeZkp(webView: WebView) {
        if (_zkpState.value is ZkpState.Idle) {
            _zkpState.value = ZkpState.Initializing
            viewModelScope.launch {
                try {
                    zkProver.initialize(webView) { success ->
                        if (success) {
                            _zkpState.value = ZkpState.Initialized(zkProver)
                        } else {
                            _zkpState.value = ZkpState.Error("Failed to initialize proof generator")
                        }
                    }
                } catch (e: Exception) {
                    _zkpState.value = ZkpState.Error("Error: ${e.message}")
                }
            }
        }
    }

    fun generateProof(minAge: String) {
        val currentZkpState = _zkpState.value
        if (currentZkpState is ZkpState.Initialized) {
            _proofState.value = ProofGenerationState.Loading
            viewModelScope.launch {
                try {
                    val result = currentZkpState.zkProver.generateProof(
                        birthYear.value.toInt(),
                        minAge.toInt(),
                        java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    )
                    when (result) {
                        is ProofResult.Success -> {
                            val verificationRequest = VerificationRequest(
                                proof = Gson().fromJson(Gson().toJson(result.proof), com.zero.id.network.Proof::class.java),
                                publicSignals = result.publicSignals
                            )
                            _proofState.value = ProofGenerationState.Success(verificationRequest)
                        }
                        is ProofResult.Error -> {
                            _proofState.value = ProofGenerationState.Error(result.message)
                        }
                    }
                } catch (e: Exception) {
                    _proofState.value = ProofGenerationState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }
}
