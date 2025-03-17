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
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentBackupBinding
import com.example.my.project.authenticator.extensions.getFirstCharacter
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.setOnDebouncedClickListener
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.utils.GoogleSignInManager
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
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
    private var prefsHelper: SharedPreferencesHelper? = null
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
        prefsHelper = SharedPreferencesHelper(requireActivity())

        firebaseAuth = FirebaseAuth.getInstance()

        binding.apply {
            ivSystemSelection.setOnCheckedChangeListener { _, isEnabled ->
                Log.d(TAG, "onCheckedChanged: $isEnabled")
                prefsHelper?.isBackedUp = isEnabled
            }

            if (prefsHelper?.isBackedUp!!) {
                ivSystemSelection.isChecked = true
            }

            logout.setOnClickListener {
                logoutUser()
            }

            binding.tvEmail.text = prefsHelper?.userEmail!!
            ivProfileImage.text = prefsHelper?.userEmail?.getFirstCharacter().toString()

            icBack.setOnClickListener { findNavController().popBackStack() }

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
                    findNavController().popBackStack()
                }
            })
    }

    private fun logoutUser() {
        prefsHelper?.userEmail = ""
        prefsHelper?.isBackedUp = false
        prefsHelper?.isBackedGone = false

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
                Log.e("LogoutError", "Error clearing credentials: ${e.message}", e) // ✅ Logs exact error details
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
                    prefsHelper?.userEmail = email
                    binding.tvEmail.text = prefsHelper?.userEmail!!
                    binding.ivProfileImage.text =
                        prefsHelper?.userEmail?.getFirstCharacter().toString()
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
}

private const val TAG = "BackupFragment"