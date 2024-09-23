package com.example.my.project.authenticator.utils

data class HomeState(
    val totpList: List<TotpCardState> = emptyList(),
    val editingTotp: EditTotpState? = null,
)

data class TotpCardState(
    val id: Int,
    val name: String,
    var oneTimeCode: Int,
    var secondsLeft: Int,
)

data class EditTotpState(
    val id: Int,
    val name: String,
    val base32Secret: String,
)