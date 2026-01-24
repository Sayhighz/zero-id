package com.zero.id.app.utils

import android.util.Log
import com.zero.id.app.security.KeyStoreException
import com.zero.id.app.zkp.ZKProverException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Centralized error handling utility
 * Converts exceptions to user-friendly messages
 */
object ErrorHandler {

    private const val TAG = "ErrorHandler"

    /**
     * Handle an error and log it
     * @param error The error/exception to handle
     * @param context Additional context about where the error occurred
     */
    fun handleError(error: Throwable, context: String) {
        Log.e(TAG, "Error in $context: ${error.message}", error)

        // Additional error tracking could be added here
        // e.g., Firebase Crashlytics, Sentry, etc.
    }

    /**
     * Get a user-friendly error message for an exception
     * @param error The error/exception
     * @return User-friendly error message
     */
    fun getUserFriendlyMessage(error: Throwable): String {
        return when (error) {
            // ZKProver exceptions
            is ZKProverException.NotInitialized -> {
                "Proof generator not initialized. Please try again."
            }
            is ZKProverException.WasmLoadFailed -> {
                "Failed to load cryptographic components. Please restart the app."
            }
            is ZKProverException.ProofGenerationTimeout -> {
                "Proof generation took too long. Please try again."
            }
            is ZKProverException.InvalidInput -> {
                error.message ?: "Invalid input provided"
            }

            // KeyStore exceptions
            is KeyStoreException -> {
                "Secure storage error: ${error.message ?: "Unknown error"}"
            }

            // Network exceptions
            is UnknownHostException -> {
                "Network error: Unable to reach server. Check your internet connection."
            }
            is SocketTimeoutException -> {
                "Network timeout: Server is taking too long to respond. Please try again."
            }
            is IOException -> {
                "Network error: ${error.message ?: "Connection failed"}"
            }

            // Generic exceptions
            is IllegalArgumentException -> {
                error.message ?: "Invalid input provided"
            }
            is IllegalStateException -> {
                error.message ?: "Application is in an invalid state"
            }

            // Unknown errors
            else -> {
                "An unexpected error occurred: ${error.message ?: "Unknown error"}"
            }
        }
    }

    /**
     * Get a short error message suitable for toast or snackbar
     * @param error The error/exception
     * @return Brief error message
     */
    fun getShortErrorMessage(error: Throwable): String {
        return when (error) {
            is ZKProverException.ProofGenerationTimeout -> "Proof generation timed out"
            is ZKProverException.InvalidInput -> "Invalid input"
            is ZKProverException.NotInitialized -> "Not initialized"
            is UnknownHostException -> "No internet connection"
            is SocketTimeoutException -> "Connection timeout"
            is IOException -> "Network error"
            is KeyStoreException -> "Security error"
            else -> "Error occurred"
        }
    }

    /**
     * Determine if an error is recoverable (user can retry)
     * @param error The error/exception
     * @return true if error is recoverable, false otherwise
     */
    fun isRecoverable(error: Throwable): Boolean {
        return when (error) {
            is ZKProverException.NotInitialized -> true
            is ZKProverException.ProofGenerationTimeout -> true
            is UnknownHostException -> true
            is SocketTimeoutException -> true
            is IOException -> true
            is ZKProverException.WasmLoadFailed -> false // Requires app restart
            is ZKProverException.InvalidInput -> true // User can fix input
            else -> false
        }
    }
}
