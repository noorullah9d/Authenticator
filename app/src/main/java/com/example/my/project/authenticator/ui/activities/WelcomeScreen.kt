package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ActivityWelcomeScreenBinding
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.setOnDebouncedClickListener
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.utils.GoogleSignInManager
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WelcomeScreen : BaseActivity() {

    private lateinit var binding: ActivityWelcomeScreenBinding

    private lateinit var auth: FirebaseAuth

    private var prefsHelper: SharedPreferencesHelper? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWelcomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        prefsHelper = SharedPreferencesHelper(this@WelcomeScreen)

        binding.tvContinueWithoutAccount.setOnClickListener {
            startActivityWithAnimation<MainActivity>()
            finish()
        }

        binding.btnStartAccount.setOnDebouncedClickListener {
            if (isInternetAvailable()) {
                startGoogleSignIn()
            } else {
                toast(getString(R.string.no_internet_connection))
            }
        }
    }

    private fun startGoogleSignIn() {
        lifecycleScope.launch {
            GoogleSignInManager.googleSignIn(
                context = this@WelcomeScreen,
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
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    prefsHelper?.userEmail = email
                    val user = auth.currentUser
                    toast(getString(R.string.signed_in_successfully))
                    startActivityWithAnimation<MainActivity>()
                    finish()
                    Log.d(TAG, "signInWithCredential:success $user")
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "Invalid Credentials"
                        is FirebaseAuthUserCollisionException -> "Email already in use"
                        is FirebaseAuthInvalidUserException -> "Invalid User"
                        else -> "Authentication Failed"
                    }
                    toast(errorMessage)
                    Log.d(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }
}

private const val TAG = "WelcomeScreen"