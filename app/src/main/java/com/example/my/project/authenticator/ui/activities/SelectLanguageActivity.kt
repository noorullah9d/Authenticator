package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.my.project.authenticator.adapters.LanguagesAdapterNew
import com.example.my.project.authenticator.databinding.ActivitySelectLanguageBinding
import com.example.my.project.authenticator.extensions.clickWithExtraDebounce
import com.example.my.project.authenticator.extensions.getLanguageList
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.model.LanguageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectLanguageActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectLanguageBinding
    private lateinit var languagesAdapter: LanguagesAdapterNew

    private val viewModel: LanguageViewModel by viewModels<LanguageViewModel>()

    private var selectedLanguage = "en"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySelectLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLanguagesRecyclerView()
        binding.confirm.clickWithExtraDebounce {
            navigateToMainScreen()
        }

    }


    private fun initLanguagesRecyclerView() {
        languagesAdapter = LanguagesAdapterNew(viewModel.getLanguage()) {
            selectedLanguage = it.code
        }
        languagesAdapter.setData(getLanguageList())
        binding.languagesRecycler.adapter = languagesAdapter
    }

//
//    @Deprecated("Deprecated in Java")
//    override fun onBackPressed() {
//        navigateToMainScreen()
//    }


    private fun navigateToMainScreen() {
        startActivityWithAnimation<MainActivity>()
//        viewModel.setLanguage(selectedLanguage)
//        viewModel.setLanguageFirstTime("true")
//        TinyDB.getInstance(this).putBoolean(Constants.IS_FIRST_TIME, true)
//        val intent = Intent(this, MainActivity::class.java)
//        startActivity(intent)
        finishAffinity()
    }
}