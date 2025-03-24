package com.example.my.project.authenticator.model

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.my.project.authenticator.utils.APP_THEME
import com.example.my.project.authenticator.utils.AppTheme
import com.example.my.project.authenticator.utils.PrefsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CardSelectionViewModel @Inject constructor(/*val appPreference: PrefsHelper*/) : ViewModel() {

    var selectedIndex: Int? = 0
    private val _selectedCardIndex = MutableLiveData<Int?>()
    val selectedCardIndex: LiveData<Int?> = _selectedCardIndex


    private val _selectedTheme = MutableLiveData(getSelectedTheme())
    val selectedTheme: LiveData<AppTheme> get() = _selectedTheme


    private val _selectedCategoryIndex = MutableLiveData<Int?>()
    val selectedCategoryIndex: LiveData<Int?> get() = _selectedCategoryIndex

    fun setSelectedCategory(index: Int) {
        _selectedCategoryIndex.value = index
    }


    fun getAppTheme(): String {
        return PrefsHelper.getStringPreference(
            APP_THEME,
            AppTheme.SYSTEM_DEFAULT.name
        )
    }


    fun changeTheme(themeId: AppTheme) {
        PrefsHelper.setStringPreference(APP_THEME, themeId.toString())
        val themeMode = when (themeId) {
            AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(themeMode)
        _selectedTheme.value = themeId
    }

    private fun getSelectedTheme(): AppTheme {
        val themeName = PrefsHelper.getStringPreference(APP_THEME, AppTheme.SYSTEM_DEFAULT.name)
        return try {
            AppTheme.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            AppTheme.SYSTEM_DEFAULT
        }
    }


    fun toggleCardSelection(index: Int) {
        if (_selectedCardIndex.value == index) {
            _selectedCardIndex.value = null
        } else {
            _selectedCardIndex.value = index
        }
    }

    fun getSelectedCardIndex(): Int? {
        return _selectedCardIndex.value
    }
}