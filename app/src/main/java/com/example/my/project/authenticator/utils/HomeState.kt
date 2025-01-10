package com.example.my.project.authenticator.utils

data class HomeState(
    val totpList: List<TotpCardState> = emptyList(),
    val editingTotp: EditTotpState? = null,
)

data class TotpCardState(
    val id: Int,
    val secretKey: String,
    val name: String,
    var oneTimeCode: Int,
    var secondsLeft: Int,
    var SHA: String,
    var OTP: String,
    var filePath: String,
    var category: String,
)

data class EditTotpState(
    val id: Int,
    val name: String,
    val base32Secret: String,
    val shaStr: String,
    val totpVsHop: String,
    val filePath: String
)