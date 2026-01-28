package com.zero.id.app.ui.screens.proof

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.zero.id.app.model.UserProfile
import com.zero.id.app.ui.theme.ZeroIDTheme
import com.zero.id.network.RetrofitClient
import com.zero.id.network.VerificationRequest
import kotlinx.coroutines.launch

/**
 * Proof generation screen
 * Input form for user data requested by the agency
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofGenerationScreen(
    onNavigateBack: () -> Unit,
    onVerificationRequest: (VerificationRequest, String?) -> Unit,
    viewModel: ProofGenerationViewModel = viewModel(),
    requestJson: String
) {
    val state by viewModel.state.collectAsState()
    val birthYear by viewModel.birthYear.collectAsState()
    val requestedFields = remember {
        try {
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            Gson().fromJson<Map<String, Any>>(requestJson, type)
        } catch (e: Exception) {
            emptyMap<String, Any>()
        }
    }
    val sessionId = requestedFields["sessionId"] as? String
    val minAge = requestedFields["minAge"] as? String
    val coroutineScope = rememberCoroutineScope()

    // Handle state changes
    LaunchedEffect(state) {
        when (val currentState = state) {
            is ProofGenerationState.Success -> {
                if (sessionId != null) {
                    coroutineScope.launch {
                        val userProfile = UserProfile(
                            fullName = "Nonthee Panatuek", // Placeholder
                            birthYear = birthYear.toInt(),
                            phoneNumber = "090-000-0000" // Placeholder
                        )
                        RetrofitClient.instance.submitUserData(sessionId, userProfile)
                    }
                }
                onVerificationRequest(currentState.verificationRequest, minAge)
                viewModel.resetState()
            }
            is ProofGenerationState.Error -> {
                // Error will be shown in the UI, don't navigate
            }
            else -> {}
        }
    }

    LaunchedEffect(requestJson) {
        viewModel.updateMinAge(minAge ?: "")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Proof") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is ProofGenerationState.Loading -> {
                    LoadingContent()
                }
                else -> {
                    InputFormContent(
                        birthYear = birthYear,
                        requestedFields = requestedFields,
                        onBirthYearChange = viewModel::updateBirthYear,
                        onGenerateProof = viewModel::generateProof,
                        errorMessage = (state as? ProofGenerationState.Error)?.message,
                        isLoading = state is ProofGenerationState.Loading
                    )
                }
            }
        }
    }
}

/**
 * Loading state content
 */
@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Generating zero-knowledge proof...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "This may take a few seconds",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

/**
 * Input form content
 */
@Composable
private fun InputFormContent(
    birthYear: String,
    requestedFields: Map<String, Any>,
    onBirthYearChange: (String) -> Unit,
    onGenerateProof: () -> Unit,
    errorMessage: String?,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "The agency has requested the following information:",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // User Input Fields
        OutlinedTextField(
            value = birthYear,
            onValueChange = onBirthYearChange,
            label = { Text("Birth Year") },
            placeholder = { Text("e.g., 1990") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        requestedFields.forEach { (field, value) ->
            if (field != "minAge" && field != "sessionId" && field != "type") {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text(field) },
                    placeholder = { Text("e.g., $value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Privacy Notice Card
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Privacy Notice",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "The proof is generated entirely on your device. Your personal information never leaves your device and cannot be derived from the proof.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Error message
        if (errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // Generate Button
        Button(
            onClick = onGenerateProof,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading && birthYear.isNotEmpty()
        ) {
            Text(
                text = "Generate Proof",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun ProofGenerationScreenPreview() {
    ZeroIDTheme {
        ProofGenerationScreen(
            onNavigateBack = {},
            onVerificationRequest = { _, _ -> },
            requestJson = "{\"minAge\": 18, \"city\": \"New York\"}"
        )
    }
}
