package com.zero.id.app.network

data class ChallengeResponse(
    val requestId: String,
    val verifierName: String,
    val minAge: Int,
    val minSalary: Int,
    val currentYear: Int,
    val callbackUrl: String
)
