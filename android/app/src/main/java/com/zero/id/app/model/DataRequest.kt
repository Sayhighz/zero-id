package com.zero.id.app.model

/**
 * Represents a request for user data from a third party (e.g., a bank).
 * This model is parsed from the QR code.
 */
data class DataRequest(
    val type: String,
    val requester: String,
    val purpose: String,
    val claims: List<String>,
    val callbackUrl: String
)
