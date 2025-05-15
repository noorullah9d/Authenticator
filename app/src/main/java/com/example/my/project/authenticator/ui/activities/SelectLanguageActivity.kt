package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.admob.NativeAd
import com.example.my.project.authenticator.admob.admob_native_languages
import com.example.my.project.authenticator.databinding.ActivitySelectLanguageBinding
import com.example.my.project.authenticator.databinding.GntLanguagesBinding
import com.example.my.project.authenticator.databinding.ShimmerLayoutLanguagesNativeBinding
import com.example.my.project.authenticator.extensions.clickWithExtraDebounce
import com.example.my.project.authenticator.extensions.getLanguageList
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.safeAddView
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.ui.viewModel.LanguageViewModel
import com.example.my.project.authenticator.ui.adapters.LanguagesAdapterNew
import com.example.my.project.authenticator.utils.PrefsHelper
import com.example.my.project.authenticator.utils.PrefsHelper.isAdsRemoved
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectLanguageActivity : BaseActivity() {
    private val binding: ActivitySelectLanguageBinding by lazy {
        ActivitySelectLanguageBinding.inflate(layoutInflater)
    }

    private val viewModel: LanguageViewModel by viewModels<LanguageViewModel>()
    private lateinit var languagesAdapter: LanguagesAdapterNew
    private var selectedLanguage = "en"
    private var isFromSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        isFromSettings = intent.getBooleanExtra("isFromSettings", false)
        if (!isFromSettings) loadAndShowAdd() else binding.adFrame.hide()
        initLanguagesRecyclerView()
        setupClickListeners()
        handleBackPress()
    }

    private fun loadAndShowAdd() {
        if (!isInternetAvailable() || isAdsRemoved) {
            binding.adFrame.hide()
            return
        }
        binding.adFrame.show()
        val shimmer = ShimmerLayoutLanguagesNativeBinding.inflate(layoutInflater)
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
            admob_native_languages
        )
    }

    private fun showNativeAd() {
        try {
            binding.apply {
                adFrame.show()
                NativeAd.admobNativeAd?.let {
                    val adView = GntLanguagesBinding.inflate(layoutInflater)
                    NativeAd.populateNativeAdView(it, adView)
                    adFrame.removeAllViews()
                    adFrame.safeAddView(adView.root)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isFromSettings) finish() else navigateToForward()
            }
        })
    }

    private fun setupClickListeners() {
        binding.apply {
            icBack.setOnClickListener {
                if (isFromSettings) finish() else navigateToForward()
            }

            confirm.clickWithExtraDebounce {
                viewModel.setLanguage(selectedLanguage)
                viewModel.setLanguageFirstTime("true")
                PrefsHelper.isLanguageShown = true
                navigateToForward()
            }
        }
    }

    private fun initLanguagesRecyclerView() {
        languagesAdapter = LanguagesAdapterNew(viewModel.getLanguage()) {
            selectedLanguage = it.code
        }
        languagesAdapter.setData(getLanguageList())
        binding.languagesRecycler.adapter = languagesAdapter
    }

    private fun navigateToForward() {
        if (!PrefsHelper.isOnBoardingShown) {
            startActivityWithAnimation<OnBoardingActivity>()
            finish()
        } else {
            startActivityWithAnimation<MainActivity>()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NativeAd.admobNativeAd?.destroy()
        NativeAd.admobNativeAd = null
    }
}