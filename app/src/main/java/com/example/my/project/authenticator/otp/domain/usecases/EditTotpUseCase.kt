package com.example.my.project.authenticator.otp.domain.usecases

import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository
import java.security.SecureRandom

class EditTotpUseCase(
    private val repository: TotpKeyRepository,
    private val encryptor: SecretEncryptor,
) {
    suspend operator fun invoke(
        email: String,
        id: Int,
        name: String,
        issuer: String,
        plainSecret: ByteArray,
        secretKey: String,
        shaStr: String,
        totpVsHop: String,
        filePath: String
    ) {
        val random = SecureRandom()
        val iv = ByteArray(encryptor.ivSize)
        random.nextBytes(iv)
//        repository.editKey(EncryptedTotpKey(id,email=email,name= name, "",secretKey,encryptor.encrypt(plainSecret, iv), iv,shaStr,totpVsHop=totpVsHop,filePath))
        repository.editKey(
            EncryptedTotpKey(
                id = id,
                email = email,
                shaStr = shaStr,
                issuer = issuer,
                totpVsHop = totpVsHop,
                name = name,
                category = "",
                filePath = filePath,
                secretKey = secretKey,
                secret = encryptor.encrypt(plainSecret, iv),
                iv = iv
            )
        )
    }
}