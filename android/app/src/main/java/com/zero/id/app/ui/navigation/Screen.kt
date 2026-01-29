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
    object Result : Screen("result/{isSuccess}/{message}?details={details}&minAge={minAge}&birthYear={birthYear}&userProfileJson={userProfileJson}") {
        fun createRoute(
            isSuccess: Boolean,
            message: String = "",
            details: String? = null,
            minAge: String? = null,
            birthYear: String? = null,
            userProfileJson: String? = null
        ): String {
            val baseRoute = "result/$isSuccess/$message"
            val queryParams = mutableListOf<String>()
            details?.let { queryParams.add("details=$it") }
            minAge?.let { queryParams.add("minAge=$it") }
            birthYear?.let { queryParams.add("birthYear=$it") }
            userProfileJson?.let { queryParams.add("userProfileJson=$it") }

            return if (queryParams.isNotEmpty()) {
                "$baseRoute?${queryParams.joinToString("&")}"
            } else {
                baseRoute
            }
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
