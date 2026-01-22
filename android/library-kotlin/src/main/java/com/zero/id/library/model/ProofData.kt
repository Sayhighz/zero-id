package com.zero.id.library.model

/**
 * Request parameters for proof generation
 * @param birthYear User's birth year
 * @param minAge Minimum age requirement to prove
 * @param currentYear Current year for age calculation
 */
data class ProofRequest(
    val birthYear: Int,
    val minAge: Int,
    val currentYear: Int
)

/**
 * Zero-knowledge proof data with public signals
 * @param proof The zero-knowledge proof object (pi_a, pi_b, pi_c, protocol)
 * @param publicSignals Public signals from the circuit [isOldEnough, minAge, currentYear]
 * @param timestamp Unix timestamp of when proof was generated
 */
data class ProofData(
    val proof: Map<String, Any>,
    val publicSignals: List<String>,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Computed property: true if age requirement is met
     * Based on first public signal (0 or 1)
     */
    val isOldEnough: Boolean
        get() = publicSignals.getOrNull(0) == "1"

    /**
     * Computed property: the minimum age that was verified
     * Based on second public signal
     */
    val verifiedMinAge: Int
        get() = publicSignals.getOrNull(1)?.toIntOrNull() ?: 0

    /**
     * Computed property: the year used for verification
     * Based on third public signal
     */
    val verificationYear: Int
        get() = publicSignals.getOrNull(2)?.toIntOrNull() ?: 0
}

/**
 * Result from backend verification
 * @param success Whether the proof was successfully verified
 * @param message Human-readable message about verification result
 */
data class VerificationResult(
    val success: Boolean,
    val message: String
)
