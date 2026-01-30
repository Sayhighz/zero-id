package com.zero.id.app.network

import com.google.gson.annotations.SerializedName

data class VerificationRequest(
    @SerializedName("proof") val proof: Proof,
    @SerializedName("publicSignals") val publicSignals: List<String>
)
