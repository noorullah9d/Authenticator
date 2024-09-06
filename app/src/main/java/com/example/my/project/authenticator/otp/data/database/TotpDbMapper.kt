package com.example.my.project.authenticator.otp.data.database

import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey

object TotpDbMapper {
    fun fromTotpKey(totpKey: EncryptedTotpKey) = TotpDbEntity(
        id = totpKey.id,
        name = totpKey.name,
        secret = totpKey.secret,
        iv = totpKey.iv,
    )

    fun toTotpKey(totpEntity: TotpDbEntity) = EncryptedTotpKey(
        id = totpEntity.id,
        name = totpEntity.name,
        secret = totpEntity.secret,
        iv = totpEntity.iv,
    )
}