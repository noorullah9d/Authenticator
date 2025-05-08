package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentViewPasswordBinding
import com.example.my.project.authenticator.extensions.copyTextToClipboard
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.openFragment
import com.example.my.project.authenticator.extensions.setProfileImage
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.extensions.toFormattedDate
import com.example.my.project.authenticator.otp.domain.model.Password

class ViewPasswordFragment : Fragment() {
    private lateinit var binding: FragmentViewPasswordBinding

    private var password: Password? = null
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentViewPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        password = arguments?.getParcelable<Password>("password")
        Log.d("ViewPasswordFragment", "onViewCreated: password= $password")

        populateData(password)
        setupClickListeners()
        handleBackPress()
    }

    private fun populateData(password: Password?) {
        password?.let {
            binding.apply {
                tvName.text = it.name
                tvLastModified.text = getString(R.string.last_modified_time, it.lastModified.toFormattedDate())
                tvUrl.text = it.url
                tvEmailOrUsername.text = it.emailOrUsername
                tvPassword.text = "*".repeat(it.password.length)

                if (it.profileImagePath.isNullOrEmpty()) {
                    ivProfileImage.hide()
                    tvProfileImage.show()
                    tvProfileImage.setProfileImage(it.name)
                } else {
                    tvProfileImage.hide()
                    ivProfileImage.show()
                    ivProfileImage.load(it.profileImagePath.toUri()) {
                        placeholder(R.drawable.ic_profile_placeholder)
                        error(R.drawable.ic_profile_placeholder)
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            icBack.setOnClickListener {
                findNavController().popBackStack()
            }

            icEdit.setOnClickListener {
                password?.let {
                    val bundle = Bundle().apply {
                        putParcelable("password", it)
                        putBoolean("edit", true)
                    }
                    requireActivity().openFragment(R.id.addPasswordFragment, false, bundle)
                }
            }

            icCopyUsername.setOnClickListener {
                requireActivity().copyTextToClipboard(password?.emailOrUsername.toString())
            }

            icCopyPassword.setOnClickListener {
                requireActivity().copyTextToClipboard(password?.password.toString())
            }

            icTogglePassword.setOnClickListener {
                isPasswordVisible = !isPasswordVisible

                val passwordString = password?.password // store this somewhere securely
                passwordString?.let {
                    if (isPasswordVisible) {
                        binding.tvPassword.text = it
                        binding.icTogglePassword.setImageResource(R.drawable.ic_eye_open)
                    } else {
                        binding.tvPassword.text = "*".repeat(it.length)
                        binding.icTogglePassword.setImageResource(R.drawable.ic_eye_close)
                    }
                }
            }
        }
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
}