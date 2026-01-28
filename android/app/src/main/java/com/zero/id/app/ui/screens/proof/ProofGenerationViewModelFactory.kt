package com.zero.id.app.ui.screens.proof

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ProofGenerationViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProofGenerationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProofGenerationViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
