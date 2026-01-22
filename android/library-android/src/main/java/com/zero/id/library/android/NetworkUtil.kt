package com.zero.id.library.android

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

/**
 * Utility for checking network connectivity
 */
object NetworkUtil {

    /**
     * Check if device has an active network connection
     * @param context Application or Activity context
     * @return true if network is available, false otherwise
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    /**
     * Check if device is connected to Wi-Fi
     * @param context Application or Activity context
     * @return true if connected to Wi-Fi, false otherwise
     */
    fun isWifiConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected
        }
    }

    /**
     * Check if device is connected to mobile data
     * @param context Application or Activity context
     * @return true if connected to mobile data, false otherwise
     */
    fun isMobileDataConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_MOBILE && networkInfo.isConnected
        }
    }

    /**
     * Get a descriptive string of current network type
     * @param context Application or Activity context
     * @return String describing network type (e.g., "WiFi", "Mobile", "None")
     */
    fun getNetworkType(context: Context): String {
        return when {
            isWifiConnected(context) -> "WiFi"
            isMobileDataConnected(context) -> "Mobile"
            isNetworkAvailable(context) -> "Unknown"
            else -> "None"
        }
    }
}
