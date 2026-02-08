package com.zero.id.app.zkp

import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * ZKProver manages Zero-Knowledge proof generation using WebView and SnarkJS
 */
class ZKProver(private val context: Context) {

    private var webView: WebView? = null
    private var isInitialized = false
    private var initCallback: ((Boolean) -> Unit)? = null
    private var proofContinuation: CancellableContinuation<ProofResult>? = null
    private val gson = Gson()

    companion object {
        private const val TAG = "ZKProver"
    }

    /**
     * Initialize WebView and load the ZK prover HTML
     */
    fun initialize(webView: WebView, onReady: (Boolean) -> Unit) {
        this.webView = webView
        this.initCallback = onReady

        webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true

            // Critical: Allow fetch() from file:// URLs
            settings.allowFileAccessFromFileURLs = true
            settings.allowUniversalAccessFromFileURLs = true

            // Performance optimizations
            settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            settings.databaseEnabled = true
            settings.setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)

            // Enable WebView debugging in debug builds
            WebView.setWebContentsDebuggingEnabled(true) // Always enabled for development

            // Add console message handler for debugging
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                    message?.let {
                        Log.d(TAG, "JS Console [${it.messageLevel()}]: ${it.message()} " +
                                "at ${it.sourceId()}:${it.lineNumber()}")
                    }
                    return true
                }
            }

            // Add page load tracking
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    Log.d(TAG, "WebView page loaded: $url")
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    Log.e(TAG, "WebView error: $description at $failingUrl (code: $errorCode)")
                }
            }

            // Add JavaScript interface
            addJavascriptInterface(WebAppInterface(), "Android")

            // Load the HTML file from assets
            loadUrl("file:///android_asset/zkprover.html")
            Log.d(TAG, "Loading zkprover.html")
        }
    }

    /**
     * Generate a zero-knowledge proof
     * @param birthYear User's birth year
     * @param salary User's salary
     * @param minAge Minimum age requirement to prove
     * @param minSalary Minimum salary requirement to prove
     * @param currentYear Current year
     * @return ProofResult containing the proof or error
     */
    suspend fun generateProof(
        birthYear: Int,
        salary: Int,
        minAge: Int,
        minSalary: Int,
        currentYear: Int
    ): ProofResult {
        // Check initialization
        if (!isInitialized) {
            throw ZKProverException.NotInitialized()
        }

        // Input validation
        if (birthYear < 1900) {
            throw ZKProverException.InvalidInput("birthYear", "must be >= 1900, got $birthYear")
        }
        if (birthYear > currentYear) {
            throw ZKProverException.InvalidInput("birthYear", "cannot be in the future")
        }

        val startTime = System.currentTimeMillis()
        Log.d(TAG, "Starting proof generation for birthYear=$birthYear, salary=$salary, minAge=$minAge, minSalary=$minSalary")

        return try {
            withTimeout(15_000) { // Increased timeout to 15s
                suspendCancellableCoroutine { continuation ->
                    val javascript = """
                        (async function() {
                            try {
                                const result = await window.generateProof($birthYear, $salary, $minAge, $minSalary, $currentYear);
                                // Result will be sent via Android.onProofGenerated
                            } catch (error) {
                                console.error('Error calling generateProof:', error);
                                Android.onProofGenerated(JSON.stringify({ error: error.message }));
                            }
                        })();
                    """.trimIndent()

                    proofContinuation = continuation

                    // Set up cancellation
                    continuation.invokeOnCancellation {
                        Log.d(TAG, "Proof generation cancelled")
                        proofContinuation = null
                    }

                    webView?.evaluateJavascript(javascript) { result ->
                        val elapsedTime = System.currentTimeMillis() - startTime
                        Log.d(TAG, "JavaScript evaluation completed in ${elapsedTime}ms")
                    }
                }
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "Proof generation timed out")
            throw ZKProverException.ProofGenerationTimeout(15)
        } finally {
            val totalTime = System.currentTimeMillis() - startTime
            Log.d(TAG, "Proof generation completed in ${totalTime}ms")
        }
    }

    /**
     * JavaScript interface for communication between WebView and Kotlin
     */
    inner class WebAppInterface {
        @JavascriptInterface
        fun onWasmLoaded(success: Boolean) {
            Log.d(TAG, "WASM loaded: $success")
            isInitialized = success
            initCallback?.invoke(success)
        }

        @JavascriptInterface
        fun onProofGenerated(jsonResult: String) {
            Log.d(TAG, "Proof generated, parsing result...")
            val continuation = proofContinuation ?: run {
                Log.w(TAG, "No continuation available for proof result")
                return
            }
            proofContinuation = null

            try {
                val result = parseProofResult(jsonResult)
                continuation.resume(result)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse proof result", e)
                continuation.resumeWithException(e)
            }
        }
    }

    /**
     * Parse JSON proof result into ProofResult sealed class
     */
    private fun parseProofResult(json: String): ProofResult {
        return try {
            val jsonObject = gson.fromJson(json, JsonObject::class.java)

            if (jsonObject.has("error")) {
                ProofResult.Error(jsonObject.get("error").asString)
            } else {
                val proofType = object : TypeToken<Map<String, Any>>() {}.type
                val proof = gson.fromJson<Map<String, Any>>(
                    jsonObject.get("proof"),
                    proofType
                )

                val signalsType = object : TypeToken<List<String>>() {}.type
                val publicSignals = gson.fromJson<List<String>>(
                    jsonObject.get("publicSignals"),
                    signalsType
                )

                Log.d(TAG, "Proof parsed successfully. Public signals: $publicSignals")
                ProofResult.Success(proof, publicSignals)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse proof JSON", e)
            ProofResult.Error("Failed to parse proof: ${e.message}")
        }
    }
}

/**
 * Result of a proof generation operation
 */
sealed class ProofResult {
    data class Success(
        val proof: Map<String, Any>,
        val publicSignals: List<String>
    ) : ProofResult()

    data class Error(val message: String) : ProofResult()
}

/**
 * Custom exceptions for ZKProver operations
 */
sealed class ZKProverException(message: String) : Exception(message) {
    class NotInitialized : ZKProverException("ZKProver not initialized. Call initialize() first.")
    class WasmLoadFailed(reason: String) : ZKProverException("Failed to load WASM: $reason")
    class ProofGenerationTimeout(seconds: Int) : ZKProverException("Proof generation timed out after $seconds seconds")
    class InvalidInput(field: String, reason: String) : ZKProverException("Invalid $field: $reason")
}
