package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.admob.NativeAd
import com.example.my.project.authenticator.admob.admob_native_home
import com.example.my.project.authenticator.databinding.FragmentVaultBinding
import com.example.my.project.authenticator.databinding.GntSmallBinding
import com.example.my.project.authenticator.databinding.ShimmerSmallNativeBinding
import com.example.my.project.authenticator.extensions.copyTextToClipboard
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.hideKeyboard
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.openFragment
import com.example.my.project.authenticator.extensions.safeAddView
import com.example.my.project.authenticator.extensions.sharePassword
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.extensions.showKeyboard
import com.example.my.project.authenticator.extensions.showPasswordOptionsBottomSheet
import com.example.my.project.authenticator.otp.domain.model.Password
import com.example.my.project.authenticator.ui.adapters.PasswordAdapter
import com.example.my.project.authenticator.ui.viewModel.VaultViewModel
import com.example.my.project.authenticator.utils.PrefsHelper.isAdsRemoved
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VaultFragment : Fragment() {
    private lateinit var binding: FragmentVaultBinding
    private lateinit var adapter: PasswordAdapter

    private val viewModel by viewModels<VaultViewModel>()
    private var allPasswords: List<Password> = emptyList() // master list
    private val searchQuery = MutableStateFlow("")         // current query
    private var isSearchActive: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentVaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observePasswords()
        setupClickListeners()
        initSearch()
        handleBackPress()
    }

    private fun initSearch() {
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery.value = newText.orEmpty()
                return true
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = PasswordAdapter(
            items = emptyList(),
            onItemClick = { password ->
                requireActivity().showPasswordOptionsBottomSheet(
                    password = password,
                    onOptionSelected = { id ->
                        when (id) {
                            R.id.tvView -> {
                                val bundle = Bundle().apply {
                                    putParcelable("password", password)
                                }
                                requireActivity().openFragment(
                                    R.id.viewPasswordFragment,
                                    true,
                                    bundle
                                )
                            }

                            R.id.tvEdit -> {
                                val bundle = Bundle().apply {
                                    putParcelable("password", password)
                                    putBoolean("edit", true)
                                }
                                requireActivity().openFragment(
                                    R.id.addPasswordFragment,
                                    true,
                                    bundle
                                )
                            }

                            R.id.tvCopyUrl -> {
                                requireActivity().copyTextToClipboard(password.url.toString())
                            }

                            R.id.tvCopyUsername -> {
                                requireActivity().copyTextToClipboard(password.emailOrUsername.toString())
                            }

                            R.id.tvCopyPassword -> {
                                requireActivity().copyTextToClipboard(password.password.toString())
                            }

                            R.id.tvShare -> {
                                requireActivity().sharePassword(password)
                            }

                            R.id.tvDelete -> {
                                viewModel.deletePassword(password)
                            }
                        }
                    }
                )
            },
            onCopyClicked = { password ->
                requireActivity().copyTextToClipboard(password)
            }
        )

        binding.recyclerViewPasswords.adapter = adapter
    }

    @OptIn(FlowPreview::class)
    private fun observePasswords() {
        lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.passwords.collect { list ->
                        binding.progressBar.hide()
                        binding.ivSearchView.isVisible = list.isNotEmpty()
                        allPasswords = list
                        filterPasswords(searchQuery.value)
                    }
                }
                launch {
                    searchQuery.debounce(300)
                        .collect { query ->
                            filterPasswords(query)
                        }
                }
            }
        }

        viewModel.loadPasswords()
    }

    private fun filterPasswords(query: String) {
        val filtered = if (query.isBlank()) {
            allPasswords
        } else {
            allPasswords.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.emailOrUsername.contains(query, ignoreCase = true) ||
                        it.url?.contains(query, ignoreCase = true) == true
            }
        }

        adapter.updateList(filtered)

        binding.apply {
//            progressBar.isVisible = filtered.isEmpty()
            recyclerViewPasswords.isVisible = filtered.isNotEmpty()
            if (isSearchActive) searchPlaceHolder.isVisible = filtered.isEmpty()
            llPlaceHolderLayout.isVisible = allPasswords.isEmpty()
            fabAddPassword.isVisible = filtered.isNotEmpty()
        }
    }


    private fun setupClickListeners() {
        val bundle = Bundle().apply {
            putParcelable("password", null)
            putBoolean("edit", false)
        }

        binding.apply {
            btnAddPassword.setOnClickListener {
                requireActivity().openFragment(R.id.addPasswordFragment, true, bundle)
            }

            fabAddPassword.setOnClickListener {
                requireActivity().openFragment(R.id.addPasswordFragment, true, bundle)
            }

            ivSearchView.setOnClickListener {
                activateSearch()
            }

            tvCancel.setOnClickListener {
                deactivateSearch()
            }
        }
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val navOptions =
                        NavOptions.Builder().setPopUpTo(R.id.homeFragment, true).build()
                    findNavController().navigate(R.id.homeFragment, null, navOptions)
                }
            })
    }

    private fun deactivateSearch() {
        isSearchActive = false
        binding.apply {
            searchQuery.value = ""
            clTopLayout.show()
            search.setQuery("", false)
            search.clearFocus()
            searchPlaceHolder.hide()
            searchViewLayout.hide()
            hideKeyboard()
        }
    }

    private fun activateSearch() {
        isSearchActive = true
        binding.apply {
            clTopLayout.hide()
            searchViewLayout.show()
            search.requestFocus()
            search.showKeyboard()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "admobNativeAd onDestroyView: called!")
        NativeAd.admobNativeAd?.destroy()
        NativeAd.admobNativeAd = null
    }
}

private const val TAG = "VaultFragment"