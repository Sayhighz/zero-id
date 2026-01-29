package com.zero.id.app.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.gson.Gson
import com.zero.id.app.model.DataRequest
import com.zero.id.app.ui.screens.home.HomeScreen
import com.zero.id.app.ui.screens.qr.QRScannerScreen
import kotlinx.coroutines.launch

@Composable
fun NavGraphV2(navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "home_v2") {
        composable("home_v2") {
            HomeScreen(
                onNavigateToProofGeneration = { /*TODO*/ },
                onNavigateToQrScanner = { navController.navigate("qr_scanner_v2") },
                onVerifyFromJson = { jsonString ->
                    Log.d("NavGraphV2", "Received JSON: $jsonString")
                    // Implement test logic here
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
    }
}
