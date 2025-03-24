package com.example.my.project.authenticator.admob

import android.content.Context
import com.google.android.gms.ads.AdSize

class AdaptiveAds(private val context: Context) {
    val adSize: AdSize
        get() {
            val outMetrics = context.resources.displayMetrics
            val adWidthPixels = outMetrics.widthPixels.toFloat()
            val adWidth = (adWidthPixels / outMetrics.density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }
}