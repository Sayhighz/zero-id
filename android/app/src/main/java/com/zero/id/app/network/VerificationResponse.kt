package com.zero.id.app.network

import com.google.gson.annotations.SerializedName

data class VerificationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("isQualified") val isQualified: Boolean? = null,
    @SerializedName("message") val message: String,
    @SerializedName("details") val details: VerificationDetails?
)

data class VerificationDetails(
    @SerializedName("isOldEnough") val isOldEnough: Boolean,
    @SerializedName("minAge") val minAge: String,
    @SerializedName("currentYear") val currentYear: String
)
