package com.example.my.project.authenticator.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentQRScannerScreenBinding
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.otp.viewModel.HomeViewModel
import com.example.my.project.authenticator.utils.BarcodeAnalyzer
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.Executors

@AndroidEntryPoint
class QRScannerScreen : Fragment() {

    private lateinit var binding: FragmentQRScannerScreenBinding
    private val homeViewModel by viewModels<HomeViewModel>()
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private var result = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentQRScannerScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cancelScanning.setOnClickListener {
            findNavController().popBackStack()
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 1001)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Unbind all previous use cases before rebinding
            cameraProvider.unbindAll()

            // Define the preview use case
            val preview = Preview.Builder()
                .setTargetResolution(Size(640, 480)) // Set a compatible resolution
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // ImageAnalyzer for detecting barcodes
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(Size(640, 480)) // Ensure matching resolution with Preview
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor(), BarcodeAnalyzer { barcode ->
                        result++
                        if (result < 2) {
                            val infoData = barcode.displayValue?.let { it1 -> parseTotpUri(it1) }
                            Log.d(TAG, "Scanned TOTP: Secret = ${infoData?.first}, Name = ${infoData?.second}")

                            val addResult = homeViewModel.addTotp(infoData?.second ?: "", infoData?.first ?: "")
                            if (addResult) {
                                requireActivity().finish()
                            } else {
                                toast(requireActivity().getString(R.string.error_occurs))
                            }
                        }
                    })
                }

            try {
                // Bind the lifecycle of the camera to the fragment with the preview and imageAnalyzer
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

                // Set the preview surface
                preview.setSurfaceProvider(binding.previewView.surfaceProvider)

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun parseTotpUri(totpUri: String): Pair<String, String>? {
        return try {
            val uri = Uri.parse(totpUri)
            val secret = uri.getQueryParameter("secret")
            val label = uri.path?.substring(1)
            val name = label?.substringAfter(':', "") ?: ""
            if (secret != null && name.isNotEmpty()) {
                Pair(secret, name)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse TOTP URI: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "QRScannerScreen"
    }
}
