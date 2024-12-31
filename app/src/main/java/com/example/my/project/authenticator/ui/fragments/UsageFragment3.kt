package com.example.my.project.authenticator.ui.fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentUsage3Binding


class UsageFragment3 : Fragment() {

    private lateinit var binding: FragmentUsage3Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUsage3Binding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.ivIntroImg.setAnimation(R.raw.create_backup_dark)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.ivIntroImg.setAnimation(R.raw.create_backup)
            }
        }

    }


}