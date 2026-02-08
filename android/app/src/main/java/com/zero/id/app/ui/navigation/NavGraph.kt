package com.zero.id.app.ui.navigation

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.zero.id.app.network.RetrofitClient
import com.zero.id.app.network.VerificationRequest
import com.zero.id.app.security.ProfileStorage
import com.zero.id.app.ui.screens.face.FaceScanScreen
import com.zero.id.app.ui.screens.home.HomeScreen
import com.zero.id.app.ui.screens.home.HomeViewModel
import com.zero.id.app.ui.screens.proof.ProofDisplayScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModel
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModelFactory
import com.zero.id.app.ui.screens.qr.QRScannerScreen
import com.zero.id.app.ui.screens.verification.VerificationResultScreen
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val profileStorage = ProfileStorage(context)
    
    val homeViewModel: HomeViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Home.route) {
            val challengeResponse by homeViewModel.challengeResponse.collectAsState()
            
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
                            // เปลี่ยนไปใช้ verifyDirect
                            val response = RetrofitClient.instance.verifyDirect(verificationRequest)
                            val body = response.body()
                            Log.d("Verification", "Endpoint: verify-direct, Body: $body")

                            val isFinalSuccess = response.isSuccessful &&
                                               body?.success == true &&
                                               body.isQualified == true

                            val message = body?.message ?: if (isFinalSuccess) "Success" else "Verification Failed"
                            navController.navigate(Screen.VerificationResult.createRoute(isFinalSuccess, message))
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

        composable(route = Screen.ProofGeneration.route) {
            val challenge by homeViewModel.challengeResponse.collectAsState()
            val userProfile = profileStorage.getProfile()
            
            val factory = ProofGenerationViewModelFactory(
                application = application,
                minAge = challenge?.minAge,
                minSalary = challenge?.minSalary,
                currentYear = challenge?.currentYear ?: 2026
            )
            val proofViewModel: ProofGenerationViewModel = viewModel(factory = factory)
            
            ProofGenerationScreen(
                onNavigateBack = { navController.popBackStack() },
                onVerificationRequest = { verificationRequest ->
                    coroutineScope.launch {
                        try {
                            // เปลี่ยนไปใช้ verifyDirect
                            val response = RetrofitClient.instance.verifyDirect(verificationRequest)
                            val body = response.body()
                            Log.d("Verification", "Endpoint: verify-direct, Body: $body")

                            val isFinalSuccess = response.isSuccessful &&
                                               body?.success == true &&
                                               body.isQualified == true

                            val message = body?.message ?: if (isFinalSuccess) "Verification Completed" else "Verification Failed"
                            navController.navigate(Screen.VerificationResult.createRoute(isFinalSuccess, message))
                        } catch (e: Exception) {
                            navController.navigate(Screen.VerificationResult.createRoute(false, "Network Error: ${e.message}"))
                        }
                    }
                },
                viewModel = proofViewModel
            )

            androidx.compose.runtime.LaunchedEffect(Unit) {
                proofViewModel.generateProof(userProfile.birthYear, userProfile.salary)
            }
        }

        composable(route = Screen.ProofDisplay.route) {
            ProofDisplayScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToResult = { isSuccess, message ->
                    navController.navigate(Screen.VerificationResult.createRoute(isSuccess, message))
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
                            // เปลี่ยนไปใช้ verifyDirect
                            val response = RetrofitClient.instance.verifyDirect(verificationRequest)
                            val body = response.body()
                            Log.d("Verification", "Endpoint: verify-direct, Body: $body")

                            val isFinalSuccess = response.isSuccessful &&
                                               body?.success == true &&
                                               body.isQualified == true

                            val message = body?.message ?: if (isFinalSuccess) "Verification Completed" else "Verification Failed"
                            navController.navigate(Screen.VerificationResult.createRoute(isFinalSuccess, message)) {
                                popUpTo(Screen.Home.route)
                            }
                        } catch (e: Exception) {
                            navController.navigate(Screen.VerificationResult.createRoute(false, "Error: ${e.message}"))
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
