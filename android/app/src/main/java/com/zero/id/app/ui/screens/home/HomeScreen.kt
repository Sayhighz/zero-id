package com.zero.id.app.ui.screens.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.zero.id.app.ui.screens.ageproof.GenerateAgeProofScreen
import com.zero.id.app.ui.theme.ZeroIDTheme
import java.io.IOException

/**
 * Home screen of ZeroID app
 * Entry point showing app branding and main action
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProofGeneration: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onVerifyFromJson: (String) -> Unit
) {
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val image = InputImage.fromFilePath(context, it)
                val scanner = BarcodeScanning.getClient()
                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        if (barcodes.isNotEmpty()) {
                            barcodes.first().rawValue?.let(onVerifyFromJson)
                        } else {
                            // Optional: Handle case where no QR code is found in the image
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle scanning failure
                        e.printStackTrace()
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ZeroID") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Security icon
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Security",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Headline
            Text(
                text = "Zero-Knowledge Identity",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "Prove your age without revealing your birth date",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Primary action button
            GenerateAgeProofScreen(
                onNavigateToProofGeneration = onNavigateToProofGeneration,
                onNavigateToQrScanner = onNavigateToQrScanner,
                onVerifyFromJson = onVerifyFromJson
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Image, contentDescription = "Scan from image")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan QR from Image")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Information card
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "How it works",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoPoint(
                        number = "1",
                        text = "Enter your birth year and minimum age requirement"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InfoPoint(
                        number = "2",
                        text = "Generate a zero-knowledge proof on your device"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InfoPoint(
                        number = "3",
                        text = "Prove you meet the age requirement without revealing your actual age"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Your birth date never leaves your device",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Composable for displaying numbered information points
 */
@Composable
private fun InfoPoint(number: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ZeroIDTheme {
        HomeScreen(
            onNavigateToProofGeneration = {},
            onNavigateToQrScanner = {},
            onVerifyFromJson = {}
        )
    }
}
