package com.zero.id.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zero.id.app.ui.screens.ageproof.GenerateAgeProofScreen
import com.zero.id.app.ui.theme.ZeroIDTheme

/**
 * Home screen of ZeroID app
 * Entry point showing app branding and main action
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProofGeneration: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onNavigateToQrGenerator: () -> Unit,
    onVerifyFromJson: (String) -> Unit
) {
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
                text = "Prove your identity securely",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Primary action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GenerateAgeProofScreen(
                        onNavigateToProofGeneration = onNavigateToProofGeneration,
                        onNavigateToQrScanner = onNavigateToQrScanner,
                        onVerifyFromJson = onVerifyFromJson
                    )
                }
                
                Button(
                    onClick = onNavigateToQrGenerator,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create QR")
                }
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
                        text = "Features",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InfoPoint(
                        number = "1",
                        text = "Generate ZK Proofs for age verification"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InfoPoint(
                        number = "2",
                        text = "Scan QR codes to verify others"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InfoPoint(
                        number = "3",
                        text = "Create your own QR codes with personal info"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Privacy first, always.",
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
            onNavigateToQrGenerator = {},
            onVerifyFromJson = {}
        )
    }
}
