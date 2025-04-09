package com.example.my.project.authenticator.ui.fragments

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.GoogleSignInBinding
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.setOnDebouncedClickListener
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.utils.GoogleSignInManager
import com.example.my.project.authenticator.utils.PrefsHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class GoogleSignInDialog : BottomSheetDialogFragment() {

    private lateinit var homeViewModel: () -> Unit
    private lateinit var binding: GoogleSignInBinding
    private lateinit var context: Context

    private lateinit var auth: FirebaseAuth

    companion object {
        fun newInstance(homeViewModel: () -> Unit): GoogleSignInDialog {
            val fragment = GoogleSignInDialog()
            fragment.homeViewModel = homeViewModel
            return fragment
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.context = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = GoogleSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> binding.ivLockMode.setAnimation(R.raw.welcome_dark)
            Configuration.UI_MODE_NIGHT_NO -> binding.ivLockMode.setAnimation(R.raw.welcome)
        }

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.isDraggable = false
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            val layoutParams = it.layoutParams
            layoutParams.height = (context.resources.displayMetrics.heightPixels * 0.75).toInt()
            it.layoutParams = layoutParams
        }

        auth = FirebaseAuth.getInstance()

        binding.tvContinueWithoutAccount.setOnClickListener { dismiss() }
        binding.btnStartAccount.setOnDebouncedClickListener {
            if (requireActivity().isInternetAvailable()) {
                startGoogleSignIn()
            } else {
                toast(getString(R.string.no_internet_connection))
            }
        }
    }

    private fun startGoogleSignIn() {
        lifecycleScope.launch {
            GoogleSignInManager.googleSignIn(
                context = context,
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
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    homeViewModel.invoke()
                    PrefsHelper.userEmail = email
                    val user = auth.currentUser
                    toast(getString(R.string.signed_in_successfully))
                    dismiss()
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


private const val TAG = "GoogleSignIn"