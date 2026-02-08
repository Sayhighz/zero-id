package com.zero.id.app.ui.screens.proof

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.zero.id.app.network.Proof
import com.zero.id.app.network.VerificationRequest
import com.zero.id.library.android.ZkpProver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * State for proof generation screen
 */
sealed class ProofGenerationState {
    object Idle : ProofGenerationState()
    object Loading : ProofGenerationState()
    data class Success(
        val verificationRequest: VerificationRequest,
        val proofJson: String,
        val publicSignalsJson: String
    ) : ProofGenerationState()
    data class Error(val message: String) : ProofGenerationState()
}

/**
 * ViewModel for proof generation screen
 */
class ProofGenerationViewModel(
    application: Application,
    private val minAge: Int?,
    private val minSalary: Int?,
    private val currentYear: Int
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<ProofGenerationState>(ProofGenerationState.Idle)
    val state: StateFlow<ProofGenerationState> = _state.asStateFlow()

    private val zkpProver = ZkpProver(application)
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun generateProof(birthYear: Int, salary: Int) {
        if (minAge == null || minSalary == null) {
            _state.value = ProofGenerationState.Error("Missing requirements from verifier.")
            return
        }

        _state.value = ProofGenerationState.Loading

        viewModelScope.launch {
            try {
                val input = mapOf(
                    "birthYear" to birthYear.toString(),
                    "salary" to salary.toString(),
                    "minAge" to minAge.toString(),
                    "minSalary" to minSalary.toString(),
                    "currentYear" to currentYear.toString()
                )

                val (proofJson, publicSignalsJson) = zkpProver.generateProof(input)

                val proof = Gson().fromJson(proofJson, Proof::class.java)
                val publicSignals = Gson().fromJson(publicSignalsJson, List::class.java)

                val verificationRequest = VerificationRequest(
                    proof = proof,
                    publicSignals = publicSignals.map { it.toString() }
                )

                _state.value = ProofGenerationState.Success(
                    verificationRequest = verificationRequest,
                    proofJson = gson.toJson(proof),
                    publicSignalsJson = gson.toJson(publicSignals)
                )

            } catch (e: Exception) {
                _state.value = ProofGenerationState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun resetState() {
        _state.value = ProofGenerationState.Idle
    }
}
