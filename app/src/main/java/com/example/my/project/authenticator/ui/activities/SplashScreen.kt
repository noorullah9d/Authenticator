package com.example.my.project.authenticator.ui.activities

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.my.project.authenticator.databinding.FragmentSplashBinding
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashScreen : BaseActivity() {

    private lateinit var binding: FragmentSplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val prefsHelper = SharedPreferencesHelper(applicationContext)




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

        val animator = ObjectAnimator.ofInt(binding.progressBar, "progress", 0, 100)
        animator.setDuration(4000)
        animator.start()


    }


}

