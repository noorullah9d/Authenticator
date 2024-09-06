package com.example.my.project.authenticator.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.databinding.FragmentQRScannerScreenBinding
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.ui.activities.MainActivity
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

@AndroidEntryPoint
class QRScannerScreen : Fragment() {

    private lateinit var binding: FragmentQRScannerScreenBinding
    private lateinit var barcodeDetector: BarcodeDetector
    var cameraSource: CameraSource? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentQRScannerScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.cancelScanning.setOnClickListener {
            findNavController().popBackStack()
        }


        barcodeDetector = BarcodeDetector.Builder(requireActivity())
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        cameraSource = CameraSource.Builder(requireActivity(), barcodeDetector)
            .setAutoFocusEnabled(true)
            .setRequestedPreviewSize(1280, 1024)
            .build()


        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 1001)
        }


        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                // Release resources
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() > 0) {
                    lifecycleScope.launch(
                        Dispatchers.Main
                    ) {
                        val barcode = barcodes.valueAt(0)
                        requireActivity().startActivityWithAnimation<MainActivity>()
                        requireActivity().finishAffinity()
                        Log.d(TAG, "receiveDetections: ${barcode.email}")
                    }
                }
            }
        })


    }


    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        }
    }


    private fun startCamera() {
        binding.cameraPreview.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                    cameraSource?.start(binding.cameraPreview.holder)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource?.stop()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource?.release() // Release camera resources
    }


}

private const val TAG = "QRScannerScreen"