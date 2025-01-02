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
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.adapters.OnboardingAdapter
import com.example.my.project.authenticator.databinding.ActivityOnBoardingBinding
import com.example.my.project.authenticator.extensions.beGone
import com.example.my.project.authenticator.extensions.beVisible
import com.example.my.project.authenticator.extensions.privacyPolicy
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.otp.data.database.Categories
import com.example.my.project.authenticator.otp.viewModel.HomeViewModel
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class OnBoardingActivity : BaseActivity() {
    private lateinit var binding: ActivityOnBoardingBinding

    private val homeViewModel by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        addPolicyAndTerms()
        viewPager()
        onClickView()


    }

    private fun onClickView() {

        homeViewModel.addCategories(Categories(0, "Default"))
        homeViewModel.addCategories(Categories(0, "Office"))
        homeViewModel.addCategories(Categories(0, "Family"))

        binding.btnStart.setOnClickListener {
            when (binding.viewPager.currentItem) {
                0 -> {
                    binding.viewPager.currentItem++
                }

                1 -> {
                    binding.viewPager.currentItem++
                }

                2 -> {
                    val prefsHelper = SharedPreferencesHelper(this)
                    prefsHelper.isUserFirstTime = false
                    startActivityWithAnimation<MainActivity>()
                    finish()
                }
            }

        }


        binding.skip.setOnClickListener {
            val prefsHelper = SharedPreferencesHelper(this)

            prefsHelper.isUserFirstTime = false
            startActivityWithAnimation<MainActivity>()
            finish()
        }

    }

    private fun viewPager() {
        binding.viewPager.adapter = OnboardingAdapter(this)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                when (position) {
                    0 -> {
                        binding.skip.beVisible()
                        binding.btnStart.text = getString(R.string.get_started)
                        binding.icons.setImageResource(R.drawable.ic_first_start)
                    }

                    1 -> {
                        binding.skip.beVisible()
                        binding.btnStart.text = getString(R.string.next)
                        binding.icons.setImageResource(R.drawable.ic_second_start)
                    }

                    2 -> {
                        binding.skip.beGone()
                        binding.btnStart.text = getString(R.string.let_s_go)
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
                privacyPolicy("https://galixo.ai/authenticator/privacy-policy")
            }
        }
        spannableString.setSpan(privacyPolicySpan, privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLUE), privacyPolicyStart, privacyPolicyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val termsOfServiceStart = text.indexOf("Terms of Service")
        val termsOfServiceEnd = termsOfServiceStart + "Terms of Service".length
        val termsOfServiceSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                privacyPolicy("https://galixo.ai/authenticator/terms-and-conditions")
            }
        }
        spannableString.setSpan(termsOfServiceSpan, termsOfServiceStart, termsOfServiceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(UnderlineSpan(), termsOfServiceStart, termsOfServiceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(ForegroundColorSpan(Color.BLUE), termsOfServiceStart, termsOfServiceEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.privacyPolicyTextView.text = spannableString
        binding.privacyPolicyTextView.movementMethod = LinkMovementMethod.getInstance()
    }


}
