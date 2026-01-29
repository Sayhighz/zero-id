package com.zero.id.app.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
            details?.let { queryParams.add("details=${URLEncoder.encode(it, StandardCharsets.UTF_8.toString())}") }
            minAge?.let { queryParams.add("minAge=${URLEncoder.encode(it, StandardCharsets.UTF_8.toString())}") }
            birthYear?.let { queryParams.add("birthYear=${URLEncoder.encode(it, StandardCharsets.UTF_8.toString())}") }
            userProfileJson?.let { queryParams.add("userProfileJson=${URLEncoder.encode(it, StandardCharsets.UTF_8.toString())}") }

            return if (queryParams.isNotEmpty()) {
                "$baseRoute?${queryParams.joinToString("&")}"
            } else {
                baseRoute
            }
        }
    }

    /**
     * Consent screen for data sharing requests
     */
    object Consent : Screen("consent/{requestJson}") {
        fun createRoute(requestJson: String): String {
            val encodedJson = URLEncoder.encode(requestJson, StandardCharsets.UTF_8.toString())
            return "consent/$encodedJson"
        }
    }

    /**
     * QR scanner screen - scans QR codes for verification
     */
    object QRScanner : Screen("qr_scanner")
}
