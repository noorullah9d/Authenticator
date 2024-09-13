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
import com.example.my.project.authenticator.utils.EditTotpState
import com.example.my.project.authenticator.utils.HomeState
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import com.example.my.project.authenticator.utils.TotpCardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
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
    private val editTotpUseCase = EditTotpUseCase(totpKeyRepo, secretEncryptor)
    private val generateTotpCodeUseCase = GenerateTotpCodeUseCase(totpCodeGenerator, secretEncryptor, getUnixTime = { System.currentTimeMillis().milliseconds })

    private val totpKeyFlow = totpKeyRepo.getAllKeys(sharedPreferencesHelper.userEmail).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val homeState = MutableLiveData(HomeState())
    private lateinit var oneSecondTimer: Timer

    private var isDataFetched = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            autoUpdate()
        }
    }

    suspend fun setRemote(): Int {
        return totpKeyRepo.getAllData().size
    }


    fun fetchAndSave() {
        saveFirebase.retrieveDataFromDB(sharedPreferencesHelper.userEmail) { accounts, errorMessage ->
            accounts?.forEach { account ->
                val secret = Base32().decode(account.passcode)

                Log.d(TAG, "fetchAndSave: ")
                if (!isDataFetched) {
                    Log.d(TAG, "isDataFetched: ")
                    viewModelScope.launch {
                        Log.d(TAG, "viewModelScope: ")
                        addTotpUseCase(sharedPreferencesHelper.userEmail, secret, account.accountName)
                    }
                }
            }
            isDataFetched = true
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

        if (currentSecondsLeft == (defaultUpdateStepMs / 1000).toInt()) {
            updateStateList()
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                homeState.value = homeState.value?.totpList?.map { it.copy(secondsLeft = currentSecondsLeft) }?.let {
                    homeState.value?.copy(totpList = it)
                }
            }
        }
    }

    private fun updateStateList(keyList: List<EncryptedTotpKey> = totpKeyFlow.value) {
        viewModelScope.launch {
            val list = withContext(Dispatchers.Default) {
                keyList.map {
                    val currentTotp = try {
                        generateTotpCodeUseCase(it)
                    } catch (e: IllegalArgumentException) {
                        -999999
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

    fun addTotp(name: String, base32Secret: String): Boolean {
        if (!isSecretCorrect(base32Secret)) return false
        if (sharedPreferencesHelper.userEmail != "")
            saveFirebase.saveDataToDB(email = sharedPreferencesHelper.userEmail, base32Secret, name)
        val secret = Base32().decode(base32Secret)
        try {
            viewModelScope.launch {
                addTotpUseCase(sharedPreferencesHelper.userEmail, secret, name)
            }
        } catch (e: IllegalArgumentException) {
            return false
        }
        return true
    }

    fun removeTotpById(totpCard: TotpCardState) {
        viewModelScope.launch {

            if (sharedPreferencesHelper.userEmail != "") {
                saveFirebase.deleteAccount(sharedPreferencesHelper.userEmail, totpCard.name)
            }

            val toDelete = totpKeyFlow.value.find { key -> key.id == totpCard.id }
            toDelete?.let {
                totpKeyRepo.removeKey(it)
            }
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
            editTotpUseCase(sharedPreferencesHelper.userEmail, edited.id, edited.name, secret)
        }
    }

    private val base32Regex = Regex("[A-Za-z2-7]+=*")
    private fun isSecretCorrect(secret: String): Boolean {
        return base32Regex.matchEntire(secret) != null
    }
}

private const val TAG = "HomeViewModel"