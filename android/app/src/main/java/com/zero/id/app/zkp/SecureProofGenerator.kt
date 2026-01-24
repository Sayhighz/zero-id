package com.zero.id.app.zkp

import android.content.Context
import android.security.keystore.KeyInfo
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.zero.id.app.security.KeyStoreException
import com.zero.id.app.security.KeyStoreManager
import com.zero.id.library.model.ProofData

/**
 * Combines Zero-Knowledge proofs with Android KeyStore signing
 * Provides cryptographically signed proofs that cannot be forged
 */
class SecureProofGenerator(context: Context) {

    private val keyStoreManager = KeyStoreManager(context)
    private val gson = Gson()

    companion object {
        private const val TAG = "SecureProofGenerator"
    }

    /**
     * Initialize secure storage and generate key pair if not exists
     * @param useStrongBox Attempt to use StrongBox if available
     */
    fun initializeSecureStorage(useStrongBox: Boolean = true) {
        if (!keyStoreManager.hasKey()) {
            Log.d(TAG, "No key found, generating new key pair")
            keyStoreManager.generateKeyPair(useStrongBox)
            Log.d(TAG, "Secure storage initialized: ${keyStoreManager.getSecurityLevelDescription()}")
        } else {
            Log.d(TAG, "Key already exists: ${keyStoreManager.getSecurityLevelDescription()}")
        }
    }

    /**
     * Sign a zero-knowledge proof with the device's private key
     * @param proofData The proof to sign
     * @return SignedProof containing the proof, signature, and public key
     */
    fun signProof(proofData: ProofData): SignedProof {
        if (!keyStoreManager.hasKey()) {
            throw KeyStoreException("No key found. Call initializeSecureStorage() first.")
        }

        try {
            // Serialize proof to JSON for signing
            val proofJson = gson.toJson(proofData)
            val proofBytes = proofJson.toByteArray(Charsets.UTF_8)

            // Sign with private key
            val signatureBytes = keyStoreManager.signData(proofBytes)

            // Get public key
            val publicKey = keyStoreManager.getPublicKey()
                ?: throw KeyStoreException("Public key not found")
            val publicKeyBytes = publicKey.encoded

            // Convert to Base64 for easy transmission
            val signatureBase64 = Base64.encodeToString(signatureBytes, Base64.NO_WRAP)
            val publicKeyBase64 = Base64.encodeToString(publicKeyBytes, Base64.NO_WRAP)

            Log.d(TAG, "Proof signed successfully. Signature length: ${signatureBytes.size} bytes")

            return SignedProof(
                proof = proofData,
                signature = signatureBase64,
                publicKey = publicKeyBase64,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign proof", e)
            throw KeyStoreException("Failed to sign proof: ${e.message}", e)
        }
    }

    /**
     * Verify a signed proof's signature
     * @param signedProof The signed proof to verify
     * @return true if signature is valid, false otherwise
     */
    fun verifySignedProof(signedProof: SignedProof): Boolean {
        return try {
            // Serialize proof to JSON (same as signing)
            val proofJson = gson.toJson(signedProof.proof)
            val proofBytes = proofJson.toByteArray(Charsets.UTF_8)

            // Decode signature from Base64
            val signatureBytes = Base64.decode(signedProof.signature, Base64.NO_WRAP)

            // Verify with our stored key
            val isValid = keyStoreManager.verifySignature(proofBytes, signatureBytes)

            Log.d(TAG, "Signature verification result: $isValid")
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Failed to verify signature", e)
            false
        }
    }

    /**
     * Verify a signed proof using the embedded public key (external verification)
     * This allows anyone to verify the proof without access to the private key
     * @param signedProof The signed proof to verify
     * @return true if signature is valid, false otherwise
     */
    fun verifySignedProofWithPublicKey(signedProof: SignedProof): Boolean {
        return try {
            // Serialize proof to JSON
            val proofJson = gson.toJson(signedProof.proof)
            val proofBytes = proofJson.toByteArray(Charsets.UTF_8)

            // Decode signature and public key from Base64
            val signatureBytes = Base64.decode(signedProof.signature, Base64.NO_WRAP)
            val publicKeyBytes = Base64.decode(signedProof.publicKey, Base64.NO_WRAP)

            // Reconstruct public key
            val keyFactory = java.security.KeyFactory.getInstance("EC")
            val publicKey = keyFactory.generatePublic(
                java.security.spec.X509EncodedKeySpec(publicKeyBytes)
            )

            // Verify signature
            val verifier = java.security.Signature.getInstance("SHA256withECDSA")
            verifier.initVerify(publicKey)
            verifier.update(proofBytes)
            val isValid = verifier.verify(signatureBytes)

            Log.d(TAG, "External signature verification result: $isValid")
            isValid
        } catch (e: Exception) {
            Log.e(TAG, "Failed to verify signature with public key", e)
            false
        }
    }

    /**
     * Get information about the KeyStore key
     */
    fun getKeyInfo(): KeyInfo? {
        return keyStoreManager.getKeyInfo()
    }

    /**
     * Get a human-readable description of the security level
     */
    fun getSecurityLevelDescription(): String {
        return keyStoreManager.getSecurityLevelDescription()
    }

    /**
     * Check if StrongBox is supported on this device
     */
    fun isStrongBoxSupported(): Boolean {
        return keyStoreManager.isStrongBoxSupported()
    }

    /**
     * Delete the key from secure storage
     * WARNING: This will invalidate all signed proofs!
     */
    fun deleteKey() {
        keyStoreManager.deleteKey()
        Log.d(TAG, "Key deleted from secure storage")
    }
}

/**
 * A zero-knowledge proof that has been cryptographically signed
 * The signature proves the proof was generated on this specific device
 * @param proof The zero-knowledge proof data
 * @param signature Base64-encoded ECDSA signature
 * @param publicKey Base64-encoded EC public key
 * @param timestamp Unix timestamp when proof was signed
 */
data class SignedProof(
    val proof: ProofData,
    val signature: String,
    val publicKey: String,
    val timestamp: Long
) {
    /**
     * Get a unique identifier for this signed proof
     */
    fun getProofId(): String {
        val combined = "$signature-$timestamp"
        return Base64.encodeToString(
            combined.toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        ).take(16)
    }
}
