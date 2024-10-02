package com.example.my.project.authenticator.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.my.project.authenticator.databinding.FragmentUsage3Binding


class UsageFragment3 : Fragment() {

    private lateinit var binding: FragmentUsage3Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUsage3Binding.inflate(inflater, container, false)
        return binding.root
    }


}