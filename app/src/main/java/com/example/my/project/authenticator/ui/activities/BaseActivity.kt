package com.example.my.project.authenticator.ui.activities

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.my.project.authenticator.utils.AppPreference
import com.example.my.project.authenticator.utils.LanguageContextWrapper
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        if (newBase != null) {
            val appPrefrence = AppPreference(newBase).getStringPreference(AppPreference.LANGUAGES, "en")
            Log.d(TAG,"attachBaseContext: $appPrefrence")
            val locale = Locale.forLanguageTag(appPrefrence)
            super.attachBaseContext(LanguageContextWrapper.wrap(newBase, locale))
        }

    }
}

private const val TAG = "BaseActivity"