package com.zero.id.network

import com.google.gson.annotations.SerializedName

data class VerificationRequest(
    val proof: Proof,
    val publicSignals: List<String>
)

data class Proof(
    @SerializedName("pi_a")
    val piA: List<String>,
    @SerializedName("pi_b")
    val piB: List<List<String>>,
    @SerializedName("pi_c")
    val piC: List<String>,
    val protocol: String,
    val curve: String
)
