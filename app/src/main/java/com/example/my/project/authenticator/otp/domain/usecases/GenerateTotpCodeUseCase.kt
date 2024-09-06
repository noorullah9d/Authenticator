package com.example.my.project.authenticator.otp.domain.usecases

import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.crypto.TotpCodeGenerator
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import kotlin.time.Duration

class GenerateTotpCodeUseCase(
    private val generator: TotpCodeGenerator,
    private val encryptor: SecretEncryptor,
    private val getUnixTime: () -> Duration,
) {
    operator fun invoke(totpKey: EncryptedTotpKey): Int {
        val secret = encryptor.decrypt(totpKey.secret, totpKey.iv)
        return generator.generate(secret, getUnixTime())
    }
}