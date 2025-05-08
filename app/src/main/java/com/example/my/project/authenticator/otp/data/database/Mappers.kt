package com.example.my.project.authenticator.otp.data.database

import com.example.my.project.authenticator.otp.domain.entities.PasswordEntity
import com.example.my.project.authenticator.otp.domain.model.Password

fun PasswordEntity.toPassword(): Password {
    return Password(
        id = id,
        name = name,
        url = url,
        emailOrUsername = emailOrUsername,
        password = password,
        notes = notes,
        profileImagePath = this@toPassword.profileImagePath,
        lastModified = lastModified
    )
}

fun Password.toEntity(): PasswordEntity {
    return PasswordEntity(
        id = id,
        name = name,
        url = url,
        emailOrUsername = emailOrUsername,
        password = password,
        notes = notes,
        profileImagePath = profileImagePath,
        lastModified = lastModified
    )
}
