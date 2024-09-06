package com.example.my.project.authenticator.otp.domain.crypto

import kotlin.time.Duration

interface TotpCodeGenerator {
    fun generate(secret: ByteArray, unixTime: Duration): Int
}