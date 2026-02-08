package com.zero.id.app.ui.navigation

import android.app.Application
import android.util.Log
import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.zero.id.app.network.RetrofitClient
import com.zero.id.app.network.VerificationRequest
import com.zero.id.app.ui.screens.face.FaceScanScreen
import com.zero.id.app.ui.screens.home.HomeScreen
import com.zero.id.app.ui.screens.home.HomeViewModel
import com.zero.id.app.ui.screens.proof.ProofGenerationScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModel
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModelFactory
import com.zero.id.app.ui.screens.qr.QRScannerScreen
import com.zero.id.app.ui.screens.verification.VerificationResultScreen
import com.zero.id.app.zkp.ZKProver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Shared HomeViewModel to allow QR scanner to update challenge state
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToProofGeneration = {
                    navController.navigate(Screen.ProofGeneration.route)
                },
                onNavigateToQrScanner = {
                    navController.navigate(Screen.QRScanner.route)
                },
                onVerifyFromJson = {
                    coroutineScope.launch {
                        try {
                            val verificationRequest = Gson().fromJson(it, VerificationRequest::class.java)
                            val response = RetrofitClient.instance.verify(verificationRequest)
                            if (response.isSuccessful && response.body()?.success == true) {
                                navController.navigate(Screen.VerificationResult.createRoute(true))
                            } else {
                                navController.navigate(Screen.VerificationResult.createRoute(false))
                            }
                        } catch (e: Exception) {
                            navController.navigate(Screen.VerificationResult.createRoute(false))
                        }
                    }
                },
                onNavigateToFaceScan = {
                    navController.navigate(Screen.FaceScan.route)
                },
                viewModel = homeViewModel
            )
        }

        composable(route = Screen.QRScanner.route) { 
            QRScannerScreen { scannedContent ->
                if (scannedContent.startsWith("http")) {
                    homeViewModel.fetchChallengeFromUrl(scannedContent)
                    navController.popBackStack() // Go back to Home to show the challenge dialog
                } else {
                    coroutineScope.launch {
                        try {
                            val verificationRequest = Gson().fromJson(scannedContent, VerificationRequest::class.java)
                            val response = RetrofitClient.instance.verify(verificationRequest)
                            val isSuccess = response.isSuccessful && response.body()?.success == true
                            navController.navigate(Screen.VerificationResult.createRoute(isSuccess)) {
                                popUpTo(Screen.Home.route)
                            }
                        } catch (e: Exception) {
                            navController.navigate(Screen.VerificationResult.createRoute(false)) {
                                popUpTo(Screen.Home.route)
                            }
                        }
                    }
                }
            }
        }

        // ... Rest of the composables (FaceScan, ProofGeneration, VerificationResult) remain same
        composable(route = Screen.FaceScan.route) {
            FaceScanScreen(
                onBack = { navController.popBackStack() },
                onRetry = { /* Logic */ },
                onUseFingerprint = { /* Logic */ },
                onUsePassword = { /* Logic */ }
            )
        }

        composable(route = Screen.ProofGeneration.route) {
            val webView = remember { WebView(context) }
            var zkProver by remember { mutableStateOf<ZKProver?>(null) }
            var isInitialized by remember { mutableStateOf(false) }
            var initError by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                try {
                    withContext(Dispatchers.Main) {
                        val prover = ZKProver(context)
                        prover.initialize(webView) { success ->
                            isInitialized = success
                            if (success) zkProver = prover else initError = "Failed to initialize proof generator"
                        }
                    }
                } catch (e: Exception) {
                    initError = "Error: ${e.message}"
                }
            }

            DisposableEffect(Unit) { onDispose { webView.destroy() } }

            if (initError != null) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("Initialization Failed", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(initError ?: "Unknown error", textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { navController.popBackStack() }) { Text("Go Back") }
                }
            } else if (isInitialized && zkProver != null) {
                val factory = ProofGenerationViewModelFactory(
                    context.applicationContext as Application,
                    zkProver!!
                )
                val viewModel: ProofGenerationViewModel = viewModel(factory = factory)
                ProofGenerationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onVerificationRequest = {
                        coroutineScope.launch {
                            try {
                                val response = RetrofitClient.instance.verify(it)
                                val isSuccess = response.isSuccessful && response.body()?.success == true
                                navController.navigate(Screen.VerificationResult.createRoute(isSuccess)) {
                                    popUpTo(Screen.Home.route)
                                }
                            } catch (e: Exception) {
                                navController.navigate(Screen.VerificationResult.createRoute(false)) {
                                    popUpTo(Screen.Home.route)
                                }
                            }
                        }
                    },
                    viewModel = viewModel
                )
            } else {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Initializing proof generator...")
                }
            }
        }
        
        composable(
            route = Screen.VerificationResult.route,
            arguments = listOf(navArgument("isSuccess") { type = NavType.BoolType })
        ) { backStackEntry ->
            val isSuccess = backStackEntry.arguments?.getBoolean("isSuccess") ?: false
            VerificationResultScreen(
                isSuccess = isSuccess,
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
