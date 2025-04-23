package com.example.my.project.authenticator.admob

import android.app.Activity
import android.util.Log
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.GntLanguagesBinding
import com.example.my.project.authenticator.databinding.GntMediumBinding
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.invisible
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.utils.PrefsHelper
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT

object ExitNativeAd {
    var mNativeAd: NativeAd? = null
    var isLoading = false
    var result: ((Boolean) -> Unit)? = null

    fun loadAd(activity: Activity) {
        if (!activity.isInternetAvailable()) {
            result?.invoke(false)
            return
        }

        if (PrefsHelper.isAdsRemoved) {
            result?.invoke(false)
            return
        }

        if (mNativeAd != null) {
            result?.invoke(true)
            return
        }

        if (isLoading) {
            return
        }

        isLoading = true
        val builder = AdLoader.Builder(activity, activity.getString(R.string.admob_native_id_exit))
        builder.forNativeAd { ad ->
            Log.d("ExitNativeAd", "Has headline: ${ad.headline != null}")
            Log.d("ExitNativeAd", "Has media content: ${ad.mediaContent != null}")
            mNativeAd = ad
        }

        val videoOptions =
            VideoOptions.Builder().setStartMuted(true).build()

        val adOptions =
            NativeAdOptions.Builder().setVideoOptions(videoOptions).setAdChoicesPlacement(
                ADCHOICES_TOP_RIGHT
            ).build()

        builder.withNativeAdOptions(adOptions)

        val adLoader =
            builder
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        Log.d("ExitNativeAd","connected native onAdFailedToLoad!")
                        mNativeAd = null
                        isLoading = false
                        result?.invoke(false)
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Log.d("ExitNativeAd","connected native onAdLoaded!")
                        isLoading = false
                        result?.invoke(true)
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
                        Log.d("ExitNativeAd","connected native onAdImpression!")
                    }
                }
                )
                .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun populateNativeAdView(nativeAd: NativeAd, binding: GntLanguagesBinding) {

        val nativeAdView = binding.root

        // Set the media view.
        nativeAdView.mediaView = binding.mediaView

        // Set other ad assets.
        nativeAdView.headlineView = binding.adHeadline
//        nativeAdView.bodyView = binding.adBody
        nativeAdView.callToActionView = binding.adCallToAction
        nativeAdView.iconView = binding.adAppIcon
//        nativeAdView.priceView = binding.adPrice
        nativeAdView.starRatingView = binding.adStars
//        nativeAdView.storeView = binding.adStore
//        nativeAdView.advertiserView = binding.adAdvertiser

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        binding.adHeadline.text = nativeAd.headline
        nativeAd.mediaContent?.let { binding.mediaView.mediaContent = it }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        /*if (nativeAd.body == null) {
            adViewBind.body.invisible()
        } else {
            adViewBind.body.show()
            adViewBind.body.text = nativeAd.body
        }*/

        if (nativeAd.callToAction == null) {
            binding.adCallToAction.invisible()
        } else {
            binding.adCallToAction.show()
            binding.adCallToAction.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            binding.adAppIcon.hide()
        } else {
            binding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
            binding.adAppIcon.show()
        }

        if (nativeAd.starRating == null) {
            binding.adStars.invisible()
        } else {
            binding.adStars.rating = nativeAd.starRating!!.toFloat()
            binding.adStars.show()
        }

        nativeAdView.setNativeAd(nativeAd)
    }
}