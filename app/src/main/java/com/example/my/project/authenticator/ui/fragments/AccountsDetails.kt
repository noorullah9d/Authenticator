package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentAccountsDetailsBinding
import com.example.my.project.authenticator.extensions.logFirebaseEvent
import com.example.my.project.authenticator.extensions.showReplaceAccountDialog
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.otp.viewModel.HomeViewModel
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountsDetails : Fragment() {
    private lateinit var binding: FragmentAccountsDetailsBinding


    private val homeViewModel by viewModels<HomeViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAccountsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            ivBackIcon.setOnClickListener {
                findNavController().popBackStack()
            }




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
                    if (isExists) {
                        showReplace(accountName, accountKey)
                    } else {
                        val result = homeViewModel.addTotp(accountName, accountKey,"")
                        if (result) {
                            requireActivity().logFirebaseEvent("scan_option", mapOf("passkey" to "clicked"))
                            requireActivity().finish()
                        } else {
                            toast(requireActivity().getString(R.string.error_occurs))
                        }
                    }
                }
            }



            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        }

    }


    private fun showReplace(accountName: String, passKey: String) {
        showReplaceAccountDialog(
            onReplace = {
                findNavController().popBackStack()
            },
            onKeep = {
                homeViewModel.addTotp(accountName, passKey)
            }
        )
    }


    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().popBackStack()
        }
    }



}

