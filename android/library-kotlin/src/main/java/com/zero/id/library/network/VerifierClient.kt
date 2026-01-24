package com.zero.id.library.network

import com.google.gson.Gson
import com.zero.id.library.model.ProofData
import com.zero.id.library.model.VerificationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Client for communicating with the backend verifier service
 * Sends zero-knowledge proofs for verification
 */
class VerifierClient(
    private val baseUrl: String = "http://10.0.2.2:3000" // Android emulator localhost
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Verify a zero-knowledge proof with the backend server
     * @param proofData The proof data to verify
     * @return VerificationResult indicating success or failure
     */
    suspend fun verifyProof(proofData: ProofData): VerificationResult = withContext(Dispatchers.IO) {
        try {
            // Prepare request body
            val requestBody = mapOf(
                "proof" to proofData.proof,
                "publicSignals" to proofData.publicSignals
            )
            val json = gson.toJson(requestBody)
            val body = json.toRequestBody(jsonMediaType)

            // Build request
            val request = Request.Builder()
                .url("$baseUrl/api/verify")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()

            // Execute request
            val response = client.newCall(request).execute()

            // Parse response
            response.use {
                if (!response.isSuccessful) {
                    return@withContext VerificationResult(
                        success = false,
                        message = "Server error: ${response.code} ${response.message}"
                    )
                }

                val responseBody = response.body?.string()
                if (responseBody.isNullOrEmpty()) {
                    return@withContext VerificationResult(
                        success = false,
                        message = "Empty response from server"
                    )
                }

                // Parse JSON response
                val responseMap = gson.fromJson(responseBody, Map::class.java)
                val success = responseMap["success"] as? Boolean ?: false
                val message = responseMap["message"] as? String ?: "Unknown response"

                VerificationResult(success = success, message = message)
            }
        } catch (e: IOException) {
            VerificationResult(
                success = false,
                message = "Network error: ${e.message ?: "Connection failed"}"
            )
        } catch (e: Exception) {
            VerificationResult(
                success = false,
                message = "Error: ${e.message ?: "Unknown error"}"
            )
        }
    }

    /**
     * Check if the backend server is reachable
     * @return true if server responds, false otherwise
     */
    suspend fun ping(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/health")
                .get()
                .build()

            val response = client.newCall(request).execute()
            response.close()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
