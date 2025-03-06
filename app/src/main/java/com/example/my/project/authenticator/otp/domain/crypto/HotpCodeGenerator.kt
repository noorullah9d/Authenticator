package com.example.my.project.authenticator.otp.domain.crypto

interface HotpCodeGenerator {
    fun generate(secretKey: String, counter: Long, shaStr: String): Int
}