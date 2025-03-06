package com.example.my.project.authenticator.otp.domain.usecases

import android.util.Log
import com.example.my.project.authenticator.otp.domain.crypto.HotpCodeGenerator
import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import com.example.my.project.authenticator.utils.SharedPreferencesHelper

class GenerateHotpCodeUseCase(
    private val generator: HotpCodeGenerator,
    private val encryptor: SecretEncryptor,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) {
    operator fun invoke(totpKey: EncryptedTotpKey, isRegenerate: Boolean): Int {
        val secret = encryptor.decrypt(totpKey.secret, totpKey.iv)
        Log.d("GenerateHotpCodeUseCase", "invoke: secret= $totpKey")
        val counterKey = "hotp_counter_${totpKey.id}" // Unique counter key per account
        val currentCounter = sharedPreferencesHelper.getLong(counterKey, 0L)
        Log.d("GenerateHotpCodeUseCase", "generate: counter= $currentCounter")

        val newOtp = generator.generate(totpKey.secretKey, currentCounter, totpKey.shaStr)
        Log.d("GenerateHotpCodeUseCase", "newOtp= $newOtp")

        if (isRegenerate) sharedPreferencesHelper.putLong(counterKey, currentCounter + 1) // Increment counter
        return newOtp
    }
}