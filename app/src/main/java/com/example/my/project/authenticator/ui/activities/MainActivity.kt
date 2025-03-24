package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ActivityMainBinding
import com.example.my.project.authenticator.ui.fragments.GoogleSignInDialog
import com.example.my.project.authenticator.utils.PrefsHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment }
    private val navController by lazy { navHostFragment.navController }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!PrefsHelper.firstMain) {
            val bottomSheetFragment = GoogleSignInDialog.newInstance {
                val currentDestinationId = navController.currentDestination?.id
                currentDestinationId?.let { id ->
                    navController.popBackStack(id, true)
                    navController.navigate(id)
                }
            }
            bottomSheetFragment.show(supportFragmentManager, "StaticBottomSheet")
            PrefsHelper.firstMain = true
        }
    }
}