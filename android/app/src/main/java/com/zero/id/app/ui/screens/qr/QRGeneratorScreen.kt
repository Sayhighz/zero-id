package com.zero.id.app.ui.screens.qr

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.zero.id.app.model.UserProfile
import com.zero.id.network.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRGeneratorScreen(
    onNavigateBack: () -> Unit
) {
    var minAgeReq by remember { mutableStateOf("20") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var sessionId by remember { mutableStateOf<String?>(null) }
    var receivedProfile by remember { mutableStateOf<UserProfile?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Polling for data
    LaunchedEffect(sessionId) {
        if (sessionId != null) {
            while (receivedProfile == null) {
                try {
                    val response = RetrofitClient.instance.getSessionData(sessionId!!)
                    if (response.isSuccessful && response.body() != null) {
                        receivedProfile = response.body()
                    }
                } catch (e: Exception) {
                    errorMessage = e.message
                }
                delay(2000) // Poll every 2 seconds
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Identity") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Set minimum age requirement and show this QR to the person you want to verify.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = minAgeReq,
                onValueChange = { minAgeReq = it },
                label = { Text("Minimum Age Required") },
                modifier = Modifier.fillMaxWidth()
            )

            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val response = RetrofitClient.instance.createSession()
                            if (response.isSuccessful && response.body() != null) {
                                val newSessionId = response.body()!!.sessionId
                                sessionId = newSessionId
                                val requestData = mapOf(
                                    "type" to "IDENTITY_REQUEST",
                                    "sessionId" to newSessionId,
                                    "minAge" to minAgeReq
                                )
                                qrBitmap = generateQRCode(Gson().toJson(requestData))
                            } else {
                                errorMessage = response.errorBody()?.string() ?: "Unknown error"
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Generate Request QR")
            }

            Spacer(modifier = Modifier.height(32.dp))

            qrBitmap?.let {
                Text("Scan this with ZeroID app", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .size(250.dp)
                            .padding(16.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (receivedProfile == null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Waiting for scanner response...")
                    }
                }
            }

            receivedProfile?.let { profile ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Data Received!", style = MaterialTheme.typography.titleLarge)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Name: ${profile.fullName}")
                        Text("Birth Year: ${profile.birthYear}")
                        Text("Phone: ${profile.phoneNumber}")
                        Text("Status: Verified Age > $minAgeReq", color = Color(0xFF388E3C))
                    }
                }
            }
        }
    }
}

private fun generateQRCode(content: String): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
        }
    }
    return bitmap
}
