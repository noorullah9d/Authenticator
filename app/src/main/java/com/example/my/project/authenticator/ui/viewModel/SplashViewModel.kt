package com.example.my.project.authenticator.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my.project.authenticator.otp.domain.repository.RemoteConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val remoteConfigRepository: RemoteConfigRepository
): ViewModel() {

    fun getRemoteConfig() = viewModelScope.launch(Dispatchers.IO) {
        remoteConfigRepository.getRemoteResponse()
    }
}