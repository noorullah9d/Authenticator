package com.example.my.project.authenticator.otp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Password(
    val id: Int = 0,
    val name: String,
    val url: String? = null,
    val emailOrUsername: String,
    val password: String,
    val notes: String? = null,
    val profileImagePath: String? = null,
    val lastModified: Long = System.currentTimeMillis()
): Parcelable