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
import androidx.compose.runtime.collectAsState
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
import com.zero.id.app.ui.screens.proof.ProofDisplayScreen
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
                    homeViewModel.clearLastProof()
                    navController.navigate(Screen.ProofDisplay.route)
                },
                onNavigateToQrScanner = {
                    navController.navigate(Screen.QRScanner.route)
                },
                onVerifyFromJson = {
                    coroutineScope.launch {
                        try {
                            val verificationRequest = Gson().fromJson(it, VerificationRequest::class.java)
                            val response = RetrofitClient.instance.verify(verificationRequest)
                            val isSuccess = response.isSuccessful && response.body()?.success == true
                            val message = response.body()?.message ?: if (isSuccess) "Success" else "Failed"
                            navController.navigate(Screen.VerificationResult.createRoute(isSuccess, message))
                        } catch (e: Exception) {
                            navController.navigate(Screen.VerificationResult.createRoute(false, "Error: ${e.message}"))
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
                    navController.popBackStack() 
                } else {
                    coroutineScope.launch {
                        try {
                            val verificationRequest = Gson().fromJson(scannedContent, VerificationRequest::class.java)
                            val response = RetrofitClient.instance.verify(verificationRequest)
                            val isSuccess = response.isSuccessful && response.body()?.success == true
                            val message = response.body()?.message ?: if (isSuccess) "Success" else "Failed"
                            navController.navigate(Screen.VerificationResult.createRoute(isSuccess, message)) {
                                popUpTo(Screen.Home.route)
                            }
                        } catch (e: Exception) {
                            navController.navigate(Screen.VerificationResult.createRoute(false, "Error: ${e.message}")) {
                                popUpTo(Screen.Home.route)
                            }
                        }
                    }
                }
            }
        }

        composable(route = Screen.FaceScan.route) {
            FaceScanScreen(
                onBack = { navController.popBackStack() },
                onRetry = { /* Logic */ },
                onUseFingerprint = { /* Logic */ },
                onUsePassword = { /* Logic */ }
            )
        }

        composable(route = Screen.ProofGeneration.route) {
            Text("Bypassed")
        }

        composable(route = Screen.ProofDisplay.route) {
            LaunchedEffect(Unit) {
                try {
                    val proofJson = context.assets.open("zkp/circuits/proof.json").bufferedReader().use { it.readText() }
                    val publicJson = context.assets.open("zkp/circuits/public.json").bufferedReader().use { it.readText() }
                    
                    // Specific mock data as requested
                    val mockPublicSignals = listOf("1", "20", "15000", "2026")
                    
                    val verificationRequest = com.zero.id.app.network.VerificationRequest(
                        proof = Gson().fromJson(proofJson, com.zero.id.app.network.Proof::class.java),
                        publicSignals = mockPublicSignals
                    )
                    
                    homeViewModel.setLastProofData(
                        verificationRequest,
                        proofJson,
                        Gson().toJson(mockPublicSignals)
                    )
                } catch (e: Exception) {
                    Log.e("NavGraph", "Error loading mock proof assets", e)
                }
            }

            ProofDisplayScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { isSuccess, message ->
                    navController.navigate(Screen.VerificationResult.createRoute(isSuccess, message))
                },
                viewModel = homeViewModel
            )
        }
        
        composable(
            route = Screen.VerificationResult.route,
            arguments = listOf(
                navArgument("isSuccess") { type = NavType.BoolType },
                navArgument("message") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val isSuccess = backStackEntry.arguments?.getBoolean("isSuccess") ?: false
            val message = backStackEntry.arguments?.getString("message") ?: ""
            VerificationResultScreen(
                isSuccess = isSuccess,
                message = message,
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
