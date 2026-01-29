package com.zero.id.app.ui.navigation

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.zero.id.app.ServiceLocator
import com.zero.id.app.model.DataRequest
import com.zero.id.app.ui.screens.ageproof.GenerateAgeProofScreen
import com.zero.id.app.ui.screens.home.HomeScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModel
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModelFactory
import com.zero.id.app.ui.screens.qr.QRScannerScreen
import com.zero.id.app.ui.screens.result.ResultScreen
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun NavGraphV2(navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "home_v2") {
        composable("home_v2") {
            HomeScreen(
                onNavigateToProofGeneration = { /*TODO*/ },
                onNavigateToAgeProof = { navController.navigate("generate_age_proof") },
                onNavigateToQrScanner = { navController.navigate("qr_scanner_v2") },
                onVerifyRequest = { jsonString ->
                    Log.d("NavGraphV2", "Received JSON: $jsonString")
                    // Implement test logic here
                },
                onDataShared = { isSuccess, message ->
                    Log.d("NavGraphV2", "Data shared: $isSuccess, $message")
                }
            )
        }
        composable("qr_scanner_v2") {
            QRScannerScreen { qrContent ->
                coroutineScope.launch {
                    try {
                        val gson = Gson()
                        val dataRequest = gson.fromJson(qrContent, DataRequest::class.java)
                        if (dataRequest != null && dataRequest.type == "DATA_REQUEST") {
                            // Navigate to a test consent screen if needed
                            Log.d("NavGraphV2", "Data Request found: $dataRequest")
                        } else {
                            Log.d("NavGraphV2", "Verification Request found.")
                        }
                    } catch (e: Exception) {
                        Log.e("NavGraphV2", "Error parsing QR content", e)
                    }
                }
            }
        }
        composable("generate_age_proof") {
            val factory = ProofGenerationViewModelFactory(
                LocalContext.current.applicationContext as Application,
                ServiceLocator.provideZKProver(LocalContext.current)
            )
            val viewModel: ProofGenerationViewModel = viewModel(factory = factory)

            GenerateAgeProofScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onProofGenerated = { proof ->
                    val encodedProof = URLEncoder.encode(proof, StandardCharsets.UTF_8.toString())
                    navController.navigate("result_v2/$encodedProof")
                }
            )
        }
        composable(
            "result_v2/{proof}",
            arguments = listOf(navArgument("proof") { type = NavType.StringType })
        ) { backStackEntry ->
            val proof = backStackEntry.arguments?.getString("proof")
            // This is a simplified result screen navigation.
            // You might want to pass more data depending on your logic.
            ResultScreen(
                isSuccess = true, // Replace with actual logic
                message = "Proof generated successfully.", // Replace with actual logic
                details = null,
                minAge = null,
                birthYear = null,
                userProfile = null,
                onNavigateHome = {
                    navController.navigate("home_v2") {
                        popUpTo("home_v2") { inclusive = true }
                    }
                },
                onRetry = {
                    navController.popBackStack()
                }
            )
        }
    }
}
