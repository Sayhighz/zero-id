package com.zero.id.app.ui.screens.qr

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun QRScannerScreen(onQrCodeScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            Box(modifier = Modifier.fillMaxSize()) {
                val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
                val cameraProvider = remember(cameraProviderFuture) { cameraProviderFuture.get() }
                val previewView = remember { PreviewView(context) }
                val imageAnalysis = remember {
                    ImageAnalysis.Builder()
                        .build()
                        .also {
                            it.setAnalyzer(Executors.newSingleThreadExecutor(), QrCodeAnalyzer {
                                onQrCodeScanned(it)
                                cameraProvider.unbindAll()
                            })
                        }
                }

                AndroidView(
                    factory = { previewView },
                    modifier = Modifier.fillMaxSize(),
                    update = {
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                )

                Text(
                    text = "กรุณาจัด QR code ให้อยู่ในกรอบ",
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Canvas(modifier = Modifier.fillMaxSize()) {                    val rect = Rect(
                        center = center,
                        radius = size.minDimension / 2 * 0.8f
                    )

                    val path = Path().apply {
                        addRect(rect)
                    }

                    drawPath(
                        path = path,
                        color = Color.White,
                        style = Stroke(width = 4.dp.toPx())
                    )
                }
            }
        } else {
            Text("Camera permission is required to scan QR codes.", modifier = Modifier.padding(16.dp))
        }
    }
}

private class QrCodeAnalyzer(private val onQrCodeScanned: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()
    private val scanner = BarcodeScanning.getClient(options)
    private var isScanning = true

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        isScanning = false
                        barcodes.first().rawValue?.let(onQrCodeScanned)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("QrCodeAnalyzer", "Error scanning QR code", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}
