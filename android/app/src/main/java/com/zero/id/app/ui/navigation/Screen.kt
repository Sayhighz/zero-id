package com.zero.id.app.ui.navigation

import android.net.Uri

/**
 * Sealed class representing all screens in the ZeroID app
 */
sealed class Screen(val route: String) {
    /**
     * Home screen - entry point of the app
     */
    object Home : Screen("home")

    /**
     * Proof generation screen - input form for age verification
     */
    object ProofGeneration : Screen("proof_generation")

    /**
     * Display the generated proof and public signals
     */
    object ProofDisplay : Screen("proof_display")

    /**
     * Face scan screen - authenticates the user
     */
    object FaceScan : Screen("face_scan")

    /**
     * QR scanner screen - scans QR codes for verification
     */
    object QRScanner : Screen("qr_scanner")

    /**
     * Verification result screen - displays the result of a verification
     */
    object VerificationResult : Screen("verification_result/{isSuccess}/{message}") {
        fun createRoute(isSuccess: Boolean, message: String): String {
            val encodedMessage = Uri.encode(message)
            return "verification_result/$isSuccess/$encodedMessage"
        }
    }

    companion object {
        /**
         * Get all screen routes for navigation graph setup
         */
        fun getAllRoutes(): List<String> {
            return listOf(
                Home.route,
                ProofGeneration.route,
                ProofDisplay.route,
                FaceScan.route,
                QRScanner.route,
                VerificationResult.route
            )
        }
    }
}
