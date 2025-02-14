package com.example.my.project.authenticator.model

import androidx.lifecycle.ViewModel
import com.example.my.project.authenticator.utils.AppPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(private val appPreference: AppPreference) : ViewModel() {

    fun setLanguage(value: String) {
        appPreference.setStringPreference(AppPreference.LANGUAGES, value)
    }

    fun getLanguage(): String {
        return appPreference.getStringPreference(AppPreference.LANGUAGES, "en")
    }

    fun setLanguageFirstTime(value: String) {
        appPreference.setStringPreference(AppPreference.LANGUAGES_FIRST_TIME, value)
    }

    fun getLanguageFirstTime(): String {
        return appPreference.getStringPreference(AppPreference.LANGUAGES_FIRST_TIME, "false")
    }
}