package com.example.my.project.authenticator.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.adapters.AccountAdapter
import com.example.my.project.authenticator.databinding.FragmentHomeBinding
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.model.Account
import com.example.my.project.authenticator.ui.activities.ProfileScreen
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import com.example.my.project.authenticator.utils.TOTPGenerator
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


class HomeFragment : Fragment() {


    private lateinit var binding: FragmentHomeBinding


    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var prefsHelper : SharedPreferencesHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsHelper = SharedPreferencesHelper(requireActivity())

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val data = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            } else {
                Log.d(TAG, "Google sign-in canceled or failed")
            }
        }


        retrieveDataFromDB(prefsHelper?.userEmail!!)


        binding.apply {





            icProfile.setOnClickListener {

            }


            btnAddCode.setOnClickListener {
                requireActivity().startActivityWithAnimation<ProfileScreen>()
            }


            signIn.setOnClickListener {
                signInWithGoogle()
            }


        }


    }


    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(task: com.google.android.gms.tasks.Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle: " + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.d(TAG, "Google sign-in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d(TAG, "signInWithCredential:success $user")
                } else {
                    Log.d(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }


    private fun retrieveDataFromDB(email: String) {
        val firebaseFirestore = FirebaseFirestore.getInstance()
        firebaseFirestore.collection("users").document(email)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val accountsList = document.get("accounts") as? List<Map<String, String>>
                    val accountObjects = accountsList?.map {
                        Account(it["accountName"].toString(), it["passcode"].toString())
                    } ?: listOf()
                    // Pass the accountObjects to the RecyclerView adapter
                    setupRecyclerView(accountObjects)
                } else {
                    toast("No data found for this email")
                }
            }
            .addOnFailureListener { e ->
                toast("Failed to retrieve data: ${e.message}")
            }
    }


    private fun setupRecyclerView(accounts: List<Account>) {
        binding.accountData.layoutManager = LinearLayoutManager(requireActivity())
        binding.accountData.adapter = AccountAdapter(accounts)
    }


}

private const val TAG = "HomeFragment"