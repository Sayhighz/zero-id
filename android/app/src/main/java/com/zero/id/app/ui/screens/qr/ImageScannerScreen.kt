package com.zero.id.app.ui.screens.qr

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.IOException

@Composable
fun ImageScannerScreen(
    onScanned: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                isScanning = true
                try {
                    val inputImage = InputImage.fromFilePath(context, uri)
                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                    val scanner = BarcodeScanning.getClient(options)

                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty()) {
                                barcodes.first().rawValue?.let(onScanned)
                            } else {
                                Toast.makeText(context, "No QR Code found in image", Toast.LENGTH_SHORT).show()
                            }
                            onNavigateBack()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to scan image: ${e.message}", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                } catch (e: IOException) {
                    Toast.makeText(context, "Failed to read image", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            } else {
                // User cancelled the picker
                onNavigateBack()
            }
        }
    )

    // Launch the picker as soon as the composable enters the composition
    LaunchedEffect(Unit) {
        pickImageLauncher.launch("image/*")
    }

    if (isScanning) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Scanning...")
        }
    }
}
