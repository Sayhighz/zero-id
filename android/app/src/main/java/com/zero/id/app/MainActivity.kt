package com.zero.id.app

import android.os.Bundle
import android.webkit.WebView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zero.id.app.zkp.ProofResult
import com.zero.id.app.zkp.ZKProver
import kotlinx.coroutines.launch

/**
 * Main Activity for testing ZKProver integration
 * This is a temporary test implementation for Day 1
 * Will be replaced with Compose UI in Day 4
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var zkProver: ZKProver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create WebView programmatically for testing
        webView = WebView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        setContentView(webView)

        // Initialize ZK Prover
        zkProver = ZKProver(this)
        zkProver.initialize(webView) { success ->
            if (success) {
                Toast.makeText(this, "ZK Prover Ready", Toast.LENGTH_SHORT).show()
                // Test proof generation after initialization
                testProofGeneration()
            } else {
                Toast.makeText(this, "Failed to initialize ZK Prover", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Test proof generation with sample data
     */
    private fun testProofGeneration() {
        lifecycleScope.launch {
            try {
                Toast.makeText(
                    this@MainActivity,
                    "Generating proof for birthYear=1990, minAge=18...",
                    Toast.LENGTH_SHORT
                ).show()

                val result = zkProver.generateProof(
                    birthYear = 1990,
                    minAge = 18,
                    currentYear = 2025
                )

                when (result) {
                    is ProofResult.Success -> {
                        val isOldEnough = result.publicSignals.getOrNull(0)
                        Toast.makeText(
                            this@MainActivity,
                            "Proof generated! Is old enough: $isOldEnough\nPublic signals: ${result.publicSignals}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is ProofResult.Error -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Error: ${result.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Exception: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
