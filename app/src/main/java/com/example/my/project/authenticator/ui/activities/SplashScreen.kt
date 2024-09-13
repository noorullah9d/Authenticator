package com.example.my.project.authenticator.ui.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentSplashBinding
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {

    private lateinit var binding: FragmentSplashBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)


        window.statusBarColor = ContextCompat.getColor(this, R.color.secondary_color)

        // Set navigation bar color (bottom bar)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.secondary_color)

        // Make status bar icons dark
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        // Make navigation bar icons dark (optional for API 26+)
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val prefsHelper = SharedPreferencesHelper(applicationContext)


        lifecycleScope.launch {
            delay(8000)



            if (prefsHelper.isUserFirstTime) {
                startActivityWithAnimation<OnBoardingActivity>()
                finish()
            } else {
                startActivityWithAnimation<MainActivity>()
                finish()
            }

        }


    }


}

private const val TAG = "SplashScreen"