package com.example.my.project.authenticator.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.adapters.OnboardingAdapter
import com.example.my.project.authenticator.databinding.ActivityOnBoardingBinding
import com.example.my.project.authenticator.extensions.privacyPolicy
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addPolicyAndTerms()
        viewPager()
        onClickView()


    }

    private fun onClickView() {

        binding.btnStart.setOnClickListener {

            val prefsHelper = SharedPreferencesHelper(this)

            prefsHelper.isUserFirstTime = false

            if (binding.viewPager.currentItem == 2) {
                startActivityWithAnimation<WelcomeScreen>()
                finish()
            } else {
                binding.viewPager.currentItem++
            }
        }
    }

    private fun viewPager() {
        binding.viewPager.adapter = OnboardingAdapter(this)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                when (position) {
                    0 -> {
                        binding.icons.setImageResource(R.drawable.ic_first_start)

                    }

                    1 -> {
                        binding.icons.setImageResource(R.drawable.ic_second_start)
                    }

                    2 -> {
                        binding.icons.setImageResource(R.drawable.ic_third_start)
                    }
                }
            }
        })
    }

    private fun addPolicyAndTerms() {
        val text = getString(R.string.logs_policy)

        val spannableString = SpannableString(text)


        val privacyPolicyStart = text.indexOf("Privacy Policy")
        val privacyPolicyEnd = privacyPolicyStart + "Privacy Policy".length
        val privacyPolicySpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@OnBoardingActivity, "Privacy Policy Clicked", Toast.LENGTH_SHORT).show()
                privacyPolicy("https://www.google.com")
            }
        }
        spannableString.setSpan(privacyPolicySpan, privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLUE), privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val termsOfServiceStart = text.indexOf("Terms of Service")
        val termsOfServiceEnd = termsOfServiceStart + "Terms of Service".length
        val termsOfServiceSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                Toast.makeText(this@OnBoardingActivity, "Terms of Service Clicked", Toast.LENGTH_SHORT).show()
               privacyPolicy("https://www.google.com")
            }
        }
        spannableString.setSpan(termsOfServiceSpan, termsOfServiceStart, termsOfServiceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), termsOfServiceStart, termsOfServiceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLUE), termsOfServiceStart, termsOfServiceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.privacyPolicyTextView.text = spannableString
        binding.privacyPolicyTextView.movementMethod = LinkMovementMethod.getInstance()
    }


}
