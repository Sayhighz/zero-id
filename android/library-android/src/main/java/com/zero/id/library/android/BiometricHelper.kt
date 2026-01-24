package com.zero.id.library.android

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Helper class for biometric authentication (fingerprint, face recognition)
 * Uses AndroidX Biometric library for consistent experience across devices
 */
class BiometricHelper(private val activity: FragmentActivity) {

    private val biometricManager = BiometricManager.from(activity)

    /**
     * Check if biometric authentication is available on this device
     * @return true if biometric hardware is present and enrolled, false otherwise
     */
    fun isBiometricAvailable(): Boolean {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                // Device doesn't have biometric hardware
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                // Hardware is unavailable (e.g., being used by another app)
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // User hasn't enrolled any biometrics
                false
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                // Security update required
                false
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                // Biometric authentication not supported
                false
            }
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                // Status unknown
                false
            }
            else -> false
        }
    }

    /**
     * Get a detailed message about biometric availability status
     */
    fun getBiometricStatusMessage(): String {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                "Biometric authentication is available"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                "This device doesn't have biometric hardware"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                "Biometric hardware is currently unavailable"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                "No biometrics enrolled. Please set up fingerprint or face recognition in Settings"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                "A security update is required for biometric authentication"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                "Biometric authentication is not supported"
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                "Biometric status is unknown"
            else ->
                "Biometric authentication is not available"
        }
    }

    /**
     * Authenticate the user with biometrics (fingerprint or face)
     * @param title Title to display in the biometric prompt
     * @param subtitle Optional subtitle for the prompt
     * @param description Optional description for the prompt
     * @param negativeButtonText Text for the cancel button
     * @param onSuccess Callback when authentication succeeds
     * @param onError Callback when authentication fails with error message
     */
    fun authenticate(
        title: String,
        subtitle: String = "",
        description: String = "",
        negativeButtonText: String = "Cancel",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isBiometricAvailable()) {
            onError(getBiometricStatusMessage())
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Don't call onError here - this is called for each failed attempt
                    // Let the user try again
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply {
                if (subtitle.isNotEmpty()) setSubtitle(subtitle)
                if (description.isNotEmpty()) setDescription(description)
            }
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Authenticate with a cryptographic object
     * Useful for operations that require cryptographic confirmation
     */
    fun authenticateWithCrypto(
        title: String,
        cryptoObject: BiometricPrompt.CryptoObject,
        subtitle: String = "",
        description: String = "",
        negativeButtonText: String = "Cancel",
        onSuccess: (BiometricPrompt.CryptoObject?) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isBiometricAvailable()) {
            onError(getBiometricStatusMessage())
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess(result.cryptoObject)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Let the user try again
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply {
                if (subtitle.isNotEmpty()) setSubtitle(subtitle)
                if (description.isNotEmpty()) setDescription(description)
            }
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }
}
