package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentAuthenticatorBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthenticatorFragment : Fragment() {

    private lateinit var binding: FragmentAuthenticatorBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthenticatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.apply {

            ivBackPress.setOnClickListener {
                requireActivity().finish()
            }

            manually.setOnClickListener {

                val bundle = Bundle().apply {
                    putInt("accountId", 3)
                }

                findNavController().navigate(R.id.accountsDetails,bundle)
            }



            rlCode.setOnClickListener {
                val bundle = Bundle().apply {
                    putInt("accountId", 3)
                }

                findNavController().navigate(R.id.QRScannerScreen,bundle)
            }

        }

    }


}