package com.example.my.project.authenticator.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.my.project.authenticator.utils.Constants.isFirstTime

class SharedPreferencesHelper(context: Context) {


    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "my_prefs"
    }


    fun setStringPreference(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getStringPreference(key: String, def: String = ""): String {
        return sharedPreferences.getString(key, def)!!
    }


    var isUserFirstTime: Boolean
        get() = sharedPreferences.getBoolean(isFirstTime, true)
        set(value) = sharedPreferences.edit().putBoolean(isFirstTime, value).apply()


    var userEmail: String
        get() = sharedPreferences.getString(Constants.userEmail, "") ?: ""
        set(value) = sharedPreferences.edit().putString(Constants.userEmail, value).apply()

    var userPassword: String
        get() = sharedPreferences.getString(Constants.userPassword, "") ?: ""
        set(value) = sharedPreferences.edit().putString(Constants.userPassword, value).apply()

    var userTheme: String
        get() = sharedPreferences.getString(Constants.userThemes, "") ?: ""
        set(value) = sharedPreferences.edit().putString(Constants.userThemes, value).apply()

    var isBackedUp: Boolean
        get() = sharedPreferences.getBoolean(Constants.backUp, false)
        set(value) = sharedPreferences.edit().putBoolean(Constants.backUp, value).apply()

    var firstMain: Boolean
        get() = sharedPreferences.getBoolean(Constants.firstTimeMain, false)
        set(value) = sharedPreferences.edit().putBoolean(Constants.firstTimeMain, value).apply()

    var isBackedGone: Boolean
        get() = sharedPreferences.getBoolean(Constants.backUpGone, false)
        set(value) = sharedPreferences.edit().putBoolean(Constants.backUpGone, value).apply()


    var isFingerprintEnabled: Boolean
        get() = sharedPreferences.getBoolean(Constants.FingerprintEnabled, false)
        set(value) = sharedPreferences.edit().putBoolean(Constants.FingerprintEnabled, value).apply()

}