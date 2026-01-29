package com.zero.id.app.ui.screens.consent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zero.id.app.model.DataRequest
import com.zero.id.app.model.UserProfile

// A helper extension function to get data from UserProfile based on a claim string
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentScreen(
    dataRequest: DataRequest,
    userProfile: UserProfile,
    onConfirm: (Map<String, String?>) -> Unit,
    onCancel: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Data Sharing Request") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    dataRequest.claims.forEach { claim ->
                        val data = userProfile.getClaim(claim)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$claim: ${data ?: "(Not available)"}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = {
                    val requestedData = dataRequest.claims.associateWith { claim -> userProfile.getClaim(claim) }
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
