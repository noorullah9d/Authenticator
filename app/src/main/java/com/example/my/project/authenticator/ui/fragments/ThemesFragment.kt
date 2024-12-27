package com.example.my.project.authenticator.ui.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.FragmentThemesBinding
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.model.CardSelectionViewModel
import com.example.my.project.authenticator.utils.AppTheme
import com.example.my.project.authenticator.utils.Constants
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemesFragment : Fragment() {
    private lateinit var binding: FragmentThemesBinding
    private var prefsHelper: SharedPreferencesHelper? = null

    private val cardSelectionViewModel by viewModels<CardSelectionViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentThemesBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsHelper = SharedPreferencesHelper(requireActivity())

        binding.apply {

            val colorStateList = ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_enabled), intArrayOf(android.R.attr.state_enabled)), intArrayOf(
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


            cardSelectionViewModel.selectedTheme.observe(viewLifecycleOwner) {

                if (it.name == AppTheme.SYSTEM_DEFAULT.name) {
                    darkRadio.isChecked = false
                } else darkRadio.isChecked = it.name != AppTheme.LIGHT.name
            }



            lightRadio.setOnCheckedChangeListener { compoundButton, isEnabled ->
                if (isEnabled) {
                    if (systemSelection.isChecked) {
                        lightRadio.isChecked = false
                        toast("Please Turn off system theme")
                    } else {

                        prefsHelper?.userTheme = Constants.LIGHT
                        darkRadio.isChecked = false
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        cardSelectionViewModel.changeTheme(AppTheme.LIGHT)
                    }
                }
            }

            systemSelection.setOnCheckedChangeListener { _, isEnabled ->
                if (isEnabled) {
                    prefsHelper?.userTheme = ""
                    darkRadio.isEnabled = false
                    lightRadio.isEnabled = false
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    darkRadio.isEnabled = true
                    lightRadio.isEnabled = true
                }
            }

            darkRadio.setOnCheckedChangeListener { compoundButton, isEnabled ->
                if (isEnabled) {
                    if (systemSelection.isChecked) {
                        darkRadio.isChecked = false
                        toast("Please Turn off system theme")
                    } else {
                        prefsHelper?.userTheme = Constants.DARK
                        lightRadio.isChecked = false
                        cardSelectionViewModel.changeTheme(AppTheme.DARK)
                    }
                }
            }

            when(prefsHelper?.userTheme){
                Constants.DARK->{
                    darkRadio.isChecked = true
                    lightRadio.isChecked = false
                }
                Constants.LIGHT->{
                    darkRadio.isChecked = false
                    lightRadio.isChecked = true
                }
                else->{
                    darkRadio.isChecked = false
                    lightRadio.isChecked = false
                    systemSelection.isChecked=  true
                }
            }

        }

    }


}
