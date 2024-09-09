package com.example.my.project.authenticator.otp.data.crypto

import android.util.Log
import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AesGcmSecretEncryptor(
    private val secretKey: SecretKey,
) : SecretEncryptor {
    private val cipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
    override val ivSize: Int
        get() = 12

    override fun encrypt(plainSecret: ByteArray, iv: ByteArray?): ByteArray {
        if (iv == null) throw IllegalArgumentException("IV must not be null")
        init(Cipher.ENCRYPT_MODE, iv)
        return cipher.doFinal(plainSecret)
    }

    override fun decrypt(encryptedSecret: ByteArray, iv: ByteArray?): ByteArray {
        init(Cipher.DECRYPT_MODE, iv!!)
        return try {
            cipher.doFinal(encryptedSecret)
        }catch (e:Exception){
            ByteArray(0)
        }

    }

    private fun init(mode: Int, iv: ByteArray) {
        cipher.init(mode, secretKey, GCMParameterSpec(128, iv))
    }
}


private const val TAG = "AesGcmSecretEncryptor"