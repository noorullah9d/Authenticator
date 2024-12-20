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
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentBackupBinding
import com.example.my.project.authenticator.extensions.getFirstCharacter
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.setOnDebouncedClickListener
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class BackupFragment : Fragment() {
    private var prefsHelper: SharedPreferencesHelper? = null
    private lateinit var binding: FragmentBackupBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsHelper = SharedPreferencesHelper(requireActivity())

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        Log.d(TAG, "backupFragment ")


        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d(TAG, "onViewCreated: ${result.resultCode}")
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            } else {
                Log.d(TAG, "Google sign-in canceled or failed $result")
            }
        }



        binding.apply {


            ivSystemSelection.setOnCheckedChangeListener { _, isEnabled ->
                Log.d(TAG, "onCheckedChanged: $isEnabled")
                prefsHelper?.isBackedUp = isEnabled
            }

            if (prefsHelper?.isBackedUp!!) {
                ivSystemSelection.isEnabled = true
            }


            logout.setOnClickListener {
                prefsHelper?.userEmail = ""
                prefsHelper?.isBackedUp = false
                prefsHelper?.isBackedGone = false
                findNavController().popBackStack()
            }

            ivProfileImage.text = prefsHelper?.userEmail?.getFirstCharacter().toString()



            backPress.setOnClickListener { findNavController().popBackStack() }

            gmailSwitching.setOnDebouncedClickListener {
                if (requireActivity().isInternetAvailable()) {
                    changeGoogleAccount()
                } else {
                    toast(getString(R.string.no_internet_connection))
                }
            }

        }


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        })


    }


    private fun changeGoogleAccount() {
        googleSignInClient.revokeAccess().addOnCompleteListener(requireActivity()) {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(task: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle: " + account.email)
            firebaseAuthWithGoogle(account.idToken!!, account.email!!)
        } catch (e: ApiException) {
            Log.d(TAG, "Google sign-in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, email: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    prefsHelper?.userEmail = email
                    Log.d(TAG, "firebaseAuthWithGoogle: $email")
                } else {
                    Log.d(TAG, "failed")
                    toast(getString(R.string.not_logged_in))
                }
            }
    }


}

private const val TAG = "BackupFragment"