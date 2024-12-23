package com.example.my.project.authenticator.otp.domain.usecases

import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository
import java.security.SecureRandom

class AddNewTotpUseCase(
    private val repository: TotpKeyRepository,
    private val encryptor: SecretEncryptor,
) {
    suspend operator fun invoke(email: String, categories: String = "Default", plainSecret: ByteArray, name: String, secretKey: String) {
        val random = SecureRandom()
        val iv = ByteArray(encryptor.ivSize)
        random.nextBytes(iv)
        repository.addKey(EncryptedTotpKey(0, email, name, category = categories, secretKey, encryptor.encrypt(plainSecret, iv), iv))
//        repository.addKey(EncryptedTotpKey(id = 0, email = email, name = name, category = categories, secretKey = secretKey, secret = encryptor.encrypt(plainSecret, iv), iv = iv))
    }
}