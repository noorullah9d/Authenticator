package com.example.my.project.authenticator.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.utils.PrefsHelper
import com.example.my.project.authenticator.utils.isInterstitialShowing
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object FragInterstitial {
    private var isLoadingAd: Boolean = false
    private var mInterstitialAd: InterstitialAd? = null

    fun loadAd(context: Context, adId: String) {
        if (PrefsHelper.isAdsRemoved) {
            return
        }

        if (adId.isEmpty()) {
            return
        }

        if (isLoadingAd) {
            return
        }

        if (mInterstitialAd != null) {
            return
        }
        isLoadingAd = true
        Log.e("FragInterstitial: ", "interstitial ad load called")
        InterstitialAd.load(
            context,
            adId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    Log.e("FragInterstitial: ", "interstitial ad failed: $p0")
                    mInterstitialAd = null
                    isLoadingAd = false
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    Log.e("FragInterstitial: ", "interstitial ad loaded")
                    mInterstitialAd = interstitialAd
                    isLoadingAd = false
                }
            }
        )
    }

    fun showAd(
        activity: Activity,
        onDismissed: () -> Unit = {}
    ) {
        if (PrefsHelper.isAdsRemoved || !activity.isInternetAvailable()) {
            onDismissed.invoke()
            return
        }

        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback =
                object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        onDismissed.invoke()
                        mInterstitialAd = null
                        isLoadingAd = false
                        isInterstitialShowing = false
                    }

                    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                        super.onAdFailedToShowFullScreenContent(p0)
                        onDismissed.invoke()
                        mInterstitialAd = null
                        isLoadingAd = false
                        isInterstitialShowing = false
                    }

                    override fun onAdShowedFullScreenContent() {
                        super.onAdShowedFullScreenContent()
                        isInterstitialShowing = true
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        mInterstitialAd = null
                        isInterstitialShowing = true
                    }
                }
            mInterstitialAd?.show(activity)
        } else onDismissed.invoke()
    }
}