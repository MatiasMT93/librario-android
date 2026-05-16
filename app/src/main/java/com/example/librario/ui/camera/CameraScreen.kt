package com.example.librario.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    onCodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- NUEVO: SEMÁFORO PARA EVITAR DOBLE ESCANEO ---
    var isProcessing by remember { mutableStateOf(false) }

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

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({
                        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { code ->
                                    // --- LÓGICA DE BLOQUEO ---
                                    // Solo procesamos si NO estamos procesando ya
                                    if (!isProcessing) {
                                        isProcessing = true // Bloqueamos futuros escaneos inmediatamente

                                        // Corremos en el hilo principal para interactuar con la UI
                                        ContextCompat.getMainExecutor(ctx).execute {
                                            onCodeScanned(code)
                                        }
                                    }
                                })
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (exc: Exception) {
                            exc.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            ScannerOverlay()

            Text(
                text = if (isProcessing) "¡Código detectado!" else "Apunta al código de barras",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
            )
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Se necesita permiso de cámara para escanear.")
        }
    }
}

// ... (El resto del código ScannerOverlay dejalo igual que antes) ...
@Composable
fun ScannerOverlay(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "scanLine"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val scanBoxWidth = canvasWidth * 0.85f
        val scanBoxHeight = canvasWidth * 0.5f
        val left = (canvasWidth - scanBoxWidth) / 2
        val top = (canvasHeight - scanBoxHeight) / 2
        val right = left + scanBoxWidth
        val bottom = top + scanBoxHeight

        drawRect(Color.Black.copy(alpha = 0.6f), Offset(0f, 0f), androidx.compose.ui.geometry.Size(canvasWidth, top))
        drawRect(Color.Black.copy(alpha = 0.6f), Offset(0f, bottom), androidx.compose.ui.geometry.Size(canvasWidth, canvasHeight - bottom))
        drawRect(Color.Black.copy(alpha = 0.6f), Offset(0f, top), androidx.compose.ui.geometry.Size(left, scanBoxHeight))
        drawRect(Color.Black.copy(alpha = 0.6f), Offset(right, top), androidx.compose.ui.geometry.Size(canvasWidth - right, scanBoxHeight))

        val strokeWidth = 5.dp.toPx()
        val cornerLength = 40.dp.toPx()
        val borderColor = Color.White

        drawLine(borderColor, Offset(left, top), Offset(left + cornerLength, top), strokeWidth)
        drawLine(borderColor, Offset(left, top), Offset(left, top + cornerLength), strokeWidth)
        drawLine(borderColor, Offset(right, top), Offset(right - cornerLength, top), strokeWidth)
        drawLine(borderColor, Offset(right, top), Offset(right, top + cornerLength), strokeWidth)
        drawLine(borderColor, Offset(left, bottom), Offset(left + cornerLength, bottom), strokeWidth)
        drawLine(borderColor, Offset(left, bottom), Offset(left, bottom - cornerLength), strokeWidth)
        drawLine(borderColor, Offset(right, bottom), Offset(right - cornerLength, bottom), strokeWidth)
        drawLine(borderColor, Offset(right, bottom), Offset(right, bottom - cornerLength), strokeWidth)

        val currentY = top + (scanBoxHeight * animatedProgress)
        drawLine(Color.Red, Offset(left + 20f, currentY), Offset(right - 20f, currentY), 3.dp.toPx())
        drawLine(Color.Red.copy(alpha = 0.5f), Offset(left + 20f, currentY), Offset(right - 20f, currentY), 8.dp.toPx())
    }
}

