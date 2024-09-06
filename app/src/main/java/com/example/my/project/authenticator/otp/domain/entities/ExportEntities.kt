package com.example.my.project.authenticator.otp.domain.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ExportEntity

@Serializable
@SerialName("no_encryption")
class NoEncryptionExport(
    val keysList: List<UnencryptedKey>,
): ExportEntity()

@Serializable
class UnencryptedKey(
    val name: String,
    val base32Secret: String,
)

@Serializable
@SerialName("key_encryption")
class KeyEncryptionExport(
    val keysList: List<EncryptedKey>,
    val base64EncryptionKeySalt: String? = null,
): ExportEntity()

@Serializable
class EncryptedKey(
    val name: String,
    val base64Secret: String,
    val base64Iv: String,
)

@Serializable
@SerialName("full_encryption")
class FullEncryptionExport(
    // decodes to List<UnencryptedKey>
    val base64Data: String,
    val base64Iv: String,
    val base64EncryptionKeySalt: String? = null,
): ExportEntity()