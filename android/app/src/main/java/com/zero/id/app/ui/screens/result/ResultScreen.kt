package com.zero.id.app.ui.screens.result

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zero.id.app.model.UserProfile
import com.zero.id.app.network.VerificationDetails
import com.zero.id.app.ui.theme.ErrorRed
import com.zero.id.app.ui.theme.SuccessGreen
import com.zero.id.app.ui.theme.ZeroIDTheme
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    isSuccess: Boolean,
    message: String,
    verificationDetails: VerificationDetails?,
    userProfile: UserProfile? = null,
    onNavigateHome: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Verification Result",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            ResultStatusHeader(isSuccess = isSuccess)

            Spacer(modifier = Modifier.height(32.dp))

            if (isSuccess) {
                SuccessContent(
                    verificationDetails = verificationDetails,
                    userProfile = userProfile,
                    onNavigateHome = onNavigateHome
                )
            } else {
                ErrorContent(
                    message = message,
                    onRetry = onRetry,
                    onNavigateHome = onNavigateHome
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ResultStatusHeader(isSuccess: Boolean) {
    val backgroundColor = if (isSuccess) SuccessGreen else ErrorRed
    val icon = if (isSuccess) Icons.Default.Check else Icons.Default.Close
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = 0.1f))
            .padding(16.dp)
            .background(backgroundColor, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.White
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = if (isSuccess) "Success!" else "Failed",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = backgroundColor
        )
    )
}

@Composable
private fun SuccessContent(
    verificationDetails: VerificationDetails?,
    userProfile: UserProfile?,
    onNavigateHome: () -> Unit
) {
    Text(
        text = "Verification successful",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))
    
    if (verificationDetails != null) {
        VerificationDetailsCard(verificationDetails = verificationDetails)
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (userProfile != null) {
        UserProfileDetailsCard(userProfile = userProfile)
        Spacer(modifier = Modifier.height(16.dp))
    }

    Spacer(modifier = Modifier.height(24.dp))

    ActionButtons(
        onPrimaryClick = onNavigateHome,
        primaryText = "Go Home",
        primaryIcon = Icons.Default.Home,
    )
}

@Composable
private fun VerificationDetailsCard(verificationDetails: VerificationDetails) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Verification Proof",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            DetailItem(label = "Is Old Enough", value = verificationDetails.isOldEnough.toString(), color = if (verificationDetails.isOldEnough) SuccessGreen else ErrorRed)
            DetailItem(label = "Minimum Age", value = verificationDetails.minAge)
            DetailItem(label = "Current Year", value = verificationDetails.currentYear)
        }
    }
}

@Composable
private fun UserProfileDetailsCard(userProfile: UserProfile) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("th", "TH"))
    
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "User Information",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            DetailItem(label = "Full Name", value = userProfile.fullName)
            DetailItem(
                label = "Salary", 
                value = "${NumberFormat.getNumberInstance().format(userProfile.salary)} THB",
                color = MaterialTheme.colorScheme.primary
            )
            DetailItem(label = "Phone", value = userProfile.phoneNumber)
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onNavigateHome: () -> Unit
) {
    Text(
        text = message.ifEmpty { "We couldn't verify your proof" },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "What happened?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "The system encountered an error while processing the zero-knowledge proof. This could be due to a network issue or an invalid QR code.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))

    ActionButtons(
        onPrimaryClick = onRetry,
        primaryText = "Try Again",
        primaryIcon = Icons.Default.Refresh,
        onSecondaryClick = onNavigateHome,
        secondaryText = "Cancel",
        secondaryIcon = Icons.Default.Home
    )
}

@Composable
private fun DetailItem(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}

@Composable
private fun ActionButtons(
    onPrimaryClick: () -> Unit,
    primaryText: String,
    primaryIcon: ImageVector,
    onSecondaryClick: (() -> Unit)? = null,
    secondaryText: String? = null,
    secondaryIcon: ImageVector? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onPrimaryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(primaryIcon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(primaryText, style = MaterialTheme.typography.titleMedium)
        }

        if (onSecondaryClick != null && secondaryText != null && secondaryIcon != null) {
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onSecondaryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(secondaryIcon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(secondaryText, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultScreenSuccessPreview() {
    ZeroIDTheme {
        ResultScreen(
            isSuccess = true,
            message = "Verification successful",
            verificationDetails = VerificationDetails(isOldEnough = true, minAge = "18", currentYear = "2023"),
            userProfile = UserProfile(),
            onNavigateHome = {},
            onRetry = {}
        )
    }
}
