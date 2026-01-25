package com.zero.id.app.network

// โครงสร้างสำหรับส่งไป
data class ProofData(
    val pi_a: List<String>,
    val pi_b: List<List<String>>,
    val pi_c: List<String>,
    val protocol: String,
    val curve: String
)

data class VerifyRequest(
    val proof: ProofData,
    val publicSignals: List<String>
)

// โครงสร้างสำหรับรับกลับ
data class VerifyResponse(
    val success: Boolean,
    val message: String? = null
)