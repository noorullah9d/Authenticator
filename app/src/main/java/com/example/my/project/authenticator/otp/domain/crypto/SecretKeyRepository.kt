package com.example.my.project.authenticator.otp.domain.crypto

import javax.crypto.SecretKey

interface SecretKeyRepository {
    fun generateRandomKey(alias: String): SecretKey

    fun getKey(alias: String): SecretKey?

    fun deleteKey(alias: String)

    fun getAliases(): List<String>
}