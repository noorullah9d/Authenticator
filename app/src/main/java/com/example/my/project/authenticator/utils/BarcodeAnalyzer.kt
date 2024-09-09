package com.example.my.project.authenticator.utils

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer (private val onBarcodeDetected: (Barcode) -> Unit) : ImageAnalysis.Analyzer {

    private val barcodeScanner = BarcodeScanning.getClient()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            // Process the image using ML Kit's barcode scanner
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        onBarcodeDetected(barcode) // Pass the detected barcode to the callback
                    }
                }
                .addOnFailureListener {
                    Log.d("BarcodeAnalyzer", "Barcode scanning failed", it)
                }
                .addOnCompleteListener {
                    imageProxy.close() // Close the image proxy when done
                }
        }
    }
}