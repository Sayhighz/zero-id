package com.zero.id.app.ui.screens.proof

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zero.id.app.network.RetrofitClient
import com.zero.id.app.ui.screens.home.HomeViewModel
import com.zero.id.app.ui.theme.WalletBackground
import com.zero.id.app.ui.theme.WalletPrimary
import com.zero.id.app.ui.theme.WalletTextPrimary
import com.zero.id.app.ui.theme.WalletTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofDisplayScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResult: (Boolean, String) -> Unit,
    viewModel: HomeViewModel
) {
    val proofJson by viewModel.lastProofJson.collectAsState()
    val publicSignalsJson by viewModel.lastPublicSignalsJson.collectAsState()
    val verificationRequest by viewModel.lastVerificationRequest.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isVerifying by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ผลการสร้างหลักฐาน") },
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
        containerColor = WalletBackground,
        bottomBar = {
            if (proofJson != null && publicSignalsJson != null) {
                Box(modifier = Modifier.padding(24.dp)) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isVerifying = true
                                try {
                                    verificationRequest?.let { request ->
                                        val response = RetrofitClient.instance.verifyDirect(request)
                                        val isSuccess = response.isSuccessful && response.body()?.success == true
                                        val message = response.body()?.message ?: if (isSuccess) "Verification Passed" else "Verification Failed"
                                        onNavigateToResult(isSuccess, message)
                                    }
                                } catch (e: Exception) {
                                    onNavigateToResult(false, "Network Error: ${e.message}")
                                } finally {
                                    isVerifying = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WalletPrimary),
                        enabled = !isVerifying
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                        } else {
                            Text("ส่งข้อมูลเพื่อตรวจสอบ (Verify)", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (proofJson == null || publicSignalsJson == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = WalletPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("กำลังโหลดข้อมูลหลักฐาน...", color = WalletTextSecondary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(top = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Proof :",
                        color = WalletTextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = proofJson ?: "",
                        color = WalletTextPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Public :",
                        color = WalletTextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = publicSignalsJson ?: "",
                        color = WalletTextPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
