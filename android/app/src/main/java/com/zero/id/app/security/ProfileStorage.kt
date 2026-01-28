package com.zero.id.app.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.zero.id.app.model.UserProfile

/**
 * Secure storage manager for user's private identity profile.
 * Uses EncryptedSharedPreferences to store data safely on device.
 */
class ProfileStorage(context: Context) {

    private val gson = Gson()
    
    // Initialize MasterKey for encryption
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Initialize EncryptedSharedPreferences
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_user_profile",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Save user profile to secure storage
     */
    fun saveProfile(profile: UserProfile) {
        val json = gson.toJson(profile)
        sharedPreferences.edit().putString("profile_data", json).apply()
    }

    /**
     * Retrieve user profile from secure storage
     */
    fun getProfile(): UserProfile {
        val json = sharedPreferences.getString("profile_data", null)
        return if (json != null) {
            gson.fromJson(json, UserProfile::class.java)
        } else {
            UserProfile() // Return empty profile if not found
        }
    }

    /**
     * Clear all stored profile data
     */
    fun clearProfile() {
        sharedPreferences.edit().remove("profile_data").apply()
    }
}
