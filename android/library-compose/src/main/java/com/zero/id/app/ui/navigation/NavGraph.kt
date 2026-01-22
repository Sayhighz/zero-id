package com.zero.id.app.ui.navigation

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zero.id.app.ServiceLocator
import com.zero.id.app.ui.screens.home.HomeScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModel
import com.zero.id.app.ui.screens.result.ResultScreen
import com.zero.id.app.zkp.ZKProver
import kotlinx.coroutines.Dispatchers
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
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Home Screen
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToProofGeneration = {
                    navController.navigate(Screen.ProofGeneration.route)
                }
            )
        }

        // Proof Generation Screen
        composable(route = Screen.ProofGeneration.route) {
            val context = LocalContext.current
            var zkProver by remember { mutableStateOf<ZKProver?>(null) }
            var isInitialized by remember { mutableStateOf(false) }

            // Initialize ZKProver with WebView
            LaunchedEffect(Unit) {
                withContext(Dispatchers.Main) {
                    val webView = WebView(context)
                    val prover = ServiceLocator.provideZKProver(context)
                    prover.initialize(webView) { success ->
                        isInitialized = success
                        if (success) {
                            zkProver = prover
                        }
                    }
                }
            }

            // Cleanup WebView when leaving screen
            DisposableEffect(Unit) {
                onDispose {
                    // WebView cleanup handled by ServiceLocator
                }
            }

            val viewModel: ProofGenerationViewModel = viewModel(
                factory = androidx.lifecycle.ViewModelProvider.Factory {
                    object : androidx.lifecycle.ViewModel() {
                        init {
                            // Placeholder - actual ViewModel created below
                        }
                    }
                },
                key = "proof_gen_vm"
            )

            // Update ViewModel with initialized ZKProver
            if (isInitialized && zkProver != null) {
                ProofGenerationScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToResult = { isSuccess, message ->
                        navController.navigate(Screen.Result.createRoute(isSuccess, message)) {
                            // Pop up to home screen to avoid back stack issues
                            popUpTo(Screen.Home.route)
                        }
                    },
                    viewModel = ProofGenerationViewModel(zkProver)
                )
            } else {
                // Show loading state while initializing
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
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
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val isSuccess = backStackEntry.arguments?.getBoolean("isSuccess") ?: false
            val message = backStackEntry.arguments?.getString("message") ?: ""

            ResultScreen(
                isSuccess = isSuccess,
                message = message,
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
    }
}
