package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.admob.NativeAd
import com.example.my.project.authenticator.admob.admob_native_onboarding
import com.example.my.project.authenticator.ui.adapters.HowWorksAdapter
import com.example.my.project.authenticator.databinding.ActivityHowToWorkScreenBinding
import com.example.my.project.authenticator.databinding.GntSmallBinding
import com.example.my.project.authenticator.databinding.ShimmerSmallNativeBinding
import com.example.my.project.authenticator.extensions.finishWithAnimation
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.safeAddView
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.ui.fragments.UsageFragment1
import com.example.my.project.authenticator.ui.fragments.UsageFragment2
import com.example.my.project.authenticator.ui.fragments.UsageFragment3
import com.example.my.project.authenticator.ui.fragments.UsageFragment4
import com.example.my.project.authenticator.utils.PrefsHelper.isAdsRemoved
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HowToWorkScreen : AppCompatActivity() {
    private lateinit var binding: ActivityHowToWorkScreenBinding
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHowToWorkScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewPager = binding.viewPager

        loadAndShowAdd()
        setupViewPager()
        setupClickListeners()
        handleBackPress()
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishWithAnimation()
            }
        })
    }

    private fun setupClickListeners() {
        binding.apply {
            icBack.setOnClickListener {
                finishWithAnimation()
            }

            next.setOnClickListener {
                if (viewPager.currentItem == 3) {
                    finishWithAnimation()
                } else {
                    viewPager.currentItem++
                }
            }
        }
    }

    private fun setupViewPager() {
        val fragmentList =
            arrayListOf(UsageFragment1(), UsageFragment2(), UsageFragment3(), UsageFragment4())
        val adapter = HowWorksAdapter(this, fragmentList)
        viewPager.adapter = adapter

        val darkColor = ContextCompat.getColor(this, R.color.secondary_color)
        val lightColor = ContextCompat.getColor(this, R.color.card_light_color)

        binding.apply {
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                    when (position) {
                        0 -> {
                            next.text = getString(R.string.next)
                            mcvFirst.setBackgroundColor(darkColor)
                            ivProgress.setImageResource(R.drawable.first_ui)
                            lightColor(mcvSecond, mcvThird, mcvFourth, lightColor)
                        }

                        1 -> {
                            next.text = getString(R.string.next)
                            mcvSecond.setBackgroundColor(darkColor)
                            ivProgress.setImageResource(R.drawable.second_ui)
                            lightColor(mcvFirst, mcvThird, mcvFourth, lightColor)
                        }

                        2 -> {
                            next.text = getString(R.string.next)
                            mcvThird.setBackgroundColor(darkColor)
                            ivProgress.setImageResource(R.drawable.third_ui)
                            lightColor(mcvSecond, mcvFirst, mcvFourth, lightColor)
                        }

                        3 -> {
                            mcvFourth.setBackgroundColor(darkColor)
                            ivProgress.setImageResource(R.drawable.fourth_ui)
                            lightColor(mcvSecond, mcvThird, mcvFirst, lightColor)
                            next.text = getString(R.string.done)
                        }
                    }
                }
            })
        }
    }

    private fun loadAndShowAdd() {
        if (!isInternetAvailable() || isAdsRemoved) {
            binding.adFrame.hide()
            return
        }
        binding.adFrame.show()
        val shimmer = ShimmerSmallNativeBinding.inflate(layoutInflater)
        binding.adFrame.apply {
            removeAllViews()
            safeAddView(shimmer.root)
            shimmer.root.startShimmerAnimation()
        }

        if (NativeAd.admobNativeAd != null) {
            showNativeAd()
            return
        }

        NativeAd.result = {
            if (it) {
                showNativeAd()
            } else {
                binding.adFrame.hide()
            }
        }

        NativeAd.loadAd(
            this,
            admob_native_onboarding
        )
    }

    private fun showNativeAd() {
        try {
            binding.apply {
                adFrame.show()
                NativeAd.admobNativeAd?.let {
                    val adView = GntSmallBinding.inflate(layoutInflater)
                    NativeAd.populateNativeAdView(it, adView)
                    adFrame.removeAllViews()
                    adFrame.safeAddView(adView.root)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun lightColor(
        carView1: MaterialCardView,
        carView2: MaterialCardView,
        carView3: MaterialCardView,
        color: Int
    ) {
        carView1.setBackgroundColor(color)
        carView2.setBackgroundColor(color)
        carView3.setBackgroundColor(color)
    }

    override fun onDestroy() {
        super.onDestroy()
        NativeAd.admobNativeAd?.destroy()
        NativeAd.admobNativeAd = null
    }
}