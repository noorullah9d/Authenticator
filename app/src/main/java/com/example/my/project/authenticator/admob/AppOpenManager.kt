package com.example.my.project.authenticator.admob

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.ui.activities.SplashScreen
import com.example.my.project.authenticator.utils.PrefsHelper
import com.example.my.project.authenticator.utils.isInterstitialShowing
import com.google.android.gms.ads.AdActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback

class AppOpenManager(private var application: Application) :
    LifecycleObserver,
    Application.ActivityLifecycleCallbacks {


    private var loadCallback: AppOpenAdLoadCallback? = null

    private var currentActivity: Activity? = null
    var isShowingAd = false
    private var isLoadingAd = false
    var shouldShowAd = true /* used for stopping ad to appear forcefully */

    /** Creates and returns ad request.  */
    private val adRequest: AdRequest
        get() = AdRequest.Builder().build()

    /** Utility method that checks if ad exists and can be shown.  */
    private val isAdAvailable: Boolean get() = appOpenAd != null

    init {
        this.application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    companion object {
        private var appOpenAd: AppOpenAd? = null
        fun destroyAd() {
            appOpenAd = null
        }
    }

    /** Request an ad  */
    fun fetchAd() {

        if (isAdAvailable || isLoadingAd || !application.isInternetAvailable()) {
            return
        }

        if (PrefsHelper.isAdsRemoved) {
            return
        }

        loadCallback = object : AppOpenAdLoadCallback() {

            override fun onAdLoaded(p0: AppOpenAd) {
                super.onAdLoaded(p0)
                appOpenAd = p0
                isLoadingAd = false
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                isLoadingAd = false
            }

        }
        isLoadingAd = true
        AppOpenAd.load(
            application.applicationContext,
            application.applicationContext.getString(R.string.admob_app_open_id),
            adRequest,
            loadCallback!!
        )
    }

    /** Shows the ad if one isn't already showing.  */
    private fun showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.

        if (PrefsHelper.isAdsRemoved) {
            return
        }

        try {
            if (!isShowingAd && isAdAvailable) {
                val fullScreenContentCallback: FullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            appOpenAd = null
                            isShowingAd = false
                            fetchAd()
                        }

                        override fun onAdShowedFullScreenContent() {
                            isShowingAd = true
                        }

                        override fun onAdImpression() {
                            super.onAdImpression()
                        }
                    }
                appOpenAd?.fullScreenContentCallback = fullScreenContentCallback

                Handler(Looper.getMainLooper()).postDelayed({
                    if (currentActivity != null && currentActivity !is SplashScreen && currentActivity !is AdActivity && !isInterstitialShowing) {
                        currentActivity?.let {
                            appOpenAd?.show(it)
                        }
                    }
                }, 500)

            } else {
                fetchAd()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** LifecycleObserver methods  */
    @OnLifecycleEvent(ON_START)
    fun onStart() {
        try {
            showAdIfAvailable()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityCreated(activity: Activity, p1: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, p1: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {}
}

