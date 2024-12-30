package com.example.my.project.authenticator.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.adapters.StorageDetailsSpinnerArrayAdapter
import com.example.my.project.authenticator.databinding.FragmentAccountsDetailsBinding
import com.example.my.project.authenticator.extensions.beVisible
import com.example.my.project.authenticator.extensions.createNewGroupDialog
import com.example.my.project.authenticator.extensions.logFirebaseEvent
import com.example.my.project.authenticator.extensions.showReplaceAccountDialog
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.otp.data.database.Categories
import com.example.my.project.authenticator.otp.viewModel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountsDetails : Fragment() {
    private lateinit var binding: FragmentAccountsDetailsBinding
    private val homeViewModel by viewModels<HomeViewModel>()
    private var exportOptions: List<String>? = null
    private val totp = listOf("TOTP", "HOTP")
    private val sha = listOf("SHA1", "SHA256")
    private var category: String? = null
    var accountId = 0


    private lateinit var pickImageLauncher: ActivityResultLauncher<String>


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAccountsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountId = arguments?.getInt("accountId") ?: 0


        val accountName = arguments?.getString("key_name") ?: ""
        val secretKey = arguments?.getString("secret_key") ?: ""
        val tool = arguments?.getString("tool")


        binding.etAccountName.setText(accountName)
        binding.etAccountKey.setText(secretKey)


        clickListeners()


        lifecycleScope.launch {
            homeViewModel.getAllGroups().collectLatest { groups ->
                exportOptions = groups.map {
                    it.categories
                }
                setupExportOptionsSpinner()
            }
        }

        totpOptionsSpinner()
        shaSpinner()


    }

    private fun clickListeners() {
        binding.apply {


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
                        var result = false
                        lifecycleScope.launch(Dispatchers.IO) {
                            val addResult = homeViewModel.addTotp(accountName, accountKey, "", categories = category ?: "Default")
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


            profileImage.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }


            pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    handleImageUri(uri)
                } else {
                    toast("No image selected")
                }
            }


        }

    }

    private fun handleImageUri(uri: Uri) {
        binding.profileImage.setImageURI(uri)
    }

    private fun showReplace(id: Int, accountName: String, passKey: String, tool: String = "") {
        showReplaceAccountDialog(onReplace = {
            val result = homeViewModel.replaceTotp(id, accountName, passKey, tool)
            if (result) {
                requireActivity().finish()
                findNavController().popBackStack()
            }

        }, onKeep = {
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
        })
    }

    private fun setupExportOptionsSpinner() {

        binding.dropdownIcon.setOnClickListener {
            binding.spSelectGroup.performClick()
        }

        val options = exportOptions
        Log.d(TAG, "setupExportOptionsSpinner: $options")
        val exportOptionsAdapter = StorageDetailsSpinnerArrayAdapter(
            requireActivity(), options ?: listOf(), true, binding.spSelectGroup
        ) {
            createNewGroupDialog { groupName ->
                val category = Categories(0, groupName)
                homeViewModel.addCategories(category)
            }
        }

        binding.spSelectGroup.adapter = exportOptionsAdapter

        binding.spSelectGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                category = exportOptions?.get(position) ?: ""
                Log.d(TAG, "onItemSelected: $category")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d(TAG, "onNothingSelected: ")
            }
        }
    }

    private fun totpOptionsSpinner() {
        val options = totp


        binding.totp.setOnClickListener {
            binding.spCodeSelection.performClick()
        }

        val exportOptionsAdapter = StorageDetailsSpinnerArrayAdapter(
            requireActivity(), options, false, binding.spCodeSelection
        ) {

        }

        binding.spCodeSelection.adapter = exportOptionsAdapter

        binding.spCodeSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                category = exportOptions?.get(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun shaSpinner() {

        val options = sha

        binding.sha1.setOnClickListener {
            binding.spShaSelection.performClick()
        }

        val exportOptionsAdapter = StorageDetailsSpinnerArrayAdapter(requireActivity(), options, false, binding.spShaSelection) {}

        binding.spShaSelection.adapter = exportOptionsAdapter

        binding.spShaSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


}

private const val TAG = "AccountsDetails"