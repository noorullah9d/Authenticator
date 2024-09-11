package com.example.my.project.authenticator.otp.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my.project.authenticator.otp.data.crypto.AesGcmSecretEncryptor
import com.example.my.project.authenticator.otp.domain.crypto.PasswordHasher
import com.example.my.project.authenticator.otp.domain.crypto.SaveFirebase
import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.entities.ExportEntity
import com.example.my.project.authenticator.otp.domain.entities.NoEncryptionExport
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository
import com.example.my.project.authenticator.otp.domain.usecases.AddNewTotpUseCase
import com.example.my.project.authenticator.otp.domain.usecases.ImportKeysUseCase
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.stateIn
import org.apache.commons.codec.binary.Base32
import java.io.InputStream
import javax.crypto.AEADBadTagException
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val saveFirebase: SaveFirebase,
    private val repository: TotpKeyRepository,
    private val repositoryEncryptor: SecretEncryptor,
    private val sharedPreferencesHelper: SharedPreferencesHelper,
    private val passwordHasher: PasswordHasher
) : ViewModel() {
    private val addNewTotpUseCase = AddNewTotpUseCase(repository, repositoryEncryptor)
    private val importUseCase = ImportKeysUseCase()
    private lateinit var exportEntity: ExportEntity

    var count = 0

    private val _importScreenState = MutableLiveData<ImportScreenState?>()
    val importScreenState: MutableLiveData<ImportScreenState?> = _importScreenState

    init {
        _importScreenState.value = ImportScreenState()
    }

    suspend fun prepareAndCheckPassword(inputStream: InputStream): Boolean {
        exportEntity = importUseCase.prepare(inputStream)
        return exportEntity !is NoEncryptionExport
    }

    suspend fun import(password: String? = null) {
        try {
            _importScreenState.postValue(_importScreenState.value?.copy(errorText = null))
            val importedKeys = importUseCase(exportEntity) { salt ->
                password?.let {
                    val secretKey = SecretKeySpec(passwordHasher.hash(password.toByteArray(), salt), "AES")
                    AesGcmSecretEncryptor(secretKey)
                }
            }
            val storedKeys = repository.getAllKeys().stateIn(viewModelScope).value
            val updatedState = _importScreenState.value?.copy(
                importedKeys = importedKeys.map { unencryptedKey ->
                    ImportedItemState(
                        unencryptedKey.name,
                        unencryptedKey.base32Secret,
                        nameSimilarity = storedKeys.find { it.name == unencryptedKey.name }?.name,
                        secretSimilarity = storedKeys.find {
                            repositoryEncryptor.decrypt(it.secret, it.iv)
                                .contentEquals(Base32().decode(unencryptedKey.base32Secret.toByteArray()))
                        }?.name
                    )
                }
            )
            _importScreenState.postValue(updatedState)
        } catch (e: AEADBadTagException) {
            _importScreenState.postValue(_importScreenState.value?.copy(errorText = "Error: wrong key or broken file"))
        } catch (e: Exception) {
            _importScreenState.postValue(_importScreenState.value?.copy(errorText = "Unexpected ${e.message}, while importing file"))
        }
    }

    suspend fun addSelected() {
        _importScreenState.value?.importedKeys?.filter { it.checked }?.forEach {
            saveFirebase.saveDataToDB(email = sharedPreferencesHelper.userEmail ?: "", it.secretKey, it.name)
            addNewTotpUseCase(Base32().decode(it.secretKey), it.name)
        }
    }

    fun changeCheck(index: Int) {
        val currentList = _importScreenState.value?.importedKeys?.toMutableList()
        currentList?.let {
            it[index] = it[index].copy(checked = !it[index].checked)
            _importScreenState.postValue(_importScreenState.value?.copy(importedKeys = it))
        }
    }

}

private const val TAG = "ImportViewModel"