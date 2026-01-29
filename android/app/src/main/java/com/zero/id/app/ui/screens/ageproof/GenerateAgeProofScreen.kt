package com.zero.id.app.ui.screens.ageproof

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.IOException

@Composable
fun GenerateAgeProofScreen(
    onNavigateToProofGeneration: () -> Unit,
    onNavigateToQrScanner: () -> Unit
) {
    var showMethodDialog by remember { mutableStateOf(false) }
    var showQrSourceDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                try {
                    val inputImage = InputImage.fromFilePath(context, uri)
                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                    val scanner = BarcodeScanning.getClient(options)
                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            barcodes.firstOrNull()?.rawValue?.let {
                                // onVerifyFromJson(it) // This was removed
                            }
                        }
                        .addOnFailureListener {
                            // Task failed with an exception
                            it.printStackTrace()
                        }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    )

    Button(onClick = { showMethodDialog = true }) {
        Text("Generate Age Proof")
    }

    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            title = { Text("Choose verification method") },
            text = { Text("You can generate a proof manually or scan a QR code.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMethodDialog = false
                        onNavigateToProofGeneration()
                    }
                ) {
                    Text("Manual")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showMethodDialog = false
                        showQrSourceDialog = true
                    }
                ) {
                    Text("QR Code")
                }
            }
        )
    }

    if (showQrSourceDialog) {
        AlertDialog(
            onDismissRequest = { showQrSourceDialog = false },
            title = { Text("Choose QR Code Source") },
            text = { Text("Scan a new QR code using the camera or select an image from your gallery.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showQrSourceDialog = false
                        onNavigateToQrScanner()
                    }
                ) {
                    Text("Use Camera")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showQrSourceDialog = false
                        pickImageLauncher.launch("image/*")
                    }
                ) {
                    Text("From Picture")
                }
            }
        )
    }
}
