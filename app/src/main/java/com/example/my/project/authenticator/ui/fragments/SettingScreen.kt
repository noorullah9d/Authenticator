package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.my.project.authenticator.databinding.FragmentSettingScreenBinding
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.ui.activities.FeedbackScreen
import com.example.my.project.authenticator.ui.activities.ImportExportScreen
import com.example.my.project.authenticator.ui.activities.SelectLanguageActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingScreen : Fragment() {

    private lateinit var binding: FragmentSettingScreenBinding

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
                requireActivity().startActivityWithAnimation<SelectLanguageActivity>()
            }

            recentlyDeleted.setOnClickListener {

            }

            importExport.setOnClickListener {
                requireActivity().startActivityWithAnimation<ImportExportScreen>()
            }

            feedback.setOnClickListener {
                requireActivity().startActivityWithAnimation<FeedbackScreen>()
            }

            howToWork.setOnClickListener {

            }

        }


    }

}