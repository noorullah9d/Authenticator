package com.example.my.project.authenticator.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my.project.authenticator.otp.domain.model.Password
import com.example.my.project.authenticator.otp.domain.repository.PasswordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository: PasswordRepository
) : ViewModel() {

    private val _passwords = MutableStateFlow<List<Password>>(emptyList())
    val passwords: StateFlow<List<Password>> = _passwords

    fun loadPasswords() {
        viewModelScope.launch {
            _passwords.value = repository.getAllPasswords()
        }
    }

    fun addPassword(password: Password) {
        viewModelScope.launch {
            repository.insertPassword(password)
            loadPasswords()
        }
    }

    fun updatePassword(password: Password) {
        viewModelScope.launch {
            repository.updatePassword(password)
            loadPasswords()
        }
    }

    fun deletePassword(password: Password) {
        viewModelScope.launch {
            repository.deletePassword(password)
            loadPasswords()
        }
    }
}
