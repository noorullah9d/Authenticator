package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.admob.NativeAd
import com.example.my.project.authenticator.databinding.ActivityImportExportScreenBinding
import com.example.my.project.authenticator.databinding.GntSmallBinding
import com.example.my.project.authenticator.databinding.ShimmerSmallNativeBinding
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.safeAddView
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.ui.viewModel.ImportViewModel
import com.example.my.project.authenticator.utils.PrefsHelper.isAdsRemoved
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ImportExportScreen : BaseActivity() {

    private lateinit var binding: ActivityImportExportScreenBinding
    private val viewModel by viewModels<ImportViewModel>()


    private lateinit var getInputStreamLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportExportScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getInputStreamLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { content ->
            if (content == null) {
                finish()
                return@registerForActivityResult
            }

            lifecycleScope.launch {
                this@ImportExportScreen.contentResolver?.openInputStream(content)?.use { importStream ->
                    val isPasswordNeeded = viewModel.prepareAndCheckPassword(importStream)
                    if (!isPasswordNeeded) {
                        viewModel.import()
                    } else {
//                        toast("Corrupted File")
//                        showPasswordDialog()
                    }
                }
            }
        }
        
        setupClickListeners()
        loadAndShowAdd()
    }

    private fun setupClickListeners() {
        binding.apply {
            importFile.setOnClickListener {
                startActivityWithAnimation<ImportScreen>()

            }

            exportFile.setOnClickListener {
                startActivityWithAnimation<ExportScreen>()
            }

            icBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun loadAndShowAdd() {
        if (!isInternetAvailable() || isAdsRemoved) {
            binding.adFrame.hide()
            return
        }
        binding.adFrame.show()
        val shimmer = ShimmerSmallNativeBinding.inflate(layoutInflater)
        binding.adFrame.apply {
            removeAllViews()
            safeAddView(shimmer.root)
            shimmer.root.startShimmerAnimation()
        }

        if (NativeAd.admobNativeAd != null) {
            showNativeAd()
            return
        }

        NativeAd.result = {
            if (it) {
                showNativeAd()
            } else {
                binding.adFrame.hide()
            }
        }

        NativeAd.loadAd(
            this,
            getString(R.string.admob_native_id_transfer_codes)
        )
    }

    private fun showNativeAd() {
        binding.apply {
            adFrame.show()
            NativeAd.admobNativeAd?.let {
                val adView = GntSmallBinding.inflate(layoutInflater)
                NativeAd.populateNativeAdView(it, adView)
                adFrame.removeAllViews()
                adFrame.safeAddView(adView.root)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NativeAd.admobNativeAd?.destroy()
        NativeAd.admobNativeAd = null
    }
}