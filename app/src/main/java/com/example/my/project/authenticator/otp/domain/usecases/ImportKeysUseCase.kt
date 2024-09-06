package com.example.my.project.authenticator.otp.domain.usecases

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base32
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.apache.commons.codec.binary.Base64
import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.entities.ExportEntity
import com.example.my.project.authenticator.otp.domain.entities.FullEncryptionExport
import com.example.my.project.authenticator.otp.domain.entities.KeyEncryptionExport
import com.example.my.project.authenticator.otp.domain.entities.NoEncryptionExport
import com.example.my.project.authenticator.otp.domain.entities.UnencryptedKey
import java.io.InputStream

class ImportKeysUseCase {
    @OptIn(ExperimentalSerializationApi::class)
    suspend fun prepare(inputStream: InputStream): ExportEntity {
        return withContext(Dispatchers.IO) {
            Json.decodeFromStream<ExportEntity>(inputStream)
        }
    }

    operator fun invoke(
        exportEntity: ExportEntity,
        getExportEncryptor: (keySalt: ByteArray?) -> SecretEncryptor? = { null }
    ): List<UnencryptedKey> {
        return when (exportEntity) {
            is NoEncryptionExport -> exportEntity.keysList

            is KeyEncryptionExport -> {
                val base64 = Base64()
                // apache Base64.decode doesn't work with String on Android
                val exportEncryptor = getExportEncryptor(base64.decode(exportEntity.base64EncryptionKeySalt?.toByteArray()))!!
                exportEntity.keysList.map {
                    val plainSecret = exportEncryptor.decrypt(
                        base64.decode(it.base64Secret.toByteArray()),
                        base64.decode(it.base64Iv.toByteArray())
                    )
                    UnencryptedKey(it.name, Base32().encode(plainSecret).decodeToString())
                }
            }

            is FullEncryptionExport -> {
                val base64 = Base64()
                val exportEncryptor = getExportEncryptor(base64.decode(exportEntity.base64EncryptionKeySalt?.toByteArray()))!!
                val json = exportEncryptor.decrypt(
                    base64.decode(exportEntity.base64Data.toByteArray()),
                    base64.decode(exportEntity.base64Iv.toByteArray())
                )
                Json.decodeFromString<List<UnencryptedKey>>(json.decodeToString())
            }
        }
    }
}