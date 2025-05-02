package com.example.my.project.authenticator.otp.domain.model

data class Account(
    val accountName: String,
    val passcode: String,
    val category: String,
    val issuer: String,
    val shaStr: String,
    val totpVsHop: String,
    val filePath: String
)
