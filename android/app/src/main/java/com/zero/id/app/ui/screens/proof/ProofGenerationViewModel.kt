package com.zero.id.app.ui.screens.proof

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
    private val zkProver: ZKProver? = null
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow<ProofGenerationState>(ProofGenerationState.Idle)
    val state: StateFlow<ProofGenerationState> = _state.asStateFlow()

    private val _birthYear = MutableStateFlow("")
    val birthYear: StateFlow<String> = _birthYear.asStateFlow()

    private val _minAge = MutableStateFlow("")
    val minAge: StateFlow<String> = _minAge.asStateFlow()

    private val currentYear = 2026
    private val profileStorage = ServiceLocator.provideProfileStorage(application)
    private val gson = GsonBuilder().setPrettyPrinting().create()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val profile = profileStorage.getProfile()
        if (profile.birthYear > 0) {
            _birthYear.value = profile.birthYear.toString()
        }
    }

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
            else -> null
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

        _state.value = ProofGenerationState.Loading

        viewModelScope.launch {
            try {
                val result = zkProver.generateProof(
                    birthYear = _birthYear.value.toInt(),
                    minAge = _minAge.value.toInt(),
                    currentYear = currentYear
                )

                when (result) {
                    is ProofResult.Success -> {
                        val verificationRequest = VerificationRequest(
                            proof = Gson().fromJson(Gson().toJson(result.proof), Proof::class.java),
                            publicSignals = result.publicSignals
                        )
                        _state.value = ProofGenerationState.Success(
                            verificationRequest = verificationRequest,
                            proofJson = gson.toJson(result.proof),
                            publicSignalsJson = gson.toJson(result.publicSignals)
                        )
                    }
                    is ProofResult.Error -> {
                        _state.value = ProofGenerationState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _state.value = ProofGenerationState.Error(e.message ?: "Error")
            }
        }
    }

    fun resetState() {
        _state.value = ProofGenerationState.Idle
    }
}
