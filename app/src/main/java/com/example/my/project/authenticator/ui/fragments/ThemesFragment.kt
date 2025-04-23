package com.example.my.project.authenticator.ui.fragments

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.admob.NativeAd
import com.example.my.project.authenticator.databinding.FragmentThemesBinding
import com.example.my.project.authenticator.databinding.GntSmallBinding
import com.example.my.project.authenticator.databinding.ShimmerSmallNativeBinding
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.safeAddView
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.ui.viewModel.CardSelectionViewModel
import com.example.my.project.authenticator.utils.AppTheme
import com.example.my.project.authenticator.utils.DARK
import com.example.my.project.authenticator.utils.LIGHT
import com.example.my.project.authenticator.utils.PrefsHelper
import com.example.my.project.authenticator.utils.PrefsHelper.isAdsRemoved
import com.example.my.project.authenticator.utils.SYSTEM_DEFAULT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemesFragment : Fragment() {
    private lateinit var binding: FragmentThemesBinding

    private val cardSelectionViewModel by viewModels<CardSelectionViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentThemesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            val colorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(-android.R.attr.state_enabled),
                    intArrayOf(android.R.attr.state_enabled)
                ), intArrayOf(
                    ContextCompat.getColor(requireActivity(), R.color.light_gray),
                    ContextCompat.getColor(requireActivity(), R.color.n_sky_blue)
                )
            )

            lightRadio.buttonTintList = colorStateList
            lightRadio.invalidate()

            darkRadio.buttonTintList = colorStateList
            darkRadio.invalidate()

            icBack.setOnClickListener {
                findNavController().popBackStack()
            }

            requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        findNavController().popBackStack()
                    }
                })

            cardSelectionViewModel.selectedTheme.observe(viewLifecycleOwner) {

                if (it.name == AppTheme.SYSTEM_DEFAULT.name) {
                    darkRadio.isChecked = false
                } else darkRadio.isChecked = it.name != AppTheme.LIGHT.name
            }

            when (PrefsHelper.userTheme) {
                DARK -> {
                    darkRadio.isChecked = true
                    lightRadio.isChecked = false
                }

                LIGHT -> {
                    darkRadio.isChecked = false
                    lightRadio.isChecked = true
                }

                SYSTEM_DEFAULT -> {
                    darkRadio.isChecked = false
                    lightRadio.isChecked = false
                    darkRadio.isEnabled = false
                    lightRadio.isEnabled = false
                    systemSelection.isChecked = true
                }

                else -> {
                    systemSelection.isChecked = false
                }
            }

            lightRadio.setOnCheckedChangeListener { _, isEnabled ->
                if (isEnabled) {
                    if (systemSelection.isChecked) {
                        lightRadio.isEnabled = false
                        darkRadio.isEnabled = false
                    } else {

                        darkRadio.isChecked = false
                        PrefsHelper.userTheme = LIGHT
//                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        cardSelectionViewModel.changeTheme(AppTheme.LIGHT)
                    }

                }
            }

            systemSelection.setOnCheckedChangeListener { _, isEnabled ->
                if (isEnabled) {
                    PrefsHelper.userTheme = SYSTEM_DEFAULT
                    darkRadio.isEnabled = false
                    lightRadio.isEnabled = false
                    cardSelectionViewModel.changeTheme(AppTheme.SYSTEM_DEFAULT)
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    PrefsHelper.userTheme = ""
                    themesAvailability()
                    darkRadio.isEnabled = true
                    lightRadio.isEnabled = true
//                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }

            darkRadio.setOnCheckedChangeListener { _, isEnabled ->
                if (isEnabled) {
                    if (systemSelection.isChecked) {
                        lightRadio.isEnabled = false
                        darkRadio.isEnabled = false
                    } else {

                        PrefsHelper.userTheme = DARK
                        lightRadio.isChecked = false
                        cardSelectionViewModel.changeTheme(AppTheme.DARK)
                    }
                }
            }
        }

        loadAndShowAdd()
    }

    private fun loadAndShowAdd() {
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
            getString(R.string.admob_native_id_backup_theme)
        )
    }

    private fun showNativeAd() {
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
    }

    private fun themesAvailability() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.darkRadio.isChecked = true
                binding.lightRadio.isChecked = false
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.darkRadio.isChecked = false
                binding.lightRadio.isChecked = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        NativeAd.admobNativeAd?.destroy()
        NativeAd.admobNativeAd = null
    }

    override fun onDestroy() {
        super.onDestroy()
        NativeAd.admobNativeAd?.destroy()
        NativeAd.admobNativeAd = null
    }
}