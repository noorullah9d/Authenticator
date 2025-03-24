package com.example.my.project.authenticator.admob

import android.content.Context
import com.example.my.project.authenticator.utils.PrefsHelper
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

object BannerAd {
    var bannerAdView: AdView? = null

    private var isLoading: Boolean = false

    var result: ((Boolean) -> Unit)? = null


    fun Context.loadBannerAd(adId: String) {
        if (bannerAdView != null) {
            result?.invoke(true)
            return
        }

        if (adId.isEmpty() || PrefsHelper.isAdsRemoved) {
            result?.invoke(false)
            return
        }

        val adaptiveAds = AdaptiveAds(this)
        val adView = AdView(this)

        adView.setAdSize(adaptiveAds.adSize)
        adView.adUnitId = adId
        adView.loadAd(AdRequest.Builder().build())
        adView.adListener = object : AdListener() {
            override fun onAdClicked() {}

            override fun onAdClosed() {}

            override fun onAdFailedToLoad(adError: LoadAdError) {
                isLoading = false
            }

            override fun onAdImpression() {}

            override fun onAdLoaded() {
                isLoading = false
                bannerAdView = adView
                result?.invoke(true)
                /*serverBanner?.bannerAdPaidEventListener(
                    admobId = successfulBannerId,
                    adType = BANNER_AD_TYPE,
                    "successful_connection_screen"
                )*/
            }

            override fun onAdOpened() {}
        }
    }
}