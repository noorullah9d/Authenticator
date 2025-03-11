package com.example.my.project.authenticator.model

import androidx.annotation.Keep

@Keep
data class GuideItem(
    var name: String = "",
    var url: String = "",
    var imgRes: Int = -1
)
