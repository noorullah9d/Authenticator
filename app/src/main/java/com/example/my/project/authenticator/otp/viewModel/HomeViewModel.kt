package com.example.my.project.authenticator.otp.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.crypto.TotpCodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository
import com.example.my.project.authenticator.otp.domain.usecases.AddNewTotpUseCase
import com.example.my.project.authenticator.otp.domain.usecases.EditTotpUseCase
import com.example.my.project.authenticator.otp.domain.usecases.GenerateTotpCodeUseCase
import com.example.my.project.authenticator.utils.EditTotpState
import com.example.my.project.authenticator.utils.HomeState
import com.example.my.project.authenticator.utils.TotpCardState
import kotlinx.coroutines.flow.stateIn
import org.apache.commons.codec.binary.Base32
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds


private const val defaultUpdateStepMs = 30_000L

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val totpKeyRepo: TotpKeyRepository,
    private val secretEncryptor: SecretEncryptor,
    totpCodeGenerator: TotpCodeGenerator
) : ViewModel() {
    private val addTotpUseCase = AddNewTotpUseCase(totpKeyRepo, secretEncryptor)
    private val editTotpUseCase = EditTotpUseCase(totpKeyRepo, secretEncryptor)
    private val generateTotpCodeUseCase = GenerateTotpCodeUseCase(
        totpCodeGenerator,
        secretEncryptor,
        getUnixTime = { System.currentTimeMillis().milliseconds }
    )

    private val totpKeyFlow = totpKeyRepo.getAllKeys().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val homeState = mutableStateOf(HomeState())
    private lateinit var oneSecondTimer: Timer

    init {
        viewModelScope.launch {
            autoUpdate()
        }
    }

    // Setup database updates with timer updates
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
            // IllegalStateException: Reading a state that was created after the snapshot was taken or in a snapshot that has not yet been applied
            homeState.value = homeState.value.copy(
                totpList = homeState.value.totpList.map { it.copy(secondsLeft = currentSecondsLeft) }
            )
        }
    }

    private fun updateStateList(keyList: List<EncryptedTotpKey> = totpKeyFlow.value) {
        val list = keyList.map {
            // Try-catch for the rare circumstance that invalid (empty) secret is in the database
            val currentTotp = try {
                generateTotpCodeUseCase(it)
            } catch (e: IllegalArgumentException) {
                -999999
            }
            TotpCardState(
                it.id,
                it.name,
                currentTotp,
                countSecondsLeft()
            )
        }
        homeState.value = homeState.value.copy(totpList = list)
    }

    private fun countSecondsLeft(
        currentTime: Long = System.currentTimeMillis(),
        timeStep: Long = defaultUpdateStepMs
    ): Int {
        return ((timeStep - currentTime % timeStep).toDouble() / 1000).roundToInt()
    }

    // Returns false if any problem arises, otherwise true
    fun addTotp(name: String, base32Secret: String): Boolean {
        if (!isSecretCorrect(base32Secret)) return false
        val secret = Base32().decode(base32Secret)
        try {
            viewModelScope.launch {
                addTotpUseCase(secret, name)
            }
        } catch (e: IllegalArgumentException) {
            return false
        }
        return true
    }

    fun removeTotpById(id: Int) {
        viewModelScope.launch {
            val toDelete = totpKeyFlow.value.find { key -> key.id == id }!!
            totpKeyRepo.removeKey(toDelete)
        }
    }

    fun requestEdit(id: Int) {
        val toEdit = totpKeyFlow.value.find { key -> key.id == id }
        homeState.value = homeState.value.copy(
            editingTotp = toEdit?.let {
                EditTotpState(
                    id, toEdit.name, Base32().encode(
                        secretEncryptor.decrypt(toEdit.secret, toEdit.iv)
                    ).decodeToString()
                )
            }
        )
    }

    fun editTotp(edited: EditTotpState) {
        if (!isSecretCorrect(edited.base32Secret)) return
        val secret = Base32().decode(edited.base32Secret)
        viewModelScope.launch {
            editTotpUseCase(edited.id, edited.name, secret)
        }
    }

    private val base32Regex = Regex("[A-Za-z2-7]+=*")
    fun isSecretCorrect(secret: String): Boolean {
        return base32Regex.matchEntire(secret) != null
    }
}

private const val TAG = "HomeViewModel"