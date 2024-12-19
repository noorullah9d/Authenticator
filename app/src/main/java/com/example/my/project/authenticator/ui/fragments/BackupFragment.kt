package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.databinding.FragmentBackupBinding
import com.example.my.project.authenticator.extensions.getFirstCharacter
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BackupFragment : Fragment() {
    private var prefsHelper: SharedPreferencesHelper? = null
    private lateinit var binding: FragmentBackupBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsHelper = SharedPreferencesHelper(requireActivity())
        binding.apply {
            logout.setOnClickListener {
                prefsHelper?.userEmail = ""
            }

            ivProfileImage.text = prefsHelper?.userEmail?.getFirstCharacter().toString()

            systemSelection.setOnCheckedChangeListener { _, isEnabled ->
                if (isEnabled) {
                    prefsHelper?.userTheme = ""
                }
            }

            backPress.setOnClickListener { findNavController().popBackStack() }


        }


    }

}