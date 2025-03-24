package com.example.my.project.authenticator.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PrefsHelper {
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getLong(key: String, default: Long = 0L): Long {
        return sharedPreferences.getLong(key, default)
    }

    fun putLong(key: String, value: Long) {
        sharedPreferences.edit { putLong(key, value) }
    }

    fun setStringPreference(key: String, value: String) {
        sharedPreferences.edit { putString(key, value) }
    }

    fun getStringPreference(key: String, def: String = ""): String {
        return sharedPreferences.getString(key, def)!!
    }

    var isOnBoardingShown
        get() = sharedPreferences.getBoolean(IS_ONBOARDING_SHOWN, false)
        set(value) = sharedPreferences.edit { putBoolean(IS_ONBOARDING_SHOWN, value)}

    var isLanguageShown
        get() = sharedPreferences.getBoolean(IS_LANGUAGE_SHOWN, false)
        set(value) = sharedPreferences.edit { putBoolean(IS_LANGUAGE_SHOWN, value)}

    var isAdsRemoved
        get() = sharedPreferences.getBoolean(IS_PREMIUM, false)
        set(value) = sharedPreferences.edit { putBoolean(IS_PREMIUM, value) }

    var isUserFirstTime: Boolean
        get() = sharedPreferences.getBoolean(IS_FIRST_TIME, true)
        set(value) = sharedPreferences.edit { putBoolean(IS_FIRST_TIME, value) }


    var userEmail: String
        get() = sharedPreferences.getString(USER_EMAIL, "") ?: ""
        set(value) = sharedPreferences.edit { putString(USER_EMAIL, value) }

    var userPassword: String
        get() = sharedPreferences.getString(USER_PASSWORD, "") ?: ""
        set(value) = sharedPreferences.edit { putString(USER_PASSWORD, value) }

    var userTheme: String
        get() = sharedPreferences.getString(USER_THEME, SYSTEM_DEFAULT) ?: ""
        set(value) = sharedPreferences.edit { putString(USER_THEME, value) }

    var isBackedUp: Boolean
        get() = sharedPreferences.getBoolean(BACKUP, false)
        set(value) = sharedPreferences.edit { putBoolean(BACKUP, value) }

    var firstMain: Boolean
        get() = sharedPreferences.getBoolean(FIRST_TIME_MAIN, false)
        set(value) = sharedPreferences.edit { putBoolean(FIRST_TIME_MAIN, value) }

    var isBackedGone: Boolean
        get() = sharedPreferences.getBoolean(BACK_UP_GONE, false)
        set(value) = sharedPreferences.edit { putBoolean(BACK_UP_GONE, value) }


    var isFingerprintEnabled: Boolean
        get() = sharedPreferences.getBoolean(FINGERPRINT_ENABLED, false)
        set(value) = sharedPreferences.edit { putBoolean(FINGERPRINT_ENABLED, value) }
}