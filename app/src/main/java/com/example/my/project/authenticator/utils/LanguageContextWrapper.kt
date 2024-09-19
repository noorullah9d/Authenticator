package com.example.my.project.authenticator.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.*

class LanguageContextWrapper(base: Context?) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context?, newLocale: Locale?): LanguageContextWrapper {
            if (context == null) return LanguageContextWrapper(context)
            val configuration = context.resources.configuration
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(newLocale)
                val localeList = LocaleList(newLocale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
            } else {
                configuration.setLocale(newLocale)
            }
            return LanguageContextWrapper(context.createConfigurationContext(configuration))
        }
    }
}