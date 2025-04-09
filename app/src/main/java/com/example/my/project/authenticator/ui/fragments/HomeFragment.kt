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
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.admob.FragInterstitial
import com.example.my.project.authenticator.admob.NativeAd
import com.example.my.project.authenticator.databinding.FragmentHomeBinding
import com.example.my.project.authenticator.databinding.GntSmallBinding
import com.example.my.project.authenticator.databinding.ShimmerSmallNativeBinding
import com.example.my.project.authenticator.extensions.copyTextToClipboard
import com.example.my.project.authenticator.extensions.getFirstCharacter
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.logFirebaseEvent
import com.example.my.project.authenticator.extensions.safeAddView
import com.example.my.project.authenticator.extensions.setOnDebouncedClickListener
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.extensions.showCustomDialog
import com.example.my.project.authenticator.extensions.showEditAccountBottomSheet
import com.example.my.project.authenticator.extensions.showExitBottomSheet
import com.example.my.project.authenticator.extensions.showReplaceAccountDialog
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.model.CardSelectionViewModel
import com.example.my.project.authenticator.ui.activities.HowToWorkScreen
import com.example.my.project.authenticator.ui.activities.ProfileScreen
import com.example.my.project.authenticator.ui.adapters.AccountAdapter
import com.example.my.project.authenticator.ui.adapters.CategoryAdapter
import com.example.my.project.authenticator.ui.viewModel.HomeViewModel
import com.example.my.project.authenticator.utils.GoogleSignInManager
import com.example.my.project.authenticator.utils.PrefsHelper
import com.example.my.project.authenticator.utils.PrefsHelper.isAdsRemoved
import com.example.my.project.authenticator.utils.TotpCardState
import com.example.my.project.authenticator.utils.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel by viewModels<HomeViewModel>()

    private val selectionViewModel by viewModels<CardSelectionViewModel>()

    private lateinit var auth: FirebaseAuth
    private lateinit var accountAdapter: AccountAdapter
    private var adapter: CategoryAdapter? = null
    private var isSearchActive: Boolean = false

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
        loadAndShowAdd()
        Log.d(TAG, "admobNativeAd onViewCreated: called")

        auth = FirebaseAuth.getInstance()

        observerData()
        backPress()
        clickListeners()
        loadFragmentInterstitial()
    }

    private fun loadFragmentInterstitial() {
        FragInterstitial.loadAd(
            requireContext(),
            getString(R.string.admob_interstitial_fragment)
        )
    }

    private fun loadAndShowAdd() {
        Log.d(TAG, "admobNativeAd loadAndShowAdd: called")
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
            getString(R.string.admob_native_id_home)
        )
    }

    private fun showNativeAd() {
        Log.d(TAG, "admobNativeAd showNativeAd: called")
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

    private fun backPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    when {
                        binding.clTopLayout.isVisible -> {
                            requireActivity().apply {
                                showExitBottomSheet {
                                    finishAffinity()
                                }
                            }
                        }

                        binding.searchViewLayout.isVisible -> {
                            updateUiState(UiState.DEFAULT)
                            deactivateSearch()
                        }

                        binding.clDeleteSelection.isVisible -> {
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
                    searchViewLayout.hide()
                    clDeleteSelection.hide()
                    clEditing.hide()
                }

                UiState.SEARCH -> {
                    clTopLayout.show()
                    searchViewLayout.hide()
                }

                UiState.DELETE_SELECTION -> {
                    deselectAll()
                }
            }
        }
    }

    private fun startGoogleSignIn() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // ✅ User is already signed in, proceed directly
            toast("Already signed in as: ${currentUser.displayName}")
            return
        }

        lifecycleScope.launch {
            GoogleSignInManager.googleSignIn(
                context = requireContext(),
                apiKey = getString(R.string.web_client_id),
                filterByAuthorizedAccounts = false,
                doOnSuccess = { credentials ->
                    toast("Signed in as: ${credentials.id}")
                    println("Signed in as: ${credentials.id}")
                    firebaseAuthWithGoogle(idToken = credentials.idToken, email = credentials.id)
                },
                doOnError = { exception ->
                    println("Sign in failed: ${exception.message}")
                    toast("Sign in failed: ${exception.message}")
                }
            )
        }
    }

    private fun clickListeners() {
        binding.apply {
            rlNotBackUp.setOnDebouncedClickListener {
                backup()
            }

            ivCross.setOnDebouncedClickListener {
                rlNotBackUp.hide()
                PrefsHelper.isBackedGone = true
            }

            ivBackIcon.setOnClickListener {
                deselectAll()
            }

            if (PrefsHelper.isBackedGone) {
                rlNotBackUp.hide()
            }

            backup.setOnDebouncedClickListener {
                backup()
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
                    startGoogleSignIn()
                } else {
                    toast(getString(R.string.no_internet_connection))
                }
            }

            settings.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_settingScreen)
            }

            ivSearchView.setOnClickListener {
                activateSearch()
            }

            tvCancel.setOnClickListener {
                deactivateSearch()
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


                val selectedItem = accountAdapter.getSelectedAccounts()[0]

                val intent = Intent(requireActivity(), ProfileScreen::class.java)
                intent.putExtra("key_name", selectedItem.name)
                intent.putExtra("id", selectedItem.id)
                intent.putExtra("secret_key", selectedItem.secretKey)
                intent.putExtra("tool", "")
                intent.putExtra("SHA", selectedItem.cryptography)
                intent.putExtra("filePath", selectedItem.filePath)
                intent.putExtra("OTP", selectedItem.type)
                intent.putExtra("category", selectedItem.category)
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

    private fun deactivateSearch() {
        isSearchActive = false
        binding.apply {
            clTopLayout.show()
            categoriesAccount.show()
            rlNotBackUp.show()
            search.setQuery("", false)
            search.clearFocus()
            searchPlaceHolder.hide()
            searchViewLayout.hide()
        }
    }

    private fun activateSearch() {
        isSearchActive = true
        binding.apply {
            clTopLayout.hide()
            categoriesAccount.hide()
            rlNotBackUp.hide()
            searchViewLayout.show()
            search.requestFocus()
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.showSoftInput(view?.findFocus(), 0)
        }
    }

    private fun backup() {
        if (PrefsHelper.userEmail == "") {
            if (requireActivity().isInternetAvailable()) {
                startGoogleSignIn()
            } else {
                toast(getString(R.string.no_internet_connection))
            }
        } else {
//            requireActivity().openFragment(R.id.backupFragment, true)
            findNavController().navigate(R.id.action_homeFragment_to_backupFragment)
        }
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
                    FragInterstitial.showAd(
                        requireActivity(),
                        onDismissed = {
                            loadFragmentInterstitial()
                            val intent = Intent(requireActivity(), ProfileScreen::class.java)
                            intent.putExtra("edit", 0)
                            intent.putExtra("bundle", "ivScanQR")
                            intent.putExtra("backStack", 1)
                            startActivity(intent)
                        }
                    )
                }

                "ivEnterKey" -> {
                    FragInterstitial.showAd(
                        requireActivity(),
                        onDismissed = {
                            loadFragmentInterstitial()
                            val intent = Intent(requireActivity(), ProfileScreen::class.java)
                            intent.putExtra("bundle", "ivEnterKey")
                            intent.putExtra("edit", 0)
                            intent.putExtra("backStack", 1)
                            startActivity(intent)
                        }
                    )
                }

                "dismiss" -> {
//                            btnStartOpt.setImageResource(R.drawable.add)
                }
            }
        }
    }

    private fun placeHolder() {
        binding.apply {
            if (isSearchActive) {
                searchPlaceHolder.show()
            } else {
                llPlaceHolderLayout.show()
                buttonsPlaceHolders.show()
            }

            faButton.hide()
            progressBar.hide()
            accountData.hide()
        }
    }

    private fun observerData() {
        binding.apply {
            lifecycleScope.launch {
                homeViewModel.getAllGroups().collectLatest {
                    adapter = CategoryAdapter(
                        it,
                        selectionViewModel,
                        catsId = { _ -> },
                        groupCallBack = { group ->
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
                        }
                    )

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
                Log.d("EditAccount", "observerData: accounts = ${homeState.totpList.size}")
                if (homeState.totpList.isNotEmpty()) {
                    if (clEditing.isVisible) faButton.hide()
                    else faButton.show()

                    progressBar.hide()
                    if (isSearchActive) searchPlaceHolder.hide()
                    else {
                        buttonsPlaceHolders.hide()
                        llPlaceHolderLayout.hide()
                    }

                    accountData.show()

                    if (!::accountAdapter.isInitialized) {
                        accountAdapter = AccountAdapter(
                            accounts = /*homeState.totpList.toMutableList()*/mutableListOf(),
                            onDeleteSelected = { position, totpCardState ->
                                deleteSelection(totpCardState)
                            },
                            onHOTPRefreshClicked = { account ->
                                homeViewModel.regenerateHOTP(account)
                            },
                            onItemClick = { account ->
//                                requireContext().copyTextToClipboard(account.oneTimeCode.toString())
                                // open editing bottom sheet
                                requireActivity().showEditAccountBottomSheet(
                                    account,
                                    onNameChanged = { newName ->
                                        updateAccountName(newName, account)
                                    },
                                    onCopy = {
                                        requireContext().copyTextToClipboard(account.oneTimeCode.toString())
                                    },
                                    onDelete = {
                                        deleteAccounts()
                                    }
                                )
                            }
                        )

                        accountAdapter.updateAccounts(homeState.totpList)

                        binding.accountData.adapter = accountAdapter
                        binding.accountData.layoutManager = LinearLayoutManager(requireActivity())
                    } else {
                        Log.d(TAG, "observerData: isInitialized")
                        accountAdapter.updateAccounts(homeState.totpList)
                    }
                } else {
                    Log.d(TAG, "observerData: placeHolder")
                    placeHolder()
                }
            }
        }
    }

    private fun updateAccountName(
        newName: String,
        account: TotpCardState
    ) {
        val accountKey = account.secretKey
        /*val isExists = homeViewModel.isKeyExists(newName, accountKey)
        if (isExists > 0) {
            showReplace(isExists, newName, accountKey, account = account)
        } else {*/
        lifecycleScope.launch {
            try {
                val addResult = withContext(Dispatchers.IO) {
                    homeViewModel.addTotp(
                        newName,
                        accountKey,
                        "",
                        categories = account.category,
                        shaStr = account.cryptography,
                        totpVsHop = account.type,
                        filePath = account.filePath,
                        account.id
                    )
                }

                if (addResult) {
                    requireActivity().logFirebaseEvent(
                        "scan_option",
                        mapOf("codescan" to "clicked")
                    )
                } else {
                    toast(requireActivity().getString(R.string.error_occurs))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating account: ${e.message}", e)
                toast(requireActivity().getString(R.string.error_occurs))
            }
        }
//        }
    }

    private fun showReplace(
        id: Int,
        accountName: String,
        passKey: String,
        tool: String = "",
        account: TotpCardState
    ) {
        showReplaceAccountDialog(
            onReplace = {
                val result = homeViewModel.replaceTotp(id, accountName, passKey, tool)
                /*if (result) {
                    requireActivity().finish()
                    findNavController().popBackStack()
                }*/

            }, onKeep = {
                var result = false
                lifecycleScope.launch(Dispatchers.IO) {
                    val addResult = homeViewModel.addTotp(
                        accountName,
                        passKey,
                        tool,
                        shaStr = account.cryptography,
                        totpVsHop = account.type,
                        filePath = account.filePath
                    )
                    result = addResult

                }.invokeOnCompletion {
                    if (result) {
                        requireActivity().logFirebaseEvent(
                            "scan_option",
                            mapOf("codescan" to "clicked")
                        )
                    } else {
                        toast(requireActivity().getString(R.string.error_occurs))
                    }
                }
            })
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
            if (PrefsHelper.userEmail != "") {
                PrefsHelper.userEmail.getFirstCharacter()
                if (PrefsHelper.isBackedUp) {
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

    private fun setFromRemote() {
        lifecycleScope.launch {
            if (homeViewModel.setRemote(PrefsHelper.userEmail) == 0) {
                homeViewModel.fetchFromRemoteAndSave()
            }
        }.invokeOnCompletion {
            val data = homeViewModel.setRemote(PrefsHelper.userEmail)
            if (data == 0) {
                Log.d(TAG, "setFromRemote: placeHolder")
                placeHolder()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, email: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                homeViewModel.clearTotpData()
                PrefsHelper.userEmail = email

                PrefsHelper.userEmail.getFirstCharacter()

                lifecycleScope.launch {
                    homeViewModel.refreshTotpKeyFlow()
                    homeViewModel.setCategory("")
                }

                setFromRemote()

                // go to backup screen
//                requireActivity().openFragment(R.id.backupFragment, true)
                findNavController().navigate(R.id.action_homeFragment_to_backupFragment)
            } else {
                val errorMessage = when (task.exception) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid Credentials"
                    is FirebaseAuthUserCollisionException -> "Email already in use"
                    is FirebaseAuthInvalidUserException -> "Invalid User"
                    else -> "Authentication Failed"
                }
                toast(errorMessage)
                println(task.exception)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "admobNativeAd onDestroyView: called!")
        NativeAd.admobNativeAd?.destroy()
        NativeAd.admobNativeAd = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "admobNativeAd onDestroy: called!")
        NativeAd.admobNativeAd?.destroy()
        NativeAd.admobNativeAd = null
    }
}

private const val TAG = "HomeFragment"