package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.admob.NativeAd
import com.example.my.project.authenticator.admob.admob_native_backup
import com.example.my.project.authenticator.databinding.FragmentBackupBinding
import com.example.my.project.authenticator.databinding.GntSmallBinding
import com.example.my.project.authenticator.databinding.ShimmerSmallNativeBinding
import com.example.my.project.authenticator.extensions.getFirstCharacter
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.safeAddView
import com.example.my.project.authenticator.extensions.setOnDebouncedClickListener
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.extensions.showAskPasswordDialog
import com.example.my.project.authenticator.extensions.showLogoutBottomSheet
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.utils.GoogleSignInManager
import com.example.my.project.authenticator.utils.PrefsHelper
import com.example.my.project.authenticator.utils.PrefsHelper.isAdsRemoved
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class BackupFragment : Fragment() {
    private lateinit var binding: FragmentBackupBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.apply {
            ivSystemSelection.setOnCheckedChangeListener { _, isEnabled ->
                Log.d(TAG, "onCheckedChanged: $isEnabled")
                PrefsHelper.isBackedUp = isEnabled
            }

            if (PrefsHelper.isBackedUp) {
                ivSystemSelection.isChecked = true
            }

            logout.setOnClickListener {
                requireActivity().showLogoutBottomSheet(
                    onLogout = {
                        logoutUser()
                    }
                )
            }

            binding.tvEmail.text = PrefsHelper.userEmail
            ivProfileImage.text = PrefsHelper.userEmail.getFirstCharacter().toString()

            icBack.setOnClickListener {
                val navOptions = NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
                findNavController().navigate(R.id.homeFragment, null, navOptions)
            }

            gmailSwitching.setOnDebouncedClickListener {
                if (requireActivity().isInternetAvailable()) {
                    signOutAndSignInAgain()
                } else {
                    toast(getString(R.string.no_internet_connection))
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val navOptions =
                        NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
                    findNavController().navigate(R.id.homeFragment, null, navOptions)
                }
            }
        )

        loadAndShowAdd()
    }

    private fun loadAndShowAdd() {
        if (!requireContext().isInternetAvailable() || isAdsRemoved) {
            binding.adFrame.hide()
            return
        }
        binding.adFrame.show()
        val shimmer = ShimmerSmallNativeBinding.inflate(layoutInflater)
        binding.adFrame.apply {
            removeAllViews()
            safeAddView(shimmer.root)
            shimmer.root.startShimmerAnimation()
        }

        if (NativeAd.admobNativeAd != null) {
            showNativeAd()
            return
        }

        NativeAd.result = {
            if (it) {
                showNativeAd()
            } else {
                binding.adFrame.hide()
            }
        }

        NativeAd.loadAd(
            requireActivity(),
            admob_native_backup
        )
    }

    private fun showNativeAd() {
        if (isAdded) {
            binding.apply {
                adFrame.show()
                NativeAd.admobNativeAd?.let {
                    val adView = GntSmallBinding.inflate(layoutInflater)
                    NativeAd.populateNativeAdView(it, adView)
                    adFrame.removeAllViews()
                    adFrame.safeAddView(adView.root)
                }
            }
        }
    }

    private fun logoutUser() {
        PrefsHelper.userEmail = ""
        PrefsHelper.isBackedUp = false
        PrefsHelper.isBackedGone = false

        // ✅ Sign out from Firebase
        firebaseAuth.signOut()

        // ✅ Clear Google Credentials
        val credentialManager = CredentialManager.create(requireContext())
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                }
                toast("Logged out successfully")

                // ✅ Only navigate back after clearing credentials
                withContext(Dispatchers.Main) {
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
                Log.e(
                    "LogoutError",
                    "Error clearing credentials: ${e.message}",
                    e
                ) // ✅ Logs exact error details
                toast("Logout failed: ${e.localizedMessage}")
            }
        }
    }

    private fun signOutAndSignInAgain() {
        // ✅ Sign out from Firebase
        firebaseAuth.signOut()

        // ✅ Clear Google Credentials
        val credentialManager = CredentialManager.create(requireContext())
        lifecycleScope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                toast("Signed out successfully")
                // ✅ Relaunch sign-in flow
                startGoogleSignIn()
            } catch (e: Exception) {
                toast("Sign out failed: ${e.message}")
            }
        }
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

    private fun firebaseAuthWithGoogle(idToken: String, email: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    PrefsHelper.userEmail = email
                    binding.tvEmail.text = PrefsHelper.userEmail
                    binding.ivProfileImage.text =
                        PrefsHelper.userEmail.getFirstCharacter().toString()
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

private const val TAG = "BackupFragment"