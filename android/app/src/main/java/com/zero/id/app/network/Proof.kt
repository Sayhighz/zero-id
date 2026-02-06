package com.zero.id.app.network

import com.google.gson.annotations.SerializedName

data class Proof(
    @SerializedName("pi_a") val piA: List<String>,
    @SerializedName("pi_b") val piB: List<List<String>>,
    @SerializedName("pi_c") val piC: List<String>,
    @SerializedName("protocol") val protocol: String,
    @SerializedName("curve") val curve: String
)
