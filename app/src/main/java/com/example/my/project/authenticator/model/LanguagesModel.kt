package com.example.my.project.authenticator.model

import androidx.annotation.Keep

@Keep
data class LanguagesModel (
    var name:String="",
    var localName:String="",
    var code:String=""
)