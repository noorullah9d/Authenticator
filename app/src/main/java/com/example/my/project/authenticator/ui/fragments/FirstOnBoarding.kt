package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.my.project.authenticator.databinding.FragmentOnBoardingScreen1Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FirstOnBoarding : Fragment() {
    private lateinit var binding: FragmentOnBoardingScreen1Binding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnBoardingScreen1Binding.inflate(inflater, container, false)
        return binding.root
    }


}