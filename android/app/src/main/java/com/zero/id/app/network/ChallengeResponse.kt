package com.zero.id.app.network

import com.google.gson.annotations.SerializedName

/**
 * ChallengeResponse represents the requirements from a verifier.
 * Using String for numeric fields to handle flexible data types from URL/JSON.
 */
data class ChallengeResponse(
    @SerializedName("verifierName") val verifierName: String? = null,
    @SerializedName("minAge") val minAge: String? = null,
    @SerializedName("minSalary") val minSalary: String? = null,
    @SerializedName("currentYear") val currentYear: String? = null,
    @SerializedName("callbackUrl") val callbackUrl: String? = null
)
