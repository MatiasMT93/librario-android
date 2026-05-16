package com.example.librario.ui.camera

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

// Esta clase se encarga de traducir lo que ve la cámara para que ML Kit lo entienda
class BarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    // Configuramos el escáner para que busque TODO tipo de códigos (EAN, QR, etc)
    // Si querés que sea más rápido solo para libros, podrías usar FORMAT_EAN_13
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            // Convertimos la imagen de la cámara a un formato que ML Kit entienda
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // Si encontramos códigos...
                    for (barcode in barcodes) {
                        // Verificamos que tenga un valor de texto
                        barcode.rawValue?.let { code ->
                            onBarcodeDetected(code)
                        }
                    }
                }
                .addOnFailureListener {
                    // Si falla, no hacemos nada (el usuario sigue apuntando)
                }
                .addOnCompleteListener {
                    // MUY IMPORTANTE: Cerrar el frame para que la cámara pueda procesar el siguiente
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}