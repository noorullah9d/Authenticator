package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.my.project.authenticator.databinding.FragmentUsage2Binding


class UsageFragment2 : Fragment() {
    private lateinit var binding: FragmentUsage2Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUsage2Binding.inflate(inflater, container, false)
        return binding.root
    }


}