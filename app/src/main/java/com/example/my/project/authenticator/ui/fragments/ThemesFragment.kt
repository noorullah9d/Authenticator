package com.example.my.project.authenticator.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentThemesBinding
import com.example.my.project.authenticator.utils.Constants
import com.example.my.project.authenticator.utils.SharedPreferencesHelper


class ThemesFragment : Fragment() {
    private lateinit var binding: FragmentThemesBinding
    private var prefsHelper: SharedPreferencesHelper? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentThemesBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsHelper = SharedPreferencesHelper(requireActivity())

        binding.apply {

            val colorStateList = ColorStateList(
                arrayOf<IntArray>(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled)), intArrayOf(
                    ContextCompat.getColor(requireActivity(), R.color.light_gray),
                    ContextCompat.getColor(requireActivity(), R.color.n_sky_blue)
                )
            )

            lightRadio.buttonTintList = colorStateList
            lightRadio.invalidate()

            darkRadio.buttonTintList = colorStateList
            darkRadio.invalidate()


            backPress.setOnClickListener {
                findNavController().popBackStack()
            }

            lightRadio.setOnCheckedChangeListener { compoundButton, isEnabled ->
                if (isEnabled) {
                    prefsHelper?.userTheme = Constants.light
                    darkRadio.isChecked = false
                }
            }

            systemSelection.setOnCheckedChangeListener { _, isEnabled ->
                if (isEnabled) {
                    prefsHelper?.userTheme = ""
                }
            }

            darkRadio.setOnCheckedChangeListener { compoundButton, isEnabled ->
                if (isEnabled) {
                    prefsHelper?.userTheme = Constants.dark
                    lightRadio.isChecked = false
                }
            }

        }

    }

}