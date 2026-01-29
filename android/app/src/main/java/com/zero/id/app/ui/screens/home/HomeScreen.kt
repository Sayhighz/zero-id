package com.zero.id.app.ui.screens.home

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.zero.id.app.model.DataRequest
import com.zero.id.app.model.UserProfile
import com.zero.id.app.security.ProfileStorage
import com.zero.id.app.ui.screens.consent.getClaim
import com.zero.id.app.ui.theme.ZeroIDTheme
import com.zero.id.network.RetrofitClient
import kotlinx.coroutines.launch
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProofGeneration: () -> Unit,
    onNavigateToAgeProof: () -> Unit, // Added this line
    onNavigateToQrScanner: () -> Unit,
    onVerifyRequest: (String) -> Unit,
    onDataShared: (Boolean, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showConsentDialog by remember { mutableStateOf(false) }
    var currentDataRequest by remember { mutableStateOf<DataRequest?>(null) }

    val onVerifyFromJson: (String) -> Unit = { jsonString ->
        coroutineScope.launch {
            try {
                val dataRequest = Gson().fromJson(jsonString, DataRequest::class.java)
                if (dataRequest?.type == "DATA_REQUEST") {
                    currentDataRequest = dataRequest
                    showConsentDialog = true
                } else {
                    onVerifyRequest(jsonString)
                }
            } catch (e: Exception) {
                onDataShared(false, "Invalid QR Code format")
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Log.d("HomeScreen", "Image URI selected: $it")
            try {
                val image = InputImage.fromFilePath(context, it)
                BarcodeScanning.getClient().process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isEmpty()) {
                            Log.d("HomeScreen", "No QR code found in the image.")
                            onDataShared(false, "No QR code found in the image.")
                        } else {
                            val qrCodeValue = barcodes.first().rawValue
                            Log.d("HomeScreen", "QR code found: $qrCodeValue")
                            qrCodeValue?.let(onVerifyFromJson)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("HomeScreen", "Barcode scanning from image failed", e)
                        onDataShared(false, "Failed to scan QR code.")
                    }
            } catch (e: IOException) {
                Log.e("HomeScreen", "File reading from URI failed", e)
                onDataShared(false, "Failed to read image file.")
            }
        } ?: run {
            Log.d("HomeScreen", "Image URI is null, no image selected.")
        }
    }

    if (showConsentDialog && currentDataRequest != null) {
        val userProfile = ProfileStorage(context).getProfile()
        ConsentDialog(
            dataRequest = currentDataRequest!!,
            userProfile = userProfile,
            onConfirm = {
                showConsentDialog = false
                coroutineScope.launch {
                    try {
                        val requestedData = currentDataRequest!!.claims.associateWith { claim -> userProfile.getClaim(claim) }
                        val response = RetrofitClient.instance.submitUserData(requestedData)
                        if (response.isSuccessful) {
                            onDataShared(true, "Data shared successfully")
                        } else {
                            onDataShared(false, "Failed to share data (Server error)")
                        }
                    } catch (e: Exception) {
                        Log.e("HomeScreen", "Data submission failed", e)
                        onDataShared(false, "Data submission failed: ${e.message}")
                    }
                }
            },
            onCancel = {
                showConsentDialog = false
            }
        )
    }

    Scaffold(
        topBar = { /* ... */ }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ... (Existing UI content like Icon, Text, etc.)

            Spacer(modifier = Modifier.height(48.dp))

            // New Button to Navigate to Age Proof Screen
            Button(onClick = onNavigateToAgeProof) {
                Text("Prove Minimum Age")
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onNavigateToProofGeneration) {
                Text("Generate Manual Proof")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateToQrScanner) {
                Text("Scan QR from Camera")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                Icon(Icons.Default.Image, contentDescription = "Scan from image")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan QR from Image")
            }

            // ... (Information card)
        }
    }
}

@Composable
fun ConsentDialog(
    dataRequest: DataRequest,
    userProfile: UserProfile,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Data Sharing Request") },
        text = {
            Column {
                Text("${dataRequest.requester} requests the following information:")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Purpose: ${dataRequest.purpose}", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                dataRequest.claims.forEach {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${it}: ${userProfile.getClaim(it) ?: "(Not available)"}")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onCancel) { Text("Cancel") } }
    )
}

// ... (InfoPoint and Preview can be simplified or updated)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ZeroIDTheme {
        HomeScreen(
            onNavigateToProofGeneration = {},
            onNavigateToAgeProof = {},
            onNavigateToQrScanner = {},
            onVerifyRequest = {},
            onDataShared = { _, _ -> }
        )
    }
}
