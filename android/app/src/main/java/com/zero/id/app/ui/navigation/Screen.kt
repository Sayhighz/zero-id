package com.zero.id.app.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ProofGeneration : Screen("proof_generation")
    object GenerateAgeProof : Screen("generate_age_proof") // Added this line
    object QRScanner : Screen("qr_scanner")

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

    object Consent : Screen("consent/{requestJson}") {
        fun createRoute(requestJson: String): String {
            val encodedJson = URLEncoder.encode(requestJson, StandardCharsets.UTF_8.toString())
            return "consent/$encodedJson"
        }
    }
}
