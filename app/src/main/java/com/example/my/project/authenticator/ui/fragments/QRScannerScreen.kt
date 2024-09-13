package com.example.my.project.authenticator.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentQRScannerScreenBinding
import com.example.my.project.authenticator.extensions.logFirebaseEvent
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.otp.viewModel.HomeViewModel
import com.example.my.project.authenticator.utils.BarcodeAnalyzer
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
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

        checkAndRequestCameraPermission()

    }


    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            toast(getString(R.string.camera_permission_denied))
            requireActivity().finish()
        }
    }


    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

    }


    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            cameraProvider.unbindAll()

            val preview = Preview.Builder()
                .setTargetResolution(Size(640, 480))
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(Size(640, 480))
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor(), BarcodeAnalyzer { barcode ->
                        result++
                        if (result < 2) {
                            val infoData = barcode.displayValue?.let { it1 -> parseTotpUri(it1) }
                            Log.d(TAG, "Scanned TOTP: Secret = ${infoData?.first}, Name = ${infoData?.second}")

                            val addResult = homeViewModel.addTotp(infoData?.second ?: "", infoData?.first ?: "")
                            if (addResult) {
                                requireActivity().logFirebaseEvent("scan_option", mapOf("codescan" to "clicked"))
                                requireActivity().finish()
                            } else {
                                toast(requireActivity().getString(R.string.error_occurs))
                            }
                        }
                    })
                }

            try {
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )

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
