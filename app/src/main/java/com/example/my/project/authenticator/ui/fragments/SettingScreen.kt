package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentSettingScreenBinding
import com.example.my.project.authenticator.extensions.getLanguageName
import com.example.my.project.authenticator.extensions.privacyPolicy
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.model.LanguageViewModel
import com.example.my.project.authenticator.ui.activities.FeedbackScreen
import com.example.my.project.authenticator.ui.activities.HowToWorkScreen
import com.example.my.project.authenticator.ui.activities.ImportExportScreen
import com.example.my.project.authenticator.ui.activities.SelectLanguageActivity
import com.example.my.project.authenticator.utils.Constants
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import com.ra.fingerprint_auth.FingerprintCallback
import com.ra.fingerprint_auth.FingerprintManager
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingScreen : Fragment() {
    private val languageViewModel by viewModels<LanguageViewModel>()
    private lateinit var binding: FragmentSettingScreenBinding
    private var prefsHelper: SharedPreferencesHelper? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsHelper = SharedPreferencesHelper(requireActivity())
        binding.apply {

            when (prefsHelper?.userTheme) {
                "" -> {
                    tvTheme.text = getString(R.string.system)
                }

                Constants.dark -> {
                    tvTheme.text = Constants.dark
                }

                Constants.light -> {
                    tvTheme.text = Constants.light
                }
            }


            ivUseFingerprintNext.setOnCheckedChangeListener { _, isEnabled ->
                if (isEnabled) {
                    fingerprint()
                }
            }

            tvLanguageCode.text = languageViewModel.getLanguage().getLanguageName()


            languageSelection.setOnClickListener {
                requireActivity().startActivityWithAnimation<SelectLanguageActivity>()
            }
            if (prefsHelper?.userPassword?.isNotEmpty()==true){
                tvSetPassword.text = getString(R.string.change_password)
            }

            setPassword.setOnClickListener {
                findNavController().navigate(R.id.action_settingScreen_to_setPasswordFragment)
            }

            appThemes.setOnClickListener {
                findNavController().navigate(R.id.action_settingScreen_to_themesFragment)
            }

            importExport.setOnClickListener {
                requireActivity().startActivityWithAnimation<ImportExportScreen>()
            }

            feedback.setOnClickListener {
                requireActivity().startActivityWithAnimation<FeedbackScreen>()
            }

            ivSettingsCancel.setOnClickListener {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.homeFragment, true)
                    .build()

                findNavController().navigate(R.id.homeFragment, null, navOptions)
            }


            privacyPolicy.setOnClickListener {
                requireActivity().privacyPolicy("https://galixo.ai/authenticator/privacy-policy")
            }

            ivBackup.setOnClickListener {
                findNavController().navigate(R.id.action_settingScreen_to_backupFragment)
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

    private fun fingerprint() {
        FingerprintManager.FingerprintBuilder(requireActivity()).setTitle("Add your title")
            .setSubtitle("Add your subtitle")
            .setDescription("Add your description")
            .setNegativeButtonText("Add button text")
            .build().authenticate(object : FingerprintCallback {
                override fun onAuthenticationCancelled() {
                    Log.d(TAG, "onAuthenticationCancelled: ")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    Log.d(TAG, "onAuthenticationError: ")
                }

                override fun onAuthenticationFailed() {
                    Log.d(TAG, "onAuthenticationFailed: ")
                }

                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                    Log.d(TAG, "onAuthenticationHelp: ")
                }

                override fun onAuthenticationSuccessful() {
                    Log.d(TAG, "onAuthenticationSuccessful: ")
                }

                override fun onBiometricAuthenticationInternalError(error: String?) {
                    Log.d(TAG, "onBiometricAuthenticationInternalError: ")
                }

                override fun onBiometricAuthenticationNotAvailable() {
                    Log.d(TAG, "onBiometricAuthenticationNotAvailable: ")
                }

                override fun onBiometricAuthenticationNotSupported() {
                    Log.d(TAG, "onBiometricAuthenticationNotSupported: ")
                }

                override fun onBiometricAuthenticationPermissionNotGranted() {
                    Log.d(TAG, "onBiometricAuthenticationPermissionNotGranted: ")
                }

                override fun onSdkVersionNotSupported() {
                    Log.d(TAG, "onSdkVersionNotSupported: ")
                }

            })
    }

}

private const val TAG = "SettingScreen"