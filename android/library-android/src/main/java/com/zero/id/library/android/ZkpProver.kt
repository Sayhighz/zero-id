package com.zero.id.library.android

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.gson.Gson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ZkpProver handles ZKP proof generation on the client-side using a WebView
 * to execute snarkjs (JavaScript).
 */
class ZkpProver(private val context: Context) {

    private val TAG = "ZkpProver"
    private val gson = Gson()
    private var webView: WebView? = null
    private val isWebViewReady = CompletableDeferred<Boolean>()

    init {
        initWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        Handler(Looper.getMainLooper()).post {
            try {
                Log.d(TAG, "Initializing WebView...")
                val wv = WebView(context)
                wv.settings.javaScriptEnabled = true
                wv.settings.allowFileAccess = true
                wv.settings.allowContentAccess = true
                wv.settings.domStorageEnabled = true
                
                WebView.setWebContentsDebuggingEnabled(true)
                
                wv.webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d(TAG, "JS Console: ${consoleMessage?.message()}")
                        return true
                    }
                }
                
                wv.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d(TAG, "WebView Loaded")
                        isWebViewReady.complete(true)
                    }
                }
                
                wv.addJavascriptInterface(this, "AndroidProver")
                
                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="utf-8">
                        <script src="https://cdn.jsdelivr.net/npm/snarkjs@0.7.4/build/snarkjs.min.js"></script>
                    </head>
                    <body>
                        <script>
                            async function generateProof(inputStr, wasmBase64, zkeyBase64) {
                                try {
                                    console.log("JS: Received data, converting base64...");
                                    
                                    // Convert base64 to Uint8Array
                                    const wasmBuf = Uint8Array.from(atob(wasmBase64), c => c.charCodeAt(0));
                                    const zkeyBuf = Uint8Array.from(atob(zkeyBase64), c => c.charCodeAt(0));
                                    
                                    const input = JSON.parse(inputStr);
                                    console.log("JS: Starting fullProve with buffers...");
                                    
                                    const { proof, publicSignals } = await snarkjs.groth16.fullProve(
                                        input, 
                                        wasmBuf, 
                                        zkeyBuf
                                    );
                                    
                                    console.log("JS: Proof Success!");
                                    AndroidProver.onProofGenerated(JSON.stringify(proof), JSON.stringify(publicSignals));
                                } catch (error) {
                                    console.error("JS Error: " + error.message);
                                    AndroidProver.onError(error.message);
                                }
                            }
                        </script>
                    </body>
                    </html>
                """.trimIndent()
                
                wv.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null)
                webView = wv
            } catch (e: Exception) {
                Log.e(TAG, "Failed to init WebView", e)
                isWebViewReady.completeExceptionally(e)
            }
        }
    }

    private var proofDeferred: CompletableDeferred<Pair<String, String>>? = null

    suspend fun generateProof(
        input: Map<String, Any>,
        wasmPath: String = "zkp/circuits/age_salary_check_js/age_salary_check.wasm",
        zkeyPath: String = "zkp/circuits/circuit_final.zkey"
    ): Pair<String, String> = withContext(Dispatchers.Default) {
        
        Log.d(TAG, "Waiting for WebView...")
        isWebViewReady.await()
        
        Log.d(TAG, "Reading assets and converting to Base64...")
        // Read binary files from assets and convert to Base64 to bypass WebView fetch restrictions
        val wasmBase64 = context.assets.open(wasmPath).use { it.readBytes() }.let { Base64.encodeToString(it, Base64.NO_WRAP) }
        val zkeyBase64 = context.assets.open(zkeyPath).use { it.readBytes() }.let { Base64.encodeToString(it, Base64.NO_WRAP) }
        
        val inputStr = gson.toJson(input)
        
        withContext(Dispatchers.Main) {
            Log.d(TAG, "Executing JS with data buffers...")
            proofDeferred = CompletableDeferred()
            webView?.evaluateJavascript("generateProof('$inputStr', '$wasmBase64', '$zkeyBase64')", null)
        }
        
        val result = proofDeferred!!.await()
        Log.d(TAG, "Proof calculation finished!")
        result
    }

    @JavascriptInterface
    fun onProofGenerated(proof: String, publicSignals: String) {
        Log.d(TAG, "Interface: Success")
        proofDeferred?.complete(Pair(proof, publicSignals))
    }

    @JavascriptInterface
    fun onError(error: String) {
        Log.e(TAG, "Interface: Error: $error")
        proofDeferred?.completeExceptionally(Exception(error))
    }
}
