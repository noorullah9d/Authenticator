package com.example.my.project.authenticator.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.admob.NativeAd
import com.example.my.project.authenticator.ui.adapters.StorageDetailsSpinnerArrayAdapter
import com.example.my.project.authenticator.databinding.FragmentAccountsDetailsBinding
import com.example.my.project.authenticator.databinding.GntSmallBinding
import com.example.my.project.authenticator.databinding.ShimmerSmallNativeBinding
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.extensions.createNewGroupDialog
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.logFirebaseEvent
import com.example.my.project.authenticator.extensions.safeAddView
import com.example.my.project.authenticator.extensions.showReplaceAccountDialog
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.otp.data.database.Categories
import com.example.my.project.authenticator.ui.viewModel.HomeViewModel
import com.example.my.project.authenticator.utils.PrefsHelper.isAdsRemoved
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AccountsDetails : Fragment() {
    private lateinit var binding: FragmentAccountsDetailsBinding
    private val homeViewModel by viewModels<HomeViewModel>()
    private var exportOptions: List<String>? = null
    private var totp = listOf("TOTP", "HOTP")
    private var sha = listOf("SHA1", "SHA256")
    private var category: String? = null
    private var SHA: String? = null
    private var OTP: String? = null
    private var filePath: String? = null
    private var id: Int? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAccountsDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val edit = arguments?.getInt("edit")
        if (edit == 1) {
            id = arguments?.getInt("id") ?: -1

            SHA = arguments?.getString("SHA") ?: "SHA1"
            filePath = arguments?.getString("filePath") ?: ""
            OTP = arguments?.getString("OTP") ?: "TOTP"
            category = arguments?.getString("category") ?: "Default"
            val tool = arguments?.getString("tool")


            if (filePath != "") displayImage(filePath!!)

            sha = when (SHA) {
                "SHA1" -> {
                    listOf("SHA1", "SHA256")
                }

                "SHA256" -> {
                    listOf("SHA256", "SHA1")
                }

                else -> {
                    listOf("SHA256", "SHA1")
                }
            }

            totp = when (OTP) {
                "TOTP" -> {
                    listOf("TOTP", "HOTP")
                }

                "HOTP" -> {
                    listOf("HOTP", "TOTP")
                }

                else -> {
                    listOf("TOTP", "HOTP")
                }
            }
        }

        val accountName = arguments?.getString("key_name") ?: ""
        val secretKey = arguments?.getString("secret_key") ?: ""

        binding.etAccountName.setText(accountName)
        binding.etAccountKey.setText(secretKey)

        clickListeners()

        lifecycleScope.launch {
            homeViewModel.getAllGroups().collectLatest { groups ->
                exportOptions = groups.map {
                    it.categories
                }
                Log.d(TAG, "onViewCreated: $category")
                setupExportOptionsSpinner()
            }
        }

        totpOptionsSpinner()
        shaSpinner()
        loadAndShowAdd()
    }

    private fun loadAndShowAdd() {
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
            getString(R.string.admob_native_id_qr)
        )
    }

    private fun showNativeAd() {
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

    private fun clickListeners() {
        binding.apply {


            ivBackIcon.setOnClickListener {
                requireActivity().finish()
            }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })

            btnAdd.setOnClickListener {
                val accountName = etAccountName.text.toString()
                val accountKey = etAccountKey.text.toString()

                val validationMessage = validateInputs(accountName, accountKey)
                if (validationMessage != null) {
                    toast(requireActivity().getString(validationMessage))
                    return@setOnClickListener
                }

                val isExists = homeViewModel.isKeyExists(accountName, accountKey)
                if (isExists > 0) {
                    showReplace(isExists, accountName, accountKey)
                } else {
                    Log.d(TAG, "Adding TOTP with category: $category")
                    lifecycleScope.launch {
                        try {
                            val addResult = withContext(Dispatchers.IO) {
                                if (id != -1) homeViewModel.addTotp(accountName, accountKey, "", categories = category ?: "", shaStr = SHA ?: "SHA1", totpVsHop = OTP ?: "TOTP", filePath = filePath ?: "")
                                else homeViewModel.addTotp(accountName, accountKey, "", categories = category ?: "", shaStr = SHA ?: "SHA1", totpVsHop = OTP ?: "TOTP", filePath = filePath ?: "", id!!)
                            }

                            if (addResult) {
                                requireActivity().logFirebaseEvent("scan_option", mapOf("codescan" to "clicked"))
                                requireActivity().finish()
                            } else {
                                toast(requireActivity().getString(R.string.error_occurs))
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error adding TOTP: ${e.message}", e)
                            toast(requireActivity().getString(R.string.error_occurs))
                        }
                    }
                }
            }

            ivTheme.setOnClickListener { llAdvLL.show() }

            tvTheme.setOnClickListener { llAdvLL.show() }


            profileImage.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }


        }

    }


    private fun displayImage(imagePath: String) {
        binding.profileImage.setImageURI(Uri.parse(imagePath))
        filePath = imagePath
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                filePath = uri.toString()
                displayImage(uri.toString()) // Display the image
            } catch (e: SecurityException) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Permission error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
        }
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
                val addResult = homeViewModel.addTotp(accountName, passKey, tool, shaStr = SHA ?: "SHA1", totpVsHop = OTP ?: "TOTP", filePath = filePath ?: "")
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

        val options = exportOptions ?: listOf()
        Log.d(TAG, "setupExportOptionsSpinner: $options")

        val exportOptionsAdapter = StorageDetailsSpinnerArrayAdapter(
            requireActivity(), options, true, binding.spSelectGroup
        ) {

            createNewGroupDialog { groupName ->
                val category = Categories(0, groupName)
                homeViewModel.addCategories(category)
                binding.spSelectGroup.performClick()
            }
        }
        binding.spSelectGroup.adapter = exportOptionsAdapter

        val currentCategory = category
        val selectedPosition = options.indexOf(currentCategory)
        Log.d(TAG, "setupExportOptionsSpinner: Current Category: $currentCategory, Position: $selectedPosition")


        if (selectedPosition >= 0) {
            binding.spSelectGroup.post {
                binding.spSelectGroup.setSelection(selectedPosition, false)
            }
        }

        binding.spSelectGroup.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                category = options.getOrNull(position) ?: ""
                Log.d(TAG, "onItemSelected: Selected Category: $category")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d(TAG, "onNothingSelected: No selection made")
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
                OTP = totp[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
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
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                SHA = sha[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun validateInputs(accountName: String, accountKey: String): Int? {
        return when {
            accountName.isEmpty() && accountKey.isEmpty() -> R.string.field_should_not_empty
            accountName.isEmpty() -> R.string.account_should_not_empty
            accountKey.isEmpty() -> R.string.key_should_not_empty
            else -> null
        }
    }


}

private const val TAG = "AccountsDetails"