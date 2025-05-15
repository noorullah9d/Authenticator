package com.example.my.project.authenticator.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.admob.FragInterstitial
import com.example.my.project.authenticator.admob.NativeAd
import com.example.my.project.authenticator.admob.admob_interstitial_fragment
import com.example.my.project.authenticator.admob.admob_native_add_password
import com.example.my.project.authenticator.databinding.FragmentAddPasswordBinding
import com.example.my.project.authenticator.databinding.GntSmallBinding
import com.example.my.project.authenticator.databinding.ShimmerSmallNativeBinding
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.invisible
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.safeAddView
import com.example.my.project.authenticator.extensions.setProfileImage
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.extensions.showPasswordGenerationBottomSheet
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.otp.domain.model.Password
import com.example.my.project.authenticator.ui.viewModel.VaultViewModel
import com.example.my.project.authenticator.utils.PrefsHelper.isAdsRemoved
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPasswordFragment : Fragment() {
    private lateinit var binding: FragmentAddPasswordBinding

    private val viewModel by viewModels<VaultViewModel>()

    private var filePath: String? = null
    private var mPassword: Password? = null
    private var shouldEdit: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPassword = arguments?.getParcelable<Password>("password")
        shouldEdit = arguments?.getBoolean("edit", false) == true
        Log.d("AddPasswordFragment", "onViewCreated: password= $mPassword -- edit=$shouldEdit")

        if (mPassword != null) populateFields(mPassword!!)

        loadAndShowAdd()
        setupClickListeners()
        handleBackPress()
    }

    private fun loadFragmentInterstitial() {
        FragInterstitial.loadAd(
            requireContext(),
            admob_interstitial_fragment
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
            admob_native_add_password
        )
    }

    private fun showNativeAd() {
        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d(TAG, "admobNativeAd showNativeAd: called")
    }

    private fun populateFields(password: Password) {
        binding.apply {
            tvSave.text = if (shouldEdit) getString(R.string.update) else getString(R.string.save)
            etAccountName.setText(password.name)
            etUrl.setText(password.url)
            etEmail.setText(password.emailOrUsername)
            etPassword.setText(password.password)
            etNote.setText(password.notes)

            if (password.profileImagePath.isNullOrEmpty()) {
                ivProfileImage.hide()
                tvProfileImage.show()
                tvProfileImage.setProfileImage(password.name)
            } else {
                tvProfileImage.hide()
                ivProfileImage.show()
                ivProfileImage.load(password.profileImagePath.toUri()) {
                    placeholder(R.drawable.ic_profile_placeholder)
                    error(R.drawable.ic_profile_placeholder)
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        binding.apply {
            val name = etAccountName.text?.toString()?.trim()
            val email = etEmail.text?.toString()?.trim()
            val password = etPassword.text?.toString()?.trim()

            if (name.isNullOrEmpty()) {
                inputLayoutAccountName.error = getString(R.string.field_should_not_empty)
                isValid = false
            } else {
                inputLayoutAccountName.error = null
            }

            if (email.isNullOrEmpty()) {
                inputLayoutEmail.error = getString(R.string.field_should_not_empty)
                isValid = false
            } else {
                inputLayoutEmail.error = null
            }

            if (password.isNullOrEmpty()) {
                inputLayoutPassword.error = getString(R.string.field_should_not_empty)
                isValid = false
            } else {
                inputLayoutPassword.error = null
            }
        }
        return isValid
    }

    private fun setupClickListeners() {
        binding.apply {
            inputLayoutPassword.setEndIconOnClickListener {
                requireActivity().showPasswordGenerationBottomSheet {
                    etPassword.setText(it)
                }
            }

            icBack.setOnClickListener {
                findNavController().popBackStack()
            }

            tvSave.setOnClickListener {
                if (validateInputs()) {
                    FragInterstitial.showAd(
                        requireActivity(),
                        onDismissed = {
                            loadFragmentInterstitial()
                            savePassword()
                        }
                    )
                }
            }

            ivProfileImage.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }

            tvProfileImage.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }
        }
    }

    private fun savePassword() {
        val name = binding.etAccountName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val url = binding.etUrl.text.toString().trim()
        val note = binding.etNote.text.toString().trim()
        val imagePath = mPassword?.profileImagePath
        val time = System.currentTimeMillis()

        if (shouldEdit) {
            val passwordItem = Password(
                id = mPassword?.id ?: 0,
                name = name,
                emailOrUsername = email,
                password = password,
                url = url,
                notes = note,
                profileImagePath = filePath ?: imagePath,
                lastModified = time
            )
            viewModel.updatePassword(passwordItem)
            toast(getString(R.string.password_updated))
        } else {
            val passwordItem = Password(
                name = name,
                emailOrUsername = email,
                password = password,
                url = url,
                notes = note,
                profileImagePath = filePath ?: "",
                lastModified = time
            )
            viewModel.addPassword(passwordItem)
            toast(getString(R.string.password_saved))
        }
        findNavController().popBackStack()
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val navOptions =
                        NavOptions.Builder().setPopUpTo(R.id.vaultFragment, true).build()
                    findNavController().navigate(R.id.vaultFragment, null, navOptions)
                }
            })
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    toast("Permission error: ${e.message}")
                }

                filePath = uri.toString()
                displayImage(uri.toString())
            } else {
                toast("No image selected")
            }
        }

    private fun displayImage(imagePath: String) {
        binding.tvProfileImage.invisible()
        binding.ivProfileImage.show()
        binding.ivProfileImage.setImageURI(imagePath.toUri())
        filePath = imagePath
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "admobNativeAd onDestroyView: called!")
        NativeAd.admobNativeAd?.destroy()
        NativeAd.admobNativeAd = null
    }
}

private const val TAG = "AddPasswordFragment"