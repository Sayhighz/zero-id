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
    object Result : Screen("result/{isSuccess}/{message}") {
        fun createRoute(isSuccess: Boolean, message: String = ""): String {
            return "result/$isSuccess/$message"
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
                Result.route
            )
        }
    }
}
