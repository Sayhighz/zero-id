package com.zero.id.app.ui.screens.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.zero.id.app.network.ChallengeResponse
import com.zero.id.app.security.ProfileStorage
import com.zero.id.app.ui.screens.proof.JsonCodeBlock
import com.zero.id.app.ui.theme.WalletBackground
import com.zero.id.app.ui.theme.WalletPrimary
import com.zero.id.app.ui.theme.WalletSurface
import com.zero.id.app.ui.theme.WalletTextPrimary
import com.zero.id.app.ui.theme.WalletTextSecondary
import java.io.IOException

@Composable
fun HomeScreen(
    onNavigateToProofGeneration: () -> Unit,
    onNavigateToQrScanner: () -> Unit,
    onVerifyFromJson: (String) -> Unit,
    onNavigateToFaceScan: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val userProfile = ProfileStorage(context).getProfile()
    var showQrSourceDialog by remember { mutableStateOf(false) }
    var showVerifyOptionsDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val challengeResponse by viewModel.challengeResponse.collectAsState()
    val lastProofJson by viewModel.lastProofJson.collectAsState()
    val lastPublicSignalsJson by viewModel.lastPublicSignalsJson.collectAsState()
    val lastVerificationRequest by viewModel.lastVerificationRequest.collectAsState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                try {
                    val inputImage = InputImage.fromFilePath(context, uri)
                    val options = BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build()
                    val scanner = BarcodeScanning.getClient(options)
                    scanner.process(inputImage)
                        .addOnSuccessListener { barcodes ->
                            barcodes.firstOrNull()?.rawValue?.let { scannedContent ->
                                if (scannedContent.startsWith("http")) {
                                    viewModel.fetchChallengeFromUrl(scannedContent)
                                } else {
                                    onVerifyFromJson(scannedContent)
                                }
                            }
                        }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WalletBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigateToFaceScan() }
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WalletPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ZeroID",
                        color = WalletTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "กระเป๋าตัวตนดิจิทัล",
                        color = WalletTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier.background(WalletSurface, CircleShape)
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = WalletTextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Last Proof Section
        AnimatedVisibility(
            visible = lastProofJson != null,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "หลักฐานล่าสุดที่สร้าง",
                        color = WalletPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.clearLastProof() }) {
                        Text("ล้างข้อมูล", color = WalletTextSecondary, fontSize = 12.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(WalletSurface)
                        .padding(16.dp)
                ) {
                    JsonCodeBlock(
                        title = "Proof JSON",
                        json = lastProofJson ?: "",
                        onCopy = { /* Handled in JsonCodeBlock */ }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    JsonCodeBlock(
                        title = "Public Signals",
                        json = lastPublicSignalsJson ?: "",
                        onCopy = { /* Handled in JsonCodeBlock */ }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { lastVerificationRequest?.let { onVerifyFromJson(com.google.gson.Gson().toJson(it)) } },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WalletPrimary)
                    ) {
                        Text("ส่งยืนยันอีกครั้ง", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Status
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "สถานะกระเป๋า", color = WalletTextSecondary, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(WalletPrimary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "ใช้งานได้", color = WalletPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ID Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(WalletSurface)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = WalletTextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Samsung Knox", color = WalletTextSecondary, fontSize = 10.sp)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = WalletTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "บัตรประชาชน", color = WalletTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Thai National ID Card", color = WalletTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(WalletPrimary))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "ยืนยันตัวตนแล้ว", color = WalletPrimary, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Card Info Fields
                CardInfoField(label = "เลขประจำตัวประชาชน", value = "1 XXXX XXXXX XX X")
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        CardInfoField(label = "ชื่อ-นามสกุล", value = "นาย ●●●●●●")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        CardInfoField(label = "วันหมดอายุ", value = "XX/XX/XXXX")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = WalletPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Blockchain Verified", color = WalletTextSecondary, fontSize = 10.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, WalletPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, tint = WalletPrimary, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuickActionButton(icon = Icons.Default.Description, label = "เอกสาร")
            QuickActionButton(icon = Icons.Default.History, label = "ประวัติ")
            QuickActionButton(icon = Icons.Default.Settings, label = "ตั้งค่า")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Main Action Button
        Button(
            onClick = { showVerifyOptionsDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(WalletPrimary, Color(0xFF2DDA9E))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "ยืนยันตัวตน",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text(
            text = "• ใช้งานง่าย ปลอดภัย และรวดเร็ว •",
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            textAlign = TextAlign.Center,
            color = WalletTextSecondary,
            fontSize = 12.sp
        )
    }

    if (showVerifyOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showVerifyOptionsDialog = false },
            title = { Text("เลือกวิธีรับข้อมูล", color = WalletTextPrimary) },
            text = { Text("คุณต้องการรับข้อกำหนดจากระบบ หรือสแกน QR Code เพื่อรับข้อกำหนด?", color = WalletTextSecondary) },
            containerColor = WalletSurface,
            confirmButton = {
                TextButton(
                    onClick = {
                        showVerifyOptionsDialog = false
                        viewModel.generateChallenge()
                    }
                ) {
                    Text("ระบบ (API)", color = WalletPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showVerifyOptionsDialog = false
                        showQrSourceDialog = true
                    }
                ) {
                    Text("สแกน QR Code", color = WalletPrimary)
                }
            }
        )
    }

    if (showQrSourceDialog) {
        AlertDialog(
            onDismissRequest = { showQrSourceDialog = false },
            title = { Text("เลือกช่องทางสแกน QR Code", color = WalletTextPrimary) },
            text = { Text("คุณต้องการใช้กล้องสแกน หรือเลือกรูปภาพจากแกลเลอรี?", color = WalletTextSecondary) },
            containerColor = WalletSurface,
            confirmButton = {
                TextButton(
                    onClick = {
                        showQrSourceDialog = false
                        onNavigateToQrScanner()
                    }
                ) {
                    Text("ใช้กล้อง", color = WalletPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showQrSourceDialog = false
                        pickImageLauncher.launch("image/*")
                    }
                ) {
                    Text("เลือกจากรูปภาพ", color = WalletPrimary)
                }
            }
        )
    }

    challengeResponse?.let {
        ChallengeDetailsDialog(
            challenge = it, 
            onDismiss = { viewModel.clearChallenge() },
            onConfirm = {
                // Do NOT clear challenge here, we need it in the next screen
                onNavigateToProofGeneration()
            }
        )
    }
}

@Composable
fun ChallengeDetailsDialog(challenge: ChallengeResponse, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(challenge.verifierName, color = WalletTextPrimary) },
        text = {
            Column {
                Text("ต้องการข้อมูลดังนี้:", color = WalletTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(" - อายุขั้นต่ำ: ${challenge.minAge}")
                Text(" - เงินเดือนขั้นต่ำ: ${challenge.minSalary}")
                Text(" - ปีปัจจุบัน: ${challenge.currentYear}")
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("สร้าง Proof", color = WalletPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ยกเลิก", color = WalletTextSecondary)
            }
        },
        containerColor = WalletSurface
    )
}


@Composable
fun CardInfoField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(12.dp)
    ) {
        Text(text = label, color = WalletTextSecondary, fontSize = 10.sp)
        Text(text = value, color = WalletTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun QuickActionButton(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(WalletSurface)
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = WalletPrimary, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = WalletTextSecondary, fontSize = 13.sp)
    }
}
