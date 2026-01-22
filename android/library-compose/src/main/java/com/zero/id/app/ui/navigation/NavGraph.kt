package com.zero.id.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zero.id.app.ui.screens.home.HomeScreen
import com.zero.id.app.ui.screens.proof.ProofGenerationScreen
import com.zero.id.app.ui.screens.result.ResultScreen

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
            ProofGenerationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToResult = { isSuccess, message ->
                    navController.navigate(Screen.Result.createRoute(isSuccess, message)) {
                        // Pop up to home screen to avoid back stack issues
                        popUpTo(Screen.Home.route)
                    }
                }
            )
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
