package com.example.my.project.authenticator.otp.domain.model

data class Password(
    val id: Int = 0,
    val name: String,
    val url: String? = null,
    val emailOrUsername: String,
    val password: String,
    val notes: String? = null,
    val imageRes: Int
)