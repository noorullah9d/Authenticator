package com.example.my.project.authenticator.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.ui.adapters.AccountAdapter
import com.example.my.project.authenticator.ui.adapters.CategoryAdapter
import com.example.my.project.authenticator.databinding.FragmentHomeBinding
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.extensions.copyTextToClipboard
import com.example.my.project.authenticator.extensions.getFirstCharacter
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.logFirebaseEvent
import com.example.my.project.authenticator.extensions.setOnDebouncedClickListener
import com.example.my.project.authenticator.extensions.showCustomDialog
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.model.CardSelectionViewModel
import com.example.my.project.authenticator.ui.viewModel.HomeViewModel
import com.example.my.project.authenticator.ui.activities.HowToWorkScreen
import com.example.my.project.authenticator.ui.activities.ProfileScreen
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import com.example.my.project.authenticator.utils.TotpCardState
import com.example.my.project.authenticator.utils.UiState
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel by viewModels<HomeViewModel>()

    private val selectionViewModel by viewModels<CardSelectionViewModel>()

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private var prefsHelper: SharedPreferencesHelper? = null
    private lateinit var accountAdapter: AccountAdapter
    private var adapter: CategoryAdapter? = null


    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsHelper = SharedPreferencesHelper(requireActivity())

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    val data = result.data
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    handleSignInResult(task)
                } else {
                    toast("Google sign-in canceled or failed")
                }
            }

        observerData()
        backPress()
        clickListeners()
    }

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when {
                        binding.clTopLayout.visibility == View.VISIBLE -> {
                            requireActivity().finishAffinity()
                        }

                        binding.searchView.visibility == View.VISIBLE -> {
                            updateUiState(UiState.DEFAULT)
                        }

                        binding.clDeleteSelection.visibility == View.VISIBLE -> {
                            deselectAll()
                        }
                    }
                }
            })
    }

    private fun updateUiState(state: UiState) {
        binding.apply {
            when (state) {
                UiState.DEFAULT -> {
                    clTopLayout.show()
                    searchView.hide()
                    clDeleteSelection.hide()
                    clEditing.hide()
                }

                UiState.SEARCH -> {
                    clTopLayout.show()
                    searchView.hide()
                }

                UiState.DELETE_SELECTION -> {
                    deselectAll()
                }
            }
        }
    }


    private fun clickListeners() {
        binding.apply {

            rlNotBackUp.setOnDebouncedClickListener {
                backup()
            }

            ivCross.setOnDebouncedClickListener {
                rlNotBackUp.hide()
                sharedPreferencesHelper.isBackedGone = true
            }

            ivBackIcon.setOnClickListener {
                deselectAll()
            }

            if (sharedPreferencesHelper.isBackedGone) {
                rlNotBackUp.hide()
            }

            backup.setOnDebouncedClickListener {
                if (requireActivity().isInternetAvailable()) {
                    changeGoogleAccount()
                } else {
                    toast(getString(R.string.no_internet_connection))
                }
            }

            howAppWorks.setOnClickListener {
                requireActivity().startActivityWithAnimation<HowToWorkScreen>()
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

            settings.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_settingScreen)
            }

            ivSearchView.setOnClickListener {
                clTopLayout.hide()
                searchView.show()
                search.requestFocus()
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.showSoftInput(view?.findFocus(), 0)
            }

            tvCancel.setOnClickListener {
                clTopLayout.show()
                searchView.hide()
            }

            search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {

                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    homeViewModel.setSearchQuery(newText ?: "")
                    return true
                }
            })

            btnStartOpt.setOnClickListener {
                clickAddAccount()
            }

            faButton.setOnClickListener {
                clickAddAccount()
            }

            menuIcon.setOnClickListener {
                popupMenu(it)
            }

            edit.setOnClickListener {


                val selectedItems = accountAdapter.getSelectedAccounts()[0]

                val intent = Intent(requireActivity(), ProfileScreen::class.java)
                intent.putExtra("key_name", selectedItems.name)
                intent.putExtra("id", selectedItems.id)
                intent.putExtra("secret_key", selectedItems.secretKey)
                intent.putExtra("tool", "")
                intent.putExtra("SHA", selectedItems.cryptography)
                intent.putExtra("filePath", selectedItems.filePath)
                intent.putExtra("OTP", selectedItems.type)
                intent.putExtra("category", selectedItems.category)
                intent.putExtra("edit", 1)
                startActivity(intent)
                if (::accountAdapter.isInitialized)
                    accountAdapter.deselectAll()

                clDeleteSelection.hide()
                clTopLayout.show()
                binding.clEditing.hide()

            }

            copy.setOnClickListener {
                try {

                    val selectedItems = accountAdapter.getSelectedAccounts()[0].oneTimeCode

                    requireActivity().copyTextToClipboard(selectedItems.toString())
                } catch (e: Exception) {
                    Log.d(TAG, "clickListeners: ${e.message}")
                }
            }
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

    private fun deselectAll() {
        accountAdapter.deselectAll()
        binding.clDeleteSelection.hide()
        binding.clTopLayout.show()
        binding.clEditing.hide()
        binding.faButton.show()
    }

    private fun popupMenu(view: View) {
        val popupMenu = PopupMenu(requireActivity(), view)
        popupMenu.menuInflater.inflate(R.menu.account_options_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_delete -> {
                    deleteAccounts()
                    true
                }

                R.id.menu_copy -> {
                    accountAdapter.selectAll()
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun deleteAccounts() {
        val selectedItems = accountAdapter.getSelectedAccounts()
        if (selectedItems.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.IO) {
                selectedItems.forEach { account ->
                    homeViewModel.removeTotpById(account)
                }
            }.invokeOnCompletion {
                CoroutineScope(Dispatchers.Main).launch {
                    accountAdapter.removeAccounts(selectedItems)
                    refreshCurrentFragment()
                }
            }
        }
    }

    private fun refreshCurrentFragment() {
        val id = findNavController().currentDestination?.id
        findNavController().popBackStack(id!!, true)
        findNavController().navigate(id)
    }

    private fun clickAddAccount() {
        requireActivity().logFirebaseEvent("scan_option", mapOf("passkey" to "clicked"))
        requireActivity().showCustomDialog { result ->

            when (result) {
                "ivScanQR" -> {
                    val intent = Intent(requireActivity(), ProfileScreen::class.java)
                    intent.putExtra("edit", 0)
                    intent.putExtra("bundle", "ivScanQR")
                    intent.putExtra("backStack", 1)
                    startActivity(intent)
                }

                "ivEnterKey" -> {
                    val intent = Intent(requireActivity(), ProfileScreen::class.java)
                    intent.putExtra("bundle", "ivEnterKey")
                    intent.putExtra("edit", 0)
                    intent.putExtra("backStack", 1)
                    startActivity(intent)
                }

                "dismiss" -> {
//                            btnStartOpt.setImageResource(R.drawable.add)
                }
            }
        }
    }

    private fun placeHolder() {
        binding.apply {
            llPlaceHolderLayout.show()
            faButton.hide()
            progressBar.hide()
            accountData.hide()
            /*llBackUphoworks.beVisible()
            btnStartOpt.beVisible()*/
            buttonsPlaceHolders.show()
            /*count++
            if (count >= 3 && sharedPreferencesHelper.userEmail != "") {
                llPlaceHolderLayout.beVisible()
                faButton.beGone()
                progressBar.beGone()
                accountData.beGone()
                llBackUphoworks.beVisible()
                btnStartOpt.beVisible()
            } else if (sharedPreferencesHelper.userEmail == "") {
                llPlaceHolderLayout.beVisible()
                faButton.beInVisible()
                progressBar.beGone()
                accountData.beGone()
                llBackUphoworks.beVisible()
                btnStartOpt.beVisible()

            }*/
        }
    }

    private fun observerData() {
        binding.apply {
            lifecycleScope.launch {
                homeViewModel.getAllGroups().collectLatest {
                    adapter = CategoryAdapter(it, selectionViewModel, catsId = { _ ->
                    }, groupCallBack = { group ->
                        if (group == "Default") homeViewModel.setCategory("Default")
                        else homeViewModel.setCategory(group)

                        /*if (::accountAdapter.isInitialized) {
                            accountAdapter.deselectAll()
                            clDeleteSelection.beGone()
                            if (searchView.visibility != View.VISIBLE) {
                                clTopLayout.beVisible()
                                searchView.beGone()
                            }

                            binding.clEditing.beGone()
                        }*/
                    })

                    categoriesAccount.layoutManager = LinearLayoutManager(
                        requireActivity(),
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    categoriesAccount.adapter = adapter
                }
            }

            selectionViewModel.selectedCategoryIndex.observe(viewLifecycleOwner) { selectedIndex ->
                adapter?.updateSelectedIndex(selectedIndex)
            }

            emailCondition()

            homeViewModel.homeState.observe(viewLifecycleOwner) { homeState ->

                Log.d(TAG, "observerData: accounts = ${homeState.totpList}")
                if (homeState.totpList.isNotEmpty()) {
                    if (clEditing.visibility == View.VISIBLE) faButton.hide()
                    else faButton.show()

                    progressBar.hide()
                    buttonsPlaceHolders.hide()
                    llPlaceHolderLayout.hide()
                    accountData.show()

                    if (!::accountAdapter.isInitialized) {
                        accountAdapter =
                            AccountAdapter(
                                accounts = /*homeState.totpList.toMutableList()*/mutableListOf(),
                                onDeleteSelected = { position, totpCardState ->
                                    deleteSelection(totpCardState)
                                },
                                onHOTPRefreshClicked = { account ->
                                    homeViewModel.generateHOTP(account)
                                }
                            )

                        accountAdapter.updateAccounts(homeState.totpList)

                        binding.accountData.adapter = accountAdapter
                        binding.accountData.layoutManager = LinearLayoutManager(requireActivity())
                    } else {
//                        Log.d(TAG, "observerData: isInitialized")
                        accountAdapter.updateAccounts(homeState.totpList)
                    }
                } else {
                    Log.d(TAG, "observerData: placeHolder")
                    placeHolder()
                }
            }
        }
    }

    private fun deleteSelection(totpCardState: List<TotpCardState>) {
        binding.apply {
            when (totpCardState.size) {
                0 -> {
                    accountAdapter.deselectAll()
                    clDeleteSelection.hide()
                    clTopLayout.show()
                    clEditing.hide()
                    faButton.hide()
                }

                1 -> {
                    clEditing.show()
                    faButton.hide()
                    clTopLayout.hide()
                    clDeleteSelection.show()
                }

                else -> {
                    clEditing.hide()
                }
            }
        }
    }

    private fun emailCondition() {
        binding.apply {
            if (sharedPreferencesHelper.userEmail != "") {
                sharedPreferencesHelper.userEmail.getFirstCharacter()
                if (sharedPreferencesHelper.isBackedUp) {
                    bgRectangle.setImageResource(R.drawable.ic_backed_up)
                    ivBlock.hide()
                    tvBackedUp.text = getString(R.string.your_data_is_backed_up_successfully)
                    ivCross.show()
                    ivBackedUp.show()
                    ivNext.hide()
                }

//                setFromRemote()
            } else {
                bgRectangle.setImageResource(R.drawable.ic_back_up_frame)
                tvBackedUp.text = getString(R.string.data_is_not_backed_up_yet)
                ivCross.hide()
                ivBackedUp.hide()
                ivNext.show()
                /*if (homeViewModel.setRemote(sharedPreferencesHelper.userEmail) == 0) {
                    placeHolder()
                }*/
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
            if (homeViewModel.setRemote(sharedPreferencesHelper.userEmail) == 0) {
                homeViewModel.fetchFromRemoteAndSave()
            }
        }.invokeOnCompletion {
            val data = homeViewModel.setRemote(sharedPreferencesHelper.userEmail)
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
            firebaseAuthWithGoogle(account.idToken!!, account.email!!)
        } catch (e: ApiException) {
            Log.d(TAG, "Google sign-in failed", e)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, email: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                homeViewModel.clearTotpData()
                prefsHelper?.userEmail = email

                sharedPreferencesHelper.userEmail.getFirstCharacter()

                lifecycleScope.launch {
                    homeViewModel.refreshTotpKeyFlow()
                    homeViewModel.setCategory("")
                }

                setFromRemote()

                // go to backup screen
                findNavController().navigate(R.id.action_settingScreen_to_backupFragment)
            } else {
                toast(getString(R.string.not_logged_in))
            }
        }
    }
}

private const val TAG = "HomeFragment"