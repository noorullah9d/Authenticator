package com.example.my.project.authenticator.otp.domain.model

import androidx.annotation.Keep

@Keep
data class GuideItem(
    var name: String = "",
    var url: String = "",
    var imgRes: Int = -1
)
