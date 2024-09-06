package com.example.my.project.authenticator.otp.domain.usecases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.apache.commons.codec.binary.Base32
import org.apache.commons.codec.binary.Base64
import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.entities.EncryptedKey
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import com.example.my.project.authenticator.otp.domain.entities.ExportEntity
import com.example.my.project.authenticator.otp.domain.entities.FullEncryptionExport
import com.example.my.project.authenticator.otp.domain.entities.KeyEncryptionExport
import com.example.my.project.authenticator.otp.domain.entities.NoEncryptionExport
import com.example.my.project.authenticator.otp.domain.entities.UnencryptedKey
import java.io.OutputStream
import java.security.SecureRandom

enum class SavingMode { NoEncryption, KeyEncryption, FullEncryption }

class ExportKeysUseCase(
    private val keys: List<EncryptedTotpKey>,
    private val outputStream: OutputStream,
    private val repositoryEncryptor: SecretEncryptor,
    private val exportEncryptor: SecretEncryptor? = null,
    private val encryptionKeySalt: ByteArray? = null,
    var savingMode: SavingMode,
) {
    @OptIn(ExperimentalSerializationApi::class)
    suspend operator fun invoke() {
        val export: ExportEntity = when (savingMode) {
            SavingMode.NoEncryption -> {
                NoEncryptionExport(keys.map {
                    UnencryptedKey(
                        it.name,
                        Base32().encode(repositoryEncryptor.decrypt(it.secret, it.iv)).decodeToString()
                    )
                })
            }

            SavingMode.KeyEncryption -> {
                val base64 = Base64()
                KeyEncryptionExport(keys.map {
                    val newIv = ByteArray(exportEncryptor!!.ivSize)
                    SecureRandom().nextBytes(newIv)
                    val newEncryptedSecret = exportEncryptor.encrypt(
                        repositoryEncryptor.decrypt(it.secret, it.iv),
                        newIv
                    )
                    EncryptedKey(
                        it.name,
                        base64.encode(newEncryptedSecret).decodeToString(),
                        base64.encode(newIv).decodeToString()
                    )
                },
                    encryptionKeySalt?.let { base64.encode(it).decodeToString() }
                )
            }

            SavingMode.FullEncryption -> {
                val base64 = Base64()
                val unencryptedKeysList = keys.map {
                    UnencryptedKey(
                        it.name,
                        Base32().encode(repositoryEncryptor.decrypt(it.secret, it.iv)).decodeToString()
                    )
                }
                val newIv = ByteArray(exportEncryptor!!.ivSize)
                SecureRandom().nextBytes(newIv)
                val encryptedKeysList = exportEncryptor.encrypt(
                    Json.encodeToString(unencryptedKeysList).encodeToByteArray(), newIv
                )
                FullEncryptionExport(
                    base64.encode(encryptedKeysList).decodeToString(),
                    base64.encode(newIv).decodeToString(),
                    encryptionKeySalt?.let { base64.encode(it).decodeToString() }
                )
            }
        }
        withContext(Dispatchers.IO) {
            Json.encodeToStream(export, outputStream)
        }
    }
}

