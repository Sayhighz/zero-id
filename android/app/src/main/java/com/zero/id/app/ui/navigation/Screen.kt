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
    object Result : Screen("result/{isSuccess}/{message}") {
        fun createRoute(
            isSuccess: Boolean,
            message: String,
            details: String?,
            minAge: String?,
            birthYear: String?
        ): String {
            var route = "result/$isSuccess/$message"
            val queryParams = mutableListOf<String>()
            details?.let { queryParams.add("details=$it") }
            minAge?.let { queryParams.add("minAge=$it") }
            birthYear?.let { queryParams.add("birthYear=$it") }
            if (queryParams.isNotEmpty()) {
                route += "?" + queryParams.joinToString("&")
            }
            return route
        }
    }

    /**
     * QR scanner screen - scans QR codes for verification
     */
    object QRScanner : Screen("qr_scanner")

    /**
     * Image scanner screen - scans QR codes from an image
     */
    object ImageScanner : Screen("image_scanner")

    /**
     * QR Generator screen - generates QR codes for agency requests
     */
    object QRGenerator : Screen("qr_generator")
}
