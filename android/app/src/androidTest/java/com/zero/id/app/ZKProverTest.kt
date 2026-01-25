package com.zero.id.app

import android.webkit.WebView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zero.id.app.zkp.ProofResult
import com.zero.id.app.zkp.ZKProver
import com.zero.id.app.zkp.ZKProverException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instrumented test for ZKProver
 * Tests zero-knowledge proof generation with various inputs
 */
@RunWith(AndroidJUnit4::class)
class ZKProverTest {

    private lateinit var zkProver: ZKProver
    private lateinit var webView: WebView
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

        // Wait for initialization (max 30 seconds)
        assertTrue("WASM initialization timed out", latch.await(30, TimeUnit.SECONDS))
        assertTrue("WASM initialization failed", initSuccess)
    }

    @After
    fun tearDown() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            webView.destroy()
        }
    }

    @Test
    fun testValidAge_ShouldSucceed() = runBlocking {
        // Test: 1990-born person is >= 18 in 2025
        val result = zkProver.generateProof(
            birthYear = 1990,
            minAge = 18,
            currentYear = 2025
        )

        assertTrue("Proof generation should succeed", result is ProofResult.Success)
        val successResult = result as ProofResult.Success

        // Check public signals
        assertEquals("First signal should be 1 (old enough)", "1", successResult.publicSignals.getOrNull(0))
        assertEquals("Second signal should be minAge", "18", successResult.publicSignals.getOrNull(1))
        assertEquals("Third signal should be currentYear", "2025", successResult.publicSignals.getOrNull(2))

        // Check proof structure
        assertNotNull("Proof should not be null", successResult.proof)
        assertTrue("Proof should contain data", successResult.proof.isNotEmpty())
    }

    @Test
    fun testUnderAge_ShouldSucceed() = runBlocking {
        // Test: 2015-born person is NOT >= 18 in 2025 (only 10 years old)
        val result = zkProver.generateProof(
            birthYear = 2015,
            minAge = 18,
            currentYear = 2025
        )

        assertTrue("Proof generation should succeed", result is ProofResult.Success)
        val successResult = result as ProofResult.Success

        // Check public signals - should indicate NOT old enough
        assertEquals("First signal should be 0 (not old enough)", "0", successResult.publicSignals.getOrNull(0))
        assertEquals("Second signal should be minAge", "18", successResult.publicSignals.getOrNull(1))
        assertEquals("Third signal should be currentYear", "2025", successResult.publicSignals.getOrNull(2))
    }

    @Test
    fun testInvalidBirthYear_TooEarly_ShouldThrow() {
        val exception = assertThrows(ZKProverException.InvalidInput::class.java) {
            runBlocking {
                zkProver.generateProof(
                    birthYear = 1899, // Invalid: < 1900
                    minAge = 18,
                    currentYear = 2025
                )
            }
        }
        assertTrue(exception.message?.contains("birthYear") == true)
    }

    @Test
    fun testInvalidBirthYear_Future_ShouldThrow() {
        val exception = assertThrows(ZKProverException.InvalidInput::class.java) {
            runBlocking {
                zkProver.generateProof(
                    birthYear = 2030, // Invalid: future year
                    minAge = 18,
                    currentYear = 2025
                )
            }
        }
        assertTrue(exception.message?.contains("future") == true)
    }

    @Test
    fun testInvalidMinAge_Negative_ShouldThrow() {
        val exception = assertThrows(ZKProverException.InvalidInput::class.java) {
            runBlocking {
                zkProver.generateProof(
                    birthYear = 1990,
                    minAge = -5, // Invalid: negative
                    currentYear = 2025
                )
            }
        }
        assertTrue(exception.message?.contains("minAge") == true)
    }

    @Test
    fun testInvalidMinAge_TooLarge_ShouldThrow() {
        val exception = assertThrows(ZKProverException.InvalidInput::class.java) {
            runBlocking {
                zkProver.generateProof(
                    birthYear = 1990,
                    minAge = 200, // Invalid: > 150
                    currentYear = 2025
                )
            }
        }
        assertTrue(exception.message?.contains("minAge") == true)
    }

    @Test
    fun testPerformance_ShouldCompleteIn5Seconds() = runBlocking {
        val startTime = System.currentTimeMillis()

        val result = zkProver.generateProof(
            birthYear = 1990,
            minAge = 18,
            currentYear = 2025
        )

        val elapsedTime = System.currentTimeMillis() - startTime

        assertTrue("Proof generation should succeed", result is ProofResult.Success)
        assertTrue("Proof generation should complete in < 5 seconds, took ${elapsedTime}ms",
            elapsedTime < 5000)
    }

    @Test
    fun testEdgeCase_ExactAge() = runBlocking {
        // Test: Someone born in 2007 is exactly 18 in 2025
        val result = zkProver.generateProof(
            birthYear = 2007,
            minAge = 18,
            currentYear = 2025
        )

        assertTrue("Proof generation should succeed", result is ProofResult.Success)
        val successResult = result as ProofResult.Success

        // Should be old enough (18 == 18)
        assertEquals("First signal should be 1 (exactly old enough)", "1", successResult.publicSignals.getOrNull(0))
    }

    @Test
    fun testEdgeCase_OneYearShort() = runBlocking {
        // Test: Someone born in 2008 is only 17 in 2025
        val result = zkProver.generateProof(
            birthYear = 2008,
            minAge = 18,
            currentYear = 2025
        )

        assertTrue("Proof generation should succeed", result is ProofResult.Success)
        val successResult = result as ProofResult.Success

        // Should NOT be old enough (17 < 18)
        assertEquals("First signal should be 0 (one year short)", "0", successResult.publicSignals.getOrNull(0))
    }
}
