package com.example.my.project.authenticator.otp.viewModel

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
    private var exportEntity: ExportEntity? = null

    var count = 0
    var importedKeys: ArrayList<ImportedItemState> = ArrayList()
    var errorText: String? = null

    suspend fun prepareAndCheckPassword(inputStream: InputStream): Boolean {
        exportEntity = importUseCase.prepare(inputStream)
        return exportEntity !is NoEncryptionExport
    }

    suspend fun import(password: String? = null) {
        try {
            errorText = null
            val importedKeyList = exportEntity?.let {
                importUseCase(it) { salt ->
                    password?.let {
                        val secretKey = SecretKeySpec(passwordHasher.hash(password.toByteArray(), salt), "AES")
                        AesGcmSecretEncryptor(secretKey)
                    }
                }
            }

            val storedKeys = repository.getAllKeys(sharedPreferencesHelper.userEmail).stateIn(viewModelScope).value
            importedKeys = importedKeyList?.map { unencryptedKey ->
                ImportedItemState(
                    unencryptedKey.name,
                    unencryptedKey.base32Secret,
                    nameSimilarity = storedKeys.find { it.name == unencryptedKey.name }?.name,
                    secretSimilarity = storedKeys.find {
                        repositoryEncryptor.decrypt(it.secret, it.iv)
                            .contentEquals(Base32().decode(unencryptedKey.base32Secret.toByteArray()))
                    }?.name
                )
            }?.toCollection(ArrayList()) ?: arrayListOf()

        } catch (e: AEADBadTagException) {
            errorText = "Error: wrong key or broken file"
        } catch (e: Exception) {
            errorText = "Unexpected ${e.message}, while importing file"
        }
    }

    suspend fun addSelected() {
        importedKeys.filter { it.checked }.forEach {
            if (sharedPreferencesHelper.userEmail != "") {

//                saveFirebase.saveDataToDB(email = sharedPreferencesHelper.userEmail, it.secretKey, it.name)
                saveFirebase.saveDataToDB(email = sharedPreferencesHelper.userEmail,  it.secretKey, it.name, "","")
            }
            addNewTotpUseCase(0, sharedPreferencesHelper.userEmail, "", Base32().decode(it.secretKey), it.name, it.secretKey, it.SHA ?: "SHA1", it.type ?: "TOTP", "")
//            addNewTotpUseCase(System.currentTimeMillis().toInt(), sharedPreferencesHelper.userEmail, "", Base32().decode(it.secretKey), it.name, it.secretKey, shaStr = it.SHA ?: "SHA1", totpVsHop = it.type ?: "TOTP", filePath = "")
        }
    }

    fun changeCheck(importedList: ArrayList<ImportedItemState>, index: Int) {
        importedKeys = importedList


        /* val currentList = importedKeys
        Log.i(TAG, "changeCheck: importedKeys: ${importedKeys.map { it.checked }}")
        currentList[index].checked = !currentList[index].checked
        Log.i(TAG, "changeCheck: currentList: ${currentList.map { it.checked }}")
//        importedKeys = currentList.filter { it.checked } as ArrayList<ImportedItemState>
        Log.d(TAG, "changeCheck: $importedKeys")*/

    }
}

private const val TAG = "ImportViewModel"