package com.zero.id.app.ui.navigation

import android.app.Application
import android.util.Log
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.gson.Gson
import com.zero.id.app.model.DataRequest
import com.zero.id.app.ui.screens.ageproof.GenerateAgeProofScreen
import com.zero.id.app.ui.screens.home.HomeScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModel
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModelFactory
import com.zero.id.app.ui.screens.qr.QRScannerScreen
import com.zero.id.app.zkp.ZKProver
import kotlinx.coroutines.launch

@Composable
fun TestNavGraph(navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val application = context.applicationContext as Application

    NavHost(navController = navController, startDestination = "home_test") {
        composable("home_test") {
            HomeScreen(
                onNavigateToProofGeneration = { navController.navigate("proof_generation_test") },
                onNavigateToAgeProof = { navController.navigate("age_proof_test") },
                onNavigateToQrScanner = { navController.navigate("qr_scanner_test") },
                onVerifyRequest = { jsonString ->
                    Log.d("TestNavGraph", "Received JSON: $jsonString")
                    // Implement test logic here
                },
                onDataShared = { isSuccess, message ->
                    Log.d("TestNavGraph", "Data shared: $isSuccess, $message")
                }
            )
        }
        composable("age_proof_test") {
            val webView = remember { WebView(context) }
            var zkProver by remember { mutableStateOf<ZKProver?>(null) }
            var isInitialized by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val prover = ZKProver(context)
                prover.initialize(webView) { success ->
                    if (success) {
                        zkProver = prover
                    }
                    isInitialized = true // Signal that initialization attempt is complete
                }
            }

            if (isInitialized && zkProver != null) {
                val factory = ProofGenerationViewModelFactory(
                    application,
                    zkProver!!
                )
                val viewModel: ProofGenerationViewModel = viewModel(factory = factory)

                GenerateAgeProofScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onProofGenerated = { proof ->
                        Log.d("TestNavGraph", "Proof: $proof")
                        navController.popBackStack()
                    }
                )
            } else {
                // Show a loading indicator while ZKProver is initializing
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        composable("proof_generation_test") {
            val webView = remember { WebView(context) }
            var zkProver by remember { mutableStateOf<ZKProver?>(null) }
            var isInitialized by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                val prover = ZKProver(context)
                prover.initialize(webView) { success ->
                    if (success) {
                        zkProver = prover
                    }
                    isInitialized = true
                }
            }

            if (isInitialized && zkProver != null) {
                val factory = ProofGenerationViewModelFactory(
                    application,
                    zkProver!!
                )
                val viewModel: ProofGenerationViewModel = viewModel(factory = factory)
                ProofGenerationScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onVerificationRequest = { verificationRequest ->
                        Log.d("TestNavGraph", "Verification Request: $verificationRequest")
                        navController.popBackStack()
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        composable("qr_scanner_test") {
            QRScannerScreen { qrContent ->
                coroutineScope.launch {
                    try {
                        val gson = Gson()
                        val dataRequest = gson.fromJson(qrContent, DataRequest::class.java)
                        if (dataRequest != null && dataRequest.type == "DATA_REQUEST") {
                            // Navigate to a test consent screen if needed
                            Log.d("TestNavGraph", "Data Request found: $dataRequest")
                        } else {
                            Log.d("TestNavGraph", "Verification Request found.")
                        }
                    } catch (e: Exception) {
                        Log.e("TestNavGraph", "Error parsing QR content", e)
                    }
                }
            }
        }
    }
}
