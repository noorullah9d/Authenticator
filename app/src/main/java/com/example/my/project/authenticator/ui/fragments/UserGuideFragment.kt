package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.databinding.FragmentUserGuideBinding
import com.example.my.project.authenticator.extensions.getPlatformList
import com.example.my.project.authenticator.ui.adapters.GuideAdapter

class UserGuideFragment : Fragment() {
    private lateinit var binding: FragmentUserGuideBinding
    private lateinit var adapter: GuideAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentUserGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()
        setupClickListeners()
        handleBackPress()
    }

    private fun initRecyclerView() {
        adapter = GuideAdapter { url ->
            openGuideDetails(url)
        }
        adapter.setData(getPlatformList())
        binding.abc.adapter = adapter
    }

    private fun openGuideDetails(url: String) {
        val action = UserGuideFragmentDirections.actionUserGuideFragmentToGuideDetailsFragment(url)
        findNavController().navigate(action)
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })
    }

    private fun setupClickListeners() {
        binding.apply {
            icBack.setOnClickListener {
                findNavController().popBackStack()
            }
        }
    }
}