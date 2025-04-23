package com.example.my.project.authenticator.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentUsage1Binding
import com.example.my.project.authenticator.ui.viewModel.CardSelectionViewModel
import com.example.my.project.authenticator.utils.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UsageFragment1 : Fragment() {
    private lateinit var binding: FragmentUsage1Binding
    private val cardSelectionViewModel by viewModels<CardSelectionViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUsage1Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.ivIntroImg.setAnimation(R.raw.generate_qr_code_night)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.ivIntroImg.setAnimation(R.raw.qr_code_final)

            }
        }



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


