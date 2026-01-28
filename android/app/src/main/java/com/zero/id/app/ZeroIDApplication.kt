package com.zero.id.app

import android.app.Application
import android.webkit.WebView

/**
 * Application class for ZeroID
 * Initializes app-wide services and configurations
 */
class ZeroIDApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Enable WebView debugging for development
        // This allows Chrome DevTools inspection via chrome://inspect
        WebView.setWebContentsDebuggingEnabled(true)
    }
}
