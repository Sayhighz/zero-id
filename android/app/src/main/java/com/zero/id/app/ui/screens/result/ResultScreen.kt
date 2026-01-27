package com.zero.id.app.ui.screens.result

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zero.id.app.ui.theme.ErrorRed
import com.zero.id.app.ui.theme.SuccessGreen
import com.zero.id.app.ui.theme.ZeroIDTheme
import com.zero.id.network.Details

/**
 * Result screen showing proof generation outcome
 * Displays success or error state with appropriate actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    isSuccess: Boolean,
    message: String,
    details: Details?,
    onNavigateHome: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Result") },
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
            if (isSuccess) {
                SuccessContent(
                    message = message,
                    details = details,
                    onNavigateHome = onNavigateHome
                )
            } else {
                ErrorContent(
                    message = message,
                    onRetry = onRetry,
                    onNavigateHome = onNavigateHome
                )
            }
        }
    }
}

/**
 * Success state content
 */
@Composable
private fun SuccessContent(
    message: String,
    details: Details?,
    onNavigateHome: () -> Unit
) {
    // Success icon
    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = "Success",
        modifier = Modifier.size(120.dp),
        tint = SuccessGreen
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Success headline
    Text(
        text = if (details?.isOldEnough == true) "Verification Successful" else "Verification Failed",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Message
    if (message.isNotEmpty()) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Result details card
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (details?.isOldEnough == true) SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Proof Details",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            ResultDetailRow(
                label = "Status",
                value = if (details?.isOldEnough == true) "Verified ✓" else "Not Verified ✗",
                valueColor = if (details?.isOldEnough == true) SuccessGreen else ErrorRed
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (details != null) {
                ResultDetailRow(label = "Minimum Age", value = details.minAge)
                Spacer(modifier = Modifier.height(8.dp))

                ResultDetailRow(label = "Verification Year", value = details.currentYear)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Divider()

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your proof is cryptographically secure and can be verified by anyone without revealing your birth year.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Back to Home button
    Button(
        onClick = onNavigateHome,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = "Back to Home",
            style = MaterialTheme.typography.titleMedium
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Share Proof button (placeholder)
    OutlinedButton(
        onClick = { /* TODO: Implement share functionality */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = "Share Proof",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * Error state content
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onNavigateHome: () -> Unit
) {
    // Error icon
    Icon(
        imageVector = Icons.Default.Error,
        contentDescription = "Error",
        modifier = Modifier.size(120.dp),
        tint = ErrorRed
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Error headline
    Text(
        text = "Proof Generation Failed",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Error message
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = message.ifEmpty { "An unexpected error occurred" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Troubleshooting tips
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Troubleshooting Tips",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "• Check your internet connection\n" +
                        "• Ensure you entered valid birth year (1900-2025)\n" +
                        "• Ensure minimum age is between 0-150\n" +
                        "• Try again in a few moments",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Retry button
    Button(
        onClick = onRetry,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = "Try Again",
            style = MaterialTheme.typography.titleMedium
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Back to Home button
    OutlinedButton(
        onClick = onNavigateHome,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = "Back to Home",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * Helper composable for result detail rows
 */
@Composable
private fun ResultDetailRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResultScreenSuccessPreview() {
    ZeroIDTheme {
        ResultScreen(
            isSuccess = true,
            message = "Age verification successful",
            details = Details(isOldEnough = true, minAge = "20", currentYear = "2025"),
            onNavigateHome = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResultScreenErrorPreview() {
    ZeroIDTheme {
        ResultScreen(
            isSuccess = false,
            message = "Network connection error",
            details = null,
            onNavigateHome = {},
            onRetry = {}
        )
    }
}
