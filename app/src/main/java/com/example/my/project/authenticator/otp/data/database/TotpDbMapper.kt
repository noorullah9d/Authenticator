package com.example.my.project.authenticator.otp.data.database

import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey

object TotpDbMapper {
    fun fromTotpKey(totpKey: EncryptedTotpKey) = TotpDbEntity(
        id = totpKey.id,
        email = totpKey.email,
        category = totpKey.category,
        name = totpKey.name,
        secret = totpKey.secret,
        secretKey = totpKey.secretKey,
        iv = totpKey.iv,
        shaStr = totpKey.shaStr,
        totpVsHop = totpKey.totpVsHop,
        filePath= totpKey.filePath
    )

    fun toTotpKey(totpEntity: TotpDbEntity) = EncryptedTotpKey(
        id = totpEntity.id,
        email = totpEntity.email,
        name = totpEntity.name,
        category = totpEntity.category,
        secret = totpEntity.secret,
        secretKey = totpEntity.secretKey,
        iv = totpEntity.iv,
        shaStr = totpEntity.shaStr,
        totpVsHop = totpEntity.totpVsHop,
        filePath= totpEntity.filePath
    )
}