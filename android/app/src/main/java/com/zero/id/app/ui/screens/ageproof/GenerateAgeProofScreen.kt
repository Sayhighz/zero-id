package com.zero.id.app.ui.screens.ageproof

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.zero.id.app.ui.screens.proof.ProofGenerationViewModel
import com.zero.id.network.VerificationRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateAgeProofScreen(
    viewModel: ProofGenerationViewModel,
    onNavigateBack: () -> Unit,
    onProofGenerated: (String) -> Unit
) {
    var minAge by remember { mutableStateOf("") }
    val proof by viewModel.proof.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()

    // When proof is generated, navigate
    LaunchedEffect(proof) {
        proof?.let {
            // Reset proof in ViewModel to avoid re-triggering
            viewModel.resetProof()
            onProofGenerated(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate Age Proof") },
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
            OutlinedTextField(
                value = minAge,
                onValueChange = { minAge = it.filter { c -> c.isDigit() } },
                label = { Text("Minimum Age Requirement") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    minAge.toIntOrNull()?.let {
                        viewModel.generateAgeProof(it)
                    }
                },
                enabled = !isGenerating && minAge.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Generate Proof QR Code")
                }
            }
        }
    }
}
