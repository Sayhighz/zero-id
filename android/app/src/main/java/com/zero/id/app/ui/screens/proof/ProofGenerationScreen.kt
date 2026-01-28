package com.zero.id.app.ui.screens.proof

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.gson.Gson
import com.zero.id.app.ui.theme.ZeroIDTheme
import com.zero.id.network.VerificationRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofGenerationScreen(
    onNavigateBack: () -> Unit,
    onVerificationRequest: (VerificationRequest, String?) -> Unit,
    viewModel: ProofGenerationViewModel = viewModel(),
    requestJson: String
) {
    val zkpState by viewModel.zkpState.collectAsState()
    val proofState by viewModel.proofState.collectAsState()
    val birthYear by viewModel.birthYear.collectAsState()
    val context = LocalContext.current
    val webView = remember { WebView(context) }

    val requestedFields = remember {
        try {
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            Gson().fromJson<Map<String, Any>>(requestJson, type)
        } catch (e: Exception) {
            emptyMap<String, Any>()
        }
    }
    val minAge = remember {
        when (val age = requestedFields["minAge"]) {
            is Double -> age.toInt().toString()
            is String -> age
            else -> null
        }
    }

    LaunchedEffect(webView) {
        viewModel.initializeZkp(webView)
    }

    DisposableEffect(webView) {
        onDispose {
            webView.destroy()
        }
    }

    LaunchedEffect(proofState) {
        if (proofState is ProofGenerationState.Success) {
            onVerificationRequest((proofState as ProofGenerationState.Success).verificationRequest, minAge)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Proof") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
            AndroidView(factory = { webView }, modifier = Modifier.size(0.dp))

            when (zkpState) {
                is ZkpState.Initializing -> {
                    CircularProgressIndicator()
                    Text("Initializing proof generator...")
                }
                is ZkpState.Error -> {
                    Text((zkpState as ZkpState.Error).message, color = MaterialTheme.colorScheme.error)
                }
                is ZkpState.Initialized -> {
                    OutlinedTextField(
                        value = birthYear,
                        onValueChange = { viewModel.birthYear.value = it },
                        label = { Text("Birth Year") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Button(
                        onClick = { minAge?.let { viewModel.generateProof(it) } },
                        enabled = minAge != null
                    ) {
                        Text("Generate Proof")
                    }
                }
                else -> {}
            }

            if (proofState is ProofGenerationState.Loading) {
                CircularProgressIndicator()
                Text("Generating proof...")
            }

            if (proofState is ProofGenerationState.Error) {
                Text((proofState as ProofGenerationState.Error).message, color = MaterialTheme.colorScheme.error)
            }
        }
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
