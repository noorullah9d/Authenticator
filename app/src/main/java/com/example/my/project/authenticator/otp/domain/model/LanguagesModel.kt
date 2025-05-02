package com.example.my.project.authenticator.otp.domain.model

import androidx.annotation.Keep

@Keep
data class LanguagesModel (
    var name:String="",
    var localName:String="",
    var code:String=""
)