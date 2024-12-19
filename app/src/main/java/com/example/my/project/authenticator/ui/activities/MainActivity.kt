package com.example.my.project.authenticator.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ActivityMainBinding
import com.example.my.project.authenticator.extensions.logFirebaseEvent
import com.example.my.project.authenticator.extensions.showCustomDialog
import com.example.my.project.authenticator.otp.viewModel.HomeViewModel
import com.example.my.project.authenticator.ui.fragments.GoogleSignIn
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private val homeViewModel by viewModels<HomeViewModel>()
    private lateinit var binding: ActivityMainBinding
    private val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment }
    private val navController by lazy { navHostFragment.navController }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (sharedPreferencesHelper.userEmail == "") {
            val bottomSheetFragment = GoogleSignIn()
            bottomSheetFragment.show(supportFragmentManager, "StaticBottomSheet")
        }


        binding.apply {
            navController.addOnDestinationChangedListener { controller, destination, arguments ->
                Log.i(TAG, "onCreate: controller: $controller")
                Log.i(TAG, "onCreate: destination: $destination")


                if (destination.id == R.id.settingScreen) {
                    ivSettings.setImageResource(R.drawable.ic_selected_settings)
                    ivHome.setImageResource(R.drawable.ic_unselect_home)
                } else {
                    ivSettings.setImageResource(R.drawable.ic_home_settings)
                    ivHome.setImageResource(R.drawable.ic_home)
                }

            }





            ivHome.setOnClickListener {
                ivSettings.setImageResource(R.drawable.ic_home_settings)
                ivHome.setImageResource(R.drawable.ic_home)
                if (navController.currentDestination?.id != R.id.homeFragment) {
                    navController.navigate(R.id.action_settingScreen_to_homeFragment)

                }
            }

            ivSettings.setOnClickListener {
                ivSettings.setImageResource(R.drawable.ic_selected_settings)
                ivHome.setImageResource(R.drawable.ic_unselect_home)

                val navController = findNavController(R.id.nav_fragment)
                if (navController.currentDestination?.id != R.id.settingScreen) {
                    navController.navigate(R.id.action_homeFragment_to_settingScreen)
                }
            }


        }


    }

}

private const val TAG = "MainActivityLogs"