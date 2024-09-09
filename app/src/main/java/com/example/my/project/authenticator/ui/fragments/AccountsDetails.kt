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
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.otp.viewModel.HomeViewModel
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
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
                if (etPasscode.text.toString().isEmpty() && etPasscode.text.toString().isEmpty()) {
                    toast(requireActivity().getString(R.string.field_should_not_empty))
                } else if (etPasscode.text.toString().isEmpty()) {
                    toast(requireActivity().getString(R.string.account_should_not_empty))
                } else if (etPasscode.text.toString().isEmpty()) {
                    toast(requireActivity().getString(R.string.key_should_not_empty))
                } else {
                    val result = homeViewModel.addTotp(etPasscode.text.toString(), etAccountKey.text.toString())
                    if (result){
                        requireActivity().finish()
                    }else{
                        toast(requireActivity().getString(R.string.error_occurs))
                    }
                }
            }



            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)


        }


    }

    private fun saveDataToDB(email: String, passcode: String, accountName: String) {
        val firestore = FirebaseFirestore.getInstance()

        // New account data to add
        val newAccount = mapOf(
            "accountName" to accountName,
            "passcode" to passcode
        )

        // Reference to the document
        val documentRef = firestore.collection("users").document(email)

        // Get the current document
        documentRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Retrieve the existing accounts list or create a new one
                val existingAccounts = document.get("accounts") as? MutableList<Map<String, String>> ?: mutableListOf()
                existingAccounts.add(newAccount)  // Add the new account

                // Update the document with the new accounts list
                documentRef.update("accounts", existingAccounts)
                    .addOnSuccessListener {
                        toast("New account added under the same email")
                    }
                    .addOnFailureListener { e ->
                        toast("Failed to add account: ${e.message}")
                    }
            } else {
                // If no document exists, create a new one with the first account
                documentRef.set(mapOf("accounts" to listOf(newAccount)))
                    .addOnSuccessListener {
                        toast("Document created and account added")
                        requireActivity().finish()
                    }
                    .addOnFailureListener { e ->
                        toast("Failed to create document: ${e.message}")
                        requireActivity().finish()
                    }
            }
        }.addOnFailureListener { e ->
            toast("Failed to retrieve document: ${e.message}")
        }
    }


    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            findNavController().popBackStack()
        }
    }


}

private const val TAG = "AccountsDetails"