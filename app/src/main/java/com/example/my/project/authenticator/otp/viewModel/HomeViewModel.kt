package com.example.my.project.authenticator.otp.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my.project.authenticator.otp.domain.crypto.SaveFirebase
import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.crypto.TotpCodeGenerator
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository
import com.example.my.project.authenticator.otp.domain.usecases.AddNewTotpUseCase
import com.example.my.project.authenticator.otp.domain.usecases.EditTotpUseCase
import com.example.my.project.authenticator.otp.domain.usecases.GenerateTotpCodeUseCase
import com.example.my.project.authenticator.otp.domain.usecases.ReplaceTotpUseCase
import com.example.my.project.authenticator.utils.EditTotpState
import com.example.my.project.authenticator.utils.HomeState
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import com.example.my.project.authenticator.utils.TotpCardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.codec.binary.Base32
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

private const val defaultUpdateStepMs = 30_000L

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val saveFirebase: SaveFirebase,
    private val totpKeyRepo: TotpKeyRepository,
    private val secretEncryptor: SecretEncryptor,
    totpCodeGenerator: TotpCodeGenerator,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : ViewModel() {
    private val addTotpUseCase = AddNewTotpUseCase(totpKeyRepo, secretEncryptor)
    private val replaceTotpUseCase = ReplaceTotpUseCase(totpKeyRepo, secretEncryptor)
    private val editTotpUseCase = EditTotpUseCase(totpKeyRepo, secretEncryptor)
    private val generateTotpCodeUseCase = GenerateTotpCodeUseCase(totpCodeGenerator, secretEncryptor, getUnixTime = { System.currentTimeMillis().milliseconds })

    private var totpKeyFlow = totpKeyRepo.getAllKeys(sharedPreferencesHelper.userEmail).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val homeState = MutableLiveData(HomeState())


    private lateinit var oneSecondTimer: Timer


    init {
        viewModelScope.launch(Dispatchers.Main) {
            autoUpdate()
        }
    }

    fun refreshTotpKeyFlow() {
        totpKeyFlow = totpKeyRepo.getAllKeys(sharedPreferencesHelper.userEmail)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        viewModelScope.launch(Dispatchers.IO) {
            totpKeyFlow.collect { keyList ->
                updateStateList(keyList)
            }
        }
    }


    fun setRemote(email: String): Int {
        return totpKeyRepo.getAllData(email).size
    }


    fun isKeyExists(name: String, key: String) = totpKeyRepo.isKeyExists(name, key)


    fun fetchFromRemoteAndSave() {
        saveFirebase.retrieveDataFromDB(sharedPreferencesHelper.userEmail) { accounts, _ ->
            accounts?.forEach { account ->
                val secret = Base32().decode(account.passcode)

                viewModelScope.launch(Dispatchers.IO) {
                    addTotpUseCase(sharedPreferencesHelper.userEmail, secret, account.accountName, account.passcode)
                }
            }
        }
    }

    private suspend fun autoUpdate() {
        startTimer()
        totpKeyFlow.collect { keyList ->
            updateStateList(keyList)
        }
    }

    private fun startTimer() {

        if (!::oneSecondTimer.isInitialized) {
            oneSecondTimer = fixedRateTimer(
                null, true,
                initialDelay = 1000 - (System.currentTimeMillis() % 1000),
                period = 1000,
            ) {
                timerUpdates()
            }

        }

    }

    private fun timerUpdates() {
        if (totpKeyFlow.value.isEmpty()) return

        val currentSecondsLeft = countSecondsLeft()
        val updatedTotpList = homeState.value?.totpList?.toMutableList() ?: mutableListOf()

        updatedTotpList.forEachIndexed { index, totpCardState ->

            if (currentSecondsLeft.toLong() == defaultUpdateStepMs / 1000) {
                val updatedTotpCode = try {
                    generateTotpCodeUseCase(totpKeyFlow.value[index])
                } catch (e: IllegalArgumentException) {
                    110958
                } catch (e: IndexOutOfBoundsException) {
                    342233
                }

                updatedTotpList[index] = totpCardState.copy(oneTimeCode = updatedTotpCode)

            }

            updatedTotpList[index] = updatedTotpList[index].copy(secondsLeft = currentSecondsLeft)
        }

        homeState.postValue(homeState.value?.copy(totpList = updatedTotpList))
    }

    private fun updateStateList(keyList: List<EncryptedTotpKey> = totpKeyFlow.value) {
        viewModelScope.launch {
            val list = withContext(Dispatchers.Default) {
                keyList.map {
                    val currentTotp = try {
                        generateTotpCodeUseCase(it)
                    } catch (e: IllegalArgumentException) {
                        Log.d(TAG, "updateStateList: ${e.message}")
                        325786
                    }
                    TotpCardState(
                        it.id, it.name, currentTotp, countSecondsLeft()
                    )
                }
            }

            withContext(Dispatchers.Main) {
                homeState.value = homeState.value?.copy(totpList = list)
            }
        }
    }

    private fun countSecondsLeft(currentTime: Long = System.currentTimeMillis(), timeStep: Long = defaultUpdateStepMs): Int {
        return ((timeStep - currentTime % timeStep).toDouble() / 1000).roundToInt()
    }

    suspend fun addTotp(name: String, base32Secret: String, tool: String = ""): Boolean {
        if (!isSecretCorrect(base32Secret)) return false

        if (sharedPreferencesHelper.userEmail != "") {
            saveFirebase.saveDataToDB(email = sharedPreferencesHelper.userEmail, base32Secret, name, tool)
        }

        val secret = Base32().decode(base32Secret)
        return try {
            addTotpUseCase(sharedPreferencesHelper.userEmail, secret, name, base32Secret)

            refreshTotpKeyFlow()

            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun replaceTotp(id: Int, name: String, base32Secret: String, tool: String): Boolean {
        if (!isSecretCorrect(base32Secret)) return false

        return try {
            viewModelScope.launch {
                replaceTotpUseCase(id, name, base32Secret)
                refreshTotpKeyFlow()
            }
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    suspend fun removeTotpById(totpCard: TotpCardState) {


        if (sharedPreferencesHelper.userEmail != "") {
            saveFirebase.deleteAccount(sharedPreferencesHelper.userEmail, totpCard.name)
        }

        val toDelete = totpKeyFlow.value.find { key -> key.id == totpCard.id }
        toDelete?.let {
            totpKeyRepo.removeKey(it)
        }

    }

    fun requestEdit(id: Int) {
        val toEdit = totpKeyFlow.value.find { key -> key.id == id }
        homeState.value = homeState.value?.copy(editingTotp = toEdit?.let {
            EditTotpState(
                id, toEdit.name, Base32().encode(
                    secretEncryptor.decrypt(toEdit.secret, toEdit.iv)
                ).decodeToString()
            )
        })
    }

    fun editTotp(edited: EditTotpState) {
        if (!isSecretCorrect(edited.base32Secret)) return
        val secret = Base32().decode(edited.base32Secret)
        viewModelScope.launch {
            editTotpUseCase(sharedPreferencesHelper.userEmail, edited.id, edited.name, secret, edited.base32Secret)
        }
    }

    fun clearTotpData() {
        homeState.value = homeState.value?.copy(totpList = emptyList())
    }

    private val base32Regex = Regex("[A-Za-z2-7]+=*")
    private fun isSecretCorrect(secret: String): Boolean {
        val sanitizedSecret = secret.replace(" ", "")
        return base32Regex.matchEntire(sanitizedSecret) != null
    }

}

private const val TAG = "HomeViewModel"