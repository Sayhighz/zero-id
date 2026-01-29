package com.zero.id.app.ui.screens.proof

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.zero.id.app.ServiceLocator
import com.zero.id.app.zkp.ProofResult
import com.zero.id.app.zkp.ZKProver
import com.zero.id.network.VerificationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * State for proof generation screen
 */
sealed class ProofGenerationState {
    object Idle : ProofGenerationState()
    object Loading : ProofGenerationState()
    data class Success(val verificationRequest: VerificationRequest) : ProofGenerationState()
    data class Error(val message: String) : ProofGenerationState()
}

/**
 * ViewModel for proof generation screen
 * Manages user input and proof generation logic
 */
class ProofGenerationViewModel(
    application: Application,
    private val zkProver: ZKProver? = null
) : AndroidViewModel(application) {

    // --- Existing State for Manual Proof Generation ---
    private val _state = MutableStateFlow<ProofGenerationState>(ProofGenerationState.Idle)
    val state: StateFlow<ProofGenerationState> = _state.asStateFlow()

    private val _birthYear = MutableStateFlow("")
    val birthYear: StateFlow<String> = _birthYear.asStateFlow()

    private val _minAge = MutableStateFlow("")
    val minAge: StateFlow<String> = _minAge.asStateFlow()

    // --- New State for Simplified Proof Generation ---
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _proof = MutableStateFlow<String?>(null)
    val proof: StateFlow<String?> = _proof.asStateFlow()

    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private val profileStorage = ServiceLocator.provideProfileStorage(application)

    init {
        // Load initial data from profile storage
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val profile = profileStorage.getProfile()
        if (profile.birthYear > 0) {
            _birthYear.value = profile.birthYear.toString()
        }
    }

    // --- Functions for Simplified Age Proof ---
    fun generateAgeProof(minAge: Int) {
        if (zkProver == null) {
            // Handle error - perhaps update a separate error state flow
            return
        }

        val userBirthYear = profileStorage.getProfile().birthYear
        if (userBirthYear <= 0) {
            // Handle error: user profile is not complete
            return
        }

        _isGenerating.value = true
        viewModelScope.launch {
            try {
                val result = zkProver.generateProof(
                    birthYear = userBirthYear,
                    minAge = minAge,
                    currentYear = currentYear
                )
                if (result is ProofResult.Success) {
                    val verificationRequest = VerificationRequest(
                        proof = Gson().fromJson(Gson().toJson(result.proof), com.zero.id.network.Proof::class.java),
                        publicSignals = result.publicSignals
                    )
                    _proof.value = Gson().toJson(verificationRequest)
                } else {
                    // Handle error, maybe expose it via another StateFlow
                }
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun resetProof() {
        _proof.value = null
    }

    // --- Existing functions for manual proof generation ---
    fun updateBirthYear(year: String) {
        if (year.isEmpty() || (year.all { it.isDigit() } && year.length <= 4)) {
            _birthYear.value = year
        }
    }

    fun updateMinAge(age: String) {
        if (age.isEmpty() || (age.all { it.isDigit() } && age.length <= 3)) {
            _minAge.value = age
        }
    }

    fun validateInputs(): String? {
        val birthYearInt = _birthYear.value.toIntOrNull()
        val minAgeInt = _minAge.value.toIntOrNull()

        return when {
            _birthYear.value.isEmpty() -> "Please enter your birth year"
            _minAge.value.isEmpty() -> "Please enter minimum age"
            birthYearInt == null -> "Invalid birth year"
            minAgeInt == null -> "Invalid minimum age"
            birthYearInt < 1900 -> "Birth year must be 1900 or later"
            birthYearInt > currentYear -> "Birth year cannot be in the future"
            minAgeInt < 0 -> "Minimum age cannot be negative"
            minAgeInt > 150 -> "Minimum age must be 150 or less"
            else -> null // Valid
        }
    }

    fun generateProof() {
        val validationError = validateInputs()
        if (validationError != null) {
            _state.value = ProofGenerationState.Error(validationError)
            return
        }

        if (zkProver == null) {
            _state.value = ProofGenerationState.Error("ZK Prover not initialized")
            return
        }

        val birthYearInt = _birthYear.value.toInt()
        val minAgeInt = _minAge.value.toInt()

        _state.value = ProofGenerationState.Loading

        viewModelScope.launch {
            try {
                val result = zkProver.generateProof(
                    birthYear = birthYearInt,
                    minAge = minAgeInt,
                    currentYear = currentYear
                )

                when (result) {
                    is ProofResult.Success -> {
                        val verificationRequest = VerificationRequest(
                            proof = Gson().fromJson(Gson().toJson(result.proof), com.zero.id.network.Proof::class.java),
                            publicSignals = result.publicSignals
                        )
                        _state.value = ProofGenerationState.Success(verificationRequest)
                    }
                    is ProofResult.Error -> {
                        _state.value = ProofGenerationState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _state.value = ProofGenerationState.Error(
                    e.message ?: "Unknown error occurred during proof generation"
                )
            }
        }
    }

    fun resetState() {
        _state.value = ProofGenerationState.Idle
    }

    fun clearInputs() {
        _birthYear.value = ""
        _minAge.value = ""
        _state.value = ProofGenerationState.Idle
    }
}
