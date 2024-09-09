package com.example.my.project.authenticator.otp.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my.project.authenticator.otp.data.crypto.AesGcmSecretEncryptor
import com.example.my.project.authenticator.otp.domain.crypto.PasswordHasher
import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.entities.ExportEntity
import com.example.my.project.authenticator.otp.domain.entities.NoEncryptionExport
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository
import com.example.my.project.authenticator.otp.domain.usecases.AddNewTotpUseCase
import com.example.my.project.authenticator.otp.domain.usecases.ImportKeysUseCase
import kotlinx.coroutines.flow.stateIn
import org.apache.commons.codec.binary.Base32
import java.io.InputStream
import javax.crypto.AEADBadTagException
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class ImportViewModel @Inject constructor(
    private val repository: TotpKeyRepository,
    private val repositoryEncryptor: SecretEncryptor,
    private val passwordHasher: PasswordHasher
) : ViewModel() {


    private val addNewTotpUseCase = AddNewTotpUseCase(repository, repositoryEncryptor)
    private val importUseCase = ImportKeysUseCase()
    private lateinit var exportEntity: ExportEntity

    var importScreenState by mutableStateOf(ImportScreenState())
        private set

    suspend fun prepareAndCheckPassword(inputStream: InputStream): Boolean {
        exportEntity = importUseCase.prepare(inputStream)
        return exportEntity !is NoEncryptionExport
    }

    suspend fun import(password: String? = null) {
        try {
            importScreenState = importScreenState.copy(errorText = null)
            val importedKeys = importUseCase(exportEntity) { salt ->
                password?.let {
                    val secretKey = SecretKeySpec(passwordHasher.hash(password.toByteArray(), salt), "AES")
                    AesGcmSecretEncryptor(secretKey)
                }
            }
            val storedKeys = repository.getAllKeys().stateIn(viewModelScope).value
            importScreenState = importScreenState.copy(importedKeys = importedKeys.map { unencryptedKey ->
                ImportedItemState(
                    unencryptedKey.name,
                    unencryptedKey.base32Secret,
                    nameSimilarity = storedKeys.find { it.name == unencryptedKey.name }?.name,
                    secretSimilarity = storedKeys.find {
                        repositoryEncryptor.decrypt(it.secret, it.iv)
                            .contentEquals(Base32().decode(unencryptedKey.base32Secret.toByteArray()))
                    }?.name
                )
            })
        } catch (e: AEADBadTagException) {
            importScreenState = importScreenState.copy(errorText = "Error: wrong key or broken file")
        } catch (e: Exception) {
            importScreenState = importScreenState.copy(errorText = "Unexpected ${e.message}, while importing file")
        }
    }

    suspend fun addSelected() {
        importScreenState.importedKeys?.filter { it.checked }?.forEach {
            addNewTotpUseCase(Base32().decode(it.secretKey), it.name)
        }
    }

    fun changeCheck(index: Int) {
        importScreenState = importScreenState.copy(
            importedKeys = importScreenState.importedKeys?.toMutableList()?.apply {
                this[index] = this[index].copy(checked = !this[index].checked)
            }
        )
    }




}