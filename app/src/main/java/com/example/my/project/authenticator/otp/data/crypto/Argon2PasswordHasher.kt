package com.example.my.project.authenticator.otp.data.crypto

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import com.example.my.project.authenticator.otp.domain.crypto.PasswordHasher

class Argon2PasswordHasher(
    private val iterations: Int = 8,
    private val memory: Int = 32768,
    private val hashLength: Int = 32,
) : PasswordHasher {
    private val argon = Argon2Kt()

    override fun hash(password: ByteArray, salt: ByteArray?): ByteArray {
        val result = argon.hash(
            Argon2Mode.ARGON2_ID,
            password,
            salt!!,
            tCostInIterations = iterations,
            mCostInKibibyte = memory,
            parallelism = 1,
            hashLengthInBytes = hashLength,
        )
        return result.rawHashAsByteArray()
    }
}