package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import com.example.my.project.authenticator.databinding.ActivitySelectLanguageBinding
import com.example.my.project.authenticator.extensions.clickWithExtraDebounce
import com.example.my.project.authenticator.extensions.getLanguageList
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.model.LanguageViewModel
import com.example.my.project.authenticator.ui.adapters.LanguagesAdapterNew
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectLanguageActivity : BaseActivity() {
    private val binding: ActivitySelectLanguageBinding by lazy {
        ActivitySelectLanguageBinding.inflate(layoutInflater)
    }

    private val viewModel: LanguageViewModel by viewModels<LanguageViewModel>()
    private lateinit var languagesAdapter: LanguagesAdapterNew
    private var selectedLanguage = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initLanguagesRecyclerView()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            icBack.setOnClickListener {
                finish()
            }

            confirm.clickWithExtraDebounce {
                viewModel.setLanguage(selectedLanguage)
                viewModel.setLanguageFirstTime("true")
                navigateToMainScreen()
            }
        }
    }

    private fun initLanguagesRecyclerView() {
        languagesAdapter = LanguagesAdapterNew(viewModel.getLanguage()) {
            selectedLanguage = it.code
        }
        languagesAdapter.setData(getLanguageList())
        binding.languagesRecycler.adapter = languagesAdapter
    }

    private fun navigateToMainScreen() {
        startActivityWithAnimation<MainActivity>()
        finishAffinity()
    }
}