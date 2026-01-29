package com.zero.id.app.ui.screens.result

import android.content.Intent
import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.zero.id.app.model.UserProfile
import com.zero.id.app.ui.theme.ErrorRed
import com.zero.id.app.ui.theme.SuccessGreen
import com.zero.id.app.ui.theme.ZeroIDTheme
import com.zero.id.network.Details
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    isSuccess: Boolean,
    message: String,
    details: Details?,
    minAge: String?,
    birthYear: String?,
    userProfile: UserProfile?,
    onNavigateHome: () -> Unit,
    onRetry: () -> Unit
) {
    val context = LocalContext.current

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
            val currentYear = Calendar.getInstance().get(Calendar.YEAR).toString()
            val userAge = if (!birthYear.isNullOrEmpty()) {
                Calendar.getInstance().get(Calendar.YEAR) - birthYear.toInt()
            } else null

            Spacer(modifier = Modifier.height(24.dp))

            val isVerified = isSuccess && details?.isOldEnough == true
            ResultStatusHeader(isSuccess = isVerified)

            Spacer(modifier = Modifier.height(32.dp))

            if (isSuccess) {
                SuccessContent(
                    message = message,
                    details = details,
                    minAgeFallback = minAge,
                    currentYearFallback = currentYear,
                    userAge = userAge,
                    userProfile = userProfile,
                    onNavigateHome = onNavigateHome,
                    onShareClick = {
                        val contentBuilder = StringBuilder()
                        contentBuilder.append("--- Verification Result ---\n")
                        contentBuilder.append("Status: ${if (isVerified) "Verified" else "Not Verified"}\n")
                        contentBuilder.append("Minimum Required Age: ${details?.minAge ?: minAge ?: "N/A"}\n")
                        contentBuilder.append("Verification Year: ${details?.currentYear ?: currentYear}\n\n")

                        userProfile?.let {
                            contentBuilder.append("--- Shared Personal Information ---\n")
                            contentBuilder.append("Full Name: ${it.fullName}\n")
                            contentBuilder.append("Date of Birth: ${it.getFormattedBirthDate()}\n")
                            contentBuilder.append("Address: ${it.address}\n")
                            contentBuilder.append("ID Number: ${it.idNumber}\n")
                            contentBuilder.append("Phone Number: ${it.phoneNumber}\n")
                        }

                        val proofContent = contentBuilder.toString()
                        val proofFile = File(context.cacheDir, "proofs/proof_details.txt")
                        proofFile.parentFile?.mkdirs()
                        proofFile.writeText(proofContent)

                        val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", proofFile)

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "ZeroID Verification Details")
                            putExtra(Intent.EXTRA_TEXT, proofContent)
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        val chooserIntent = Intent.createChooser(shareIntent, "Share Proof Details")
                        val resInfoList = context.packageManager.queryIntentActivities(chooserIntent, 0)
                        for (resolveInfo in resInfoList) {
                            val packageName = resolveInfo.activityInfo.packageName
                            context.grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        context.startActivity(chooserIntent)
                    }
                )
            } else {
                ErrorContent(
                    message = message,
                    minAge = minAge,
                    userAge = userAge,
                    currentYear = currentYear,
                    onRetry = onRetry,
                    onNavigateHome = onNavigateHome
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ... (The rest of the file remains the same)
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
    message: String,
    details: Details?,
    minAgeFallback: String?,
    currentYearFallback: String,
    userAge: Int?,
    userProfile: UserProfile?,
    onNavigateHome: () -> Unit,
    onShareClick: () -> Unit
) {
    val isVerified = details?.isOldEnough == true

    Text(
        text = if (isVerified) "Your age is verified" else "Age requirement not met",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    if (userProfile != null) {
        UserProfileCard(userProfile = userProfile)
        Spacer(modifier = Modifier.height(16.dp))
    }

    VerificationDetailsCard(
        isVerified = isVerified,
        userAge = userAge,
        minAge = details?.minAge ?: minAgeFallback ?: "N/A",
        currentYear = details?.currentYear ?: currentYearFallback
    )

    Spacer(modifier = Modifier.height(40.dp))

    ActionButtons(
        onPrimaryClick = onNavigateHome,
        primaryText = "Go Home",
        primaryIcon = Icons.Default.Home,
        onSecondaryClick = onShareClick,
        secondaryText = "Share Proof",
        secondaryIcon = Icons.Default.Share
    )
}

@Composable
private fun UserProfileCard(userProfile: UserProfile) {
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
                text = "User Information",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            DetailItem(label = "Full Name", value = userProfile.fullName)
            DetailItem(label = "Date of Birth", value = userProfile.getFormattedBirthDate())
            DetailItem(label = "Address", value = userProfile.address)
            DetailItem(label = "ID Number", value = userProfile.idNumber)
            DetailItem(label = "Phone Number", value = userProfile.phoneNumber)
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    minAge: String?,
    userAge: Int?,
    currentYear: String,
    onRetry: () -> Unit,
    onNavigateHome: () -> Unit
) {
    Text(
        text = message.ifEmpty { "We couldn't verify your age" },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Show details even on error case if available
    VerificationDetailsCard(
        isVerified = false,
        userAge = userAge,
        minAge = minAge ?: "N/A",
        currentYear = currentYear
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
private fun VerificationDetailsCard(
    isVerified: Boolean,
    userAge: Int?,
    minAge: String,
    currentYear: String
) {
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
                text = "Verification Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            DetailItem(label = "Status", value = if (isVerified) "Verified" else "Not Verified", color = if (isVerified) SuccessGreen else ErrorRed)
            DetailItem(label = "Your Age", value = userAge?.toString() ?: "N/A")
            DetailItem(label = "Minimum Required", value = minAge)
            DetailItem(label = "Year", value = currentYear)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "This proof is generated using Zero-Knowledge Proof technology. Your exact birth year remains private.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
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
    onSecondaryClick: () -> Unit,
    secondaryText: String,
    secondaryIcon: ImageVector
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

@Preview(showBackground = true)
@Composable
fun ResultScreenSuccessPreview() {
    ZeroIDTheme {
        ResultScreen(
            isSuccess = true,
            message = "Age verification successful",
            details = Details(isOldEnough = true, minAge = "18", currentYear = "2025"),
            minAge = "18",
            birthYear = "2005",
            userProfile = UserProfile(),
            onNavigateHome = {},
            onRetry = {}
        )
    }
}
