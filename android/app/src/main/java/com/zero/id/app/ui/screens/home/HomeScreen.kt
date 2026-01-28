package com.zero.id.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zero.id.app.ui.theme.ZeroIDTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToQrScanner: () -> Unit,
    onNavigateToImageScanner: () -> Unit,
    onNavigateToQrGenerator: () -> Unit
) {
    var showScannerDialog by remember { mutableStateOf(false) }

    if (showScannerDialog) {
        AlertDialog(
            onDismissRequest = { showScannerDialog = false },
            title = { Text("Scan QR Code") },
            text = { Text("Choose how you want to scan a QR code.") },
            confirmButton = {
                TextButton(onClick = {
                    showScannerDialog = false
                    onNavigateToQrScanner()
                }) {
                    Text("Use Camera")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showScannerDialog = false
                    onNavigateToImageScanner()
                }) {
                    Text("From Picture")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("ZeroID") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Welcome to ZeroID",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                "Your private digital identity wallet.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Main Actions
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { showScannerDialog = true },
                    modifier = Modifier.weight(1f).height(100.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Scan Proof")
                    }
                }
                Button(
                    onClick = onNavigateToQrGenerator,
                    modifier = Modifier.weight(1f).height(100.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Create Request")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ZeroIDTheme {
        HomeScreen(
            onNavigateToQrScanner = {},
            onNavigateToImageScanner = {},
            onNavigateToQrGenerator = {}
        )
    }
}
