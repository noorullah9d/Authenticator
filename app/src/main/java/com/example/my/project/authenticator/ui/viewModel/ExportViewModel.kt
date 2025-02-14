package com.example.my.project.authenticator.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.my.project.authenticator.otp.data.crypto.AesGcmSecretEncryptor
import com.example.my.project.authenticator.otp.domain.crypto.PasswordHasher
import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository
import com.example.my.project.authenticator.otp.domain.usecases.ExportKeysUseCase
import com.example.my.project.authenticator.otp.domain.usecases.SavingMode
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val repository: TotpKeyRepository,
    private val repositoryEncryptor: SecretEncryptor,
    private val passwordHasher: PasswordHasher,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : ViewModel() {

    suspend fun export(savingMode: SavingMode, plainPassword: String, outputStream: OutputStream) {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val exportEncryptor = if (plainPassword.isNotBlank()) {
            val hash = passwordHasher.hash(plainPassword.encodeToByteArray(), salt)
            val secretKey = SecretKeySpec(hash, "AES")
            AesGcmSecretEncryptor(secretKey)
        } else null
        Log.d(TAG, "export: ${repository.getAllData(sharedPreferencesHelper.userEmail).size}")

        ExportKeysUseCase(
            repository.getAllData(sharedPreferencesHelper.userEmail),
            outputStream,
            repositoryEncryptor,
            exportEncryptor,
            salt,
            savingMode
        )()
    }

}

private const val TAG = "ExportViewModel"