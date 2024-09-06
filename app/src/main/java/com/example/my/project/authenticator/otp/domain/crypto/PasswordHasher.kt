package com.example.my.project.authenticator.otp.domain.crypto

interface PasswordHasher {
    fun hash(password: ByteArray, salt: ByteArray?): ByteArray
}