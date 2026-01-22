package com.zero.id.app.security

import android.content.Context
import android.content.pm.PackageManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.*
import java.security.spec.ECGenParameterSpec
import javax.crypto.KeyGenerator

/**
 * Manages secure key storage using Android KeyStore
 * Supports StrongBox (Samsung Knox) and TEE (Trusted Execution Environment)
 */
class KeyStoreManager(private val context: Context) {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    companion object {
        private const val TAG = "KeyStoreManager"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "zero_id_key"
        private const val EC_CURVE = "secp256r1"
    }

    /**
     * Check if StrongBox hardware security is supported on this device
     * StrongBox is available on Samsung Knox and newer Pixel devices
     */
    fun isStrongBoxSupported(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
    }

    /**
     * Check if a key already exists in the KeyStore
     */
    fun hasKey(): Boolean {
        return try {
            keyStore.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for key existence", e)
            false
        }
    }

    /**
     * Generate a new EC key pair in the Android KeyStore
     * @param useStrongBox Attempt to use StrongBox if available, fallback to TEE
     * @return The generated KeyPair
     */
    fun generateKeyPair(useStrongBox: Boolean = true): KeyPair {
        // Delete existing key if present
        if (hasKey()) {
            Log.d(TAG, "Deleting existing key before generating new one")
            deleteKey()
        }

        val shouldUseStrongBox = useStrongBox && isStrongBoxSupported()
        Log.d(TAG, "Generating key pair. StrongBox: $shouldUseStrongBox")

        return try {
            generateKeyPairInternal(shouldUseStrongBox)
        } catch (e: Exception) {
            if (shouldUseStrongBox) {
                Log.w(TAG, "StrongBox key generation failed, falling back to TEE", e)
                generateKeyPairInternal(useStrongBox = false)
            } else {
                throw KeyStoreException("Failed to generate key pair", e)
            }
        }
    }

    /**
     * Internal key pair generation with specific StrongBox setting
     */
    private fun generateKeyPairInternal(useStrongBox: Boolean): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            ANDROID_KEYSTORE
        )

        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec(EC_CURVE))
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setUserAuthenticationRequired(false) // No biometric required for now

        // Set StrongBox if requested
        if (useStrongBox) {
            builder.setIsStrongBoxBacked(true)
        }

        keyPairGenerator.initialize(builder.build())
        val keyPair = keyPairGenerator.generateKeyPair()

        Log.d(TAG, "Key pair generated successfully. StrongBox: $useStrongBox")
        return keyPair
    }

    /**
     * Get the private key from the KeyStore
     */
    fun getPrivateKey(): PrivateKey? {
        return try {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry
            entry?.privateKey
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving private key", e)
            null
        }
    }

    /**
     * Get the public key from the KeyStore
     */
    fun getPublicKey(): PublicKey? {
        return try {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry
            entry?.certificate?.publicKey
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving public key", e)
            null
        }
    }

    /**
     * Sign data with the private key
     * @param data Data to sign
     * @return Signature bytes
     */
    fun signData(data: ByteArray): ByteArray {
        val privateKey = getPrivateKey()
            ?: throw KeyStoreException("Private key not found. Generate a key pair first.")

        return try {
            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initSign(privateKey)
            signature.update(data)
            signature.sign()
        } catch (e: Exception) {
            Log.e(TAG, "Error signing data", e)
            throw KeyStoreException("Failed to sign data", e)
        }
    }

    /**
     * Verify a signature with the public key
     * @param data Original data
     * @param signature Signature to verify
     * @return true if signature is valid, false otherwise
     */
    fun verifySignature(data: ByteArray, signature: ByteArray): Boolean {
        val publicKey = getPublicKey()
            ?: throw KeyStoreException("Public key not found. Generate a key pair first.")

        return try {
            val verifier = Signature.getInstance("SHA256withECDSA")
            verifier.initVerify(publicKey)
            verifier.update(data)
            verifier.verify(signature)
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying signature", e)
            false
        }
    }

    /**
     * Delete the key from the KeyStore
     */
    fun deleteKey() {
        try {
            if (hasKey()) {
                keyStore.deleteEntry(KEY_ALIAS)
                Log.d(TAG, "Key deleted successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting key", e)
            throw KeyStoreException("Failed to delete key", e)
        }
    }

    /**
     * Get detailed information about the stored key
     * @return KeyInfo with hardware backing and security level details
     */
    fun getKeyInfo(): KeyInfo? {
        val privateKey = getPrivateKey() ?: return null

        return try {
            val factory = KeyFactory.getInstance(privateKey.algorithm, ANDROID_KEYSTORE)
            factory.getKeySpec(privateKey, KeyInfo::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting key info", e)
            null
        }
    }

    /**
     * Get a human-readable description of the key's security level
     */
    fun getSecurityLevelDescription(): String {
        val keyInfo = getKeyInfo() ?: return "No key found"

        return buildString {
            append("Security Level: ")
            when {
                keyInfo.isInsideSecureHardware && isStrongBoxSupported() -> {
                    append("StrongBox (Highest)")
                }
                keyInfo.isInsideSecureHardware -> {
                    append("TEE (High)")
                }
                else -> {
                    append("Software (Low)")
                }
            }

            append("\nHardware-backed: ${keyInfo.isInsideSecureHardware}")
            append("\nUser authentication required: ${keyInfo.isUserAuthenticationRequired}")
        }
    }
}

/**
 * Custom exception for KeyStore operations
 */
class KeyStoreException(message: String, cause: Throwable? = null) : Exception(message, cause)
