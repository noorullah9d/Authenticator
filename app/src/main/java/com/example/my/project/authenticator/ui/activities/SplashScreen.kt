package com.example.my.project.authenticator.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.applovin.sdk.AppLovinPrivacySettings
import com.example.my.project.authenticator.admob.NativeAd
import com.example.my.project.authenticator.admob.admob_interstitial_splash
import com.example.my.project.authenticator.admob.admob_native_languages
import com.example.my.project.authenticator.admob.loadAdmobInterstitial
import com.example.my.project.authenticator.admob.requestConsentForm
import com.example.my.project.authenticator.databinding.FragmentSplashBinding
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.ui.activities.iap.BillingViewModel
import com.example.my.project.authenticator.ui.activities.iap.FreeTrialActivity
import com.example.my.project.authenticator.ui.viewModel.CardSelectionViewModel
import com.example.my.project.authenticator.ui.viewModel.SplashViewModel
import com.example.my.project.authenticator.utils.AppTheme
import com.example.my.project.authenticator.utils.PrefsHelper
import com.example.my.project.authenticator.utils.PrefsHelper.isAdsRemoved
import com.example.my.project.authenticator.utils.isInterstitialShowing
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.mbridge.msdk.MBridgeConstans
import com.mbridge.msdk.out.MBridgeSDKFactory
import com.vungle.ads.VunglePrivacySettings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreen : BaseActivity() {
    private lateinit var binding: FragmentSplashBinding

    private val splashViewModel by viewModels<SplashViewModel>()

    private val cardSelectionViewModel by viewModels<CardSelectionViewModel>()

    @Inject
    lateinit var billingViewModel: BillingViewModel

    private var splashTimer: CountDownTimer? = null
    private var interstitialAd: InterstitialAd? = null
    private var isConsentCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSplashBinding.inflate(layoutInflater)

        // get remote config values
        splashViewModel.getRemoteConfig()

        setContentView(binding.root)
        setAppTheme()

        lifecycleScope.launch {
            isAdsRemoved = billingViewModel.isAdsRemoved()
            Log.d("SplashScreen", "isAdsRemoved: $isAdsRemoved")
        }

        if (isInternetAvailable() && !isAdsRemoved) {
            requestConsentForm {
                isConsentCompleted = true

                // mediation consent
                val sdk = MBridgeSDKFactory.getMBridgeSDK()
                sdk.setConsentStatus(this, MBridgeConstans.IS_SWITCH_ON)
                VunglePrivacySettings.setGDPRStatus(true, "1.0.0")
                AppLovinPrivacySettings.setHasUserConsent(true, this)

                // load ads here
                if (!PrefsHelper.isLanguageShown && !isAdsRemoved) {
                    NativeAd.loadAd(
                        this,
                        admob_native_languages
                    )
                }

                if (interstitialAd == null && !isAdsRemoved) {
                    loadAdmobInterstitial(
                        admob_interstitial_splash,
                        onAdLoaded = {
                            interstitialAd = it
                            Log.d("SplashScreen", "splash interstitial ad loaded")
                            cancelTimer()
                            showInterstitialAd(
                                onDismissed = {
                                    navigateForward()
                                }
                            )
                        },
                        onAdFailedToLoad = {
                            interstitialAd = null
                            Log.d("SplashScreen", "splash interstitial ad failed: ${it.message}")
                        }
                    )
                    startCountDownTimer()
                } else startPremiumCountDownTimer()
            }
        } else {
            startPremiumCountDownTimer()
        }
    }

    private fun startCountDownTimer() {
        Log.d("SplashScreen", "startCountDownTimer called!")
        cancelTimer()
        splashTimer = object : CountDownTimer(8000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("SplashScreen", "onTick: millisUntilFinished: $millisUntilFinished")
            }

            override fun onFinish() {
                navigateForward()
            }
        }
        splashTimer?.start()
    }

    private fun startPremiumCountDownTimer() {
        Log.d("SplashScreen", "startPremiumCountDownTimer called!")
        cancelTimer()
        splashTimer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.d("SplashScreen", "millisUntilFinished: $millisUntilFinished")
            }

            override fun onFinish() {
                navigateForward()
            }
        }
        splashTimer?.start()
    }

    private fun cancelTimer() {
        splashTimer?.cancel()
        splashTimer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cancelTimer()
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
        }
    }

    private fun navigateForward() {
        if (!isFinishing && !isDestroyed) {
            val currentTime = System.currentTimeMillis()
            val lastShownTime = PrefsHelper.lastPremiumShownTime
            if (shouldShowPremiumActivity(currentTime, lastShownTime) && !isAdsRemoved) {
                Log.d("SplashFragment", "show premium screen")
                PrefsHelper.lastPremiumShownTime = currentTime
                navigateToPremium()
            } else if (!PrefsHelper.isLanguageShown && NativeAd.admobNativeAd != null) {
                startActivityWithAnimation<SelectLanguageActivity>()
                finish()
            } else if (!PrefsHelper.isOnBoardingShown) {
                startActivityWithAnimation<OnBoardingActivity>()
                finish()
            } else {
                if (PrefsHelper.userPassword != "") startActivityWithAnimation<PasswordScreen>()
                else startActivityWithAnimation<MainActivity>()
                finish()
            }
        }
    }

    private fun navigateToPremium() {
        val intent = Intent(this, FreeTrialActivity::class.java)
        intent.putExtra("isFromSplash", true)
        startActivity(intent)
        finish()
    }

    private fun shouldShowPremiumActivity(currentTime: Long, lastShownTime: Long): Boolean {
        val oneDayInMillis = 24 * 60 * 60 * 1000
        return (currentTime - lastShownTime) >= oneDayInMillis
    }

    private fun setAppTheme() {
        val themeMode = when (getSelectedTheme()) {
            AppTheme.DARK -> {
                AppCompatDelegate.MODE_NIGHT_YES
            }

            AppTheme.LIGHT -> {
                AppCompatDelegate.MODE_NIGHT_NO
            }

            else -> {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    private fun getSelectedTheme(): AppTheme {
        val themeName = cardSelectionViewModel.getAppTheme()
        return try {
            AppTheme.valueOf(themeName)
        } catch (_: IllegalArgumentException) {
            AppTheme.SYSTEM_DEFAULT
        }
    }
}