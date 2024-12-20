package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.adapters.StorageDetailsSpinnerArrayAdapter
import com.example.my.project.authenticator.databinding.FragmentAccountsDetailsBinding
import com.example.my.project.authenticator.extensions.beVisible
import com.example.my.project.authenticator.extensions.logFirebaseEvent
import com.example.my.project.authenticator.extensions.showReplaceAccountDialog
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.otp.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountsDetails : Fragment() {
    private lateinit var binding: FragmentAccountsDetailsBinding
    private val homeViewModel by viewModels<HomeViewModel>()
    private val exportOptions = listOf("No encryption", "Encrypt only keys", "Encrypt everything")
    private val totp = listOf("TOTP", "HOTP")
    private val sha = listOf("SHA1", "SHA256")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAccountsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    var accountId = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupExportOptionsSpinner()
        totpOptionsSpinner()
        shaSpinner()

        binding.apply {

            accountId = arguments?.getInt("accountId") ?: 0

            ivBackIcon.setOnClickListener {
                if (accountId == 3) {
                    findNavController().popBackStack()
                } else {
                    requireActivity().finish()
                }
            }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (accountId == 3) {
                        findNavController().popBackStack()
                    } else {
                        requireActivity().finish()
                    }
                }
            })

            btnAdd.setOnClickListener {
                val accountName = etAccountName.text.toString()
                val accountKey = etAccountKey.text.toString()

                if (accountName.isEmpty() && accountKey.isEmpty()) {
                    toast(requireActivity().getString(R.string.field_should_not_empty))
                } else if (accountName.isEmpty()) {
                    toast(requireActivity().getString(R.string.account_should_not_empty))
                } else if (accountKey.isEmpty()) {
                    toast(requireActivity().getString(R.string.key_should_not_empty))
                } else {
                    val isExists = homeViewModel.isKeyExists(accountName, accountKey)
                    if (isExists > 0) {
                        showReplace(isExists, accountName, accountKey)
                    } else {
//                        val result = homeViewModel.addTotp(accountName, accountKey, "")
//                        if (result) {
//                            requireActivity().logFirebaseEvent("scan_option", mapOf("passkey" to "clicked"))
//                            requireActivity().finish()
//                        } else {
//                            toast(requireActivity().getString(R.string.error_occurs))
//                        }


                        var result = false
                        lifecycleScope.launch(Dispatchers.IO) {
                            val addResult = homeViewModel.addTotp(accountName, accountKey, "")
                            result = addResult

                        }.invokeOnCompletion {
                            if (result) {
                                requireActivity().logFirebaseEvent("scan_option", mapOf("codescan" to "clicked"))
                                requireActivity().finish()
                            } else {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    toast(requireActivity().getString(R.string.error_occurs))
                                }
                            }
                        }


                    }
                }
            }

            ivTheme.setOnClickListener { llAdvLL.beVisible() }
            tvTheme.setOnClickListener { llAdvLL.beVisible() }


        }

    }


    private fun showReplace(id: Int, accountName: String, passKey: String, tool: String = "") {
        showReplaceAccountDialog(
            onReplace = {
                val result = homeViewModel.replaceTotp(id, accountName, passKey, tool)
                if (result) {
                    requireActivity().finish()
                    findNavController().popBackStack()
                }

            },
            onKeep = {
                var result = false
                lifecycleScope.launch(Dispatchers.IO) {
                    val addResult = homeViewModel.addTotp(accountName, passKey, tool)
                    result = addResult

                }.invokeOnCompletion {
                    if (result) {
                        requireActivity().logFirebaseEvent("scan_option", mapOf("codescan" to "clicked"))
                        requireActivity().finish()
                    } else {
                        toast(requireActivity().getString(R.string.error_occurs))
                    }
                }
            }
        )
    }

    private fun setupExportOptionsSpinner() {
        val options = exportOptions

        val exportOptionsAdapter = StorageDetailsSpinnerArrayAdapter(
            requireActivity(),
            options,
            false
        )

        binding.spSelectGroup.adapter = exportOptionsAdapter

        binding.spSelectGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun totpOptionsSpinner() {
        val options = totp

        val exportOptionsAdapter = StorageDetailsSpinnerArrayAdapter(
            requireActivity(),
            options,
            false
        )

        binding.spCodeSelection.adapter = exportOptionsAdapter

        binding.spCodeSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun shaSpinner() {

        val options = sha

        val exportOptionsAdapter = StorageDetailsSpinnerArrayAdapter(requireActivity(), options, false)

        binding.spShaSelection.adapter = exportOptionsAdapter

        binding.spShaSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


}
