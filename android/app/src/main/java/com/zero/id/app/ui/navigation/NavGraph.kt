package com.zero.id.app.ui.navigation

import android.app.Application
import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.zero.id.app.model.UserProfile
import com.zero.id.app.security.ProfileStorage
import com.zero.id.app.ui.screens.home.HomeScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModel
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModelFactory
import com.zero.id.app.ui.screens.qr.QRScannerScreen
import com.zero.id.app.ui.screens.result.ResultScreen
import com.zero.id.app.zkp.ZKProver
import com.zero.id.network.Details
import com.zero.id.network.RetrofitClient
import com.zero.id.network.VerificationRequest
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
                            val minAge = verificationRequest.publicSignals[1]
                            val response = RetrofitClient.instance.verify(verificationRequest)
                            if (response.isSuccessful && response.body() != null) {
                                val details = response.body()!!.details
                                val detailsJson = Gson().toJson(details)
                                val userProfile = ProfileStorage(context).getProfile()
                                val userProfileJson = if (details?.isOldEnough == true) Gson().toJson(userProfile) else null

                                navController.navigate(
                                    Screen.Result.createRoute(
                                        true, response.body()!!.message, detailsJson,
                                        minAge = minAge, birthYear = userProfile.birthYear.toString(),
                                        userProfileJson = userProfileJson
                                    )
                                ) {
                                    popUpTo(Screen.Home.route)
                                }
                            } else {
                                navController.navigate(Screen.Result.createRoute(false, "Verification failed", minAge = minAge)) {
                                    popUpTo(Screen.Home.route)
                                }
                            }
                        } catch (e: Exception) {
                            navController.navigate(Screen.Result.createRoute(false, e.message ?: "An unexpected error occurred")) {
                                popUpTo(Screen.Home.route)
                            }
                        }
                    }
                }
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
                            val birthYear = viewModel.birthYear.value
                            val minAge = it.publicSignals[1]
                            try {
                                val response = RetrofitClient.instance.verify(it)
                                if (response.isSuccessful && response.body() != null && response.body()!!.details != null) {
                                    val detailsJson = Gson().toJson(response.body()!!.details!!)
                                    navController.navigate(Screen.Result.createRoute(true, response.body()!!.message, detailsJson, minAge, birthYear)) {
                                        popUpTo(Screen.Home.route)
                                    }
                                } else {
                                    navController.navigate(Screen.Result.createRoute(false, "Verification failed", minAge = minAge, birthYear = birthYear)) {
                                        popUpTo(Screen.Home.route)
                                    }
                                }
                            } catch (e: Exception) {
                                navController.navigate(Screen.Result.createRoute(false, e.message ?: "An unexpected error occurred", minAge = minAge, birthYear = birthYear)) {
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
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("isSuccess") { type = NavType.BoolType },
                navArgument("message") { type = NavType.StringType },
                navArgument("details") { type = NavType.StringType; nullable = true },
                navArgument("minAge") { type = NavType.StringType; nullable = true },
                navArgument("birthYear") { type = NavType.StringType; nullable = true },
                navArgument("userProfileJson") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val isSuccess = backStackEntry.arguments?.getBoolean("isSuccess") ?: false
            val message = backStackEntry.arguments?.getString("message") ?: ""
            val detailsJson = backStackEntry.arguments?.getString("details")
            val details = if (detailsJson != null) Gson().fromJson(detailsJson, Details::class.java) else null
            val minAge = backStackEntry.arguments?.getString("minAge")
            val birthYear = backStackEntry.arguments?.getString("birthYear")
            val userProfileJson = backStackEntry.arguments?.getString("userProfileJson")
            val userProfile = if (userProfileJson != null) Gson().fromJson(userProfileJson, UserProfile::class.java) else null

            ResultScreen(
                isSuccess = isSuccess,
                message = message,
                details = details,
                minAge = minAge,
                birthYear = birthYear,
                userProfile = userProfile,
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onRetry = {
                    navController.navigate(Screen.Home.route) 
                }
            )
        }

        composable(route = Screen.QRScanner.route) {
            QRScannerScreen {
                coroutineScope.launch {
                    try {
                        val verificationRequest = Gson().fromJson(it, VerificationRequest::class.java)
                        val minAge = verificationRequest.publicSignals[1]
                        val response = RetrofitClient.instance.verify(verificationRequest)
                        if (response.isSuccessful && response.body() != null) {
                            val details = response.body()!!.details
                            val detailsJson = Gson().toJson(details)
                            val userProfile = ProfileStorage(context).getProfile()
                            val userProfileJson = if (details?.isOldEnough == true) Gson().toJson(userProfile) else null
                            navController.navigate(
                                Screen.Result.createRoute(
                                    true, response.body()!!.message, detailsJson,
                                    minAge = minAge, birthYear = userProfile.birthYear.toString(),
                                    userProfileJson = userProfileJson
                                )
                            ) {
                                popUpTo(Screen.Home.route)
                            }
                        } else {
                            navController.navigate(
                                Screen.Result.createRoute(
                                    false, "Verification failed",
                                    minAge = minAge, birthYear = ProfileStorage(context).getProfile().birthYear.toString()
                                )
                            ) {
                                popUpTo(Screen.Home.route)
                            }
                        }
                    } catch (e: Exception) {
                        navController.navigate(Screen.Result.createRoute(false, e.message ?: "An unexpected error occurred")) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                }
            }
        }
    }
}
