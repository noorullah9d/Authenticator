package com.example.my.project.authenticator.admob

import android.app.Activity
import android.util.Log
import com.example.my.project.authenticator.databinding.GntLanguagesBinding
import com.example.my.project.authenticator.databinding.GntMediumBinding
import com.example.my.project.authenticator.databinding.GntSmallBinding
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.invisible
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

object NativeAd {
    var admobNativeAd: NativeAd? = null
    var isLoading = false
    var result: ((Boolean) -> Unit)? = null

    fun loadAd(activity: Activity, adId: String) {
        if (PrefsHelper.isAdsRemoved) {
            result?.invoke(false)
            return
        }

        if (adId.isEmpty()) {
            result?.invoke(false)
            return
        }

        if (admobNativeAd != null) {
            result?.invoke(true)
            return
        }

        if (isLoading) {
            return
        }

        isLoading = true
        val builder = AdLoader.Builder(activity, adId)
        builder.forNativeAd { ad ->
            Log.d("AdDebug", "Has headline: ${ad.headline != null}")
            Log.d("AdDebug", "Has media content: ${ad.mediaContent != null}")
            admobNativeAd = ad
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
                        Log.d("NativeAd","connected native onAdFailedToLoad!")
                        admobNativeAd = null
                        isLoading = false
                        result?.invoke(false)
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        Log.d("NativeAd","connected native onAdLoaded!")
                        isLoading = false
                        result?.invoke(true)
                    }

                    override fun onAdImpression() {
                        super.onAdImpression()
//                        admobNativeAd = null
                        Log.d("NativeAd","connected native onAdImpression!")
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

    fun populateNativeAdView(nativeAd: NativeAd, adViewBind: GntSmallBinding) {

        val nativeAdView = adViewBind.root

        nativeAdView.headlineView = adViewBind.primary
//        nativeAdView.bodyView = adViewBind.body
        nativeAdView.callToActionView = adViewBind.cta
        nativeAdView.iconView = adViewBind.icon
        nativeAdView.starRatingView = adViewBind.ratingBar
        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        adViewBind.primary.text = nativeAd.headline
        nativeAd.mediaContent?.let {}

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        /*if (nativeAd.body == null) {
            adViewBind.body.invisible()
        } else {
            adViewBind.body.show()
            adViewBind.body.text = nativeAd.body
        }*/

        if (nativeAd.callToAction == null) {
            adViewBind.cta.invisible()
        } else {
            adViewBind.cta.show()
            adViewBind.cta.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adViewBind.icon.hide()
        } else {
            adViewBind.icon.setImageDrawable(nativeAd.icon?.drawable)
            adViewBind.icon.show()
        }

        if (nativeAd.starRating == null) {
            adViewBind.ratingBar.invisible()
        } else {
            adViewBind.ratingBar.rating = nativeAd.starRating!!.toFloat()
            adViewBind.ratingBar.show()
        }

        nativeAdView.setNativeAd(nativeAd)
    }

    fun populateNativeAdView(nativeAd: NativeAd, adViewBind: GntMediumBinding) {

        val nativeAdView = adViewBind.root

        nativeAdView.headlineView = adViewBind.primary
//        nativeAdView.bodyView = adViewBind.body
        nativeAdView.callToActionView = adViewBind.cta
        nativeAdView.iconView = adViewBind.icon
        nativeAdView.starRatingView = adViewBind.ratingBar
        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        adViewBind.primary.text = nativeAd.headline
        nativeAd.mediaContent?.let {}

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        /*if (nativeAd.body == null) {
            adViewBind.body.invisible()
        } else {
            adViewBind.body.show()
            adViewBind.body.text = nativeAd.body
        }*/

        if (nativeAd.callToAction == null) {
            adViewBind.cta.invisible()
        } else {
            adViewBind.cta.show()
            adViewBind.cta.text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adViewBind.icon.hide()
        } else {
            adViewBind.icon.setImageDrawable(nativeAd.icon?.drawable)
            adViewBind.icon.show()
        }

        if (nativeAd.starRating == null) {
            adViewBind.ratingBar.invisible()
        } else {
            adViewBind.ratingBar.rating = nativeAd.starRating!!.toFloat()
            adViewBind.ratingBar.show()
        }

        nativeAdView.setNativeAd(nativeAd)
    }
}