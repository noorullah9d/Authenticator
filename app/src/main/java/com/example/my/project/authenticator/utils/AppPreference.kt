package com.example.my.project.authenticator.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.android.datatransport.BuildConfig

class AppPreference(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

    fun setStringPreference(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getStringPreference(key: String, def: String = ""): String {
        return sharedPreferences.getString(key, def)!!
    }



    companion object{
        private const val SHARED_PREF = BuildConfig.APPLICATION_ID
        const val LANGUAGES = "phone_languages"
        const val LANGUAGES_FIRST_TIME = "languages_first_time"
    }
}