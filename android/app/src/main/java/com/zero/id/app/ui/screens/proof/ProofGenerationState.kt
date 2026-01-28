package com.zero.id.app.ui.screens.proof

import com.zero.id.network.VerificationRequest

sealed class ProofGenerationState {
    object Idle : ProofGenerationState()
    object Loading : ProofGenerationState()
    data class Success(val verificationRequest: VerificationRequest) : ProofGenerationState()
    data class Error(val message: String) : ProofGenerationState()
}
