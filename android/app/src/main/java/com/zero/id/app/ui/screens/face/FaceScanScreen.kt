package com.zero.id.app.ui.screens.face

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.zero.id.app.ui.theme.WalletBackground
import com.zero.id.app.ui.theme.WalletTextSecondary
import com.zero.id.app.ui.theme.ZeroIDTheme
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FaceScanScreen(
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onUseFingerprint: () -> Unit,
    onUsePassword: () -> Unit
) {
    val cyanColor = Color(0xFF00C2E0)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(cameraPermissionState) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WalletBackground)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            IconButton(onClick = { /* More options */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Titles
        Text(
            text = "สแกนหน้าเพื่อปลดล็อก",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Knox Vault",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "วางใบหน้าของคุณให้อยู่ในกรอบ",
            color = WalletTextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Face Scan Area
        Box(
            modifier = Modifier
                .size(280.dp),
            contentAlignment = Alignment.Center
        ) {
            if (cameraPermissionState.status.isGranted) {
                AndroidView(
                    factory = {
                        val previewView = PreviewView(it)
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = CameraPreview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                        val imageAnalysis = ImageAnalysis.Builder().build()

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            // handle exception
                        }
                        previewView
                    },
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .border(2.dp, cyanColor, CircleShape)
                )
            } else {
                // Placeholder when no permission
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f))
                        .border(2.dp, cyanColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = cyanColor.copy(alpha = 0.4f)
                    )
                }
            }

            // Decorative elements
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = cyanColor.copy(alpha = 0.1f),
                    radius = size.minDimension / 2,
                    style = Stroke(width = 1.dp.toPx())
                )
                drawCircle(
                    color = cyanColor.copy(alpha = 0.05f),
                    radius = size.minDimension / 2 + 20.dp.toPx(),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            Box(modifier = Modifier.fillMaxSize().padding(30.dp)) {
                Text("⌜", color = cyanColor, fontSize = 40.sp, modifier = Modifier.align(Alignment.TopStart))
                Text("⌝", color = cyanColor, fontSize = 40.sp, modifier = Modifier.align(Alignment.TopEnd))
                Text("⌞", color = cyanColor, fontSize = 40.sp, modifier = Modifier.align(Alignment.BottomStart))
                Text("⌟", color = cyanColor, fontSize = 40.sp, modifier = Modifier.align(Alignment.BottomEnd))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Page Indicators
        Row {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (index == 1) cyanColor else Color.Gray.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Verified text
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = cyanColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ตรวจสอบโดย KBTG AINU",
                color = WalletTextSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tip Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF004D61)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = cyanColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "เคล็ดลับ",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ตรวจสอบให้แน่ใจว่าใบหน้าของคุณได้รับแสงสว่างเพียงพอและอยู่ในกรอบ",
                            color = WalletTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Retry Button
                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = cyanColor)
                ) {
                    Text(
                        text = "ลองอีกครั้ง",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomOptionItem(
                icon = Icons.Default.Fingerprint,
                label = "ใช้ลายนิ้วมือ",
                onClick = onUseFingerprint
            )
            BottomOptionItem(
                icon = Icons.Default.VpnKey,
                label = "ใช้รหัสผ่าน",
                onClick = onUsePassword
            )
        }
    }
}

@Composable
fun BottomOptionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = WalletTextSecondary, modifier = Modifier.size(32.dp))
        }
        Text(text = label, color = WalletTextSecondary, fontSize = 12.sp)
    }
}

@Preview
@Composable
fun FaceScanScreenPreview() {
    ZeroIDTheme {
        FaceScanScreen({}, {}, {}, {})
    }
}
