package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ActivityMainBinding
import com.example.my.project.authenticator.extensions.hide
import com.example.my.project.authenticator.extensions.show
import com.example.my.project.authenticator.ui.fragments.GoogleSignInDialog
import com.example.my.project.authenticator.utils.PrefsHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment }
    private val navController by lazy { navHostFragment.navController }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val bottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setupWithNavController(navController)

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

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment, R.id.vaultFragment, R.id.settingScreen ->
                    binding.bottomNavigation.show()
                else -> binding.bottomNavigation.hide()
            }
        }
    }
}