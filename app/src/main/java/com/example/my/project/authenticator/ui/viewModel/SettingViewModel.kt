package com.example.my.project.authenticator.ui.viewModel

import androidx.lifecycle.ViewModel
import com.example.my.project.authenticator.utils.AppPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val appPreference: AppPreference
) : ViewModel() {

}