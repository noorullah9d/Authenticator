package com.example.my.project.authenticator.otp.domain.usecases

import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository
import java.security.SecureRandom

class AddNewTotpUseCase(
    private val repository: TotpKeyRepository,
    private val encryptor: SecretEncryptor,
) {
    suspend operator fun invoke(
        id: Int,
        email: String,
        issuer: String,
        categories: String = "",
        plainSecret: ByteArray,
        name: String,
        secretKey: String,
        shaStr: String,
        totpVsHop: String,
        filePath: String
    ) {
        val random = SecureRandom()
        val iv = ByteArray(encryptor.ivSize)
        random.nextBytes(iv)
        if (id == 0) {

            repository.addKey(EncryptedTotpKey(id = id, email = email, name = name, issuer = issuer, shaStr = shaStr, totpVsHop = totpVsHop, filePath = filePath, category = categories, secretKey = secretKey, secret = encryptor.encrypt(plainSecret, iv), iv = iv))
        } else {
            repository.editKey(EncryptedTotpKey(id = id, email = email, name = name, issuer = issuer, shaStr = shaStr, totpVsHop = totpVsHop, filePath = filePath, category = categories, secretKey = secretKey, secret = encryptor.encrypt(plainSecret, iv), iv = iv))
        }
//        repository.addKey(EncryptedTotpKey(id = 0, email = email, name = name, category = categories, secretKey = secretKey, secret = encryptor.encrypt(plainSecret, iv), iv = iv))
    }
}