package com.zero.id.network

data class VerificationResponse(
    val success: Boolean,
    val message: String,
    val details: Details?
)

data class Details(
    val isOldEnough: Boolean,
    val minAge: String,
    val currentYear: String
)
