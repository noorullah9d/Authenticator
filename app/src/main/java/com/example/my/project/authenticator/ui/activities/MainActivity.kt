package com.example.my.project.authenticator.ui.activities

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ActivityMainBinding
import com.example.my.project.authenticator.ui.fragments.GoogleSignInDialog
import com.example.my.project.authenticator.utils.SharedPreferencesHelper
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var binding: ActivityMainBinding
    private val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment }
    private val navController by lazy { navHostFragment.navController }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (sharedPreferencesHelper.userEmail == "") {
            val bottomSheetFragment = GoogleSignInDialog.newInstance {
                val currentDestinationId = navController.currentDestination?.id
                currentDestinationId?.let { id ->
                    navController.popBackStack(id, true)
                    navController.navigate(id)
                }
            }
            bottomSheetFragment.show(supportFragmentManager, "StaticBottomSheet")
        }



    }

}
