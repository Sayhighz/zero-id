package com.zero.id.app.ui.navigation

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
    object ProofGeneration : Screen("proof_generation") {
        fun createRoute(requestJson: String): String {
            return "proof_generation?requestJson=$requestJson"
        }
    }

    /**
     * Result screen - displays proof generation result
     */
    object Result : Screen("result/{isSuccess}/{message}?minAge={minAge}&birthYear={birthYear}") {
        fun createRoute(isSuccess: Boolean, message: String, minAge: String?, birthYear: String?): String {
            var route = "result/$isSuccess/$message"
            if (minAge != null || birthYear != null) {
                route += "?"
                if (minAge != null) route += "minAge=$minAge"
                if (birthYear != null) route += (if (minAge != null) "&" else "") + "birthYear=$birthYear"
            }
            return route
        }
    }

    /**
     * QR scanner screen - scans QR codes for verification
     */
    object QRScanner : Screen("qr_scanner")

    /**
     * QR Generator screen - generates QR codes for agency requests
     */
    object QRGenerator : Screen("qr_generator")
}
