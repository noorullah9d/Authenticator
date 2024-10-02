package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentAccountsDetailsBinding
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAccountsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    var accountId = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                    Log.d(TAG, "onViewCreated: $isExists")
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
//                val result = homeViewModel.addTotp(accountName, passKey, tool)
//                if (result) {
//                    requireActivity().finish()
//                }


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


    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().popBackStack()
        }
    }


}

private const val TAG = "AccountsDetails"