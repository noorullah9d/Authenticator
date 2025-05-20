package com.example.my.project.authenticator.otp.data.repository

import android.content.Context
import android.util.Log
import com.example.my.project.authenticator.BuildConfig
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.admob.admob_app_open
import com.example.my.project.authenticator.admob.admob_interstitial_fragment
import com.example.my.project.authenticator.admob.admob_interstitial_onboarding
import com.example.my.project.authenticator.admob.admob_interstitial_splash
import com.example.my.project.authenticator.admob.admob_native_add_password
import com.example.my.project.authenticator.admob.admob_native_backup
import com.example.my.project.authenticator.admob.admob_native_exit
import com.example.my.project.authenticator.admob.admob_native_home
import com.example.my.project.authenticator.admob.admob_native_languages
import com.example.my.project.authenticator.admob.admob_native_onboarding
import com.example.my.project.authenticator.admob.admob_native_scanner
import com.example.my.project.authenticator.admob.admob_native_transfer_code
import com.example.my.project.authenticator.admob.admob_native_vault
import com.example.my.project.authenticator.otp.domain.model.AdIds
import com.example.my.project.authenticator.otp.domain.repository.RemoteConfigRepository
import com.example.my.project.authenticator.utils.YEARLY
import com.example.my.project.authenticator.utils.splashIAPExperiment
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await

