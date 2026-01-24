package com.zero.id.network

data class VerifyRequest(
    val proof: Any,                // รับ Object pi_a, pi_b, pi_c จาก WebView
    val publicSignals: List<String> // รับ ["1", "20", "2025"]
)

data class VerifyResponse(
    val success: Boolean,
    val message: String,
    val details: VerificationDetails?
)

data class VerificationDetails(
    val isOldEnough: Boolean,
    val minAge: String,
    val currentYear: String
)