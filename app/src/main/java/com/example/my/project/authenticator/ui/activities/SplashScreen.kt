package com.example.my.project.authenticator.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.my.project.authenticator.databinding.FragmentSplashBinding
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {

    private lateinit var binding: FragmentSplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefsHelper = SharedPreferencesHelper(applicationContext)


        val animator = ObjectAnimator.ofInt(binding.progressBar, "progress", 0, 100)
        animator.duration = 1000
        animator.interpolator = LinearInterpolator()

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                if (prefsHelper.isUserFirstTime) {
                    startActivityWithAnimation<OnBoardingActivity>()
                    finish()
                } else {
                    startActivityWithAnimation<MainActivity>()
                    finish()
                }
            }
        })

        animator.start()

    }



}

private const val TAG = "SplashScreen"