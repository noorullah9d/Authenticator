package com.example.my.project.authenticator.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.my.project.authenticator.ui.fragments.FirstOnBoarding
import com.example.my.project.authenticator.ui.fragments.SecondOnBoarding
import com.example.my.project.authenticator.ui.fragments.ThirdOnBoarding

class OnboardingAdapter (fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FirstOnBoarding()
            1 -> SecondOnBoarding()
            2 -> ThirdOnBoarding()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}