class RemoteConfigRepositoryImpl(
    private val context: Context,
    private val remoteConfig: FirebaseRemoteConfig
) : RemoteConfigRepository {

    override suspend fun getRemoteResponse() {
        try {
            setDefaultIds()
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) {
                    0L
                } else {
                    43200L
                }
            }

            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.fetchAndActivate().await()
            /*remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("RemoteConfig:"," Fetch and activate succeeded")

                        val response = remoteConfig.getString("debug_ad_configuration")
                        Log.d("RemoteConfig:"," test response: $response")
                    } else {
                        Log.e("RemoteConfig:"," Fetch failed")
                    }
                }*/

            if (BuildConfig.DEBUG) {
                val responseIds = remoteConfig.getString("debug_ad_configuration")
                Log.d("RemoteConfig", "debug response: $responseIds")
                val adIds = Gson().fromJson(responseIds, AdIds::class.java)
                if (responseIds.isEmpty()) {
                    return
                }

                admob_interstitial_splash =
                    if (adIds.interstitialSplash?.show == true && adIds.interstitialSplash?.adId?.isNotEmpty() == true) {
                        adIds?.interstitialSplash?.adId ?: ""
                    } else {
                        ""
                    }

                Log.d("RemoteConfig", "splash ad id: ${adIds.interstitialSplash?.adId}")
                Log.d("RemoteConfig", "splash ad show: ${adIds.interstitialSplash?.show}")

                admob_interstitial_fragment =
                    if (adIds.interstitialFragment?.show == true && adIds.interstitialFragment?.adId?.isNotEmpty() == true) {
                        adIds?.interstitialFragment?.adId ?: ""
                    } else {
                        ""
                    }

                admob_interstitial_onboarding =
                    if (adIds.interstitialOnboarding?.show == true && adIds.interstitialOnboarding?.adId?.isNotEmpty() == true) {
                        adIds?.interstitialOnboarding?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_home =
                    if (adIds.nativeHome?.show == true && adIds.nativeHome?.adId?.isNotEmpty() == true) {
                        adIds?.nativeHome?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_languages =
                    if (adIds.nativeLanguages?.show == true && adIds.nativeLanguages?.adId?.isNotEmpty() == true) {
                        adIds?.nativeLanguages?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_exit =
                    if (adIds.nativeExit?.show == true && adIds.nativeExit?.adId?.isNotEmpty() == true) {
                        adIds?.nativeExit?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_scanner =
                    if (adIds.nativeScanner?.show == true && adIds.nativeScanner?.adId?.isNotEmpty() == true) {
                        adIds?.nativeScanner?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_backup =
                    if (adIds.nativeBackup?.show == true && adIds.nativeBackup?.adId?.isNotEmpty() == true) {
                        adIds?.nativeBackup?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_transfer_code =
                    if (adIds.nativeTransferCode?.show == true && adIds.nativeTransferCode?.adId?.isNotEmpty() == true) {
                        adIds?.nativeTransferCode?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_onboarding =
                    if (adIds.nativeOnBoarding?.show == true && adIds.nativeOnBoarding?.adId?.isNotEmpty() == true) {
                        adIds?.nativeOnBoarding?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_vault =
                    if (adIds.nativeVault?.show == true && adIds.nativeVault?.adId?.isNotEmpty() == true) {
                        adIds?.nativeVault?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_add_password =
                    if (adIds.nativeAddPassword?.show == true && adIds.nativeAddPassword?.adId?.isNotEmpty() == true) {
                        adIds?.nativeAddPassword?.adId ?: ""
                    } else {
                        ""
                    }

                admob_app_open =
                    if (adIds.appOpen?.show == true && adIds.appOpen?.adId?.isNotEmpty() == true) {
//                        PrefUtils.isAppOpenAllowedFromRemoteConfig = true
                        adIds?.appOpen?.adId ?: ""
                    } else {
//                        PrefUtils.isAppOpenAllowedFromRemoteConfig = false
                        ""
                    }

                splashIAPExperiment =
                    if (adIds.splashIapExp?.show == true && adIds.splashIapExp?.adId?.isNotEmpty() == true) {
                        adIds?.splashIapExp?.adId ?: YEARLY
                    } else {
                        YEARLY
                    }
            } else {
                val responseIds = remoteConfig.getString("release_ad_configuration")
                Log.d("RemoteConfig", "release ad ids: $responseIds")
                val adIds = Gson().fromJson(responseIds, AdIds::class.java)
                if (responseIds.isEmpty()) {
                    return
                }

                admob_interstitial_splash =
                    if (adIds.interstitialSplash?.show == true && adIds.interstitialSplash?.adId?.isNotEmpty() == true) {
                        adIds?.interstitialSplash?.adId ?: ""
                    } else {
                        ""
                    }

                admob_interstitial_fragment =
                    if (adIds.interstitialFragment?.show == true && adIds.interstitialFragment?.adId?.isNotEmpty() == true) {
                        adIds?.interstitialFragment?.adId ?: ""
                    } else {
                        ""
                    }

                admob_interstitial_onboarding =
                    if (adIds.interstitialOnboarding?.show == true && adIds.interstitialOnboarding?.adId?.isNotEmpty() == true) {
                        adIds?.interstitialOnboarding?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_home =
                    if (adIds.nativeHome?.show == true && adIds.nativeHome?.adId?.isNotEmpty() == true) {
                        adIds?.nativeHome?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_languages =
                    if (adIds.nativeLanguages?.show == true && adIds.nativeLanguages?.adId?.isNotEmpty() == true) {
                        adIds?.nativeLanguages?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_exit =
                    if (adIds.nativeExit?.show == true && adIds.nativeExit?.adId?.isNotEmpty() == true) {
                        adIds?.nativeExit?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_scanner =
                    if (adIds.nativeScanner?.show == true && adIds.nativeScanner?.adId?.isNotEmpty() == true) {
                        adIds?.nativeScanner?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_backup =
                    if (adIds.nativeBackup?.show == true && adIds.nativeBackup?.adId?.isNotEmpty() == true) {
                        adIds?.nativeBackup?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_transfer_code =
                    if (adIds.nativeTransferCode?.show == true && adIds.nativeTransferCode?.adId?.isNotEmpty() == true) {
                        adIds?.nativeTransferCode?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_onboarding =
                    if (adIds.nativeOnBoarding?.show == true && adIds.nativeOnBoarding?.adId?.isNotEmpty() == true) {
                        adIds?.nativeOnBoarding?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_vault =
                    if (adIds.nativeVault?.show == true && adIds.nativeVault?.adId?.isNotEmpty() == true) {
                        adIds?.nativeVault?.adId ?: ""
                    } else {
                        ""
                    }

                admob_native_add_password =
                    if (adIds.nativeAddPassword?.show == true && adIds.nativeAddPassword?.adId?.isNotEmpty() == true) {
                        adIds?.nativeAddPassword?.adId ?: ""
                    } else {
                        ""
                    }

                admob_app_open =
                    if (adIds.appOpen?.show == true && adIds.appOpen?.adId?.isNotEmpty() == true) {
//                        PrefUtils.isAppOpenAllowedFromRemoteConfig = true
                        adIds?.appOpen?.adId ?: ""
                    } else {
//                        PrefUtils.isAppOpenAllowedFromRemoteConfig = false
                        ""
                    }

                splashIAPExperiment =
                    if (adIds.splashIapExp?.show == true && adIds.splashIapExp?.adId?.isNotEmpty() == true) {
                        adIds?.splashIapExp?.adId ?: YEARLY
                    } else {
                        YEARLY
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setDefaultIds() {
        admob_interstitial_splash = context.getString(R.string.admob_interstitial_id_splash)
        admob_interstitial_fragment = context.getString(R.string.admob_interstitial_fragment)
        admob_interstitial_onboarding = context.getString(R.string.admob_interstitial_onboarding)
        admob_native_home = context.getString(R.string.admob_native_id_home)
        admob_native_languages = context.getString(R.string.admob_native_id_languages)
        admob_native_exit = context.getString(R.string.admob_native_id_exit)
        admob_native_scanner = context.getString(R.string.admob_native_id_qr)
        admob_native_backup = context.getString(R.string.admob_native_id_backup_theme)
        admob_native_transfer_code = context.getString(R.string.admob_native_id_transfer_codes)
        admob_native_onboarding = context.getString(R.string.admob_native_id_onboarding)
        admob_native_vault = context.getString(R.string.admob_native_id_vault)
        admob_native_add_password = context.getString(R.string.admob_native_id_add_password)
        admob_app_open = context.getString(R.string.admob_app_open_id)
        splashIAPExperiment = YEARLY
    }
}