package com.zero.id.app.ui.navigation

import android.app.Application
import android.webkit.WebView
import android.widget.Toast
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
import com.zero.id.app.ServiceLocator
import com.zero.id.app.ui.screens.home.HomeScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModel
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModelFactory
import com.zero.id.app.ui.screens.qr.QRGeneratorScreen
import com.zero.id.app.ui.screens.qr.QRScannerScreen
import com.zero.id.app.ui.screens.result.ResultScreen
import com.zero.id.app.zkp.ZKProver
import com.zero.id.network.Details
import com.zero.id.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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
                onNavigateToQrGenerator = {
                    navController.navigate(Screen.QRGenerator.route)
                },
                onVerifyFromJson = {
                    coroutineScope.launch {
                        try {
                            val map = Gson().fromJson(it, Map::class.java)
                            if (map.containsKey("type") && map["type"] == "IDENTITY_REQUEST") {
                                val encodedJson = URLEncoder.encode(it, StandardCharsets.UTF_8.toString())
                                navController.navigate(Screen.ProofGeneration.createRoute(encodedJson))
                            } else {
                                Toast.makeText(context, "Invalid QR Code for Identity Request", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Unknown QR Code Format", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        composable(route = Screen.ProofGeneration.route + "?requestJson={requestJson}") {
            val requestJson = it.arguments?.getString("requestJson") ?: ""
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
                    onVerificationRequest = { verificationRequest, minAge ->
                        coroutineScope.launch {
                            val birthYear = viewModel.birthYear.value
                            try {
                                val response = RetrofitClient.instance.verify(verificationRequest)
                                val details = if (response.isSuccessful) response.body()?.details else null
                                navController.navigate(Screen.Result.createRoute(response.isSuccessful, response.message() ?: "", minAge, birthYear)) {
                                    popUpTo(Screen.Home.route)
                                }
                            } catch (e: Exception) {
                                navController.navigate(Screen.Result.createRoute(false, e.message ?: "An unexpected error occurred", minAge, birthYear)) {
                                    popUpTo(Screen.Home.route)
                                }
                            }
                        }
                    },
                    viewModel = viewModel,
                    requestJson = requestJson
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
                navArgument("minAge") { type = NavType.StringType; nullable = true },
                navArgument("birthYear") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val isSuccess = backStackEntry.arguments?.getBoolean("isSuccess") ?: false
            val message = backStackEntry.arguments?.getString("message") ?: ""
            val minAge = backStackEntry.arguments?.getString("minAge")
            val birthYear = backStackEntry.arguments?.getString("birthYear")

            ResultScreen(
                isSuccess = isSuccess,
                message = message,
                details = null, // This will be handled in the ResultScreen
                minAge = minAge,
                birthYear = birthYear,
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onRetry = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.QRScanner.route) {
            QRScannerScreen { scannedData ->
                try {
                    val map = Gson().fromJson(scannedData, Map::class.java)
                    if (map.containsKey("type") && map["type"] == "IDENTITY_REQUEST") {
                        val encodedJson = URLEncoder.encode(scannedData, StandardCharsets.UTF_8.toString())
                        navController.navigate(Screen.ProofGeneration.createRoute(encodedJson))
                    } else {
                        Toast.makeText(context, "Invalid QR Code for Identity Request", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Unknown QR Code Format", Toast.LENGTH_SHORT).show()
                }
            }
        }

        composable(route = Screen.QRGenerator.route) {
            QRGeneratorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
