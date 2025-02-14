package com.example.my.project.authenticator.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentSettingScreenBinding
import com.example.my.project.authenticator.extensions.getLanguageName
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.privacyPolicy
import com.example.my.project.authenticator.extensions.setOnDebouncedClickListener
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.model.LanguageViewModel
import com.example.my.project.authenticator.ui.activities.FeedbackScreen
import com.example.my.project.authenticator.ui.activities.HowToWorkScreen
import com.example.my.project.authenticator.ui.activities.ImportExportScreen
import com.example.my.project.authenticator.ui.activities.SelectLanguageActivity
import com.example.my.project.authenticator.utils.Constants
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.ra.fingerprint_auth.FingerprintCallback
import com.ra.fingerprint_auth.FingerprintManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingScreen : Fragment() {
    private val languageViewModel by viewModels<LanguageViewModel>()
    private lateinit var binding: FragmentSettingScreenBinding
    private var prefsHelper: SharedPreferencesHelper? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsHelper = SharedPreferencesHelper(requireActivity())


        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "onViewCreated: ${result.resultCode}")
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            } else {
                Log.d(TAG, "Google sign-in canceled or failed $result")
                toast("Google sign-in canceled or failed")
            }
        }

        binding.apply {
            if (prefsHelper?.userEmail != "") {
                emailText.text = prefsHelper?.userEmail
            } else {
                emailText.text = getText(R.string.backup_your_codes)
            }

            when (prefsHelper?.userTheme) {
                getString(R.string.system) -> {
                    tvTheme.text = getString(R.string.system)
                }

                Constants.DARK -> {
                    tvTheme.text = Constants.DARK
                }

                Constants.LIGHT -> {
                    tvTheme.text = Constants.LIGHT
                }
            }

            ivUseFingerprintNext.setOnCheckedChangeListener(null)
            ivUseFingerprintNext.isChecked = prefsHelper?.isFingerprintEnabled == true
            ivUseFingerprintNext.setOnCheckedChangeListener { _, isEnabled ->
                if (isEnabled) {
                    if (prefsHelper?.userPassword == "") {
                        findNavController().navigate(R.id.action_settingScreen_to_setPasswordFragment)
                        ivUseFingerprintNext.isChecked = false
                    } else {

                        fingerprint()
                    }
                } else {
                    prefsHelper?.isFingerprintEnabled = false
                }
            }

            tvLanguageCode.text = languageViewModel.getLanguage().getLanguageName()


            languageSelection.setOnClickListener {
                requireActivity().startActivityWithAnimation<SelectLanguageActivity>()
            }

            if (prefsHelper?.userPassword?.isNotEmpty() == true) {
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
                val navOptions = NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
                findNavController().navigate(R.id.homeFragment, null, navOptions)
            }

            rectPremium.setOnClickListener {
                toast("Coming Soon")
            }

            privacyPolicy.setOnClickListener {
                requireActivity().privacyPolicy("https://galixo.ai/authenticator/privacy-policy")
            }

            ivBackup.setOnClickListener {
                backup()
            }


            loginMail.setOnDebouncedClickListener {
                backup()
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

    private fun backup() {
        if (prefsHelper?.userEmail == "") {
            if (requireActivity().isInternetAvailable()) {
                googleSignInClient.revokeAccess().addOnCompleteListener(requireActivity()) {
                    signInWithGoogle()
                }
            } else {
                toast(getString(R.string.no_internet_connection))
            }
        } else findNavController().navigate(R.id.action_settingScreen_to_backupFragment)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun fingerprint() {
        FingerprintManager.FingerprintBuilder(requireActivity()).setTitle("Unlock to use Authenticator").setTitle("Touch the fingerprint sensor").setNegativeButtonText("Dismiss").build().authenticate(object : FingerprintCallback {
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
                prefsHelper?.isFingerprintEnabled = true
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

    private fun handleSignInResult(task: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle: " + account.email)
            firebaseAuthWithGoogle(account.idToken!!, account.email!!)
        } catch (e: ApiException) {
            Log.d(TAG, "Google sign-in failed", e)
            toast("Google sign-in failed")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, email: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                prefsHelper?.userEmail = email
                binding.emailText.text = email

                // go to backup
                findNavController().navigate(R.id.action_settingScreen_to_backupFragment)
            } else {
                Log.d(TAG, "failed")
                toast(getString(R.string.not_logged_in))
            }
        }
    }
}

private const val TAG = "SettingScreen"