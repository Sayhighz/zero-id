package com.zero.id.app.ui.navigation

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Navigation graph for ZeroID app
 * Defines all navigation routes and screen transitions
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    val coroutineScope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Home Screen
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
                            if (response.isSuccessful && response.body() != null) {
                                val detailsJson = Gson().toJson(response.body()!!.details)
                                navController.navigate(Screen.Result.createRoute(true, response.body()!!.message, detailsJson)) {
                                    popUpTo(Screen.Home.route)
                                }
                            } else {
                                navController.navigate(Screen.Result.createRoute(false, "Verification failed")) {
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

        // Proof Generation Screen
        composable(route = Screen.ProofGeneration.route) {
            val context = LocalContext.current

            // Hold WebView reference to prevent GC
            val webView = remember { WebView(context) }
            var zkProver by remember { mutableStateOf<ZKProver?>(null) }
            var isInitialized by remember { mutableStateOf(false) }
            var initError by remember { mutableStateOf<String?>(null) }

            // Initialize ZKProver with WebView
            LaunchedEffect(Unit) {
                try {
                    withContext(Dispatchers.Main) {
                        val prover = ZKProver(context)

                        // Initialize with callback
                        prover.initialize(webView) { success ->
                            isInitialized = success
                            if (success) {
                                zkProver = prover
                            } else {
                                initError = "Failed to initialize proof generator"
                            }
                        }
                    }

                    // Timeout after 30 seconds
                    delay(30000)
                    if (!isInitialized && initError == null) {
                        initError = "Initialization timeout"
                    }
                } catch (e: Exception) {
                    initError = "Error: ${e.message}"
                }
            }

            // Cleanup WebView when leaving
            DisposableEffect(Unit) {
                onDispose {
                    webView.destroy()
                }
            }

            when {
                // Show error if initialization failed
                initError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Initialization Failed",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = initError ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Go Back")
                        }
                    }
                }

                // Show screen when initialized
                isInitialized && zkProver != null -> {
                    val factory = ProofGenerationViewModelFactory(zkProver!!)
                    val viewModel: ProofGenerationViewModel = viewModel(factory = factory)
                    ProofGenerationScreen(
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onVerificationRequest = {
                            coroutineScope.launch {
                                try {
                                    val response = RetrofitClient.instance.verify(it)
                                    if (response.isSuccessful && response.body() != null) {
                                        val detailsJson = Gson().toJson(response.body()!!.details)
                                        navController.navigate(Screen.Result.createRoute(true, response.body()!!.message, detailsJson)) {
                                            popUpTo(Screen.Home.route)
                                        }
                                    } else {
                                        navController.navigate(Screen.Result.createRoute(false, "Verification failed")) {
                                            popUpTo(Screen.Home.route)
                                        }
                                    }
                                } catch (e: Exception) {
                                    navController.navigate(Screen.Result.createRoute(false, e.message ?: "An unexpected error occurred")) {
                                        popUpTo(Screen.Home.route)
                                    }
                                }
                            }
                        },
                        viewModel = viewModel
                    )
                }

                // Show loading state while initializing
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Initializing proof generator...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This may take a moment",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Result Screen
        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("isSuccess") {
                    type = NavType.BoolType
                },
                navArgument("message") {
                    type = NavType.StringType
                },
                navArgument("details") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val isSuccess = backStackEntry.arguments?.getBoolean("isSuccess") ?: false
            val message = backStackEntry.arguments?.getString("message") ?: ""
            val detailsJson = backStackEntry.arguments?.getString("details")
            val details = if (detailsJson != null) Gson().fromJson(detailsJson, Details::class.java) else null

            ResultScreen(
                isSuccess = isSuccess,
                message = message,
                details = details,
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) {
                            inclusive = true
                        }
                    }
                },
                onRetry = {
                    navController.navigate(Screen.ProofGeneration.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        // QR Scanner Screen
        composable(route = Screen.QRScanner.route) {
            QRScannerScreen {
                coroutineScope.launch {
                    try {
                        val verificationRequest = Gson().fromJson(it, VerificationRequest::class.java)
                        val response = RetrofitClient.instance.verify(verificationRequest)
                        if (response.isSuccessful && response.body() != null) {
                            val detailsJson = Gson().toJson(response.body()!!.details)
                            navController.navigate(Screen.Result.createRoute(true, response.body()!!.message, detailsJson)) {
                                popUpTo(Screen.Home.route)
                            }
                        } else {
                            navController.navigate(Screen.Result.createRoute(false, "Verification failed")) {
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
