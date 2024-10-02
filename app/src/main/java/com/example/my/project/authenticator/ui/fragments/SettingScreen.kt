package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentSettingScreenBinding
import com.example.my.project.authenticator.extensions.privacyPolicy
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.otp.viewModel.HomeViewModel
import com.example.my.project.authenticator.ui.activities.FeedbackScreen
import com.example.my.project.authenticator.ui.activities.HowToWorkScreen
import com.example.my.project.authenticator.ui.activities.ImportExportScreen
import com.example.my.project.authenticator.ui.activities.SelectLanguageActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingScreen : Fragment() {
    private val homeViewModel by viewModels<HomeViewModel>()
    private lateinit var binding: FragmentSettingScreenBinding
    private var lastBackPressedTime: Long = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            languageSelection.setOnClickListener {
//                toast("coming soon")
                requireActivity().startActivityWithAnimation<SelectLanguageActivity>()
            }

            importExport.setOnClickListener {
                requireActivity().startActivityWithAnimation<ImportExportScreen>()
            }

            feedback.setOnClickListener {
                requireActivity().startActivityWithAnimation<FeedbackScreen>()
            }


            privacyPolicy.setOnClickListener {
                requireActivity().privacyPolicy("https://galixo.ai/authenticator/privacy-policy")
            }


            termsConditions.setOnClickListener {
                requireActivity().privacyPolicy("https://galixo.ai/authenticator/terms-and-conditions")
            }

            howToWork.setOnClickListener {
                requireActivity().startActivityWithAnimation<HowToWorkScreen>()
            }



            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_settingScreen_to_homeFragment)
                }
            })


        }


    }

}