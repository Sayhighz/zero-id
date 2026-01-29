package com.zero.id.app.ui.screens.consent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zero.id.app.model.DataRequest
import com.zero.id.app.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentScreen(
    dataRequest: DataRequest,
    userProfile: UserProfile,
    onConfirm: (Map<String, String>) -> Unit,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Data Sharing Request") }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${dataRequest.requester} requests the following information:",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Purpose: ${dataRequest.purpose}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    dataRequest.claims.forEach {
                        val data = userProfile.getClaim(it)
                        if (data != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("$it: $data")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = {
                    val requestedData = dataRequest.claims.associateWith { userProfile.getClaim(it) ?: "" }
                    onConfirm(requestedData)
                }) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Confirm")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm")
                }
                Button(onClick = onCancel, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancel")
                }
            }
        }
    }
}

fun UserProfile.getClaim(claim: String): String? {
    return when (claim) {
        "fullName" -> fullName
        "birthDate" -> getFormattedBirthDate()
        "address" -> address
        "idNumber" -> idNumber
        "phoneNumber" -> phoneNumber
        else -> null
    }
}

