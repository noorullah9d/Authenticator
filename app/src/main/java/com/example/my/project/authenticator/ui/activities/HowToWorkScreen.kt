package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.ui.adapters.HowWorksAdapter
import com.example.my.project.authenticator.databinding.ActivityHowToWorkScreenBinding
import com.example.my.project.authenticator.extensions.finishWithAnimation
import com.example.my.project.authenticator.ui.fragments.UsageFragment1
import com.example.my.project.authenticator.ui.fragments.UsageFragment2
import com.example.my.project.authenticator.ui.fragments.UsageFragment3
import com.example.my.project.authenticator.ui.fragments.UsageFragment4
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HowToWorkScreen : AppCompatActivity() {

    private lateinit var binding: ActivityHowToWorkScreenBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHowToWorkScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPager = binding.viewPager


        val fragmentList = arrayListOf(UsageFragment1(), UsageFragment2(), UsageFragment3(), UsageFragment4())
        val adapter = HowWorksAdapter(this, fragmentList)
        viewPager.adapter = adapter


        val darkColor = ContextCompat.getColor(this, R.color.secondary_color)
        val lightColor = ContextCompat.getColor(this, R.color.card_light_color)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishWithAnimation()
            }
        })



        binding.apply {

            icBack.setOnClickListener {
                finishWithAnimation()
            }

            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
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


            next.setOnClickListener {
                if (viewPager.currentItem == 3) {
                    finishWithAnimation()
                } else {
                    viewPager.currentItem++
                }

            }


        }


    }


    private fun lightColor(carView1: MaterialCardView, carView2: MaterialCardView, carView3: MaterialCardView, color: Int) {
        carView1.setBackgroundColor(color)
        carView2.setBackgroundColor(color)
        carView3.setBackgroundColor(color)
    }


}