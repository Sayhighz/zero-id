package com.zero.id.app

import android.content.Context
import android.webkit.WebView
import com.zero.id.app.security.KeyStoreManager
import com.zero.id.app.zkp.SecureProofGenerator
import com.zero.id.app.zkp.ZKProver
import com.zero.id.library.network.VerifierClient

/**
 * Service Locator pattern for dependency injection
 * Provides singleton instances of app services
 */
object ServiceLocator {

    private var applicationContext: Context? = null

    // Lazy-initialized singletons
    private var _zkProver: ZKProver? = null
    private var _keyStoreManager: KeyStoreManager? = null
    private var _secureProofGenerator: SecureProofGenerator? = null
    private var _verifierClient: VerifierClient? = null
    private var _webView: WebView? = null

    /**
     * Initialize the service locator with application context
     * Must be called in Application.onCreate()
     */
    fun init(context: Context) {
        applicationContext = context.applicationContext
    }

    /**
     * Get or create ZKProver instance
     * Note: WebView must be initialized before using ZKProver
     */
    fun provideZKProver(context: Context): ZKProver {
        if (_zkProver == null) {
            _zkProver = ZKProver(context)
        }
        return _zkProver!!
    }

    /**
     * Get or create KeyStoreManager instance
     */
    fun provideKeyStoreManager(context: Context): KeyStoreManager {
        if (_keyStoreManager == null) {
            _keyStoreManager = KeyStoreManager(context)
        }
        return _keyStoreManager!!
    }

    /**
     * Get or create SecureProofGenerator instance
     */
    fun provideSecureProofGenerator(context: Context): SecureProofGenerator {
        if (_secureProofGenerator == null) {
            _secureProofGenerator = SecureProofGenerator(context)
        }
        return _secureProofGenerator!!
    }

    /**
     * Get or create VerifierClient instance
     */
    fun provideVerifierClient(): VerifierClient {
        if (_verifierClient == null) {
            _verifierClient = VerifierClient()
        }
        return _verifierClient!!
    }

    /**
     * Create a new WebView instance for ZKProver
     * Note: WebView should be created on main thread
     */
    fun provideWebView(context: Context): WebView {
        if (_webView == null) {
            _webView = WebView(context)
        }
        return _webView!!
    }

    /**
     * Clean up resources
     * Should be called in Application.onTerminate()
     */
    fun cleanup() {
        _webView?.destroy()
        _webView = null
        _zkProver = null
        _keyStoreManager = null
        _secureProofGenerator = null
        _verifierClient = null
        applicationContext = null
    }

    /**
     * Reset all services (useful for testing)
     */
    fun reset() {
        cleanup()
    }
}
