package com.example.my.project.authenticator.otp.domain.model

import com.google.gson.annotations.SerializedName

class AdModel(
    @SerializedName("enable") var show: Boolean = false,
    @SerializedName("priority") var priority: Int = 0,
    @SerializedName("adId") var adId: String = ""
)