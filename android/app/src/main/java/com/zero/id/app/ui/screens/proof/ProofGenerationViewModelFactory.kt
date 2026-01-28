package com.zero.id.app.ui.screens.proof

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zero.id.app.zkp.ZKProver

class ProofGenerationViewModelFactory(
    private val application: Application,
    private val zkProver: ZKProver
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProofGenerationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProofGenerationViewModel(application, zkProver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
