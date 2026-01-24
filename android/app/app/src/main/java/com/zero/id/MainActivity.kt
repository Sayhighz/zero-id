package com.zero.id

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.zero.id.network.ApiClient
import com.zero.id.network.ProofRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ZeroIDApp() }
    }
}

@Composable
fun ZeroIDApp() {
    var currentScreen by remember { mutableIntStateOf(0) }
    var statusText by remember { mutableStateOf("Ready to Verify") }
    var isSuccess by remember { mutableStateOf<Boolean?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // ✅ ส่วนเช็คและขอ Permission กล้อง
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) currentScreen = 1
    }

    if (currentScreen == 0) {
        ZeroIDDashboard(
            statusText = statusText,
            isSuccess = isSuccess,
            onScanClick = {
                if (hasCameraPermission) currentScreen = 1
                else launcher.launch(android.Manifest.permission.CAMERA)
            },
            onTestApiClick = { // ✅ เติม Logic ปุ่ม Test ให้ใช้งานได้จริง
                statusText = "Testing API (Mock)..."
                isSuccess = null
                scope.launch {
                    try {
                        val mockRequest = ProofRequest(
                            proof = mapOf(
                                "pi_a" to listOf("0", "0", "0"),
                                "pi_b" to listOf(listOf("0", "0"), listOf("0", "0"), listOf("0", "0")),
                                "pi_c" to listOf("0", "0", "0"),
                                "protocol" to "groth16"
                            ),
                            publicSignals = listOf("1", "20", "2025")
                        )
                        val response = withContext(Dispatchers.IO) {
                            ApiClient.instance.verifyProof(mockRequest)
                        }
                        if (response.isSuccessful && response.body()?.success == true) {
                            statusText = "API Test Success ✅"
                            isSuccess = true
                        } else {
                            statusText = "API Test Failed ❌"
                            isSuccess = false
                        }
                    } catch (e: Exception) {
                        statusText = "Error: ${e.message}"
                        isSuccess = false
                    }
                }
            }
        )
    } else {
        // ✅ ส่วนรับค่าจาก Scanner จริง
        QRScannerScreen(onQRCodeScanned = { result ->
            currentScreen = 0
            statusText = "Verifying Scanned Proof..."
            isSuccess = null

            scope.launch {
                try {
                    val request = Gson().fromJson(result, ProofRequest::class.java)
                    val response = withContext(Dispatchers.IO) {
                        ApiClient.instance.verifyProof(request)
                    }
                    if (response.isSuccessful && response.body()?.success == true) {
                        statusText = "Verification Success ✅"
                        isSuccess = true
                    } else {
                        statusText = "Invalid Proof ❌"
                        isSuccess = false
                    }
                } catch (e: Exception) {
                    statusText = "Error: Invalid Data Format"
                    isSuccess = false
                }
            }
        })
    }
}

@Composable
fun ZeroIDDashboard(
    statusText: String,
    isSuccess: Boolean?,
    onScanClick: () -> Unit,
    onTestApiClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("ZeroID", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("ZK-Proof Authentication", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(48.dp))

        Card(
            modifier = Modifier.fillMaxWidth().height(150.dp),
            colors = CardDefaults.cardColors(
                containerColor = when(isSuccess) {
                    true -> Color(0xFFE8F5E9)
                    false -> Color(0xFFFFEBEE)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = statusText, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onScanClick,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Scan QR to Verify")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onTestApiClick,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Test API (Mock Data)")
        }
    }
}