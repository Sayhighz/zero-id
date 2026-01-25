package com.zero.id.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zero.id.app.security.KeyStoreManager
import com.zero.id.app.zkp.SecureProofGenerator
import com.zero.id.library.model.ProofData
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for KeyStoreManager and SecureProofGenerator
 * Tests hardware-backed key storage and cryptographic operations
 */
@RunWith(AndroidJUnit4::class)
class KeyStoreTest {

    private lateinit var keyStoreManager: KeyStoreManager
    private lateinit var secureProofGenerator: SecureProofGenerator
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        keyStoreManager = KeyStoreManager(context)
        secureProofGenerator = SecureProofGenerator(context)

        // Clean up any existing keys
        if (keyStoreManager.hasKey()) {
            keyStoreManager.deleteKey()
        }
    }

    @After
    fun tearDown() {
        // Clean up after tests
        if (keyStoreManager.hasKey()) {
            keyStoreManager.deleteKey()
        }
    }

    @Test
    fun testKeyGeneration_Succeeds() {
        // Initially no key should exist
        assertFalse("Key should not exist initially", keyStoreManager.hasKey())

        // Generate key pair
        val keyPair = keyStoreManager.generateKeyPair(useStrongBox = false)

        // Verify key pair was created
        assertNotNull("KeyPair should not be null", keyPair)
        assertNotNull("Private key should not be null", keyPair.private)
        assertNotNull("Public key should not be null", keyPair.public)

        // Verify key exists in KeyStore
        assertTrue("Key should exist after generation", keyStoreManager.hasKey())
    }

    @Test
    fun testSignAndVerify_Succeeds() {
        // Generate key pair
        keyStoreManager.generateKeyPair(useStrongBox = false)

        // Test data
        val testData = "Test message for signing".toByteArray()

        // Sign data
        val signature = keyStoreManager.signData(testData)
        assertNotNull("Signature should not be null", signature)
        assertTrue("Signature should not be empty", signature.isNotEmpty())

        // Verify signature
        val isValid = keyStoreManager.verifySignature(testData, signature)
        assertTrue("Signature should be valid", isValid)
    }

    @Test
    fun testInvalidSignature_Detected() {
        // Generate key pair
        keyStoreManager.generateKeyPair(useStrongBox = false)

        val testData = "Test message".toByteArray()
        val signature = keyStoreManager.signData(testData)

        // Tamper with the data
        val tamperedData = "Tampered message".toByteArray()

        // Verification should fail
        val isValid = keyStoreManager.verifySignature(tamperedData, signature)
        assertFalse("Tampered data should fail verification", isValid)
    }

    @Test
    fun testStrongBoxAvailability_Check() {
        val isStrongBoxSupported = keyStoreManager.isStrongBoxSupported()

        // This test just checks the method works - result depends on device
        // On emulator: should be false
        // On Samsung Knox device: should be true
        println("StrongBox supported: $isStrongBoxSupported")

        // Get security level description
        keyStoreManager.generateKeyPair(useStrongBox = isStrongBoxSupported)
        val securityDesc = keyStoreManager.getSecurityLevelDescription()
        assertNotNull("Security description should not be null", securityDesc)
        assertTrue("Security description should not be empty", securityDesc.isNotEmpty())
        println("Security level: $securityDesc")
    }

    @Test
    fun testKeyPersistence_AcrossInstances() {
        // Generate key with first instance
        val keyPair1 = keyStoreManager.generateKeyPair(useStrongBox = false)
        val publicKey1 = keyPair1.public

        // Create new instance (simulating app restart)
        val keyStoreManager2 = KeyStoreManager(context)

        // Key should still exist
        assertTrue("Key should persist", keyStoreManager2.hasKey())

        // Public key should be the same
        val publicKey2 = keyStoreManager2.getPublicKey()
        assertNotNull("Public key should not be null", publicKey2)
        assertArrayEquals("Public keys should match", publicKey1.encoded, publicKey2!!.encoded)
    }

    @Test
    fun testKeyDeletion() {
        // Generate key
        keyStoreManager.generateKeyPair(useStrongBox = false)
        assertTrue("Key should exist", keyStoreManager.hasKey())

        // Delete key
        keyStoreManager.deleteKey()
        assertFalse("Key should not exist after deletion", keyStoreManager.hasKey())
    }

    @Test
    fun testKeyInfo_ReturnsDetails() {
        // Generate key
        keyStoreManager.generateKeyPair(useStrongBox = false)

        // Get key info
        val keyInfo = keyStoreManager.getKeyInfo()
        assertNotNull("KeyInfo should not be null", keyInfo)

        // On real device, key should be inside secure hardware
        // On emulator, this might be false
        println("Inside secure hardware: ${keyInfo?.isInsideSecureHardware}")
        println("User authentication required: ${keyInfo?.isUserAuthenticationRequired}")
    }

    @Test
    fun testSecureProofGenerator_Initialize() {
        // Initially no key
        assertFalse("Key should not exist", keyStoreManager.hasKey())

        // Initialize secure storage
        secureProofGenerator.initializeSecureStorage(useStrongBox = false)

        // Key should now exist
        assertTrue("Key should exist after initialization", keyStoreManager.hasKey())
    }

    @Test
    fun testSecureProofGenerator_SignAndVerify() {
        // Initialize
        secureProofGenerator.initializeSecureStorage(useStrongBox = false)

        // Create sample proof data
        val proofData = ProofData(
            proof = mapOf(
                "pi_a" to listOf("1", "2", "1"),
                "protocol" to "groth16"
            ),
            publicSignals = listOf("1", "18", "2025")
        )

        // Sign proof
        val signedProof = secureProofGenerator.signProof(proofData)

        // Verify signature
        assertNotNull("Signed proof should not be null", signedProof)
        assertNotNull("Signature should not be null", signedProof.signature)
        assertNotNull("Public key should not be null", signedProof.publicKey)
        assertTrue("Signature should not be empty", signedProof.signature.isNotEmpty())
        assertTrue("Public key should not be empty", signedProof.publicKey.isNotEmpty())

        // Verify with internal method
        val isValid = secureProofGenerator.verifySignedProof(signedProof)
        assertTrue("Signature should be valid", isValid)

        // Verify with external method (using embedded public key)
        val isValidExternal = secureProofGenerator.verifySignedProofWithPublicKey(signedProof)
        assertTrue("External signature verification should succeed", isValidExternal)
    }

    @Test
    fun testSecureProofGenerator_TamperedProof_FailsVerification() {
        // Initialize
        secureProofGenerator.initializeSecureStorage(useStrongBox = false)

        // Create and sign proof
        val proofData = ProofData(
            proof = mapOf("pi_a" to listOf("1", "2", "1")),
            publicSignals = listOf("1", "18", "2025")
        )
        val signedProof = secureProofGenerator.signProof(proofData)

        // Tamper with the proof data
        val tamperedProof = signedProof.copy(
            proof = proofData.copy(
                publicSignals = listOf("0", "18", "2025") // Changed from "1" to "0"
            )
        )

        // Verification should fail
        val isValid = secureProofGenerator.verifySignedProof(tamperedProof)
        assertFalse("Tampered proof should fail verification", isValid)
    }

    @Test
    fun testSecureProofGenerator_ProofId_Unique() {
        // Initialize
        secureProofGenerator.initializeSecureStorage(useStrongBox = false)

        // Create two proofs
        val proofData1 = ProofData(
            proof = mapOf("test" to "data1"),
            publicSignals = listOf("1", "18", "2025")
        )
        val proofData2 = ProofData(
            proof = mapOf("test" to "data2"),
            publicSignals = listOf("1", "21", "2025")
        )

        val signedProof1 = secureProofGenerator.signProof(proofData1)
        Thread.sleep(10) // Ensure different timestamp
        val signedProof2 = secureProofGenerator.signProof(proofData2)

        // Proof IDs should be different
        val id1 = signedProof1.getProofId()
        val id2 = signedProof2.getProofId()

        assertNotNull("Proof ID 1 should not be null", id1)
        assertNotNull("Proof ID 2 should not be null", id2)
        assertNotEquals("Proof IDs should be unique", id1, id2)
    }

    @Test
    fun testKeyGeneration_WithStrongBoxFallback() {
        // Try to generate with StrongBox, should fallback to TEE if not available
        val keyPair = keyStoreManager.generateKeyPair(useStrongBox = true)

        assertNotNull("KeyPair should not be null", keyPair)
        assertTrue("Key should exist", keyStoreManager.hasKey())

        val keyInfo = keyStoreManager.getKeyInfo()
        println("Key generation with StrongBox request:")
        println("  Inside secure hardware: ${keyInfo?.isInsideSecureHardware}")
        println("  Security description: ${keyStoreManager.getSecurityLevelDescription()}")
    }
}
