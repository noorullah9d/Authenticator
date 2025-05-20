package com.example.my.project.authenticator.otp.domain.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AdIds(
    @SerializedName("splash_interstitial") var interstitialSplash: AdModel? = AdModel(),
    @SerializedName("interstitial_fragment") var interstitialFragment: AdModel? = AdModel(),
    @SerializedName("onboarding_interstitial") var interstitialOnboarding: AdModel? = AdModel(),
    @SerializedName("home_native") var nativeHome: AdModel? = AdModel(),
    @SerializedName("language_native") var nativeLanguages: AdModel? = AdModel(),
    @SerializedName("exit_native") var nativeExit: AdModel? = AdModel(),
    @SerializedName("scanner_native") var nativeScanner: AdModel? = AdModel(),
    @SerializedName("backup_native") var nativeBackup: AdModel? = AdModel(),
    @SerializedName("transfer_code_native") var nativeTransferCode: AdModel? = AdModel(),
    @SerializedName("onboarding_native") var nativeOnBoarding: AdModel? = AdModel(),
    @SerializedName("password_native") var nativeVault: AdModel? = AdModel(),
    @SerializedName("add_password_native") var nativeAddPassword: AdModel? = AdModel(),
    @SerializedName("app_open_resume") var appOpen: AdModel? = AdModel(),
    @SerializedName("splash_iap_exp") var splashIapExp: AdModel? = AdModel(),
)