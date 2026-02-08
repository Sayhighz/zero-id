package com.zero.id.app.ui.screens.proof

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zero.id.app.network.VerificationRequest
import com.zero.id.app.security.ProfileStorage
import com.zero.id.app.ui.theme.WalletBackground
import com.zero.id.app.ui.theme.WalletPrimary
import com.zero.id.app.ui.theme.WalletSurface
import com.zero.id.app.ui.theme.WalletTextPrimary
import com.zero.id.app.ui.theme.WalletTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofGenerationScreen(
    onNavigateBack: () -> Unit,
    onVerificationRequest: (VerificationRequest) -> Unit,
    viewModel: ProofGenerationViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val userProfile = remember { ProfileStorage(context).getProfile() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("สร้างหลักฐาน (ZK Proof)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WalletBackground,
                    titleContentColor = WalletTextPrimary,
                    navigationIconContentColor = WalletTextPrimary
                )
            )
        },
        containerColor = WalletBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is ProofGenerationState.Loading -> {
                    LoadingContent()
                }
                is ProofGenerationState.Success -> {
                    ProofSuccessContent(
                        proofJson = currentState.proofJson,
                        publicSignalsJson = currentState.publicSignalsJson,
                        onVerify = { onVerificationRequest(currentState.verificationRequest) }
                    )
                }
                else -> {
                    InputFormContent(
                        birthYear = userProfile.birthYear.toString(),
                        minAge = "", // Auto-filled from verifier
                        minSalary = "", // Auto-filled from verifier
                        onBirthYearChange = {},
                        onMinAgeChange = {},
                        onMinSalaryChange = {},
                        onGenerateProof = { viewModel.generateProof(userProfile.birthYear, userProfile.salary) },
                        errorMessage = (state as? ProofGenerationState.Error)?.message,
                        isLoading = state is ProofGenerationState.Loading
                    )
                }
            }
        }
    }
}

@Composable
private fun ProofSuccessContent(
    proofJson: String,
    publicSignalsJson: String,
    onVerify: () -> Unit
) {
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "สร้างหลักฐานสำเร็จ!",
            color = WalletPrimary,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        JsonCodeBlock(
            title = "Public Signals (public.json)",
            json = publicSignalsJson,
            onCopy = { clipboardManager.setText(AnnotatedString(publicSignalsJson)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        JsonCodeBlock(
            title = "Proof (proof.json)",
            json = proofJson,
            onCopy = { clipboardManager.setText(AnnotatedString(proofJson)) }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onVerify,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WalletPrimary)
        ) {
            Text("ส่งให้ผู้ตรวจสอบ (Verify)", color = Color.Black, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun JsonCodeBlock(title: String, json: String, onCopy: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, color = WalletTextSecondary, fontSize = 14.sp)
            IconButton(onClick = onCopy) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = WalletPrimary, modifier = Modifier.size(20.dp))
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(WalletSurface)
                .padding(16.dp)
        ) {
            Text(
                text = json,
                color = WalletTextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp), color = WalletPrimary)
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "กำลังคำนวณหลักฐาน (ZK Proof)...", color = WalletTextPrimary)
        Text(text = "ขั้นตอนนี้ทำบนมือถือของคุณเท่านั้น", color = WalletTextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun InputFormContent(
    birthYear: String,
    minAge: String,
    minSalary: String,
    onBirthYearChange: (String) -> Unit,
    onMinAgeChange: (String) -> Unit,
    onMinSalaryChange: (String) -> Unit,
    onGenerateProof: () -> Unit,
    errorMessage: String?,
    isLoading: Boolean
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ระบุข้อมูลเพื่อสร้างหลักฐาน ZK",
            color = WalletTextPrimary,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = birthYear,
            onValueChange = onBirthYearChange,
            label = { Text("ปีเกิด (ค.ศ.)") },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = WalletTextPrimary,
                focusedTextColor = WalletTextPrimary,
                unfocusedBorderColor = WalletSurface,
                focusedBorderColor = WalletPrimary
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            readOnly = true // ดึงจาก Profile อัตโนมัติ
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = minAge,
            onValueChange = onMinAgeChange,
            label = { Text("อายุขั้นต่ำที่ต้องการพิสูจน์") },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = WalletTextPrimary,
                focusedTextColor = WalletTextPrimary,
                unfocusedBorderColor = WalletSurface,
                focusedBorderColor = WalletPrimary
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = minSalary,
            onValueChange = onMinSalaryChange,
            label = { Text("รายได้ขั้นต่ำที่ต้องการพิสูจน์") },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = WalletTextPrimary,
                focusedTextColor = WalletTextPrimary,
                unfocusedBorderColor = WalletSurface,
                focusedBorderColor = WalletPrimary
            ),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = false
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onGenerateProof,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = WalletPrimary),
            enabled = !isLoading
        ) {
            Text(text = "สร้างหลักฐาน", color = Color.Black, fontSize = 18.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        }
    }
}
