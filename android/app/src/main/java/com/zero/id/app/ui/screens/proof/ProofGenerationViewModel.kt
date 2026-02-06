package com.zero.id.app.ui.screens.proof

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.zero.id.app.ServiceLocator
import com.zero.id.app.network.Proof
import com.zero.id.app.network.RetrofitClient
import com.zero.id.app.network.VerificationRequest
import com.zero.id.app.zkp.ProofResult
import com.zero.id.app.zkp.ZKProver
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

    private val _state = MutableStateFlow<ProofGenerationState>(ProofGenerationState.Idle)
    val state: StateFlow<ProofGenerationState> = _state.asStateFlow()

    private val _birthYear = MutableStateFlow("")
    val birthYear: StateFlow<String> = _birthYear.asStateFlow()

    private val _minAge = MutableStateFlow("")
    val minAge: StateFlow<String> = _minAge.asStateFlow()

    private val currentYear = 2026 // Hardcoded for testing with the provided QR code
    private val profileStorage = ServiceLocator.provideProfileStorage(application)

    init {
        // Load initial data from profile storage
        loadUserProfile()
    }

    /**
     * Load user profile and set initial birth year
     */
    private fun loadUserProfile() {
        val profile = profileStorage.getProfile()
        if (profile.birthYear > 0) {
            _birthYear.value = profile.birthYear.toString()
        }
    }

    /**
     * Update birth year input
     */
    fun updateBirthYear(year: String) {
        if (year.isEmpty() || (year.all { it.isDigit() } && year.length <= 4)) {
            _birthYear.value = year
        }
    }

    /**
     * Update minimum age input
     */
    fun updateMinAge(age: String) {
        if (age.isEmpty() || (age.all { it.isDigit() } && age.length <= 3)) {
            _minAge.value = age
        }
    }

    /**
     * Validate inputs before generating proof
     */
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

    /**
     * Generate zero-knowledge proof with current inputs
     */
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
                            proof = Gson().fromJson(Gson().toJson(result.proof), Proof::class.java),
                            publicSignals = result.publicSignals
                        )
                        verifyProof(verificationRequest)
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

    private fun verifyProof(request: VerificationRequest) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.verify(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _state.value = ProofGenerationState.Success(request)
                } else {
                    _state.value = ProofGenerationState.Error(response.body()?.message ?: "Verification failed on server")
                }
            } catch (e: Exception) {
                _state.value = ProofGenerationState.Error(e.message ?: "Unknown error verifying proof")
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
