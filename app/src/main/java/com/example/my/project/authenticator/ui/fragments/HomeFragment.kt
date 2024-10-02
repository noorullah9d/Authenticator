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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.adapters.AccountAdapter
import com.example.my.project.authenticator.databinding.FragmentHomeBinding
import com.example.my.project.authenticator.extensions.beGone
import com.example.my.project.authenticator.extensions.beVisible
import com.example.my.project.authenticator.extensions.getFirstCharacter
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.setOnDebouncedClickListener
import com.example.my.project.authenticator.extensions.setProfileImage
import com.example.my.project.authenticator.extensions.showBottomSheetDialog
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.otp.viewModel.HomeViewModel
import com.example.my.project.authenticator.ui.activities.ProfileScreen
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel by viewModels<HomeViewModel>()

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var prefsHelper: SharedPreferencesHelper? = null
    private lateinit var accountAdapter: AccountAdapter

    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsHelper = SharedPreferencesHelper(requireActivity())

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

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


        if (sharedPreferencesHelper.userEmail != "") {
            sharedPreferencesHelper.userEmail.getFirstCharacter()
            binding.icProfile.beGone()
            binding.icProfileText.beVisible()
            binding.icProfileText.setProfileImage(sharedPreferencesHelper.userEmail)
            setFromRemote()
        } else {
            binding.icProfile.beVisible()
            binding.icProfileText.beGone()
            if (homeViewModel.setRemote(sharedPreferencesHelper.userEmail) == 0) {
                placeHolder()
            }
        }

        observerData()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showBottomSheetDialog(
                    onExitClicked = {
                        requireActivity().finishAffinity()
                    },
                    onCancelClicked = {
                    }
                )
            }
        })


        binding.apply {


            icProfile.setOnDebouncedClickListener {
                if (requireActivity().isInternetAvailable()) {
                    changeGoogleAccount()
                } else {
                    toast(getString(R.string.no_internet_connection))
                }
            }


            icProfileText.setOnDebouncedClickListener {
                if (requireActivity().isInternetAvailable()) {
                    changeGoogleAccount()
                } else {
                    toast(getString(R.string.no_internet_connection))
                }
            }


            btnAddCode.setOnClickListener {
                val intent = Intent(requireActivity(), ProfileScreen::class.java)
                intent.putExtra("backStack", 1)
                startActivity(intent)
            }


            signIn.setOnDebouncedClickListener {
                if (requireActivity().isInternetAvailable()) {
                    signInWithGoogle()
                } else {
                    toast(getString(R.string.no_internet_connection))


                }
            }


        }


    }

    private fun placeHolder() {
        count++
        if (count >= 3 && sharedPreferencesHelper.userEmail != "") {
            binding.llPlaceHolderLayout.beVisible()
            binding.oneTimePassword.beGone()
            binding.progressBar.beGone()
            binding.accountData.beGone()
        } else if (sharedPreferencesHelper.userEmail == "") {
            binding.llPlaceHolderLayout.beVisible()
            binding.oneTimePassword.beGone()
            binding.progressBar.beGone()
            binding.accountData.beGone()
        }
    }

    private var count = 0
    private fun observerData() {


        homeViewModel.homeState.observe(viewLifecycleOwner) { homeState ->
            if (homeState.totpList.isEmpty()) {
                placeHolder()
                if (prefsHelper?.userEmail != "")
                    binding.signIn.beGone()


            } else {

                binding.progressBar.beGone()
                binding.llPlaceHolderLayout.beGone()
                binding.oneTimePassword.beVisible()
                binding.accountData.beVisible()



                if (!::accountAdapter.isInitialized) {
                    accountAdapter = AccountAdapter(homeState.totpList.toMutableList()) { position, totpCardState ->

                        lifecycleScope.launch(Dispatchers.IO) {

                            homeViewModel.removeTotpById(totpCardState)


                        }.invokeOnCompletion {
                            CoroutineScope(Dispatchers.Main).launch {

                                if (homeState.totpList.size < accountAdapter.accounts.size) {
                                    val removedItems = accountAdapter.accounts.filter { it !in homeState.totpList }
                                    accountAdapter.removeAccounts(removedItems)
                                }
                            }


                        }
                    }


                    binding.accountData.adapter = accountAdapter
                    binding.accountData.layoutManager = LinearLayoutManager(requireActivity())


                    val swipeToDeleteCallback = accountAdapter.getSwipeToDeleteCallback(requireActivity())
                    val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
                    itemTouchHelper.attachToRecyclerView(binding.accountData)


                } else {

                    accountAdapter.accounts.clear()
                    accountAdapter.notifyDataSetChanged()

                    if (homeState.totpList.size > accountAdapter.accounts.size) {


                        val newItems = homeState.totpList.subList(accountAdapter.accounts.size, homeState.totpList.size)
                        accountAdapter.addAccounts(newItems)


                    } else {

                        homeState.totpList.forEachIndexed { index, updatedAccount ->
                            accountAdapter.updateSecondsLeftAtPosition(index, updatedAccount.secondsLeft)
                            if (updatedAccount.secondsLeft == 30) {
                                accountAdapter.updateOneTimeCodeAtPosition(index, updatedAccount.oneTimeCode)
                            }
                        }


                    }


                }
            }
        }


    }


    private fun changeGoogleAccount() {
        googleSignInClient.revokeAccess().addOnCompleteListener(requireActivity()) {
            signInWithGoogle()
        }
    }

    private fun setFromRemote() {
        lifecycleScope.launch {
            Log.d(TAG, "setFromRemote: $homeViewModel.setRemote(sharedPreferencesHelper.userEmail)")
            if (homeViewModel.setRemote(sharedPreferencesHelper.userEmail) == 0) {
                homeViewModel.fetchFromRemoteAndSave()

                Log.d(TAG, "fetchFromRemoteAndSave: ")

            }
        }.invokeOnCompletion {
            Log.d(TAG, "invokeOnCompletion: ")
            val data = homeViewModel.setRemote(sharedPreferencesHelper.userEmail)
            Log.d(TAG, "setFromRemote: $data")
            if (data == 0) {
                placeHolder()
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
                    homeViewModel.clearTotpData()
                    prefsHelper?.userEmail = email
                    Log.d(TAG, "firebaseAuthWithGoogle: $email")

                    sharedPreferencesHelper.userEmail.getFirstCharacter()
                    binding.icProfile.beGone()
                    binding.icProfileText.beVisible()
                    binding.icProfileText.setProfileImage(sharedPreferencesHelper.userEmail)

                    lifecycleScope.launch {
                        homeViewModel.refreshTotpKeyFlow()
                    }

                    setFromRemote()
                } else {
                    Log.d(TAG, "failed")
                    toast(getString(R.string.not_logged_in))
                }
            }
    }


}


private const val TAG = "HomeFragment"