package com.example.my.project.authenticator.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.admob.FragInterstitial
import com.example.my.project.authenticator.admob.NativeAd
import com.example.my.project.authenticator.admob.admob_interstitial_fragment
import com.example.my.project.authenticator.databinding.FragmentSettingScreenBinding
import com.example.my.project.authenticator.extensions.browse
import com.example.my.project.authenticator.extensions.getLanguageName
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.setOnDebouncedClickListener
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.ui.activities.FeedbackScreen
import com.example.my.project.authenticator.ui.activities.HowToWorkScreen
import com.example.my.project.authenticator.ui.activities.ImportExportScreen
import com.example.my.project.authenticator.ui.activities.SelectLanguageActivity
import com.example.my.project.authenticator.ui.activities.iap.PremiumActivity
import com.example.my.project.authenticator.ui.viewModel.LanguageViewModel
import com.example.my.project.authenticator.utils.DARK
import com.example.my.project.authenticator.utils.GoogleSignInManager
import com.example.my.project.authenticator.utils.LIGHT
import com.example.my.project.authenticator.utils.PRIVACY_POLICY_URL
import com.example.my.project.authenticator.utils.PrefsHelper
import com.example.my.project.authenticator.utils.SYSTEM_DEFAULT
import com.example.my.project.authenticator.utils.TERMS_CONDITIONS_URL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.ra.fingerprint_auth.FingerprintCallback
import com.ra.fingerprint_auth.FingerprintManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingScreen : Fragment() {
    private val languageViewModel by viewModels<LanguageViewModel>()
    private lateinit var binding: FragmentSettingScreenBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        initViews()
        setupClickListeners()
        handleBackPress()
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })
    }

    private fun initViews() {
        binding.apply {
            if (PrefsHelper.userEmail != "") {
                emailText.text = PrefsHelper.userEmail
            } else {
                emailText.text = getText(R.string.backup_your_codes)
            }

            when (PrefsHelper.userTheme) {
                SYSTEM_DEFAULT -> {
                    tvTheme.text = SYSTEM_DEFAULT
                }

                DARK -> {
                    tvTheme.text = DARK
                }

                LIGHT -> {
                    tvTheme.text = LIGHT
                }
            }

            ivUseFingerprintNext.setOnCheckedChangeListener(null)
            ivUseFingerprintNext.isChecked = PrefsHelper.isFingerprintEnabled == true
            ivUseFingerprintNext.setOnCheckedChangeListener { _, isEnabled ->
                if (isEnabled) {
                    if (PrefsHelper.userPassword == "") {
                        findNavController().navigate(R.id.action_settingScreen_to_setPasswordFragment)
                        ivUseFingerprintNext.isChecked = false
                    } else {

                        fingerprint()
                    }
                } else {
                    PrefsHelper.isFingerprintEnabled = false
                }
            }

            tvLanguageCode.text = languageViewModel.getLanguage().getLanguageName()

            if (PrefsHelper.userPassword.isNotEmpty()) {
                tvSetPassword.text = getString(R.string.change_password)
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            languageSelection.setOnClickListener {
                val intent = Intent(requireActivity(), SelectLanguageActivity::class.java)
                intent.putExtra("isFromSettings", true)
                requireActivity().startActivity(intent)
            }

            setPassword.setOnClickListener {
                findNavController().navigate(R.id.action_settingScreen_to_setPasswordFragment)
            }

            appThemes.setOnClickListener {
                FragInterstitial.showAd(
                    requireActivity(),
                    onDismissed = {
                        FragInterstitial.loadAd(
                            requireContext(),
                            admob_interstitial_fragment
                        )
                        findNavController().navigate(R.id.action_settingScreen_to_themesFragment)
                    }
                )
            }

            importExport.setOnClickListener {
                /*FragInterstitial.showAd(
                    requireActivity(),
                    onDismissed = {
                        FragInterstitial.loadAd(
                            requireContext(),
                            admob_interstitial_fragment
                        )
                        requireActivity().startActivityWithAnimation<ImportExportScreen>()
                    }
                )*/
                requireActivity().startActivityWithAnimation<ImportExportScreen>()
            }

            userGuide.setOnClickListener {
                findNavController().navigate(R.id.action_settingScreen_to_userGuideFragment)
            }

            feedback.setOnClickListener {
                requireActivity().startActivityWithAnimation<FeedbackScreen>()
            }

            /*ivSettingsCancel.setOnClickListener {
                val navOptions = NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
                findNavController().navigate(R.id.homeFragment, null, navOptions)
            }*/

            premiumView.setOnClickListener {
                requireActivity().startActivityWithAnimation<PremiumActivity>()
            }

            privacyPolicy.setOnClickListener {
                requireActivity().browse(PRIVACY_POLICY_URL)
            }

            ivBackup.setOnClickListener {
                backup()
            }

            loginMail.setOnDebouncedClickListener {
                backup()
            }

            termsConditions.setOnClickListener {
                requireActivity().browse(TERMS_CONDITIONS_URL)
            }

            howToWork.setOnClickListener {
                /*FragInterstitial.showAd(
                    requireActivity(),
                    onDismissed = {
                        FragInterstitial.loadAd(
                            requireContext(),
                            admob_interstitial_fragment
                        )
                        requireActivity().startActivityWithAnimation<HowToWorkScreen>()
                    }
                )*/
                requireActivity().startActivityWithAnimation<HowToWorkScreen>()
            }
        }
    }

    private fun backup() {
        if (PrefsHelper.userEmail == "") {
            if (requireActivity().isInternetAvailable()) {
                startGoogleSignIn()
            } else {
                toast(getString(R.string.no_internet_connection))
            }
        } else findNavController().navigate(R.id.action_settingScreen_to_backupFragment)
    }

    private fun startGoogleSignIn() {
        lifecycleScope.launch {
            GoogleSignInManager.googleSignIn(
                context = requireContext(),
                apiKey = getString(R.string.web_client_id),
                filterByAuthorizedAccounts = false,
                doOnSuccess = { credentials ->
                    println("Signed in as: ${credentials.id}")
                    firebaseAuthWithGoogle(idToken = credentials.idToken, email = credentials.id)
                },
                doOnError = { exception ->
                    println("Sign in failed: ${exception.message}")
                }
            )
        }
    }

    private fun fingerprint() {
        FingerprintManager.FingerprintBuilder(requireActivity())
            .setTitle("Unlock to use Authenticator").setTitle("Touch the fingerprint sensor")
            .setNegativeButtonText("Dismiss").build().authenticate(object : FingerprintCallback {
                override fun onAuthenticationCancelled() {
                    binding.ivUseFingerprintNext.isChecked = false
                    Log.d(TAG, "onAuthenticationCancelled: ")
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    Log.d(TAG, "onAuthenticationError: ")
                    binding.ivUseFingerprintNext.isChecked = false
                }

                override fun onAuthenticationFailed() {
                    Log.d(TAG, "onAuthenticationFailed: ")
                    binding.ivUseFingerprintNext.isChecked = false
                }

                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                    Log.d(TAG, "onAuthenticationHelp: ")
                }

                override fun onAuthenticationSuccessful() {
                    Log.d(TAG, "onAuthenticationSuccessful: ")
                    PrefsHelper.isFingerprintEnabled = true
                }

                override fun onBiometricAuthenticationInternalError(error: String?) {
                    Log.d(TAG, "onBiometricAuthenticationInternalError: ")
                    binding.ivUseFingerprintNext.isChecked = false
                }

                override fun onBiometricAuthenticationNotAvailable() {
                    Log.d(TAG, "onBiometricAuthenticationNotAvailable: ")
                    toast("Device Not Supported")
                }

                override fun onBiometricAuthenticationNotSupported() {
                    Log.d(TAG, "onBiometricAuthenticationNotSupported: ")
                }

                override fun onBiometricAuthenticationPermissionNotGranted() {
                    Log.d(TAG, "onBiometricAuthenticationPermissionNotGranted: ")
                    binding.ivUseFingerprintNext.isChecked = false
                }

                override fun onSdkVersionNotSupported() {
                    Log.d(TAG, "onSdkVersionNotSupported: ")
                    binding.ivUseFingerprintNext.isChecked = false
                }

            })
    }

    private fun firebaseAuthWithGoogle(idToken: String, email: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    PrefsHelper.userEmail = email
                    binding.emailText.text = email

                    // go to backup
                    // ✅ Safe navigation
                    if (isAdded && view != null && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                        findNavController().navigate(R.id.action_settingScreen_to_backupFragment)
                    }
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "Invalid Credentials"
                        is FirebaseAuthUserCollisionException -> "Email already in use"
                        is FirebaseAuthInvalidUserException -> "Invalid User"
                        else -> "Authentication Failed"
                    }
                    toast(errorMessage)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        NativeAd.admobNativeAd?.destroy()
        NativeAd.admobNativeAd = null
    }

    override fun onDestroy() {
        super.onDestroy()
        NativeAd.admobNativeAd?.destroy()
        NativeAd.admobNativeAd = null
    }
}

private const val TAG = "SettingScreen"