package com.zeroid

import java.security.KeyStore

/**
 * Handles hardware-backed security integration.
 * Uses Android Keystore with StrongBox (Samsung Knox Vault) 
 * to generate and isolate keys.
 */
class KnoxVaultManager {

    private val KEYSTORE_PROVIDER = "AndroidKeyStore"

    fun generateHardwareKey(alias: String) {
        // TODO: Implement KeyGenParameterSpec with setIsStrongBoxBacked(true)
        // This ensures keys are stored in Knox Vault, not software.
    }

    fun signData(alias: String, data: ByteArray): ByteArray {
        // TODO: Sign data inside the Secure Element
        return ByteArray(0) 
    }
}