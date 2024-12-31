package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.my.project.authenticator.databinding.FragmentSecondOnBoardingBinding
import com.example.my.project.authenticator.model.CardSelectionViewModel
import com.example.my.project.authenticator.utils.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecondOnBoarding : Fragment() {
    private lateinit var binding: FragmentSecondOnBoardingBinding
    private val cardSelectionViewModel by viewModels<CardSelectionViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSecondOnBoardingBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: ${getSelectedTheme()}")

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

private const val TAG = "SecondOnBoarding"