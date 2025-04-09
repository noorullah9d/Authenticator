package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.admob.NativeAd
import com.example.my.project.authenticator.admob.loadAdmobInterstitial
import com.example.my.project.authenticator.databinding.ActivityOnBoardingBinding
import com.example.my.project.authenticator.databinding.GntSmallBinding
import com.example.my.project.authenticator.databinding.ShimmerSmallNativeBinding
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.safeAddView
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.otp.data.database.Categories
import com.example.my.project.authenticator.ui.adapters.OnboardingAdapter
import com.example.my.project.authenticator.ui.viewModel.HomeViewModel
import com.example.my.project.authenticator.utils.PrefsHelper
import com.example.my.project.authenticator.utils.PrefsHelper.isAdsRemoved
import com.example.my.project.authenticator.utils.isInterstitialShowing
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity() {
    private lateinit var binding: ActivityOnBoardingBinding

    private val homeViewModel by viewModels<HomeViewModel>()
    private var interstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PrefsHelper.isOnBoardingShown = true

        viewPager()
        onClickView()
//        loadAndShowAdd()
        loadInterstitial()
    }

    private fun loadInterstitial() {
        if (interstitialAd == null) {
            loadAdmobInterstitial(
                getString(R.string.admob_interstitial_fragment),
                onAdLoaded = {
                    interstitialAd = it
                    Log.d("OnBoarding", "interstitial ad loaded")
                },
                onAdFailedToLoad = {
                    interstitialAd = null
                    Log.d("OnBoarding", "interstitial ad failed: ${it.message}")
                }
            )
        }
    }

    private fun showInterstitialAd(onDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdImpression() {
                    super.onAdImpression()
                    isInterstitialShowing = true
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                    isInterstitialShowing = true
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    onDismissed.invoke()
                    interstitialAd = null
                    isInterstitialShowing = false
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                    onDismissed.invoke()
                    isInterstitialShowing = false
                }
            }
            interstitialAd?.show(this)
        } else onDismissed.invoke()
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
            getString(R.string.admob_native_id_onboarding)
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

    private fun onClickView() {

        binding.btnStart.setOnClickListener {
            when (binding.viewPager.currentItem) {
                0 -> {
                    binding.viewPager.currentItem++
                }

                1 -> {
                    binding.viewPager.currentItem++
                }

                2 -> {
                    homeViewModel.addCategories(Categories(0, "Default"))
                    homeViewModel.addCategories(Categories(0, "Office"))
                    homeViewModel.addCategories(Categories(0, "Family"))
                    PrefsHelper.isUserFirstTime = false
//                    startActivityWithAnimation<MainActivity>()
//                    finish()

                    // show interstitial ad
                    showInterstitialAd(
                        onDismissed = {
                            if (!isDestroyed && !isFinishing) {
                                startActivityWithAnimation<MainActivity>()
                                finish()
                            }
                        }
                    )
                }
            }
        }

        binding.skip.setOnClickListener {
            homeViewModel.addCategories(Categories(0, "Default"))
            homeViewModel.addCategories(Categories(0, "Office"))
            homeViewModel.addCategories(Categories(0, "Family"))
            PrefsHelper.isUserFirstTime = false
            startActivityWithAnimation<MainActivity>()
            finish()
        }
    }

    private fun viewPager() {
        binding.viewPager.adapter = OnboardingAdapter(this)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                when (position) {
                    0 -> {
                        binding.skip.show()
                        binding.btnStart.text = getString(R.string.get_started)
                        binding.icons.setImageResource(R.drawable.ic_first_start)
                    }

                    1 -> {
                        binding.skip.show()
                        binding.btnStart.text = getString(R.string.next)
                        binding.icons.setImageResource(R.drawable.ic_second_start)
                    }

                    2 -> {
                        binding.skip.hide()
                        binding.btnStart.text = getString(R.string.let_s_go)
                        binding.icons.setImageResource(R.drawable.ic_third_start)
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        NativeAd.admobNativeAd?.destroy()
        NativeAd.admobNativeAd = null
    }
}
