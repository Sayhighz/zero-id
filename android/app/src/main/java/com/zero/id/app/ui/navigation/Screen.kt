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
    object ProofGeneration : Screen("proof_generation")

    /**
     * Result screen - displays proof generation result
     * @param isSuccess Whether the operation was successful
     */
    object Result : Screen("result/{isSuccess}/{message}?details={details}") {
        fun createRoute(isSuccess: Boolean, message: String = "", details: String? = null): String {
            val baseRoute = "result/$isSuccess/$message"
            return if (details != null) "$baseRoute?details=$details" else baseRoute
        }
    }

    /**
     * QR scanner screen - scans QR codes for verification
     */
    object QRScanner : Screen("qr_scanner")

    companion object {
        /**
         * Get all screen routes for navigation graph setup
         */
        fun getAllRoutes(): List<String> {
            return listOf(
                Home.route,
                ProofGeneration.route,
                Result.route,
                QRScanner.route
            )
        }
    }
}
