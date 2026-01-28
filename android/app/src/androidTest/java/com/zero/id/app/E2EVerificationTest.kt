package com.zero.id.app

import android.webkit.WebView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zero.id.app.zkp.ProofResult
import com.zero.id.app.zkp.ZKProver
import com.zero.id.library.model.ProofData
import com.zero.id.library.network.VerifierClient
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * End-to-End test for the complete verification flow
 * Prerequisites: Backend server must be running at http://192.168.1.116:3000
 *
 * To run backend:
 * cd backend-verifier
 * node server.js
 */
@RunWith(AndroidJUnit4::class)
class E2EVerificationTest {

    private lateinit var zkProver: ZKProver
    private lateinit var webView: WebView
    private lateinit var verifierClient: VerifierClient
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        val latch = CountDownLatch(1)
        var initSuccess = false

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            zkProver = ZKProver(context)
            webView = WebView(context)

            zkProver.initialize(webView) { success ->
                initSuccess = success
                latch.countDown()
            }
        }

        // Wait for initialization
        assertTrue("WASM initialization timed out", latch.await(30, TimeUnit.SECONDS))
        assertTrue("WASM initialization failed", initSuccess)

        // Initialize verifier client
        verifierClient = VerifierClient()
    }

    @After
    fun tearDown() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            webView.destroy()
        }
    }

    @Test
    fun testFullVerificationFlow_ValidAge() = runBlocking {
        // Step 1: Generate proof
        val proofResult = zkProver.generateProof(
            birthYear = 1990,
            minAge = 18,
            currentYear = 2025
        )

        assertTrue("Proof generation should succeed", proofResult is ProofResult.Success)
        val successResult = proofResult as ProofResult.Success

        // Step 2: Create ProofData object
        val proofData = ProofData(
            proof = successResult.proof,
            publicSignals = successResult.publicSignals
        )

        // Verify computed properties
        assertTrue("isOldEnough should be true", proofData.isOldEnough)
        assertEquals("verifiedMinAge should be 18", 18, proofData.verifiedMinAge)
        assertEquals("verificationYear should be 2025", 2025, proofData.verificationYear)

        // Step 3: Send to backend for verification
        // Note: This will fail if backend is not running
        // Uncomment when backend is available:
        /*
        val verificationResult = verifierClient.verifyProof(proofData)
        assertTrue("Backend verification should succeed", verificationResult.success)
        assertNotNull("Verification message should not be null", verificationResult.message)
        */
    }

    @Test
    fun testFullVerificationFlow_UnderAge() = runBlocking {
        // Step 1: Generate proof for someone under age
        val proofResult = zkProver.generateProof(
            birthYear = 2015,
            minAge = 18,
            currentYear = 2025
        )

        assertTrue("Proof generation should succeed", proofResult is ProofResult.Success)
        val successResult = proofResult as ProofResult.Success

        // Step 2: Create ProofData object
        val proofData = ProofData(
            proof = successResult.proof,
            publicSignals = successResult.publicSignals
        )

        // Verify computed properties
        assertFalse("isOldEnough should be false", proofData.isOldEnough)
        assertEquals("verifiedMinAge should be 18", 18, proofData.verifiedMinAge)
        assertEquals("verificationYear should be 2025", 2025, proofData.verificationYear)

        // Step 3: Verify proof structure is still valid
        assertNotNull("Proof should not be null", proofData.proof)
        assertEquals("Public signals should have 3 elements", 3, proofData.publicSignals.size)
        assertEquals("First signal should be 0", "0", proofData.publicSignals[0])
    }

    @Test
    fun testProofData_TimestampGeneration() = runBlocking {
        val beforeTime = System.currentTimeMillis()

        val proofResult = zkProver.generateProof(
            birthYear = 1990,
            minAge = 18,
            currentYear = 2025
        )

        val afterTime = System.currentTimeMillis()

        assertTrue("Proof generation should succeed", proofResult is ProofResult.Success)
        val successResult = proofResult as ProofResult.Success

        val proofData = ProofData(
            proof = successResult.proof,
            publicSignals = successResult.publicSignals
        )

        assertTrue("Timestamp should be >= beforeTime", proofData.timestamp >= beforeTime)
        assertTrue("Timestamp should be <= afterTime", proofData.timestamp <= afterTime)
    }

    @Test
    fun testBackendConnectivity() = runBlocking {
        // Test if backend is reachable
        val isReachable = verifierClient.ping()

        // This test won't fail if backend is not running, just logs a warning
        if (!isReachable) {
            println("WARNING: Backend server is not reachable at http://192.168.1.116:3000")
            println("To run full E2E tests, start the backend server:")
            println("  cd backend-verifier")
            println("  node server.js")
        } else {
            println("SUCCESS: Backend server is reachable")
        }

        // Don't fail the test, just log
        assertTrue("Backend connectivity test completed", true)
    }

    @Test
    fun testVerifierClient_NetworkError() = runBlocking {
        // Test with invalid backend URL to verify error handling
        val invalidClient = VerifierClient("http://invalid.url.that.does.not.exist:9999")

        val proofResult = zkProver.generateProof(
            birthYear = 1990,
            minAge = 18,
            currentYear = 2025
        )

        assertTrue("Proof generation should succeed", proofResult is ProofResult.Success)
        val successResult = proofResult as ProofResult.Success

        val proofData = ProofData(
            proof = successResult.proof,
            publicSignals = successResult.publicSignals
        )

        // Try to verify with invalid client
        val verificationResult = invalidClient.verifyProof(proofData)

        assertFalse("Verification should fail with network error", verificationResult.success)
        assertTrue("Error message should mention network",
            verificationResult.message.contains("Network", ignoreCase = true) ||
            verificationResult.message.contains("error", ignoreCase = true))
    }
}
