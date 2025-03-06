package com.example.my.project.authenticator.ui.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my.project.authenticator.otp.data.database.Categories
import com.example.my.project.authenticator.otp.domain.crypto.HotpCodeGenerator
import com.example.my.project.authenticator.otp.domain.crypto.SaveFirebase
import com.example.my.project.authenticator.otp.domain.crypto.SecretEncryptor
import com.example.my.project.authenticator.otp.domain.crypto.TotpCodeGenerator
import com.example.my.project.authenticator.otp.domain.entities.EncryptedTotpKey
import com.example.my.project.authenticator.otp.domain.repository.TotpKeyRepository
import com.example.my.project.authenticator.otp.domain.usecases.AddCategories
import com.example.my.project.authenticator.otp.domain.usecases.AddNewTotpUseCase
import com.example.my.project.authenticator.otp.domain.usecases.DeleteCategories
import com.example.my.project.authenticator.otp.domain.usecases.EditTotpUseCase
import com.example.my.project.authenticator.otp.domain.usecases.GenerateHotpCodeUseCase
import com.example.my.project.authenticator.otp.domain.usecases.GenerateTotpCodeUseCase
import com.example.my.project.authenticator.otp.domain.usecases.ReplaceTotpUseCase
import com.example.my.project.authenticator.utils.HomeState
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import com.example.my.project.authenticator.utils.TotpCardState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.codec.binary.Base32
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val saveFirebase: SaveFirebase,
    private val totpKeyRepo: TotpKeyRepository,
    private val secretEncryptor: SecretEncryptor,
    totpCodeGenerator: TotpCodeGenerator,
    hotpCodeGenerator: HotpCodeGenerator,
    private val sharedPreferencesHelper: SharedPreferencesHelper
) : ViewModel() {

    private val addTotpUseCase = AddNewTotpUseCase(totpKeyRepo, secretEncryptor)
    private val addCategoriesUseCase = AddCategories(totpKeyRepo)
    private val deleteCategoriesUseCase = DeleteCategories(totpKeyRepo)
    private val replaceTotpUseCase = ReplaceTotpUseCase(totpKeyRepo, secretEncryptor)
    private val editTotpUseCase = EditTotpUseCase(totpKeyRepo, secretEncryptor)
    private val generateTotpCodeUseCase = GenerateTotpCodeUseCase(totpCodeGenerator, secretEncryptor, getUnixTime = { System.currentTimeMillis().milliseconds })
    private val generateHotpCodeUseCase = GenerateHotpCodeUseCase(hotpCodeGenerator, secretEncryptor, sharedPreferencesHelper)


    private val _category = MutableStateFlow("")
    val category: StateFlow<String> get() = _category

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> get() = _searchQuery

    fun setSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    private val combinedFilter = combine(_category, _searchQuery) { category, query ->
        Pair(category, query)
    }

    private var totpKeyFlow = combinedFilter.flatMapLatest { (categoryValue, searchQuery) ->
        totpKeyRepo.getAllKeys(sharedPreferencesHelper.userEmail, categoryValue, searchQuery)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
//    private var totpKeyFlow = totpKeyRepo.getAllKeys(sharedPreferencesHelper.userEmail,"").stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val homeState = MutableLiveData(HomeState())

    private lateinit var oneSecondTimer: Timer

    /* new changes -- start */

    /** Manually generate HOTP when refresh is clicked */
    fun regenerateHOTP(account: TotpCardState) {
        viewModelScope.launch(Dispatchers.IO) {
            val newOTP = try {
                generateHotpCodeUseCase.invoke(totpKeyFlow.value.find { it.id == account.id }!!, isRegenerate = true)
            } catch (e: Exception) {
                999999 // Fallback OTP
            }

            val updatedList = homeState.value?.totpList?.map {
                if (it.id == account.id) it.copy(oneTimeCode = newOTP) else it
            } ?: emptyList()

            homeState.postValue(homeState.value?.copy(totpList = updatedList))
        }
    }

    /* new changes -- end */

    fun setCategory(newCategory: String) {
        if (newCategory == "") {
            _category.value = ""
        } else {
            _category.value = newCategory
        }
    }

    init {
        viewModelScope.launch(Dispatchers.Main) {
            autoUpdate()
            setCategory("")
        }
    }

    fun getAllGroups() = totpKeyRepo.getAllGroups()

    fun refreshTotpKeyFlow() {
        totpKeyFlow = combinedFilter.flatMapLatest { (categoryValue, searchQuery) ->
            totpKeyRepo.getAllKeys(sharedPreferencesHelper.userEmail, categoryValue, searchQuery)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
                    addTotpUseCase(
                        id = 0,
                        email = sharedPreferencesHelper.userEmail,
                        categories = "",
                        secret,
                        account.accountName,
                        account.passcode,
                        shaStr = account.shaStr,
                        totpVsHop = account.totpVsHop,
                        filePath = account.filePath
                    )
                }
            }
        }
    }

    fun addCategories(cats: Categories) = viewModelScope.launch {
        addCategoriesUseCase.invoke(cats)
    }

    fun delete(cats: Int) = viewModelScope.launch {
        deleteCategoriesUseCase.invoke(cats)
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

            // Only generate a new OTP if the account type is "TOTP"
            if (totpCardState.type == "TOTP" && currentSecondsLeft.toLong() == DEFAULT_UPDATE_STEP_MS / 1000) {
                val updatedTotpCode = try {
                    generateTotpCodeUseCase(totpKeyFlow.value[index])
                } catch (e: IllegalArgumentException) {
                    110958
                } catch (e: IndexOutOfBoundsException) {
                    342233
                }
                updatedTotpList[index] = totpCardState.copy(oneTimeCode = updatedTotpCode)

            } else if (totpCardState.type == "HOTP") {
                val updatedTotpCode = try {
                    generateHotpCodeUseCase.invoke(totpKeyFlow.value[index], isRegenerate = false)
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
                        id = it.id, secretKey = it.secretKey, name = it.name, currentTotp, secondsLeft = countSecondsLeft(), cryptography = it.shaStr, type = it.totpVsHop, filePath = it.filePath, category = it.category
                    )
                }
            }

            withContext(Dispatchers.Main) {
                homeState.value = homeState.value?.copy(totpList = list)
            }
        }
    }

    private fun countSecondsLeft(currentTime: Long = System.currentTimeMillis(), timeStep: Long = DEFAULT_UPDATE_STEP_MS): Int {
        return ((timeStep - currentTime % timeStep).toDouble() / 1000).roundToInt()
    }

    suspend fun addTotp(name: String, base32Secret: String, tool: String = "", categories: String = "", shaStr: String, totpVsHop: String, filePath: String, id: Int = 0): Boolean {
        if (!isSecretCorrect(base32Secret)) return false

        if (sharedPreferencesHelper.userEmail != "") {
            saveFirebase.saveDataToDB(email = sharedPreferencesHelper.userEmail, base32Secret, name, tool, categories)
        }

        val secret = Base32().decode(base32Secret)
        return try {
            addTotpUseCase(id, sharedPreferencesHelper.userEmail, categories, secret, name, base32Secret, shaStr = shaStr, totpVsHop = totpVsHop, filePath = filePath)
            refreshTotpKeyFlow()
            true
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, "addTotp: ${e.message}")
            false
        }
    }

    fun replaceTotp(id: Int, name: String, base32Secret: String, tool: String = "", category: String = "Default"): Boolean {
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

    /*fun requestEdit(id: Int) {
        val toEdit = totpKeyFlow.value.find { key -> key.id == id }
        homeState.value = homeState.value?.copy(editingTotp = toEdit?.let {
            EditTotpState(
                id, toEdit.name, Base32().encode(
                    secretEncryptor.decrypt(toEdit.secret, toEdit.iv)
                ).decodeToString()
            )
        })
    }*/

    /*fun editTotp(edited: EditTotpState):Boolean {
        if (!isSecretCorrect(edited.base32Secret)) return false
        val secret = Base32().decode(edited.base32Secret)
        viewModelScope.launch {
            editTotpUseCase(sharedPreferencesHelper.userEmail, edited.id, edited.name, secret, edited.base32Secret)
        }
        return true
    }
*/
    fun clearTotpData() {
        homeState.value = homeState.value?.copy(totpList = emptyList())
    }

    private val base32Regex = Regex("[A-Za-z2-7]+=*")
    private fun isSecretCorrect(secret: String): Boolean {
        val sanitizedSecret = secret.replace(" ", "")
        return base32Regex.matchEntire(sanitizedSecret) != null
    }

    companion object {
        private const val TAG = "HomeViewModel"
        private const val DEFAULT_UPDATE_STEP_MS = 30_000L
    }
}

