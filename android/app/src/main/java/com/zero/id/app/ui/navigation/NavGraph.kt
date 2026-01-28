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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.zero.id.app.ui.screens.home.HomeScreen
import com.zero.id.app.ui.screens.home.HomeViewModel
import com.zero.id.app.ui.screens.proof.ProofGenerationScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModel
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModelFactory
import com.zero.id.app.ui.screens.qr.ImageScannerScreen
import com.zero.id.app.ui.screens.qr.QRGeneratorScreen
import com.zero.id.app.ui.screens.qr.QRScannerScreen
import com.zero.id.app.ui.screens.result.ResultScreen
import com.zero.id.app.zkp.ZKProver
import com.zero.id.network.Details
import com.zero.id.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val homeViewModel: HomeViewModel = viewModel()

    LaunchedEffect(homeViewModel.navigateTo) {
        homeViewModel.navigateTo.collectLatest { route ->
            route?.let {
                navController.navigate(it)
                homeViewModel.onNavigated()
            }
        }
    }

    LaunchedEffect(homeViewModel.toastMessage) {
        homeViewModel.toastMessage.collectLatest { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                homeViewModel.onToastShown()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToQrScanner = {
                    navController.navigate(Screen.QRScanner.route)
                },
                onNavigateToImageScanner = {
                    navController.navigate(Screen.ImageScanner.route)
                },
                onNavigateToQrGenerator = {
                    navController.navigate(Screen.QRGenerator.route)
                }
            )
        }

        composable(route = Screen.ProofGeneration.route + "?requestJson={requestJson}") {
            val requestJson = it.arguments?.getString("requestJson") ?: ""
            val webView = remember { WebView(context) }
            var zkProver by remember { mutableStateOf<ZKProver?>(null) }
            var isInitialized by remember { mutableStateOf(false) }
            var initError by remember { mutableStateOf<String?>(null) }

            AndroidView(factory = { webView }, modifier = Modifier.size(0.dp))

            LaunchedEffect(Unit) {
                try {
                    val prover = withContext(Dispatchers.IO) {
                        ZKProver(context)
                    }
                    prover.initialize(webView) { success ->
                        isInitialized = success
                        if (success) zkProver = prover else initError = "Failed to initialize proof generator"
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
                    context.applicationContext as Application
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
                                val detailsJson = details?.let { Gson().toJson(it) }
                                val route = Screen.Result.createRoute(response.isSuccessful, response.message() ?: "", detailsJson, minAge, birthYear)
                                navController.navigate(route) {
                                    popUpTo(Screen.Home.route)
                                }
                            } catch (e: Exception) {
                                val route = Screen.Result.createRoute(false, e.message ?: "An unexpected error occurred", null, minAge, birthYear)
                                navController.navigate(route) {
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
            route = Screen.Result.route + "?details={details}&minAge={minAge}&birthYear={birthYear}",
            arguments = listOf(
                navArgument("isSuccess") { type = NavType.BoolType },
                navArgument("message") { type = NavType.StringType },
                navArgument("details") { type = NavType.StringType; nullable = true },
                navArgument("minAge") { type = NavType.StringType; nullable = true },
                navArgument("birthYear") { type = NavType.StringType; nullable = true }
            )
        ) { backStackEntry ->
            val isSuccess = backStackEntry.arguments?.getBoolean("isSuccess") ?: false
            val message = backStackEntry.arguments?.getString("message") ?: ""
            val detailsJson = backStackEntry.arguments?.getString("details")
            val details = remember { detailsJson?.let { Gson().fromJson(it, Details::class.java) } }
            val minAge = backStackEntry.arguments?.getString("minAge")
            val birthYear = backStackEntry.arguments?.getString("birthYear")

            ResultScreen(
                isSuccess = isSuccess,
                message = message,
                details = details,
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
                homeViewModel.onScannedData(scannedData)
                navController.popBackStack()
            }
        }

        composable(route = Screen.ImageScanner.route) {
            ImageScannerScreen(
                onScanned = {
                    homeViewModel.onScannedData(it)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.QRGenerator.route) {
            QRGeneratorScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
