package com.example.my.project.authenticator.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.my.project.authenticator.utils.Constants.isFirstTime

class SharedPreferencesHelper(context: Context) {


    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "my_prefs"
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

    var isBackedGone: Boolean
        get() = sharedPreferences.getBoolean(Constants.backUpGone, false)
        set(value) = sharedPreferences.edit().putBoolean(Constants.backUpGone, value).apply()

}