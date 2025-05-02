package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentVaultBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VaultFragment : Fragment() {
    private lateinit var binding: FragmentVaultBinding

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

        handleBackPress()
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
}