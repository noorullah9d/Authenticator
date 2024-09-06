package com.example.my.project.authenticator.otp.domain.crypto

/** Interface to use for encrypting secret byte data with one key and different iv per data */
interface SecretEncryptor {
    val ivSize: Int

    fun encrypt(plainSecret: ByteArray, iv: ByteArray?): ByteArray

    fun decrypt(encryptedSecret: ByteArray, iv: ByteArray?): ByteArray
}
