package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.my.project.authenticator.databinding.FragmentSplashBinding
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.model.CardSelectionViewModel
import com.example.my.project.authenticator.utils.AppTheme
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashScreen : BaseActivity() {
    private lateinit var binding: FragmentSplashBinding
    private val cardSelectionViewModel by viewModels<CardSelectionViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val prefsHelper = SharedPreferencesHelper(applicationContext)

        setAppTheme()

        lifecycleScope.launch {
            delay(4000)

            if (prefsHelper.isUserFirstTime) {
                startActivityWithAnimation<OnBoardingActivity>()
                finish()
            } else {
                if (prefsHelper.userPassword != "") startActivityWithAnimation<PasswordScreen>()
                else startActivityWithAnimation<MainActivity>()
                finish()
            }

        }

    }

    private fun setAppTheme() {
        val themeMode = when (getSelectedTheme()) {
            AppTheme.DARK -> {
                AppCompatDelegate.MODE_NIGHT_YES
            }

            AppTheme.LIGHT -> {
                AppCompatDelegate.MODE_NIGHT_NO
            }

            else -> {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    private fun getSelectedTheme(): AppTheme {
        val themeName = cardSelectionViewModel.getAppTheme()
        return try {
            AppTheme.valueOf(themeName)
        } catch (e: IllegalArgumentException) {
            AppTheme.LIGHT
        }
    }


}

